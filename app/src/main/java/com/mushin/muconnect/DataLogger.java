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
            try {
                mLogFileWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            mLogFileWriter = null;
            mLogFile = null;
            mLogFileName = null;
        }
    }

    private void writeHeader(Double leftCrank, Double righCrank,
                             Double accX, Double accY, Double accZ,
                             Double gyroX, Double gyroY, Double gyroZ) {

        if (!isEnabled()) {
            return;
        }
        if (!checkLogFile() || headerWritten) {
            return;
        }

        List<String> columns = new ArrayList<>();
        columns.add("Timestamp");

        if (leftCrank != null) {
            columns.add("Left Crank");
        }

        if (righCrank != null) {
            columns.add("Right Crank");
        }

        if (accX != null) {
            columns.add("Acceleration X Axis");
        }

        if (accY != null) {
            columns.add("Acceleration Y Axis");
        }

        if (accZ != null) {
            columns.add("Acceleration Z Axis");
        }

        if (gyroX != null) {
            columns.add("Gyro X Axis");
        }

        if (gyroY != null) {
            columns.add("Gyro Y Axis");
        }

        if (gyroZ != null) {
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

        writeHeader(
                config.isLeftCrankDataEnabled() ? 0.0 : null,
                config.isRightCrankDataEnabled() ? 0.0 : null,
                config.isAccXDataEnabled() ? 0.0 : null,
                config.isAccYDataEnabled() ? 0.0 : null,
                config.isAccZDataEnabled() ? 0.0 : null,
                config.isGyroXDataEnabled() ? 0.0 : null,
                config.isGyroYDataEnabled() ? 0.0 : null,
                config.isGyroZDataEnabled() ? 0.0 : null
                );
    }

    public void addData(double timestamp, Double leftCrank, Double righCrank,
                               Double accX, Double accY, Double accZ,
                               Double gyroX, Double gyroY, Double gyroZ) {

        if (!isEnabled()) {
            return;
        }

        if (!checkLogFile()) {
            return;
        }

        if (!headerWritten) {
            writeHeader(leftCrank, righCrank, accX, accY, accZ, gyroX, gyroY, gyroZ);
        }

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(timestamp);

        if (leftCrank != null) {
            stringBuilder.append(", ");
            stringBuilder.append(leftCrank);
        }

        if (righCrank != null) {
            stringBuilder.append(", ");
            stringBuilder.append(righCrank);
        }

        if (accX != null) {
            stringBuilder.append(", ");
            stringBuilder.append(accX);
        }

        if (accY != null) {
            stringBuilder.append(", ");
            stringBuilder.append(accY);
        }

        if (accZ != null) {
            stringBuilder.append(", ");
            stringBuilder.append(accZ);
        }

        if (gyroX != null) {
            stringBuilder.append(", ");
            stringBuilder.append(gyroX);
        }

        if (gyroY != null) {
            stringBuilder.append(", ");
            stringBuilder.append(gyroY);
        }

        if (gyroZ != null) {
            stringBuilder.append(", ");
            stringBuilder.append(gyroZ);
        }

        stringBuilder.append("\n");

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
