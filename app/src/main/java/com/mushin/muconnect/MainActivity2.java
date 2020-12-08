package com.mushin.muconnect;

import android.Manifest;
import android.app.Activity;
import android.app.DialogFragment;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.google.android.material.tabs.TabLayout;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.Disposable;
import polar.com.sdk.api.PolarBleApi;
import polar.com.sdk.api.PolarBleApiDefaultImpl;
import polar.com.sdk.api.errors.PolarInvalidArgument;

import com.mushin.muconnect.ui.main.PageViewModel;
import com.mushin.muconnect.ui.main.SectionsPagerAdapter;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

public class MainActivity2 extends AppCompatActivity {

    private final static String TAG = MainActivity2.class.getSimpleName();

    private PageViewModel pageViewModel;
    PolarBleApi polarApi;
    String polarDeviceId;
    PolarCallback polarCallback = new PolarCallback();

    public UartService mService = null;

    private BluetoothAdapter mBluetoothAdapter;

//    private static final int REQUEST_COARSE_LOCATION_PERMISSION = 200;
//    private String permissions = Manifest.permission.ACCESS_COARSE_LOCATION;

    Configuration deviceConfiguration = new Configuration();

    public Configuration getDeviceConfiguration() {
        return deviceConfiguration;
    }

    public void setDeviceConfiguration(Configuration newConfiguration) {
        ArrayList<String> updateCfgCommands = Configuration.getConfigurationUpdateCommands(this.deviceConfiguration, newConfiguration);
        this.deviceConfiguration = newConfiguration;

        Utils.storeDeviceConfiguration(this, newConfiguration);

//        String cfgUpdate = TextUtils.join(",", updateCfgCommands);
        bleWrite(updateCfgCommands);
    }

    private String[] permissions = {
            android.Manifest.permission.ACCESS_COARSE_LOCATION,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    private PermissionUtility permissionUtility;

    // region Activity lifecycle
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        polarApi = PolarBleApiDefaultImpl.defaultImplementation(this, PolarBleApi.FEATURE_HR);
        polarApi.setApiCallback(polarCallback);
        polarCallback.setContext(this);

        pageViewModel = new ViewModelProvider(this).get(PageViewModel.class);

        setContentView(R.layout.activity_main);
        SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(this, getSupportFragmentManager());
        ViewPager viewPager = findViewById(R.id.view_pager);
        viewPager.setAdapter(sectionsPagerAdapter);
        TabLayout tabs = findViewById(R.id.tabs);
        tabs.setupWithViewPager(viewPager);

        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            finish();
        }

        serviceInit();

        // Initializes a Bluetooth adapter.  For API level 18 and above, get a reference to
        // BluetoothAdapter through BluetoothManager.
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        // Checks if Bluetooth is supported on the device.
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

//        checkBtScanPermissions();
        permissionUtility = new PermissionUtility(this, permissions);
        if(permissionUtility.arePermissionsEnabled()){
            Log.d(TAG, "Permission already granted");
        } else {
            permissionUtility.requestMultiplePermissions();
        }

        Utils.restoreDeviceConfiguration(this, getDeviceConfiguration());
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        polarCallback.setContext(null);

        polarApi.shutDown();

        try {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(IntentReceiver);
        } catch (Exception ignore) {
//            Log.e(TAG, ignore.toString());
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        polarCallback.setContext(null);
        polarApi.backgroundEntered();

        DataLogger.getInstance().stopLogging();
    }

    @Override
    public void onResume() {
        super.onResume();
        polarCallback.setContext(this);
        polarApi.foregroundEntered();

//        Log.d(TAG, "onResume");
//        if (!mBtAdapter.isEnabled()) {
//            Log.i(TAG, "onResume - BT not enabled yet");
//            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
//            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
//        }

    }

    // endregion

    // region permissions handling
/*
    private void checkBtScanPermissions() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
//            populateList();
        } else {
            if (ContextCompat.checkSelfPermission(getApplicationContext(), permissions) != PackageManager.PERMISSION_GRANTED) {
                if (shouldShowRequestPermissionRationale(permissions)) {
                    // TODO: show explanation why location is needed
                } else {
                    requestPermissions(new String[]{permissions}, REQUEST_COARSE_LOCATION_PERMISSION);
                }
            } else {
//                populateList();
            }
        }

    }
*/
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(permissionUtility.onRequestPermissionsResult(requestCode, permissions, grantResults)) {
            Log.d(TAG, "Permissions granted");
        }
//        boolean permissionsGranted = false;
//
//        switch (requestCode){
//            case REQUEST_COARSE_LOCATION_PERMISSION:
//                permissionsGranted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
//                break;
//        }
//        if (permissionsGranted ) {
////            populateList();
//        } else {
////            Log.d(TAG, "Can't get results from BLE scan, permissions not granted");
//        }
    }

    // endregion

    // region UART service
    private void serviceInit() {
        Intent bindIntent = new Intent(this, UartService.class);
        bindService(bindIntent, mServiceConnection, Context.BIND_AUTO_CREATE);

        LocalBroadcastManager.getInstance(this).registerReceiver(IntentReceiver, makeIntentFilter());
    }

    //UART service connected/disconnected
    private ServiceConnection mServiceConnection = new ServiceConnection()
    {
        public void onServiceConnected(ComponentName className, IBinder rawBinder)
        {
            mService = ((UartService.LocalBinder) rawBinder).getService();
//            Log.d(TAG, "onServiceConnected mService= " + mService);
            if (!mService.initialize()) {
//                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            } else {
                boolean useGeneratedPasscode = Utils.getPreference(getApplicationContext(), "useGeneratedPasscode", false);
                mService.setEnableGeneratedPasscode(useGeneratedPasscode);
            }
        }

        public void onServiceDisconnected(ComponentName classname)
        {
            mService = null;
        }
    };

    private void bleWrite(ArrayList<String> data) {
        for (String str: data) {
            bleWrite(str);

            try {
                Thread.sleep(30);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void bleWrite(String data) {
        try {
            mService.writeRXCharacteristic(data.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }
    // endregion

    // region activity result handling

    ActivityResultLauncher<Intent> mScanForDeviceLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent intent = result.getData();

                        String deviceAddress = intent.getStringExtra(BluetoothDevice.EXTRA_DEVICE);
                        String deviceName = intent.getStringExtra(BluetoothDevice.EXTRA_NAME);
                        pageViewModel.setDeviceId(deviceAddress);
                        pageViewModel.setDeviceName(deviceName);

                        mService.connect(deviceAddress);
                    }
                }
            });

    ActivityResultLauncher<Intent> mConfigLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Configuration configuration = new Configuration();
                        Utils.restoreDeviceConfiguration(getApplicationContext(), configuration);
                        setDeviceConfiguration(configuration);

                        DataLogger.getInstance().setConfiguration(configuration);
                    }
                }
            });


    // endregion

    // region intent handling
    private static IntentFilter makeIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();

        // muShin
        intentFilter.addAction(UartService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(UartService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(UartService.ACTION_GATT_READY);
        intentFilter.addAction(UartService.ACTION_DATA_AVAILABLE);
        intentFilter.addAction(UartService.DEVICE_DOES_NOT_SUPPORT_UART);
        intentFilter.addAction(UartService.ACTION_GATT_RECONNECTING);

        // polar
        intentFilter.addAction(PolarCallback.POLAR_STATE_CONNECTING);
        intentFilter.addAction(PolarCallback.POLAR_STATE_CONNECTED);
        intentFilter.addAction(PolarCallback.POLAR_STATE_DISCONNECTED);
        intentFilter.addAction(PolarCallback.POLAR_ACTION_CONNECT);
        intentFilter.addAction(PolarCallback.POLAR_ACTION_DISCONNECT);
        intentFilter.addAction(PolarCallback.POLAR_STATE_HR_RECEIVED);

        // user actions
        intentFilter.addAction(PageViewModel.USER_SCAN_DEVICES_REQUEST);
        intentFilter.addAction(PageViewModel.USER_START_DATA_TRANSFER_REQUEST);
        intentFilter.addAction(PageViewModel.USER_STOP_DATA_TRANSFER_REQUEST);
        intentFilter.addAction(PageViewModel.USER_CONFIGURE_REQUEST);
        intentFilter.addAction(PageViewModel.USER_DISCONNECT_REQUEST);

        return intentFilter;
    }

    Disposable scanDisposable;

    private final BroadcastReceiver IntentReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (action.equals(PolarCallback.POLAR_ACTION_CONNECT)) {

                if(scanDisposable != null) {
                    scanDisposable.dispose();
                    scanDisposable = null;
                }

                scanDisposable = polarApi.searchForDevice().observeOn(AndroidSchedulers.mainThread()).subscribe(
                        polarDeviceInfo -> {
                            Log.d(TAG, "polar device found id: " + polarDeviceInfo.deviceId + " address: " + polarDeviceInfo.address + " rssi: " + polarDeviceInfo.rssi + " name: " + polarDeviceInfo.name + " isConnectable: " + polarDeviceInfo.isConnectable);
                            pageViewModel.setPolarConnectionState(PageViewModel.ConnectionState.Connecting);
                            polarApi.connectToDevice(polarDeviceInfo.deviceId);
                            scanDisposable.dispose();
                            scanDisposable = null;
                        },
                        throwable -> {
                            Log.d(TAG, "" + throwable.getLocalizedMessage());
                            pageViewModel.setPolarConnectionState(PageViewModel.ConnectionState.Disconnected);
                        },
                        () -> {
                            Log.d(TAG, "complete");
                            pageViewModel.setPolarConnectionState(PageViewModel.ConnectionState.Disconnected);
                        }
                );

            } else if (action.equals(PolarCallback.POLAR_ACTION_DISCONNECT)) {
                if (polarDeviceId != null) {
                    try {
                        polarApi.disconnectFromDevice(polarDeviceId);
                    } catch (PolarInvalidArgument polarInvalidArgument) {
                        polarInvalidArgument.printStackTrace();
                    }
                }
            } else if (action.equals(PolarCallback.POLAR_STATE_CONNECTING)) {
                polarDeviceId = intent.getStringExtra("polarDeviceId");
                pageViewModel.setPolarDeviceId(polarDeviceId);
                pageViewModel.setPolarConnectionState(PageViewModel.ConnectionState.Connecting);

            } else if (action.equals(PolarCallback.POLAR_STATE_CONNECTED)) {
                polarDeviceId = intent.getStringExtra("polarDeviceId");
                pageViewModel.setPolarDeviceId(polarDeviceId);
                pageViewModel.setPolarConnectionState(PageViewModel.ConnectionState.Connected);
            } else if (action.equals(PolarCallback.POLAR_STATE_DISCONNECTED)) {
                pageViewModel.setPolarConnectionState(PageViewModel.ConnectionState.Disconnected);
                polarDeviceId = null;
            } else if (action.equals(PolarCallback.POLAR_STATE_HR_RECEIVED)) {
                int hr = intent.getIntExtra("polarHR", -1);
                if (hr >= 0) {
                    pageViewModel.setPolarHR(hr);
                }
            } else if (action.equals(PageViewModel.USER_SCAN_DEVICES_REQUEST)) {
                Intent activityIntent = new Intent(MainActivity2.this, com.mushin.muconnect.DeviceListActivity.class);
                mScanForDeviceLauncher.launch(activityIntent);
            } else if (action.equals(PageViewModel.USER_START_DATA_TRANSFER_REQUEST)) {
                bleWrite(DeviceCommand.Start);
                DataLogger.getInstance().startLogging();
            } else if (action.equals(PageViewModel.USER_STOP_DATA_TRANSFER_REQUEST)) {
                bleWrite(DeviceCommand.Stop);
                DataLogger.getInstance().stopLogging();
            } else if (action.equals(PageViewModel.USER_CONFIGURE_REQUEST)) {
//                DialogFragment newFragment = new ConfigActivity();
//                newFragment.show(getFragmentManager(), "ConfigActivity");
                Intent activityIntent = new Intent(MainActivity2.this, com.mushin.muconnect.ConfigActivity.class);
                mConfigLauncher.launch(activityIntent);
            } else if (action.equals(PageViewModel.USER_DISCONNECT_REQUEST)) {
                mService.disconnect();
                DataLogger.getInstance().stopLogging();
            } else if (action.equals(UartService.ACTION_GATT_CONNECTED)) {
                pageViewModel.setConnectionState(PageViewModel.ConnectionState.Connected);
            } else if (action.equals(UartService.ACTION_GATT_DISCONNECTED)) {
                pageViewModel.setConnectionState(PageViewModel.ConnectionState.Disconnected);
                pageViewModel.setDeviceId(null);
                mService.close();
            } else if (action.equals(UartService.ACTION_GATT_RECONNECTING)) {
                pageViewModel.setConnectionState(PageViewModel.ConnectionState.Connecting);
            } else if (action.equals(UartService.ACTION_GATT_READY)) {
                pageViewModel.setConnectionState(PageViewModel.ConnectionState.Ready);

                // send configuration to the connected device
                Configuration defaultCfg = new Configuration();
                ArrayList<String> updateCfgCommands = Configuration.getConfigurationUpdateCommands(defaultCfg, getDeviceConfiguration());
                bleWrite(updateCfgCommands);

            } else if (action.equals(UartService.ACTION_DATA_AVAILABLE)) {
//                byte z[] = intent.getStringExtra(UartService.EXTRA_DATA);
                String data = intent.getStringExtra(UartService.EXTRA_DATA);
                DeviceData.process(pageViewModel, data);
            }
        }
    };
    // endregion

}