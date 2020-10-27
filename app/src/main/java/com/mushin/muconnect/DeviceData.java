package com.mushin.muconnect;

import com.mushin.muconnect.ui.main.PageViewModel;

public class DeviceData {
    private static final String crankLeftData = "crankL";
    private static final String crankRightData = "crankR";

    private static final String accData = "acc";
    private static final String accXData = "accX";
    private static final String accYData = "accY";
    private static final String accZData = "accZ";

    private static final String gyroData = "gyro";
    private static final String gyroXData = "gyroX";
    private static final String gyroYData = "gyroY";
    private static final String gyroZData = "gyroZ";

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

            for (int idx = 1; idx < dataPairs.length; idx++) {
                String[] nameValue = dataPairs[idx].trim().split("\\s+");

                if (nameValue.length < 2) {
                    continue;
                }

                if (nameValue[0].equals(crankLeftData)) {
                    model.addLeftCrankData(tickNumber, Float.parseFloat(nameValue[1]));
                } else if (nameValue[0].equals(crankRightData)) {
                    model.addRightCrankData(tickNumber, Float.parseFloat(nameValue[1]));
                } else if (nameValue[0].equals(accData) && nameValue.length == 4) {
                    model.addAccXData(tickNumber, Float.parseFloat(nameValue[1]));
                    model.addAccYData(tickNumber, Float.parseFloat(nameValue[2]));
                    model.addAccZData(tickNumber, Float.parseFloat(nameValue[3]));
                } else if (nameValue[0].equals(accXData)) {
                    model.addAccXData(tickNumber, Float.parseFloat(nameValue[1]));
                } else if (nameValue[0].equals(accYData)) {
                    model.addAccYData(tickNumber, Float.parseFloat(nameValue[1]));
                } else if (nameValue[0].equals(accZData)) {
                    model.addAccZData(tickNumber, Float.parseFloat(nameValue[1]));
                } else if (nameValue[0].equals(gyroData) && nameValue.length == 4) {
                    model.addGyroXData(tickNumber, Float.parseFloat(nameValue[1]));
                    model.addGyroYData(tickNumber, Float.parseFloat(nameValue[2]));
                    model.addGyroZData(tickNumber, Float.parseFloat(nameValue[3]));
                } else if (nameValue[0].equals(gyroXData)) {
                    model.addGyroXData(tickNumber, Float.parseFloat(nameValue[1]));
                } else if (nameValue[0].equals(gyroYData)) {
                    model.addGyroYData(tickNumber, Float.parseFloat(nameValue[1]));
                } else if (nameValue[0].equals(gyroZData)) {
                    model.addGyroZData(tickNumber, Float.parseFloat(nameValue[1]));
                }
            }
        } catch (NumberFormatException ex) {
            result = false;
        }

        return result;
    }
}
