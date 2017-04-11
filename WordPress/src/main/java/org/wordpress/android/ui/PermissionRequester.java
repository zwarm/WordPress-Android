package org.wordpress.android.ui;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.content.ContextCompat;

import org.wordpress.android.ui.prefs.AppPrefs;
import org.wordpress.android.util.PermissionUtils;

/**
 * Created by will on 3/29/17.
 */

public class PermissionRequester {
    private static final int CAMERA_AND_MEDIA_PERMISSION_REQUEST_CODE = 1;

    private Activity mActivity;
    private Fragment mFragment;

    public PermissionRequester(Activity activity) {
        if (activity == null) {
            return;
        } else {
            mActivity = activity;
        }
    }

    public PermissionRequester(Fragment fragment) {
        if (fragment == null) {
            return;
        } else {
            mFragment = fragment;
            mActivity = fragment.getActivity();
        }
    }

    public boolean showAndRequestCameraPermission() {
        if (!AppPrefs.hasCameraPermissionBeenShown()) {
            showCameraSoftAsk();
        } else {
            if (PermissionUtils.checkCameraAndStoragePermissions(mActivity)) {
                return true;
            } else if (areCameraAndStoragePermissionsDenied(mActivity)) {
                showPermissionsPrompt();
            } else {
                if (mFragment != null) {
                    return PermissionUtils.checkAndRequestCameraAndStoragePermissions(mFragment, CAMERA_AND_MEDIA_PERMISSION_REQUEST_CODE);
                } else {
                    return PermissionUtils.checkAndRequestCameraAndStoragePermissions(mActivity, CAMERA_AND_MEDIA_PERMISSION_REQUEST_CODE);
                }
            }
        }

        return false;
    }

    private void showPermissionsPrompt() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
        builder.setMessage("It's disabled");
        builder.setPositiveButton("Send me to Settings", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mActivity.startActivityForResult(new Intent(android.provider.Settings.ACTION_APPLICATION_SETTINGS), 0);
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


    private void showCameraSoftAsk() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
        builder.setMessage("We require CAMERA and STORAGE permissions. Please select \"Allow\".");
        builder.setTitle("Permissions Request");
        builder.setPositiveButton("Understood", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                AppPrefs.setCameraPermissionShown(true);
                showAndRequestCameraPermission();
                AppPrefs.setCameraPermissionShown(false);
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

    public static boolean arePermissionsDenied(Activity activity, String[] permissionList) {
        for (String permission : permissionList) {
            if (ContextCompat.checkSelfPermission(activity, permission) == PackageManager.PERMISSION_DENIED) {
                return true;
            }
        }
        return false;
    }

    public static boolean areCameraAndStoragePermissionsDenied(Activity activity) {
        return arePermissionsDenied(activity,
                new String[]{
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.CAMERA});
    }
}
