
/*
 * Copyright (c) 2015, Nordic Semiconductor
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors may be used to endorse or promote products derived from this
 * software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE
 * USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.mushin.muconnect;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import java.lang.reflect.Method;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.UUID;

import static android.bluetooth.BluetoothGatt.CONNECTION_PRIORITY_BALANCED;
import static android.bluetooth.BluetoothGatt.CONNECTION_PRIORITY_HIGH;
import static android.bluetooth.BluetoothProfile.GATT;

/**
 * Service for managing connection and data communication with a GATT server hosted on a
 * given Bluetooth LE device.
 */
public class UartService extends Service {
    private final static String TAG = UartService.class.getSimpleName();

    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private String mRequestedBluetoothDeviceAddress;
    private String mBluetoothDeviceAddress;
    private BluetoothGatt mBluetoothGatt;
    private int mConnectionState = STATE_DISCONNECTED;

    private boolean enableGeneratedPasscode = false;
    private boolean pairingReceiverRegistered = false;

    private final int maxConnectionRetryCount = 3;
    private int connectionRetryCount = 0;

    private final long reconnectionRetryMaxTime = 10 * 60 * 1000; // 10 minutes
    private long reconnectionRetryCutoff = -1;
    private final int reconnectionRetryInterval = 250;

    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_CONNECTED = 2;
    private static final int STATE_BONDING = 3;
    private static final int STATE_CONNECTION_ERROR = 4;
    private static final int STATE_DISCONNECTING = 5;
    private static final int STATE_RECONNECTING = 6;
    private static final int STATE_DISCOVERING_SERVICES = 7;
    private static final int STATE_CONNECTION_ERROR_WRITING_DESCRIPTOR = 8;
    private static final int STATE_CONNECTION_DELAY_BEFORE_DISCOVERY = 9;

    public final static String ACTION_GATT_CONNECTED =
            "com.sanahealth.nrfUART.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED =
            "com.sanahealth.nrfUART.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_READY =
            "com.sanahealth.nrfUART.ACTION_GATT_READY";
    public final static String ACTION_DATA_AVAILABLE =
            "com.sanahealth.nrfUART.ACTION_DATA_AVAILABLE";
    public final static String EXTRA_DATA =
            "com.sanahealth.nrfUART.EXTRA_DATA";
    public final static String DEVICE_DOES_NOT_SUPPORT_UART =
            "com.sanahealth.nrfUART.DEVICE_DOES_NOT_SUPPORT_UART";
    public final static String ACTION_GATT_ERROR =
            "com.sanahealth.nrfUART.ACTION_GATT_ERROR";
    public final static String ACTION_GATT_RECONNECTING =
            "com.sanahealth.nrfUART.ACTION_GATT_RECONNECTING";


    public static final UUID TX_POWER_UUID = UUID.fromString("00001804-0000-1000-8000-00805f9b34fb");
    public static final UUID TX_POWER_LEVEL_UUID = UUID.fromString("00002a07-0000-1000-8000-00805f9b34fb");
    public static final UUID CCCD = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
    public static final UUID FIRMWARE_REVISON_UUID = UUID.fromString("00002a26-0000-1000-8000-00805f9b34fb");
    public static final UUID DIS_UUID = UUID.fromString("0000180a-0000-1000-8000-00805f9b34fb");
    public static final UUID RX_SERVICE_UUID = UUID.fromString("6e400001-b5a3-f393-e0a9-e50e24dcca9e");
    public static final UUID RX_CHAR_UUID = UUID.fromString("6e400002-b5a3-f393-e0a9-e50e24dcca9e");
    public static final UUID TX_CHAR_UUID = UUID.fromString("6e400003-b5a3-f393-e0a9-e50e24dcca9e");


    private Handler discoverServicesHandler = new Handler();
    private final long discoverServicesDelay = 300;

    // Implements callback methods for GATT events that the app cares about.  For example,
    // connection change and services discovered.
    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            String intentAction;

            Log.d(TAG, "Connection state changed");
            Log.d(TAG, "Status: " + String.valueOf(status));
            Log.d(TAG, "New State: " + String.valueOf(newState));
            Log.d(TAG, "----------------------------------------------------------");

            if (newState == BluetoothProfile.STATE_CONNECTED) {

                reconnectionRetryCutoff = -1;

                intentAction = ACTION_GATT_CONNECTED;
                broadcastUpdate(intentAction);
                Log.d(TAG, "Connected to GATT server.");
                // Attempts to discover services after successful connection.

                discoverServicesHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (mConnectionState != STATE_DISCOVERING_SERVICES) {
                            boolean discoveringServices = mBluetoothGatt.discoverServices();
                            mConnectionState = STATE_DISCOVERING_SERVICES;
                            Log.d(TAG, "Attempting to start service discovery:" + discoveringServices);
                        }
                    }
                }, discoverServicesDelay);

                mConnectionState = STATE_CONNECTION_DELAY_BEFORE_DISCOVERY;

            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {

                discoverServicesHandler.removeCallbacks(null);

                if (mConnectionState == STATE_DISCONNECTED) {
                    // already disconnected
                    return;
                }

                BluetoothDevice device = gatt.getDevice();

                // try to connect again if connection state change wasn't initiated by the app
                // until maximum number of retries has been reached

                long currentTime = System.currentTimeMillis();

                if (mConnectionState != STATE_DISCONNECTING &&
                    mConnectionState != STATE_CONNECTION_ERROR_WRITING_DESCRIPTOR &&
                    mConnectionState != STATE_CONNECTION_DELAY_BEFORE_DISCOVERY &&
                        (reconnectionRetryCutoff < 0 || currentTime < reconnectionRetryCutoff)) {

                    if (reconnectionRetryCutoff < 0) {
                        reconnectionRetryCutoff = currentTime + reconnectionRetryMaxTime;
                    }

                    broadcastUpdate(ACTION_GATT_RECONNECTING);

                    if (device != null) {
                        try {
                            Thread.sleep(reconnectionRetryInterval);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        reconnect(device.getAddress());

                        mConnectionState = STATE_RECONNECTING;
                        return;
                    }
                }

                boolean unpair = ((mConnectionState == STATE_CONNECTION_ERROR_WRITING_DESCRIPTOR) ||
                        (mConnectionState == STATE_CONNECTION_DELAY_BEFORE_DISCOVERY));

                intentAction = ACTION_GATT_DISCONNECTED;
                mConnectionState = STATE_DISCONNECTED;
                Log.d(TAG, "Disconnected from GATT server.");
                if (!unpair) {
                    broadcastUpdate(intentAction);
                } else {

                    // workaround: Android 8.0+ currently is making no attempts to
                    // resend bonding parameters if BLE device has 'paired' status in Android
                    // but bonding data was deleted from BLE device.
                    // In this case BLE devices sends a bonding request to restore bond which is ignored
                    // by Android starting from version 8.0.

                    // In this case let's unpair the BLE device and start connection procedure again.
                    unpair(gatt.getDevice());
                }
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {

            Log.i(TAG, "** Made it to onServicesDiscovered");
		
            if (status == BluetoothGatt.GATT_SUCCESS) {
            	Log.w(TAG, "mBluetoothGatt = " + mBluetoothGatt );

                enableTXNotification();
            } else {
                Log.w(TAG, "onServicesDiscovered received: " + status);
                mConnectionState = STATE_CONNECTION_ERROR;
                broadcastUpdate(ACTION_GATT_ERROR);
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic,
                                         int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {
            broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt,
                                      BluetoothGattDescriptor descriptor,
                                      int status) {
            Log.w(TAG, "onDescriptorWrite received: " + status);
            if (mConnectionState == STATE_DISCOVERING_SERVICES) {
                if (status == 133 && connectionRetryCount < maxConnectionRetryCount) {
                    connectionRetryCount++;
                    mConnectionState = STATE_CONNECTION_ERROR_WRITING_DESCRIPTOR;
                } else if (status == 133) {
                    mConnectionState = STATE_CONNECTION_ERROR;
                    broadcastUpdate(ACTION_GATT_ERROR);
                } else if (status == BluetoothGatt.GATT_SUCCESS) {
                    connectionRetryCount = 0;
                    mConnectionState = STATE_CONNECTED;
                    broadcastUpdate(ACTION_GATT_READY);
                }
            }
        }
    };

    private void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private void broadcastUpdate(final String action,
                                 final BluetoothGattCharacteristic characteristic) {
        final Intent intent = new Intent(action);

        // This is handling for the notification on TX Character of NUS service
        if (TX_CHAR_UUID.equals(characteristic.getUuid())) {
        	
           // Log.d(TAG, String.format("Received TX: %d",characteristic.getValue() ));
            intent.putExtra(EXTRA_DATA, characteristic.getValue());
        } else {
        	
        }
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    public class LocalBinder extends Binder {
        UartService getService() {
            return UartService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        // After using a given device, you should make sure that BluetoothGatt.close() is called
        // such that resources are cleaned up properly.  In this particular example, close() is
        // invoked when the UI is disconnected from the Service.
        close();
        return super.onUnbind(intent);
    }

    private final IBinder mBinder = new LocalBinder();

    /**
     * Initializes a reference to the local Bluetooth adapter.
     *
     * @return Return true if the initialization is successful.
     */
    public boolean initialize() {
        // For API level 18 and above, get a reference to BluetoothAdapter through
        // BluetoothManager.
        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager == null) {
                Log.e(TAG, "Unable to initialize BluetoothManager.");
                return false;
            }
        }

        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
            return false;
        }

        return true;
    }

    /**
     * Connects to the GATT server hosted on the Bluetooth LE device.
     *
     * @param address The device address of the destination device.
     *
     * @return Return true if the connection is initiated successfully. The connection result
     *         is reported asynchronously through the
     *         {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     *         callback.
     */
    public boolean connect(final String address) {

        registerPairingReceiver();

        if (mBluetoothAdapter == null || address == null) {
            Log.w(TAG, "BluetoothAdapter not initialized or unspecified address.");
            return false;
        }
        Log.d(TAG, "************ public boolean connect!!");

        mRequestedBluetoothDeviceAddress = address;

        // Previously connected device.  Try to reconnect.
        if (mBluetoothDeviceAddress != null && address.equals(mBluetoothDeviceAddress)
                && mBluetoothGatt != null) {
            Log.d(TAG, "Trying to use an existing mBluetoothGatt for connection.");
            if (mBluetoothGatt.connect()) {
                mConnectionState = STATE_CONNECTING;
				Log.d(TAG, "************ Connected!!");
                return true;
            } else {
            Log.d(TAG, "************ Not Connected!!");
                return false;
            }
        }

        final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        if (device == null) {
            Log.w(TAG, "Device not found.  Unable to connect.");
            return false;
        }

        int bondState = device.getBondState();
        if (bondState == BluetoothDevice.BOND_NONE) {
            Log.e(TAG, "*** Device not bonded: bonding");
            mConnectionState = STATE_BONDING;
            device.createBond();
        } else {
            mBluetoothDeviceAddress = address;

            if (mBluetoothGatt != null) {
                mBluetoothGatt.close();

                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            // We want to directly connect to the device, so we are setting the autoConnect
            // parameter to false.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                mBluetoothGatt = device.connectGatt(getApplicationContext(), false, mGattCallback,
                        BluetoothDevice.TRANSPORT_LE/*, preferredPhy*//*, mHandler*/);
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                mBluetoothGatt = device.connectGatt(getApplicationContext(), false, mGattCallback,
                        BluetoothDevice.TRANSPORT_LE);
            } else {
                mBluetoothGatt = device.connectGatt(getApplicationContext(), false, mGattCallback);
            }

            Log.d(TAG, "************ Trying to create a new connection.");
            mConnectionState = STATE_CONNECTING;
        }

        return true;
    }

    /**
     * Unpairs Bluetooth LE device.
     *
     * @param device The BLE device to unpair.
     *
     */
    private void unpair(BluetoothDevice device) {
        if (device == null) {
            return;
        }

        int bondState = device.getBondState();
        if (bondState != BluetoothDevice.BOND_BONDED) {
            return;
        }

        Log.e(TAG, "*** Unbonding device");
        try {
            Method m = device.getClass().getMethod("removeBond", (Class[]) null);
            m.invoke(device, (Object[]) null);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }

    private void reconnect(String address) {
        if (mBluetoothGatt != null){
            mBluetoothGatt.disconnect();

            mBluetoothDeviceAddress = null;
            connect(address);
        }
    }

    /**
     * Requests priority parameter update for the current connection.
     * @param highPriority Request high priority if true or default priority otherwise.
     *
     */
    public void requestHighConnectionPriority(boolean highPriority) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mBluetoothGatt.requestConnectionPriority(highPriority ? CONNECTION_PRIORITY_HIGH : CONNECTION_PRIORITY_BALANCED);
        }
    }

    /**
     * Disconnects an existing connection or cancel a pending connection. The disconnection result
     * is reported asynchronously through the
     * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     * callback.
     */
    public void disconnect() {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }

        if (mConnectionState != STATE_DISCONNECTED) {
            // indicate that disconnect was initiated by the application
            mConnectionState = STATE_DISCONNECTING;
            mBluetoothGatt.disconnect();

            if (mBluetoothManager.getConnectionState(mBluetoothGatt.getDevice(), GATT) == STATE_DISCONNECTED) {
                // device is already disconnected; broadcast state change
                // probably it was caused by cancelling reconnection request
                mConnectionState = STATE_DISCONNECTED;
                broadcastUpdate(ACTION_GATT_DISCONNECTED);
            }
        }

       // mBluetoothGatt.close();
    }

    /**
     * After using a given BLE device, the app must call this method to ensure resources are
     * released properly.
     */
    public void close() {
        unregisterPairingReceiver();

        if (mBluetoothGatt == null) {
            return;
        }
        Log.w(TAG, "mBluetoothGatt closed");
        mBluetoothDeviceAddress = null;
        mBluetoothGatt.close();
        mBluetoothGatt = null;
    }

    /**
     * Request a read on a given {@code BluetoothGattCharacteristic}. The read result is reported
     * asynchronously through the {@code BluetoothGattCallback#onCharacteristicRead(android.bluetooth.BluetoothGatt, android.bluetooth.BluetoothGattCharacteristic, int)}
     * callback.
     *
     * @param characteristic The characteristic to read from.
     */
    public void readCharacteristic(BluetoothGattCharacteristic characteristic) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        Log.w(TAG, "Reading characteristic!!");
        mBluetoothGatt.readCharacteristic(characteristic);
    }

    /**
     * Enables or disables notification on a give characteristic.
     *

    */
    
    /**
     * Enable Notification on TX characteristic
     *
     * @return 
     */
    public void enableTXNotification()
    { 
    	BluetoothGattService RxService = mBluetoothGatt.getService(RX_SERVICE_UUID);
    	if (RxService == null) {
            showMessage("Rx service not found!");
            broadcastUpdate(DEVICE_DOES_NOT_SUPPORT_UART);
            return;
        }
    	BluetoothGattCharacteristic TxChar = RxService.getCharacteristic(TX_CHAR_UUID);
        if (TxChar == null) {
            showMessage("Tx charateristic not found!");
            broadcastUpdate(DEVICE_DOES_NOT_SUPPORT_UART);
            return;
        }
        mBluetoothGatt.setCharacteristicNotification(TxChar,true);
        
        BluetoothGattDescriptor descriptor = TxChar.getDescriptor(CCCD);
        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        mBluetoothGatt.writeDescriptor(descriptor);
    	
    }
    
    public void writeRXCharacteristic(byte[] value)
    {
    	if(mBluetoothGatt == null) 
		{
			showToast(getApplicationContext(),"No BLE Connection");
			return;
    		}

    	BluetoothGattService RxService = mBluetoothGatt.getService(RX_SERVICE_UUID);
    	showMessage("mBluetoothGatt null"+ mBluetoothGatt);
    	if (RxService == null) {
            showMessage("Rx service not found!");
            broadcastUpdate(DEVICE_DOES_NOT_SUPPORT_UART);
            return;
        }
    	BluetoothGattCharacteristic RxChar = RxService.getCharacteristic(RX_CHAR_UUID);
        if (RxChar == null) {
            showMessage("Rx charateristic not found!");
            broadcastUpdate(DEVICE_DOES_NOT_SUPPORT_UART);
            return;
        }
        RxChar.setValue(value);
    	boolean status = mBluetoothGatt.writeCharacteristic(RxChar);
    	
        Log.d(TAG, "write TXchar - status=" + status);  
    }

    private static void showToast(Context context, String msg)
    {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }
    
    private void showMessage(String msg) {
        Log.e(TAG, msg);
    }
    /**
     * Retrieves a list of supported GATT services on the connected device. This should be
     * invoked only after {@code BluetoothGatt#discoverServices()} completes successfully.
     *
     * @return A {@code List} of supported services.
     */
    public List<BluetoothGattService> getSupportedGattServices() {
        if (mBluetoothGatt == null) return null;

        return mBluetoothGatt.getServices();
    }

    public boolean isGeneratedPasscodeEnabled() {
        return enableGeneratedPasscode;
    }

    public void setEnableGeneratedPasscode(boolean value) {
        enableGeneratedPasscode = value;
    }

    private String getPin(Context context, BluetoothDevice device, int maxLength) {
        String pinCode = "1234567";

        if (isGeneratedPasscodeEnabled()) {

            final String fillPattern = "%mmy#V4%Wum=4^xsAk7fxQzLCBGQqgMzgP=bm*73=%EB9E#r*Acjf%v8c?=_U!Wv";

            String address = device.getAddress();
            address = address.concat(fillPattern.substring(0, 64 - address.length()));

            try {
                MessageDigest md = MessageDigest.getInstance("MD5");
                byte result[] = md.digest(address.getBytes());

                final char[] decimalArray = "0123456789".toCharArray();
                char[] decimalChars = new char[result.length * 3];
                for ( int j = 0; j < result.length; j++ ) {
                    int v = result[j] & 0xFF;
                    decimalChars[j * 3] = decimalArray[v / 100];
                    decimalChars[j * 3 + 1] = decimalArray[(v / 10) % 10];
                    decimalChars[j * 3 + 2] = decimalArray[v % 10];
                }

                pinCode = new String(decimalChars);

            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
        }

        return pinCode.substring(0, maxLength);
    }

    private void registerPairingReceiver() {
        if (pairingReceiverRegistered) {
            return;
        }

        final IntentFilter pairingRequestFilter = new IntentFilter();
        pairingRequestFilter.addAction(BluetoothDevice.ACTION_PAIRING_REQUEST);
        pairingRequestFilter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        pairingRequestFilter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY - 1);
        getApplicationContext().registerReceiver(mPairingRequestReceiver, pairingRequestFilter);

        pairingReceiverRegistered = true;
    }

    private void unregisterPairingReceiver() {
        if (!pairingReceiverRegistered) {
            return;
        }
        getApplicationContext().unregisterReceiver(mPairingRequestReceiver);

        pairingReceiverRegistered = false;
    }

    BroadcastReceiver mPairingRequestReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

            if (!mRequestedBluetoothDeviceAddress.equals(device.getAddress())) {
                return;
            }

            if (BluetoothDevice.ACTION_PAIRING_REQUEST.equals(intent.getAction()))
            {
                Log.d(TAG, "Pairing Requested");

                int type = intent.getIntExtra(BluetoothDevice.EXTRA_PAIRING_VARIANT, BluetoothDevice.ERROR);

                Log.d(TAG, "Pairing Type: " + String.valueOf(type));

                if (type == BluetoothDevice.PAIRING_VARIANT_PIN)
                {
                    String pinCode = getPin(context, device, 6);
                    device.setPin(pinCode.getBytes());

                    abortBroadcast();

                    Log.d(TAG, "Pin Sent");

                } else {
//                L.w("Unexpected pairing type: " + type);
                }
            } else if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(intent.getAction())) {
                Log.d(TAG, "Bond state changed to " + String.valueOf(intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.BOND_NONE)));

                if (intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.BOND_NONE) == BluetoothDevice.BOND_BONDED) {
                    Log.d(TAG, "Device bonded");

                    connect(device.getAddress());
                } else if (intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.BOND_NONE) == BluetoothDevice.BOND_NONE) {
                    Log.d(TAG, "Device unbonded");

                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    mBluetoothDeviceAddress = null;
                    connect(device.getAddress());
                }
            }
        }
    };
}
