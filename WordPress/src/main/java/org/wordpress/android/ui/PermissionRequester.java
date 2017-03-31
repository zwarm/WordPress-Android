package org.wordpress.android.ui;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.support.v4.content.ContextCompat;

import org.wordpress.android.ui.prefs.AppPrefs;
import org.wordpress.android.util.PermissionUtils;

/**
 * Created by will on 3/29/17.
 */

public class PermissionRequester {
    private Activity mActivity;

    public PermissionRequester(Activity activity) {
        if (activity == null) {
            mActivity = activity;
        }
    }

    public void showAndRequestCameraPermission() {
        if (AppPrefs.hasCameraPermissionBeenShown()) {
            if (checkCameraAndStoragePermissions(mActivity)) {
                PermissionUtils.checkAndRequestCameraAndStoragePermissions(mActivity, 1);
            } else {
                // go to settings
            }
        } else {
            showCameraSoftAsk();
        }
    }

    public static boolean checkCameraAndStoragePermissions(Activity activity) {
        return checkPermissions(activity,
                new String[]{
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.CAMERA});
    }

    public static boolean checkPermissions(Activity activity, String[] permissionList) {
        for (String permission : permissionList) {
            if (ContextCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }


    private void showCameraSoftAsk() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
        builder.setMessage("Listen here noob, we're gonna need permission, k?");
        builder.setTitle("LET US IN");
        builder.setPositiveButton("K, brah", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                AppPrefs.setCameraPermissionShown(true);
                showAndRequestCameraPermission();
            }
        });
        builder.setNegativeButton("Nah, bruh", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        AlertDialog dialog = builder.create();
    }
}
