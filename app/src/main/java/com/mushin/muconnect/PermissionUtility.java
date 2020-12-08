package com.mushin.muconnect;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;

import androidx.core.app.ActivityCompat;

import java.util.ArrayList;
import java.util.List;

public class PermissionUtility {

    private static int permissionsRequestCode = 202;

    private Context context;
    private String[] permissions;

    public PermissionUtility(Context context, String... permissions) {
        this.context = context;
        this.permissions = permissions;
    }

    public boolean arePermissionsEnabled() {
        for (String permission : permissions) {
            if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED)
                return false;
        }
        return true;
    }

    public void requestMultiplePermissions() {
        List<String> remainingPermissions = new ArrayList<>();
        for (String permission : permissions) {
            if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                remainingPermissions.add(permission);
            }
        }
        ActivityCompat.requestPermissions((Activity) context, remainingPermissions.toArray(new String[remainingPermissions.size()]), permissionsRequestCode);
    }

    public boolean onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == permissionsRequestCode) {
            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    if (ActivityCompat.shouldShowRequestPermissionRationale((Activity) context, permissions[i])) {
                        requestMultiplePermissions();
                    }
                    return false;
                }
            }
        }
        return true;
    }
}