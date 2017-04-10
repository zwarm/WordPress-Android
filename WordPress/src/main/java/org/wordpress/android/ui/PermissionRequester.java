package org.wordpress.android.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;

import org.wordpress.android.ui.prefs.AppPrefs;
import org.wordpress.android.util.PermissionUtils;

/**
 * Created by will on 3/29/17.
 */

public class PermissionRequester {
    private Activity mActivity;

    public PermissionRequester(Activity activity) {
        if (activity == null) {
            return;
        } else {
            mActivity = activity;
        }
    }

    public boolean showAndRequestCameraPermission() {
        if (AppPrefs.hasCameraPermissionBeenShown()) {
            if (PermissionUtils.checkAndRequestCameraAndStoragePermissions(mActivity, 1)) {
                return true;
            } else {
                mActivity.startActivityForResult(new Intent(android.provider.Settings.ACTION_APPLICATION_SETTINGS), 0);
            }
        } else {
            showCameraSoftAsk();
        }

        return false;
    }


    private void showCameraSoftAsk() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
        builder.setMessage("We require CAMERA and STORAGE permissions. Please select \"Allow\".");
        builder.setTitle("Permissions Request");
        builder.setPositiveButton("Understood", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                AppPrefs.setCameraPermissionShown(false);
                showAndRequestCameraPermission();
            }
        });
        builder.setNegativeButton("Not now", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }
}
