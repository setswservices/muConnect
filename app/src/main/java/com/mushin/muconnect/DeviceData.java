package com.mushin.muconnect;

import com.mushin.muconnect.ui.main.PageViewModel;

public class DeviceData {
    private static final String crankLeftData = "L";
    private static final String crankRightData = "R";

    private static final String accData = "acc";
    private static final String accXData = "X";
    private static final String accYData = "Y";
    private static final String accZData = "Z";

    private static final String gyroData = "gyro";
    private static final String gyroXData = "gyroX";
    private static final String gyroYData = "gyroY";
    private static final String gyroZData = "gyroZ";

    private static double lastTickNumber = -1;

    public static boolean process(PageViewModel model, String data) {
        boolean result = true;

        if (data == null) {
            return false;
        }

        String[] dataPairs = data.split(",");
        if (dataPairs.length < 2) {
            return false;
        }

        try {
            int tickNumber = Integer.parseInt(dataPairs[0]);

            if (tickNumber < lastTickNumber) {
                model.resetAllSeries();
            }

            lastTickNumber = tickNumber;

            Double leftCrank = null, rightCrank = null;
            Double accX = null, accY = null, accZ = null;
            Double gyroX = null, gyroY = null, gyroZ = null;

            for (int idx = 1; idx < dataPairs.length; idx++) {
                String[] nameValue = dataPairs[idx].trim().split("\\s+");

                if (nameValue.length < 2) {
                    continue;
                }

                if (nameValue[0].equals(crankLeftData)) {
                    leftCrank = Double.parseDouble(nameValue[1]);
                } else if (nameValue[0].equals(crankRightData)) {
                    rightCrank = Double.parseDouble(nameValue[1]);
                } else if (nameValue[0].equals(accData) && nameValue.length == 4) {
                    accX = Double.parseDouble(nameValue[1]);
                    accY = Double.parseDouble(nameValue[2]);
                    accZ = Double.parseDouble(nameValue[3]);
                } else if (nameValue[0].equals(accXData)) {
                    accX = Double.parseDouble(nameValue[1]);
                } else if (nameValue[0].equals(accYData)) {
                    accY = Double.parseDouble(nameValue[1]);
                } else if (nameValue[0].equals(accZData)) {
                    accZ = Double.parseDouble(nameValue[1]);
                } else if (nameValue[0].equals(gyroData) && nameValue.length == 4) {
                    gyroX = Double.parseDouble(nameValue[1]);
                    gyroY = Double.parseDouble(nameValue[2]);
                    gyroZ = Double.parseDouble(nameValue[3]);
                } else if (nameValue[0].equals(gyroXData)) {
                    gyroX = Double.parseDouble(nameValue[1]);
                } else if (nameValue[0].equals(gyroYData)) {
                    gyroY = Double.parseDouble(nameValue[1]);
                } else if (nameValue[0].equals(gyroZData)) {
                    gyroZ = Double.parseDouble(nameValue[1]);
                }
            }

            if (leftCrank != null || rightCrank != null) {
                model.addCranksData(tickNumber, leftCrank, rightCrank);
            }

            if (accX != null || accY != null || accZ != null) {
                model.addAccData(tickNumber, accX, accY, accZ);
            }

            if (gyroX != null || gyroY != null || gyroZ != null) {
                model.addGyroData(tickNumber, gyroX, gyroY, gyroZ);
            }

            DataLogger.getInstance().addData(tickNumber, leftCrank, rightCrank, accX, accY, accZ, gyroX, gyroY, gyroZ);

        } catch (NumberFormatException ex) {
            result = false;
        }

        return result;
    }
}
