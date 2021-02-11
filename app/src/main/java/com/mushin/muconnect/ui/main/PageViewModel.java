package com.mushin.muconnect.ui.main;

import android.app.Application;
import android.content.Intent;
import android.util.Log;
import android.view.View;

import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.mushin.muconnect.PolarCallback;
import com.mushin.muconnect.Utils;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.Transformations;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public class PageViewModel extends AndroidViewModel {

    private final static String TAG = PageViewModel.class.getSimpleName();

    private static final int MAX_DATA_POINT_COUNT = 1000;

    public static final String USER_SCAN_DEVICES_REQUEST = "com.muShin.DEVICE_SCAN_REQUESTED";
    public static final String USER_START_DATA_TRANSFER_REQUEST = "com.muShin.START_DATA_TRANSFER_REQUESTED";
    public static final String USER_STOP_DATA_TRANSFER_REQUEST = "com.muShin.STOP_DATA_TRANSFER_REQUESTED";
    public static final String USER_CONFIGURE_REQUEST = "com.muShin.CONFIGURATION_REQUESTED";
    public static final String USER_DISCONNECT_REQUEST = "com.muShin.DISCONNECT_REQUESTED";

    public PageViewModel(@NonNull Application application) {
        super(application);

        Observer showCrankDataObserver = o -> {
            Integer deviceSectionVisible = getDeviceSectionVisibility().getValue();
            Boolean showData = getShowCrankData().getValue();

            Utils.setPreference(getApplication(), "showCrankData", showData);

            crankDataVisible.setValue(
                    showData != null && deviceSectionVisible != null &&
                    showData && deviceSectionVisible == View.VISIBLE ? View.VISIBLE : View.GONE);
        };

        crankDataVisible.addSource(deviceSectionVisibility, showCrankDataObserver);
        crankDataVisible.addSource(showCrankData, showCrankDataObserver);

        Observer showAccDataObserver = o -> {
            Integer deviceSectionVisible = getDeviceSectionVisibility().getValue();
            Boolean showData = getShowAccData().getValue();

            Utils.setPreference(getApplication(), "showAccData", showData);

            accDataVisible.setValue(
                    showData != null && deviceSectionVisible != null &&
                    showData && deviceSectionVisible == View.VISIBLE ? View.VISIBLE : View.GONE);
        };

        accDataVisible.addSource(deviceSectionVisibility, showAccDataObserver);
        accDataVisible.addSource(showAccData, showAccDataObserver);

        Observer showGyroDataObserver = o -> {
            Integer deviceSectionVisible = getDeviceSectionVisibility().getValue();
            Boolean showData = getShowGyroData().getValue();

            Utils.setPreference(getApplication(), "showGyroData", showData);

            gyroDataVisible.setValue(
                    showData != null && deviceSectionVisible != null &&
                            showData && deviceSectionVisible == View.VISIBLE ? View.VISIBLE : View.GONE);
        };

        gyroDataVisible.addSource(deviceSectionVisibility, showGyroDataObserver);
        gyroDataVisible.addSource(showGyroData, showGyroDataObserver);

        Observer showOtherDataObserver = o -> {
            Integer deviceSectionVisible = getDeviceSectionVisibility().getValue();
            Boolean showData = getShowOtherData().getValue();

            Utils.setPreference(getApplication(), "showOtherData", showData);

            otherDataVisible.setValue(
                    showData != null && deviceSectionVisible != null &&
                            showData && deviceSectionVisible == View.VISIBLE ? View.VISIBLE : View.GONE);
        };

        otherDataVisible.addSource(deviceSectionVisibility, showOtherDataObserver);
        otherDataVisible.addSource(showOtherData, showOtherDataObserver);

        setShowCrankData(Utils.getPreference(getApplication(), "showCrankData", false));
        setShowAccData(Utils.getPreference(getApplication(), "showAccData", false));
        setShowGyroData(Utils.getPreference(getApplication(), "showGyroData", false));
        setShowOtherData(Utils.getPreference(getApplication(), "showOtherData", false));

//        setDeviceId("zzz");
    }

    public static class ConnectionState {
        public static final int Disconnected = 0;
        public static final int Connecting = 1;
        public static final int Connected = 2;
        public static final int Error = 3;
        public static final int Scanning = 4;
        public static final int Ready = 5;
    }

    private void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);
        LocalBroadcastManager.getInstance(getApplication()).sendBroadcast(intent);
    }

    private String getStatusString(int state) {
        String result = "unknown";
        switch(state) {
            case ConnectionState.Disconnected:
                result = "Disconnected";
                break;
            case ConnectionState.Connecting:
                result = "Connecting";
                break;
            case ConnectionState.Connected:
                result = "Connected";
                break;
            case ConnectionState.Error:
                result = "Error";
                break;
            case ConnectionState.Scanning:
                result = "Scanning";
                break;
            case ConnectionState.Ready:
                result = "Ready";
                break;
        }
        return result;
    }

    // region Polar

    private MutableLiveData<Boolean> polarConnected = new MutableLiveData<>(false);
    private MutableLiveData<Integer> polarConnectionState = new MutableLiveData<>(ConnectionState.Disconnected);
    private MutableLiveData<String> polarDeviceId = new MutableLiveData<>();
    private MutableLiveData<Integer> polarHR = new MutableLiveData<>();

    private LiveData<String> polarStatusText = Transformations.map(polarConnectionState, state -> getStatusString(state));

    public LiveData<String> getPolarStatusText() {
        return polarStatusText;
    }

    public Boolean getPolarConnected() {
        return polarConnected.getValue();
    }

    private void onPolarConnectRequest(boolean value) {
        if (value) {
            broadcastUpdate(PolarCallback.POLAR_ACTION_CONNECT);
        } else {
            broadcastUpdate(PolarCallback.POLAR_ACTION_DISCONNECT);
        }
    }

    public void setPolarConnected(Boolean polarConnected) {
        onPolarConnectRequest(polarConnected);
        this.polarConnected.postValue(polarConnected);
    }

    public Integer getPolarConnectionState() {
        return polarConnectionState.getValue();
    }

    public void setPolarConnectionState(Integer polarConnectionState) {
        this.polarConnectionState.postValue(polarConnectionState);
    }

    public MutableLiveData<String> getPolarDeviceId() {
        return polarDeviceId;
    }

    public void setPolarDeviceId(String polarDeviceId) {
        this.polarDeviceId.postValue(polarDeviceId);
    }

    public MutableLiveData<Integer> getPolarHR() {
        return polarHR;
    }

    public void setPolarHR(Integer polarHR) {
        this.polarHR.postValue(polarHR);
    }

    // endregion

    // region muShin

    private MutableLiveData<Integer> connectionState = new MutableLiveData<>(ConnectionState.Disconnected);
    private MutableLiveData<String> deviceId = new MutableLiveData<>("");
    private MutableLiveData<String> deviceName = new MutableLiveData<>("");
    private LiveData<String> statusText = Transformations.map(connectionState, state -> getStatusString(state));
    private LiveData<Integer> deviceSectionVisibility = Transformations.map(deviceId, deviceId -> deviceId != null && !deviceId.isEmpty() ? View.VISIBLE : View.GONE);
    private LiveData<Boolean> deviceReady = Transformations.map(connectionState, connectionState -> connectionState == ConnectionState.Ready);

    private MutableLiveData<Boolean> showCrankData = new MutableLiveData<>(true);
    private MutableLiveData<Boolean> showAccData = new MutableLiveData<>(true);
    private MutableLiveData<Boolean> showGyroData = new MutableLiveData<>(true);
    private MutableLiveData<Boolean> showOtherData = new MutableLiveData<>(true);

    private MediatorLiveData<Integer> crankDataVisible = new MediatorLiveData<>();
    private MediatorLiveData<Integer> accDataVisible = new MediatorLiveData<>();
    private MediatorLiveData<Integer> gyroDataVisible = new MediatorLiveData<>();
    private MediatorLiveData<Integer> otherDataVisible = new MediatorLiveData<>();

    private LineGraphSeries<DataPoint> leftCrankSeries = new LineGraphSeries<>();
    private LineGraphSeries<DataPoint> rightCrankSeries = new LineGraphSeries<>();

    private LineGraphSeries<DataPoint> accXSeries = new LineGraphSeries<>();
    private LineGraphSeries<DataPoint> accYSeries = new LineGraphSeries<>();
    private LineGraphSeries<DataPoint> accZSeries = new LineGraphSeries<>();

    private LineGraphSeries<DataPoint> gyroXSeries = new LineGraphSeries<>();
    private LineGraphSeries<DataPoint> gyroYSeries = new LineGraphSeries<>();
    private LineGraphSeries<DataPoint> gyroZSeries = new LineGraphSeries<>();

    // region getters & setters
    public MutableLiveData<Integer> getConnectionState() {
        return connectionState;
    }

    public void setConnectionState(Integer connectionState) {
        this.connectionState.postValue(connectionState);
    }

    public LiveData<String> getStatusText() {
        return statusText;
    }

    public MutableLiveData<String> getDeviceId() {
        return deviceId;
    }
    public void setDeviceId(String deviceId) {
        this.deviceId.postValue(deviceId);
    }

    public MutableLiveData<String> getDeviceName() {
        return deviceName;
    }
    public void setDeviceName(String deviceName) {
        this.deviceName.postValue(deviceName);
    }

    public LiveData<Integer> getDeviceSectionVisibility() {
        return deviceSectionVisibility;
    }

    public LiveData<Boolean> getDeviceReady() {
        return deviceReady;
    }

    public MutableLiveData<Boolean> getShowCrankData() {
        return showCrankData;
    }
    public void setShowCrankData(MutableLiveData<Boolean> showCrankData) {
        this.showCrankData = showCrankData;
        Utils.setPreference(getApplication(), "showCrankData", showCrankData.getValue());
    }
    public void setShowCrankData(boolean showCrankData) {
        this.showCrankData.postValue(showCrankData);
        Utils.setPreference(getApplication(), "showCrankData", showCrankData);
    }

    public MutableLiveData<Boolean> getShowAccData() {
        return showAccData;
    }
    public void setShowAccData(MutableLiveData<Boolean> showAccData) {
        this.showAccData = showAccData;
        Utils.setPreference(getApplication(), "showAccData", showAccData.getValue());
    }
    public void setShowAccData(boolean showAccData) {
        this.showAccData.postValue(showAccData);
        Utils.setPreference(getApplication(), "showAccData", showAccData);
    }

    public MutableLiveData<Boolean> getShowGyroData() {
        return showGyroData;
    }
    public void setShowGyroData(MutableLiveData<Boolean> showGyroData) {
        this.showGyroData = showGyroData;
        Utils.setPreference(getApplication(), "showGyroData", showGyroData.getValue());
    }
    public void setShowGyroData(boolean showGyroData) {
        this.showGyroData.postValue(showGyroData);
        Utils.setPreference(getApplication(), "showGyroData", showGyroData);
    }

    public MutableLiveData<Boolean> getShowOtherData() {
        return showOtherData;
    }
    public void setShowOtherData(MutableLiveData<Boolean> showOtherData) {
        this.showOtherData = showOtherData;
        Utils.setPreference(getApplication(), "showOtherData", showOtherData.getValue());
    }
    public void setShowOtherData(boolean showOtherData) {
        this.showOtherData.postValue(showOtherData);
        Utils.setPreference(getApplication(), "showOtherData", showOtherData);
    }

    public MediatorLiveData<Integer> getCrankDataVisible() {
        return crankDataVisible;
    }
    public MediatorLiveData<Integer> getAccDataVisible() {
        return accDataVisible;
    }
    public MediatorLiveData<Integer> getGyroDataVisible() {
        return gyroDataVisible;
    }
    public MediatorLiveData<Integer> getOtherDataVisible() {
        return otherDataVisible;
    }

    public LineGraphSeries<DataPoint> getLeftCrankSeries() {
        return leftCrankSeries;
    }
    public LineGraphSeries<DataPoint> getRightCrankSeries() {
        return rightCrankSeries;
    }

    public LineGraphSeries<DataPoint> getAccXSeries() {
        return accXSeries;
    }
    public LineGraphSeries<DataPoint> getAccYSeries() {
        return accYSeries;
    }
    public LineGraphSeries<DataPoint> getAccZSeries() {
        return accZSeries;
    }

    public LineGraphSeries<DataPoint> getGyroXSeries() {
        return gyroXSeries;
    }
    public LineGraphSeries<DataPoint> getGyroYSeries() {
        return gyroYSeries;
    }
    public LineGraphSeries<DataPoint> getGyroZSeries() {
        return gyroZSeries;
    }

    // endregion getters & setters

    // region button handlers
    public void onScanDeviceButtonClick(View view) {
        broadcastUpdate(USER_SCAN_DEVICES_REQUEST);
    }

    public void onStartButtonClick(View view) {
        broadcastUpdate(USER_START_DATA_TRANSFER_REQUEST);
    }

    public void onStopButtonClick(View view) {
        broadcastUpdate(USER_STOP_DATA_TRANSFER_REQUEST);
    }

    public void onConfigureButtonClick(View view) {
        broadcastUpdate(USER_CONFIGURE_REQUEST);
    }

    public void onDisconnectButtonClick(View view) {
        broadcastUpdate(USER_DISCONNECT_REQUEST);
    }

    // endregion button handlers

    // region graph data handling

    public void resetAllSeries() {
        leftCrankSeries.resetData(new DataPoint[0]);
        rightCrankSeries.resetData(new DataPoint[0]);

        accXSeries.resetData(new DataPoint[0]);
        accYSeries.resetData(new DataPoint[0]);
        accZSeries.resetData(new DataPoint[0]);

        gyroXSeries.resetData(new DataPoint[0]);
        gyroYSeries.resetData(new DataPoint[0]);
        gyroZSeries.resetData(new DataPoint[0]);
    }

    private double lastLeftCrankData = 0;
    private double lastRightCrankData = 0;

    public void addCranksData(double tick, Double l, Double r) {
        addLeftCrankData(tick, l == null ? lastLeftCrankData : l);
        addRightCrankData(tick, r == null ? lastRightCrankData : r);
    }

    private void addLeftCrankData(double x, double y) {
        Log.d(TAG, "Left crank data added " + String.valueOf(x) + " " + String.valueOf(y));
        leftCrankSeries.appendData(new DataPoint(x, y), true, MAX_DATA_POINT_COUNT);
        lastLeftCrankData = y;
    }

    private void addRightCrankData(double x, double y) {
        Log.d(TAG, "Right crank data added " + String.valueOf(x) + " " + String.valueOf(y));

        rightCrankSeries.appendData(new DataPoint(x, y), true, MAX_DATA_POINT_COUNT);
        lastRightCrankData = y;
    }

    private double lastAccX = 0;
    private double lastAccY = 0;
    private double lastAccZ = 0;

    public void addAccData(double tick, Double x, Double y, Double z) {
        addAccXData(tick, x == null ? lastAccX : x);
        addAccYData(tick, y == null ? lastAccY : y);
        addAccZData(tick, z == null ? lastAccZ : z);
    }

    private void addAccXData(double x, double y) {
        accXSeries.appendData(new DataPoint(x, y), true, MAX_DATA_POINT_COUNT);
        lastAccX = y;
    }

    private void addAccYData(double x, double y) {
        accYSeries.appendData(new DataPoint(x, y), true, MAX_DATA_POINT_COUNT);
        lastAccY = y;
    }

    private void addAccZData(double x, double y) {
        accZSeries.appendData(new DataPoint(x, y), true, MAX_DATA_POINT_COUNT);
        lastAccZ = y;
    }

    private double lastGyroX = 0;
    private double lastGyroY = 0;
    private double lastGyroZ = 0;

    public void addGyroData(double tick, Double x, Double y, Double z) {
        addGyroXData(tick, x == null ? lastGyroX : x);
        addGyroYData(tick, y == null ? lastGyroY : y);
        addGyroZData(tick, z == null ? lastGyroZ : z);
    }

    private void addGyroXData(double x, double y) {
        gyroXSeries.appendData(new DataPoint(x, y), true, MAX_DATA_POINT_COUNT);
        lastGyroX = y;
    }

    private void addGyroYData(double x, double y) {
        gyroYSeries.appendData(new DataPoint(x, y), true, MAX_DATA_POINT_COUNT);
        lastGyroY = y;
    }

    private void addGyroZData(double x, double y) {
        gyroZSeries.appendData(new DataPoint(x, y), true, MAX_DATA_POINT_COUNT);
        lastGyroZ = y;
    }


    // endregion graph data handling

    // endregion muShin
}