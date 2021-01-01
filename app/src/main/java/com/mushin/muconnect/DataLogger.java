package com.mushin.muconnect;

import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DataLogger {

    private final static String TAG = DataLogger.class.getSimpleName();

    public static DataLogger mInstance = null;

    public static DataLogger getInstance() {
        if (mInstance == null) {
            mInstance = new DataLogger();
        }

        return mInstance;
    }

    private boolean isEnabled = false;
    private String mLogFileName = null;
    private File mLogFile = null;
    private FileWriter mLogFileWriter = null;

    private boolean headerWritten = false;

    double m_timestamp = -1;
    Double m_leftCrank;
    Double m_rightCrank;
    Double m_accX;
    Double m_accY;
    Double m_accZ;
    Double m_gyroX;
    Double m_gyroY;
    Double m_gyroZ;

    boolean m_leftCrankEnabled;
    boolean m_rightCrankEnabled;
    boolean m_accXEnabled;
    boolean m_accYEnabled;
    boolean m_accZEnabled;
    boolean m_gyroXEnabled;
    boolean m_gyroYEnabled;
    boolean m_gyroZEnabled;

    private boolean checkLogFile() {
        if (!isEnabled()) {
            return false;
        }

        boolean result = true;

        if (mLogFileName == null) {

            headerWritten = false;

            File dlDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            File dataDir = new File(dlDir, "muShinData");

            if (dataDir.exists()) {
                Log.e(TAG, "muShinData directory already exists");
            } else {
                dataDir.mkdirs();
                Log.e(TAG, "muShinData directory created");
            }

            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
            String currentDateAndTimeString = sdf.format(new Date());

            try {

                mLogFileName = "log_" + currentDateAndTimeString + ".csv";
                mLogFile = new File(dataDir, mLogFileName);

                if (!mLogFile.createNewFile()) {
                    result = false;
                    Log.e(TAG, "Unable to create log file " + mLogFile.getAbsolutePath());
                }

                mLogFileWriter = new FileWriter(mLogFile.getAbsoluteFile(), false);
            } catch (IOException e) {
                e.printStackTrace();

                result = false;

                mLogFileName = null;
                mLogFile = null;
                mLogFileWriter = null;
            }
        }

        return result;
    }

    private void closeLogFile() {
        if (mLogFileWriter != null) {

            _flush();

            try {
                mLogFileWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            mLogFileWriter = null;
            mLogFile = null;
            mLogFileName = null;

            headerWritten = false;
        }
    }

    private void writeHeader() {

        if (!isEnabled()) {
            return;
        }
        if (!checkLogFile() || headerWritten) {
            return;
        }

        List<String> columns = new ArrayList<>();
        columns.add("Timestamp");

        if (m_leftCrankEnabled) {
            columns.add("Left Crank");
        }

        if (m_rightCrankEnabled) {
            columns.add("Right Crank");
        }

        if (m_accXEnabled) {
            columns.add("Acceleration X Axis");
        }

        if (m_accYEnabled) {
            columns.add("Acceleration Y Axis");
        }

        if (m_accZEnabled) {
            columns.add("Acceleration Z Axis");
        }

        if (m_gyroXEnabled) {
            columns.add("Gyro X Axis");
        }

        if (m_gyroYEnabled) {
            columns.add("Gyro Y Axis");
        }

        if (m_gyroZEnabled) {
            columns.add("Gyro Z Axis");
        }

        String header = TextUtils.join(", ", columns);

        try {
            mLogFileWriter.append(header);
            mLogFileWriter.append("\n");

            headerWritten = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setConfiguration(Configuration config) {

        stopLogging();

        m_leftCrankEnabled = config.isLeftCrankDataEnabled();
        m_rightCrankEnabled = config.isRightCrankDataEnabled();

        m_accXEnabled = config.isAccXDataEnabled();
        m_accYEnabled = config.isAccYDataEnabled();
        m_accZEnabled = config.isAccZDataEnabled();

        m_gyroXEnabled = config.isGyroXDataEnabled();
        m_gyroYEnabled = config.isGyroYDataEnabled();
        m_gyroZEnabled = config.isGyroZDataEnabled();
    }

    public void addData(double timestamp, Double leftCrank, Double rightCrank,
                               Double accX, Double accY, Double accZ,
                               Double gyroX, Double gyroY, Double gyroZ) {

        if (!isEnabled()) {
            return;
        }

        if (!checkLogFile()) {
            return;
        }

        if (!headerWritten) {
            writeHeader();
        }

        if (timestamp != m_timestamp) {
            _flush();
        }

        m_timestamp = timestamp;

        m_leftCrank = leftCrank!= null ? leftCrank : m_leftCrank;
        m_rightCrank = rightCrank!= null ? rightCrank : m_rightCrank;

        m_accX = accX != null ? accX : m_accX;
        m_accY = accY != null ? accY : m_accY;
        m_accZ = accZ != null ? accZ : m_accZ;

        m_gyroX = gyroX != null ? gyroX : m_gyroX;
        m_gyroY = gyroY != null ? gyroY : m_gyroY;
        m_gyroZ = gyroZ != null ? gyroZ : m_gyroZ;

    }

    private void _flush() {

        if (!isEnabled()) {
            return;
        }

        if (m_timestamp < 0) {
            return;
        }

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(m_timestamp);

        if (m_leftCrankEnabled) {
            stringBuilder.append(", ");
            stringBuilder.append(m_leftCrank != null ? m_leftCrank : 0.0);
            m_leftCrank = null;
        }

        if (m_rightCrankEnabled) {
            stringBuilder.append(", ");
            stringBuilder.append(m_rightCrank != null ? m_rightCrank : 0.0);
            m_rightCrank = null;
        }

        if (m_accXEnabled) {
            stringBuilder.append(", ");
            stringBuilder.append(m_accX != null ? m_accX : 0.0);
            m_accX = null;
        }

        if (m_accYEnabled) {
            stringBuilder.append(", ");
            stringBuilder.append(m_accY != null ? m_accY : 0.0);
            m_accY = null;
        }

        if (m_accZEnabled) {
            stringBuilder.append(", ");
            stringBuilder.append(m_accZ != null ? m_accZ : 0.0);
            m_accZ = null;
        }

        if (m_gyroXEnabled) {
            stringBuilder.append(", ");
            stringBuilder.append(m_gyroX != null ? m_gyroX : 0.0);
            m_gyroX = null;
        }

        if (m_gyroYEnabled) {
            stringBuilder.append(", ");
            stringBuilder.append(m_gyroY != null ? m_gyroY : 0.0);
            m_gyroY = null;
        }

        if (m_gyroZEnabled) {
            stringBuilder.append(", ");
            stringBuilder.append(m_gyroZ != null ? m_gyroZ : 0.0);
            m_gyroZ = null;
        }

        stringBuilder.append("\n");

        m_timestamp = -1;

        try {
            mLogFileWriter.append(stringBuilder.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void startLogging() {
        checkLogFile();
    }

    public void stopLogging() {
        closeLogFile();
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public void setEnabled(boolean enabled) {
        isEnabled = enabled;
    }
}
