package org.wordpress.android.ui;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.View;
import android.widget.FrameLayout;

import org.wordpress.android.R;
import org.wordpress.android.analytics.AnalyticsTracker;
import org.wordpress.android.ui.prefs.AppPrefs;
import org.wordpress.android.util.PermissionUtils;
import org.wordpress.android.util.ToastUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PermissionsRequestActivity extends Activity {
    private static final int CAMERA_AND_MEDIA_PERMISSION_REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(0, 0);
        setContentView(R.layout.activity_permissions_request);
        showAndRequestCameraPermission();
        setFinishOnTouchOutside(true);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case CAMERA_AND_MEDIA_PERMISSION_REQUEST_CODE:
                if (permissions.length == 0) {
                    AnalyticsTracker.track(AnalyticsTracker.Stat.ME_GRAVATAR_PERMISSIONS_INTERRUPTED);
                }  else {
                    List<String> granted = new ArrayList<>();
                    List<String> denied = new ArrayList<>();

                    for (int i = 0; i < grantResults.length; i++) {
                        if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                            granted.add(permissions[i]);
                        } else {
                            denied.add(permissions[i]);
                        }
                    }

                    if (denied.size() == 0) {
                        AnalyticsTracker.track(AnalyticsTracker.Stat.ME_GRAVATAR_PERMISSIONS_ACCEPTED);
                        showPhotoPickerForGravatar();
                    } else {
                        ToastUtils.showToast(this.getActivity(), getString(R.string
                                .gravatar_camera_and_media_permission_required), ToastUtils.Duration.LONG);
                        Map<String, Object> properties = new HashMap<>();
                        properties.put("permissions granted", granted.size() == 0 ? "[none]" : TextUtils
                                .join(",", granted));
                        properties.put("permissions denied", TextUtils.join(",", denied));
                        AnalyticsTracker.track(AnalyticsTracker.Stat.ME_GRAVATAR_PERMISSIONS_DENIED, properties);
                    }
                }
                break;
        }
    }

    public boolean showAndRequestCameraPermission() {
        if (!AppPrefs.hasCameraPermissionBeenShown()) {
            showCameraSoftAsk();
        } else {
            if (PermissionUtils.checkCameraAndStoragePermissions(this)) {
                return true;
            } else if (areCameraAndStoragePermissionsDenied(this)) {
                showPermissionsPrompt();
            } else {
                return PermissionUtils.checkAndRequestCameraAndStoragePermissions(this, CAMERA_AND_MEDIA_PERMISSION_REQUEST_CODE);
            }
        }

        return false;
    }

    private void showPermissionsPrompt() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("It's disabled");
        builder.setPositiveButton("Send me to Settings", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startActivityForResult(new Intent(android.provider.Settings.ACTION_APPLICATION_SETTINGS), 0);
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
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
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
                finish();
            }
        });
        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                finish();
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
