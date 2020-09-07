package com.mushin.muconnect;

import java.util.ArrayList;

import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;

public class Configuration extends BaseObservable {
    // misc
    private int dataInterval = 0;
    private boolean temperatureDataEnabled = false;

    // cranks
    private boolean leftCrankDataEnabled = false;
    private boolean rightCrankDataEnabled = false;

    private boolean crankDataFilterEnabled = true;
    private float crankRawThreshold = 0.01f;
    private float crankFilteredThreshold = 0.0175f;

    // accelerometer
    private boolean accXDataEnabled = false;
    private boolean accYDataEnabled = false;
    private boolean accZDataEnabled = false;

    private boolean accRawDataEnabled = false;
    private boolean accRawXDataEnabled = false;
    private boolean accRawYDataEnabled = false;
    private boolean accRawZDataEnabled = false;
    private boolean accXDataFilterEnabled = false;
    private boolean accYDataFilterEnabled = false;
    private boolean accZDataFilterEnabled = false;
    private float accXFilteredThreshold = 0;
    private float accYFilteredThreshold = 0;
    private float accZFilteredThreshold = 0;

    // gyroscope
    private boolean gyroXDataEnabled = false;
    private boolean gyroYDataEnabled = false;
    private boolean gyroZDataEnabled = false;

    private boolean gyroRawDataEnabled = false;
    private boolean gyroRawXDataEnabled = false;
    private boolean gyroRawYDataEnabled = false;
    private boolean gyroRawZDataEnabled = false;
    private boolean gyroXDataFilterEnabled = false;
    private boolean gyroYDataFilterEnabled = false;
    private boolean gyroZDataFilterEnabled = false;
    private float gyroXFilteredThreshold = 0;
    private float gyroYFilteredThreshold = 0;
    private float gyroZFilteredThreshold = 0;

    public Configuration copy(Configuration other) {
        Configuration result = new Configuration();

        result.dataInterval = other.getDataInterval();
        result.temperatureDataEnabled = other.isTemperatureDataEnabled();

        result.leftCrankDataEnabled = other.isLeftCrankDataEnabled();
        result.rightCrankDataEnabled = other.isRightCrankDataEnabled();
        result.crankDataFilterEnabled = other.isCrankDataFilterEnabled();
        result.crankRawThreshold = other.getCrankRawThreshold();
        result.crankFilteredThreshold = other.getCrankFilteredThreshold();

        result.accXDataEnabled = other.isAccXDataEnabled();
        result.accYDataEnabled = other.isAccYDataEnabled();
        result.accZDataEnabled = other.isAccZDataEnabled();
        result.accRawDataEnabled = other.isAccRawDataEnabled();
        result.accRawXDataEnabled = other.isAccRawXDataEnabled();
        result.accRawYDataEnabled = other.isAccRawYDataEnabled();
        result.accRawZDataEnabled = other.isAccRawZDataEnabled();
        result.accXDataFilterEnabled = other.isAccXDataFilterEnabled();
        result.accYDataFilterEnabled = other.isAccYDataFilterEnabled();
        result.accZDataFilterEnabled = other.isAccZDataFilterEnabled();
        result.accXFilteredThreshold = other.getAccXFilteredThreshold();
        result.accYFilteredThreshold = other.getAccYFilteredThreshold();
        result.accZFilteredThreshold = other.getAccZFilteredThreshold();

        result.gyroXDataEnabled = other.isGyroXDataEnabled();
        result.gyroYDataEnabled = other.isGyroYDataEnabled();
        result.gyroZDataEnabled = other.isGyroZDataEnabled();

        result.gyroRawDataEnabled = other.isGyroRawDataEnabled();
        result.gyroRawXDataEnabled = other.isGyroRawXDataEnabled();
        result.gyroRawYDataEnabled = other.isGyroRawYDataEnabled();
        result.gyroRawZDataEnabled = other.isGyroRawZDataEnabled();
        result.gyroXDataFilterEnabled = other.isGyroXDataFilterEnabled();
        result.gyroYDataFilterEnabled = other.isGyroYDataFilterEnabled();
        result.gyroZDataFilterEnabled = other.isGyroZDataFilterEnabled();
        result.gyroXFilteredThreshold = other.getGyroXFilteredThreshold();
        result.gyroYFilteredThreshold = other.getGyroYFilteredThreshold();
        result.gyroZFilteredThreshold = other.getGyroZFilteredThreshold();

        return result;
    }

    public static ArrayList<String> getConfigurationUpdateCommands(Configuration cfgOld, Configuration cfgNew) {
        ArrayList<String> result = new ArrayList<>();

        if (cfgOld.getDataInterval() != cfgNew.getDataInterval()) {
            result.add(DeviceCommand.SetInterval + " " + cfgNew.getDataInterval());
        }

        if (cfgOld.isTemperatureDataEnabled() != cfgNew.isTemperatureDataEnabled()) {
            result.add(cfgNew.isTemperatureDataEnabled() ? DeviceCommand.TemperatureEnabled : DeviceCommand.TemperatureDisabled);
        }

        if (cfgOld.isLeftCrankDataEnabled() != cfgNew.isLeftCrankDataEnabled()) {
            result.add(cfgNew.isLeftCrankDataEnabled() ? DeviceCommand.LeftCrankDataEnabled : DeviceCommand.LeftCrankDataDisabled);
        }

        if (cfgOld.isRightCrankDataEnabled() != cfgNew.isRightCrankDataEnabled()) {
            result.add(cfgNew.isRightCrankDataEnabled() ? DeviceCommand.RightCrankDataEnabled : DeviceCommand.RightCrankDataDisabled);
        }

        if (cfgOld.isCrankDataFilterEnabled() != cfgNew.isCrankDataFilterEnabled()) {
            result.add(cfgNew.isCrankDataFilterEnabled() ? DeviceCommand.CrankDataFilterEnabled : DeviceCommand.CrankDataFilterDisabled);
        }

        if (cfgOld.getCrankFilteredThreshold() != cfgNew.getCrankFilteredThreshold()) {
            result.add(DeviceCommand.CrankFilteredThreshold + " " + cfgNew.getCrankFilteredThreshold());
        }

        if (cfgOld.getCrankRawThreshold() != cfgNew.getCrankRawThreshold()) {
            result.add(DeviceCommand.CrankRawThreshold + " " + cfgNew.getCrankRawThreshold());
        }

        if (cfgOld.isAccXDataEnabled() != cfgNew.isAccXDataEnabled()) {
            result.add(cfgNew.isAccXDataEnabled() ? DeviceCommand.AccelerometerXDataEnabled : DeviceCommand.AccelerometerXDataDisabled);
        }

        if (cfgOld.isAccYDataEnabled() != cfgNew.isAccYDataEnabled()) {
            result.add(cfgNew.isAccYDataEnabled() ? DeviceCommand.AccelerometerYDataEnabled : DeviceCommand.AccelerometerYDataDisabled);
        }

        if (cfgOld.isAccZDataEnabled() != cfgNew.isAccZDataEnabled()) {
            result.add(cfgNew.isAccZDataEnabled() ? DeviceCommand.AccelerometerZDataEnabled : DeviceCommand.AccelerometerZDataDisabled);
        }

        if (cfgOld.isAccRawDataEnabled() != cfgNew.isAccRawDataEnabled()) {
            result.add(cfgNew.isAccRawDataEnabled() ? DeviceCommand.AccelerometerRawDataEnabled : DeviceCommand.AccelerometerRawDataDisabled);
        }

        if (cfgOld.isAccRawXDataEnabled() != cfgNew.isAccRawXDataEnabled()) {
            result.add(cfgNew.isAccRawXDataEnabled() ? DeviceCommand.AccelerometerRawXDataEnabled : DeviceCommand.AccelerometerRawXDataDisabled);
        }

        if (cfgOld.isAccRawYDataEnabled() != cfgNew.isAccRawYDataEnabled()) {
            result.add(cfgNew.isAccRawYDataEnabled() ? DeviceCommand.AccelerometerRawYDataEnabled : DeviceCommand.AccelerometerRawYDataDisabled);
        }

        if (cfgOld.isAccRawZDataEnabled() != cfgNew.isAccRawZDataEnabled()) {
            result.add(cfgNew.isAccRawZDataEnabled() ? DeviceCommand.AccelerometerRawZDataEnabled : DeviceCommand.AccelerometerRawZDataDisabled);
        }

        if (cfgOld.isAccXDataFilterEnabled() != cfgNew.isAccXDataFilterEnabled()) {
            result.add(cfgNew.isAccXDataFilterEnabled() ? DeviceCommand.AccelerometerXDataFilterEnabled : DeviceCommand.AccelerometerXDataFilterDisabled);
        }

        if (cfgOld.isAccYDataFilterEnabled() != cfgNew.isAccYDataFilterEnabled()) {
            result.add(cfgNew.isAccYDataFilterEnabled() ? DeviceCommand.AccelerometerYDataFilterEnabled : DeviceCommand.AccelerometerYDataFilterDisabled);
        }

        if (cfgOld.isAccZDataFilterEnabled() != cfgNew.isAccZDataFilterEnabled()) {
            result.add(cfgNew.isAccZDataFilterEnabled() ? DeviceCommand.AccelerometerZDataFilterEnabled : DeviceCommand.AccelerometerZDataFilterDisabled);
        }

        if (cfgOld.getAccXFilteredThreshold() != cfgNew.getAccXFilteredThreshold()) {
            result.add(DeviceCommand.AccelerometerXDataFilterThreshold + " " + cfgNew.getAccXFilteredThreshold());
        }

        if (cfgOld.getAccYFilteredThreshold() != cfgNew.getAccYFilteredThreshold()) {
            result.add(DeviceCommand.AccelerometerYDataFilterThreshold + " " + cfgNew.getAccYFilteredThreshold());
        }

        if (cfgOld.getAccZFilteredThreshold() != cfgNew.getAccZFilteredThreshold()) {
            result.add(DeviceCommand.AccelerometerZDataFilterThreshold + " " + cfgNew.getAccZFilteredThreshold());
        }

        if (cfgOld.isGyroXDataEnabled() != cfgNew.isGyroXDataEnabled()) {
            result.add(cfgNew.isGyroXDataEnabled() ? DeviceCommand.GyroscopeXDataEnabled : DeviceCommand.GyroscopeXDataDisabled);
        }

        if (cfgOld.isGyroYDataEnabled() != cfgNew.isGyroYDataEnabled()) {
            result.add(cfgNew.isGyroYDataEnabled() ? DeviceCommand.GyroscopeYDataEnabled : DeviceCommand.GyroscopeYDataDisabled);
        }

        if (cfgOld.isGyroZDataEnabled() != cfgNew.isGyroZDataEnabled()) {
            result.add(cfgNew.isGyroZDataEnabled() ? DeviceCommand.GyroscopeZDataEnabled : DeviceCommand.GyroscopeZDataDisabled);
        }

        if (cfgOld.isGyroRawDataEnabled() != cfgNew.isGyroRawDataEnabled()) {
            result.add(cfgNew.isGyroRawDataEnabled() ? DeviceCommand.GyroscopeRawDataEnabled : DeviceCommand.GyroscopeRawDataDisabled);
        }

        if (cfgOld.isGyroRawXDataEnabled() != cfgNew.isGyroRawXDataEnabled()) {
            result.add(cfgNew.isGyroRawXDataEnabled() ? DeviceCommand.GyroscopeRawXDataEnabled : DeviceCommand.GyroscopeRawXDataDisabled);
        }

        if (cfgOld.isGyroRawYDataEnabled() != cfgNew.isGyroRawYDataEnabled()) {
            result.add(cfgNew.isGyroRawYDataEnabled() ? DeviceCommand.GyroscopeRawYDataEnabled : DeviceCommand.GyroscopeRawYDataDisabled);
        }

        if (cfgOld.isGyroRawZDataEnabled() != cfgNew.isGyroRawZDataEnabled()) {
            result.add(cfgNew.isGyroRawZDataEnabled() ? DeviceCommand.GyroscopeRawZDataEnabled : DeviceCommand.GyroscopeRawZDataDisabled);
        }

        if (cfgOld.isGyroXDataFilterEnabled() != cfgNew.isGyroXDataFilterEnabled()) {
            result.add(cfgNew.isGyroXDataFilterEnabled() ? DeviceCommand.GyroscopeXDataFilterEnabled : DeviceCommand.GyroscopeXDataFilterDisabled);
        }

        if (cfgOld.isGyroYDataFilterEnabled() != cfgNew.isGyroYDataFilterEnabled()) {
            result.add(cfgNew.isGyroYDataFilterEnabled() ? DeviceCommand.GyroscopeYDataFilterEnabled : DeviceCommand.GyroscopeYDataFilterDisabled);
        }

        if (cfgOld.isGyroZDataFilterEnabled() != cfgNew.isGyroZDataFilterEnabled()) {
            result.add(cfgNew.isGyroZDataFilterEnabled() ? DeviceCommand.GyroscopeZDataFilterEnabled : DeviceCommand.GyroscopeZDataFilterDisabled);
        }

        if (cfgOld.getGyroXFilteredThreshold() != cfgNew.getGyroXFilteredThreshold()) {
            result.add(DeviceCommand.GyroscopeXDataFilterThreshold + " " + cfgNew.getGyroXFilteredThreshold());
        }

        if (cfgOld.getGyroYFilteredThreshold() != cfgNew.getGyroYFilteredThreshold()) {
            result.add(DeviceCommand.GyroscopeYDataFilterThreshold + " " + cfgNew.getGyroYFilteredThreshold());
        }

        if (cfgOld.getGyroZFilteredThreshold() != cfgNew.getGyroZFilteredThreshold()) {
            result.add(DeviceCommand.GyroscopeZDataFilterThreshold + " " + cfgNew.getGyroZFilteredThreshold());
        }

        return result;
    }

    @Bindable
    public boolean isLeftCrankDataEnabled() {
        return leftCrankDataEnabled;
    }

    public void setLeftCrankDataEnabled(boolean leftCrankDataEnabled) {
        this.leftCrankDataEnabled = leftCrankDataEnabled;
        notifyPropertyChanged(BR.leftCrankDataEnabled);
    }

    @Bindable
    public int getDataInterval() {
        return dataInterval;
    }

    public void setDataInterval(int dataInterval) {
        this.dataInterval = dataInterval;
        notifyPropertyChanged(BR.dataInterval);
    }

    @Bindable
    public boolean isTemperatureDataEnabled() {
        return temperatureDataEnabled;
    }

    public void setTemperatureDataEnabled(boolean temperatureDataEnabled) {
        this.temperatureDataEnabled = temperatureDataEnabled;
        notifyPropertyChanged(BR.temperatureDataEnabled);
    }

    @Bindable
    public boolean isRightCrankDataEnabled() {
        return rightCrankDataEnabled;
    }

    public void setRightCrankDataEnabled(boolean rightCrankDataEnabled) {
        this.rightCrankDataEnabled = rightCrankDataEnabled;
        notifyPropertyChanged(BR.rightCrankDataEnabled);
    }

    @Bindable
    public boolean isCrankDataFilterEnabled() {
        return crankDataFilterEnabled;
    }

    public void setCrankDataFilterEnabled(boolean crankDataFilterEnabled) {
        this.crankDataFilterEnabled = crankDataFilterEnabled;
        notifyPropertyChanged(BR.crankDataFilterEnabled);
    }

    @Bindable
    public float getCrankRawThreshold() {
        return crankRawThreshold;
    }

    public void setCrankRawThreshold(float crankRawThreshold) {
        this.crankRawThreshold = crankRawThreshold;
        notifyPropertyChanged(BR.crankRawThreshold);
    }

    @Bindable
    public float getCrankFilteredThreshold() {
        return crankFilteredThreshold;
    }

    public void setCrankFilteredThreshold(float crankFilteredThreshold) {
        this.crankFilteredThreshold = crankFilteredThreshold;
        notifyPropertyChanged(BR.crankFilteredThreshold);
    }

    @Bindable
    public boolean isAccXDataEnabled() {
        return accXDataEnabled;
    }

    public void setAccXDataEnabled(boolean accXDataEnabled) {
        this.accXDataEnabled = accXDataEnabled;
        notifyPropertyChanged(BR.accXDataEnabled);
    }

    @Bindable
    public boolean isAccYDataEnabled() {
        return accYDataEnabled;
    }

    public void setAccYDataEnabled(boolean accYDataEnabled) {
        this.accYDataEnabled = accYDataEnabled;
        notifyPropertyChanged(BR.accYDataEnabled);
    }

    @Bindable
    public boolean isAccZDataEnabled() {
        return accZDataEnabled;
    }

    public void setAccZDataEnabled(boolean accZDataEnabled) {
        this.accZDataEnabled = accZDataEnabled;
        notifyPropertyChanged(BR.accZDataEnabled);
    }

    @Bindable
    public boolean isAccRawDataEnabled() {
        return accRawDataEnabled;
    }

    public void setAccRawDataEnabled(boolean accRawDataEnabled) {
        this.accRawDataEnabled = accRawDataEnabled;
        notifyPropertyChanged(BR.accRawDataEnabled);
    }

    @Bindable
    public boolean isAccRawXDataEnabled() {
        return accRawXDataEnabled;
    }

    public void setAccRawXDataEnabled(boolean accRawXDataEnabled) {
        this.accRawXDataEnabled = accRawXDataEnabled;
        notifyPropertyChanged(BR.accRawXDataEnabled);
    }

    @Bindable
    public boolean isAccRawYDataEnabled() {
        return accRawYDataEnabled;
    }

    public void setAccRawYDataEnabled(boolean accRawYDataEnabled) {
        this.accRawYDataEnabled = accRawYDataEnabled;
        notifyPropertyChanged(BR.accRawYDataEnabled);
    }

    @Bindable
    public boolean isAccRawZDataEnabled() {
        return accRawZDataEnabled;
    }

    public void setAccRawZDataEnabled(boolean accRawZDataEnabled) {
        this.accRawZDataEnabled = accRawZDataEnabled;
        notifyPropertyChanged(BR.accRawZDataEnabled);
    }

    @Bindable
    public boolean isAccXDataFilterEnabled() {
        return accXDataFilterEnabled;
    }

    public void setAccXDataFilterEnabled(boolean accXDataFilterEnabled) {
        this.accXDataFilterEnabled = accXDataFilterEnabled;
        notifyPropertyChanged(BR.accXDataFilterEnabled);
    }

    @Bindable
    public boolean isAccYDataFilterEnabled() {
        return accYDataFilterEnabled;
    }

    public void setAccYDataFilterEnabled(boolean accYDataFilterEnabled) {
        this.accYDataFilterEnabled = accYDataFilterEnabled;
        notifyPropertyChanged(BR.accYDataFilterEnabled);
    }

    @Bindable
    public boolean isAccZDataFilterEnabled() {
        return accZDataFilterEnabled;
    }

    public void setAccZDataFilterEnabled(boolean accZDataFilterEnabled) {
        this.accZDataFilterEnabled = accZDataFilterEnabled;
        notifyPropertyChanged(BR.accZDataFilterEnabled);
    }

    @Bindable
    public float getAccXFilteredThreshold() {
        return accXFilteredThreshold;
    }

    public void setAccXFilteredThreshold(float accXFilteredThreshold) {
        this.accXFilteredThreshold = accXFilteredThreshold;
        notifyPropertyChanged(BR.accXFilteredThreshold);
    }

    @Bindable
    public float getAccYFilteredThreshold() {
        return accYFilteredThreshold;
    }

    public void setAccYFilteredThreshold(float accYFilteredThreshold) {
        this.accYFilteredThreshold = accYFilteredThreshold;
        notifyPropertyChanged(BR.accYFilteredThreshold);
    }

    @Bindable
    public float getAccZFilteredThreshold() {
        return accZFilteredThreshold;
    }

    public void setAccZFilteredThreshold(float accZFilteredThreshold) {
        this.accZFilteredThreshold = accZFilteredThreshold;
        notifyPropertyChanged(BR.accZFilteredThreshold);
    }

    @Bindable
    public boolean isGyroXDataEnabled() {
        return gyroXDataEnabled;
    }

    public void setGyroXDataEnabled(boolean gyroXDataEnabled) {
        this.gyroXDataEnabled = gyroXDataEnabled;
        notifyPropertyChanged(BR.gyroXDataEnabled);
    }

    @Bindable
    public boolean isGyroYDataEnabled() {
        return gyroYDataEnabled;
    }

    public void setGyroYDataEnabled(boolean gyroYDataEnabled) {
        this.gyroYDataEnabled = gyroYDataEnabled;
        notifyPropertyChanged(BR.gyroYDataEnabled);
    }

    @Bindable
    public boolean isGyroZDataEnabled() {
        return gyroZDataEnabled;
    }

    public void setGyroZDataEnabled(boolean gyroZDataEnabled) {
        this.gyroZDataEnabled = gyroZDataEnabled;
        notifyPropertyChanged(BR.gyroZDataEnabled);
    }

    @Bindable
    public boolean isGyroRawDataEnabled() {
        return gyroRawDataEnabled;
    }

    public void setGyroRawDataEnabled(boolean gyroRawDataEnabled) {
        this.gyroRawDataEnabled = gyroRawDataEnabled;
        notifyPropertyChanged(BR.gyroRawDataEnabled);
    }

    @Bindable
    public boolean isGyroRawXDataEnabled() {
        return gyroRawXDataEnabled;
    }

    public void setGyroRawXDataEnabled(boolean gyroRawXDataEnabled) {
        this.gyroRawXDataEnabled = gyroRawXDataEnabled;
        notifyPropertyChanged(BR.gyroRawXDataEnabled);
    }

    @Bindable
    public boolean isGyroRawYDataEnabled() {
        return gyroRawYDataEnabled;
    }

    public void setGyroRawYDataEnabled(boolean gyroRawYDataEnabled) {
        this.gyroRawYDataEnabled = gyroRawYDataEnabled;
        notifyPropertyChanged(BR.gyroRawYDataEnabled);
    }

    @Bindable
    public boolean isGyroRawZDataEnabled() {
        return gyroRawZDataEnabled;
    }

    public void setGyroRawZDataEnabled(boolean gyroRawZDataEnabled) {
        this.gyroRawZDataEnabled = gyroRawZDataEnabled;
        notifyPropertyChanged(BR.gyroRawZDataEnabled);
    }

    @Bindable
    public boolean isGyroXDataFilterEnabled() {
        return gyroXDataFilterEnabled;
    }

    public void setGyroXDataFilterEnabled(boolean gyroXDataFilterEnabled) {
        this.gyroXDataFilterEnabled = gyroXDataFilterEnabled;
        notifyPropertyChanged(BR.gyroXDataFilterEnabled);
    }

    @Bindable
    public boolean isGyroYDataFilterEnabled() {
        return gyroYDataFilterEnabled;
    }

    public void setGyroYDataFilterEnabled(boolean gyroYDataFilterEnabled) {
        this.gyroYDataFilterEnabled = gyroYDataFilterEnabled;
        notifyPropertyChanged(BR.gyroYDataFilterEnabled);
    }

    @Bindable
    public boolean isGyroZDataFilterEnabled() {
        return gyroZDataFilterEnabled;
    }

    public void setGyroZDataFilterEnabled(boolean gyroZDataFilterEnabled) {
        this.gyroZDataFilterEnabled = gyroZDataFilterEnabled;
        notifyPropertyChanged(BR.gyroZDataFilterEnabled);
    }

    @Bindable
    public float getGyroXFilteredThreshold() {
        return gyroXFilteredThreshold;
    }

    public void setGyroXFilteredThreshold(float gyroXFilteredThreshold) {
        this.gyroXFilteredThreshold = gyroXFilteredThreshold;
        notifyPropertyChanged(BR.gyroXFilteredThreshold);
    }

    @Bindable
    public float getGyroYFilteredThreshold() {
        return gyroYFilteredThreshold;
    }

    public void setGyroYFilteredThreshold(float gyroYFilteredThreshold) {
        this.gyroYFilteredThreshold = gyroYFilteredThreshold;
        notifyPropertyChanged(BR.gyroYFilteredThreshold);
    }

    @Bindable
    public float getGyroZFilteredThreshold() {
        return gyroZFilteredThreshold;
    }

    public void setGyroZFilteredThreshold(float gyroZFilteredThreshold) {
        this.gyroZFilteredThreshold = gyroZFilteredThreshold;
        notifyPropertyChanged(BR.gyroZFilteredThreshold);
    }

}
