package com.mushin.muconnect;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.UUID;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import polar.com.sdk.api.PolarBleApiCallback;
import polar.com.sdk.api.model.PolarDeviceInfo;
import polar.com.sdk.api.model.PolarHrData;

public class PolarCallback extends PolarBleApiCallback {

    // actions
    public static final String POLAR_ACTION_CONNECT = "com.muShin.PolarConnect";
    public static final String POLAR_ACTION_DISCONNECT = "com.muShin.PolarDisconnect";

    // states
    public static final String POLAR_STATE_CONNECTING = "com.muShin.PolarConnecting";
    public static final String POLAR_STATE_CONNECTED = "com.muShin.PolarConnected";
    public static final String POLAR_STATE_DISCONNECTED = "com.muShin.PolarDisconnected";
    public static final String POLAR_STATE_HR_RECEIVED = "com.muShin.PolarHrReceived";

    private Context _context;

    @Override
    public void blePowerStateChanged(boolean powered) {
        Log.d("MyApp","BLE power: " + powered);
    }

    @Override
    public void deviceConnected(PolarDeviceInfo polarDeviceInfo) {
        Log.d("MyApp","CONNECTED: " + polarDeviceInfo.deviceId);

        final Intent intent = new Intent(POLAR_STATE_CONNECTED);
        intent.putExtra("polarDeviceId", polarDeviceInfo.deviceId);
        broadcastUpdate(intent);
    }

    @Override
    public void deviceConnecting(PolarDeviceInfo polarDeviceInfo) {
        Log.d("MyApp","CONNECTING: " + polarDeviceInfo.deviceId);

        final Intent intent = new Intent(POLAR_STATE_CONNECTING);
        intent.putExtra("polarDeviceId", polarDeviceInfo.deviceId);
        broadcastUpdate(intent);
    }

    @Override
    public void deviceDisconnected(PolarDeviceInfo polarDeviceInfo) {
        Log.d("MyApp","DISCONNECTED: " + polarDeviceInfo.deviceId);
        final Intent intent = new Intent(POLAR_STATE_DISCONNECTED);
        intent.putExtra("polarDeviceId", polarDeviceInfo.deviceId);
        broadcastUpdate(intent);
    }

    @Override
    public void ecgFeatureReady(String identifier) {
    }

    @Override
    public void accelerometerFeatureReady(String identifier) {
    }

    @Override
    public void ppgFeatureReady(String identifier) {
    }

    @Override
    public void ppiFeatureReady(String identifier) {
    }

    @Override
    public void biozFeatureReady(String identifier) {
    }

    @Override
    public void hrFeatureReady(String identifier) {
        Log.d("MyApp","HR READY: " + identifier);
    }

    @Override
    public void disInformationReceived(String identifier, UUID uuid, String value) {
    }

    @Override
    public void batteryLevelReceived(String identifier, int level) {
    }

    @Override
    public void hrNotificationReceived(String identifier, PolarHrData data) {
        Log.d("MyApp","HR: " + data.hr);

        final Intent intent = new Intent(POLAR_STATE_HR_RECEIVED);
        intent.putExtra("polarHR", data.hr);
        broadcastUpdate(intent);
    }

    @Override
    public void polarFtpFeatureReady(String s) {
    }

    public void setContext(Context context) {
        this._context = context;
    };

    private void broadcastUpdate(final String action) {
        if (_context != null) {
            final Intent intent = new Intent(action);
            LocalBroadcastManager.getInstance(_context).sendBroadcast(intent);
        }
    }

    private void broadcastUpdate(final Intent intent) {
        if (_context != null) {
            LocalBroadcastManager.getInstance(_context).sendBroadcast(intent);
        }
    }
}
