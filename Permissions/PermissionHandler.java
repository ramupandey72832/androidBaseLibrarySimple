package com.example.permissionhandler;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.content.pm.PackageManager;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

public class PermissionHandler {

    private final Context context;
    private final ActivityResultLauncher<String[]> permissionLauncher;
    private PermissionCallback callback;

    public interface PermissionCallback {
        void onResult(List<String> grantedPermissions, List<String> deniedPermissions);
    }

    public PermissionHandler(Activity activity, PermissionCallback callback) {
        this.context = activity;
        this.callback = callback;

        this.permissionLauncher = activity.registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(),
                result -> {
                    List<String> granted = new ArrayList<>();
                    List<String> denied = new ArrayList<>();

                    for (String permission : result.keySet()) {
                        if (Boolean.TRUE.equals(result.get(permission))) {
                            granted.add(permission);
                        } else {
                            denied.add(permission);
                        }
                    }
                    if (callback != null) {
                        callback.onResult(granted, denied);
                    }
                }
        );
    }

    // ✅ Normal runtime permission check
    public boolean isPermissionGranted(String permissionName) {
        return ContextCompat.checkSelfPermission(context, permissionName)
                == PackageManager.PERMISSION_GRANTED;
    }

    // ✅ Request runtime permissions
    private void requestPermissions(List<String> permissions) {
        List<String> toRequest = new ArrayList<>();
        for (String permission : permissions) {
            if (!isPermissionGranted(permission)) {
                toRequest.add(permission);
            }
        }
        if (!toRequest.isEmpty()) {
            permissionLauncher.launch(toRequest.toArray(new String[0]));
        } else {
            if (callback != null) {
                callback.onResult(permissions, new ArrayList<>());
            }
        }
    }

    // 🔒 Special routing for single permission
    public void requestPermission(Activity activity, String permissionName) {
        switch (permissionName) {
            case android.Manifest.permission.SYSTEM_ALERT_WINDOW:
                if (!Settings.canDrawOverlays(context)) {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            Uri.parse("package:" + context.getPackageName()));
                    activity.startActivity(intent);
                }
                break;

            case android.Manifest.permission.WRITE_SETTINGS:
                if (!Settings.System.canWrite(context)) {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS,
                            Uri.parse("package:" + context.getPackageName()));
                    activity.startActivity(intent);
                }
                break;

            case android.Manifest.permission.POST_NOTIFICATIONS:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    requestPermissions(List.of(android.Manifest.permission.POST_NOTIFICATIONS));
                }
                break;

            case android.Manifest.permission.ACCESS_BACKGROUND_LOCATION:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                            Uri.parse("package:" + context.getPackageName()));
                    activity.startActivity(intent);
                }
                break;

            case android.Manifest.permission.BLUETOOTH_CONNECT:
            case android.Manifest.permission.BLUETOOTH_SCAN:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    requestPermissions(List.of(
                            android.Manifest.permission.BLUETOOTH_CONNECT,
                            android.Manifest.permission.BLUETOOTH_SCAN
                    ));
                }
                break;

            case android.Manifest.permission.ACCESS_WIFI_STATE:
            case android.Manifest.permission.CHANGE_WIFI_STATE:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    requestPermissions(List.of(
                            android.Manifest.permission.ACCESS_WIFI_STATE,
                            android.Manifest.permission.CHANGE_WIFI_STATE
                    ));
                }
                break;

            case android.Manifest.permission.READ_MEDIA_IMAGES:
            case android.Manifest.permission.READ_MEDIA_VIDEO:
            case android.Manifest.permission.READ_MEDIA_AUDIO:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    requestPermissions(List.of(
                            android.Manifest.permission.READ_MEDIA_IMAGES,
                            android.Manifest.permission.READ_MEDIA_VIDEO,
                            android.Manifest.permission.READ_MEDIA_AUDIO
                    ));
                } else {
                    requestPermissions(List.of(android.Manifest.permission.READ_EXTERNAL_STORAGE));
                }
                break;

            case android.Manifest.permission.FOREGROUND_SERVICE_CAMERA:
            case android.Manifest.permission.FOREGROUND_SERVICE_MICROPHONE:
            case android.Manifest.permission.FOREGROUND_SERVICE_LOCATION:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    requestPermissions(List.of(
                            android.Manifest.permission.FOREGROUND_SERVICE_CAMERA,
                            android.Manifest.permission.FOREGROUND_SERVICE_MICROPHONE,
                            android.Manifest.permission.FOREGROUND_SERVICE_LOCATION
                    ));
                }
                break;

            default:
                requestPermissions(List.of(permissionName));
                break;
        }
    }

    // ✅ Batch unified method
    public void requestPermissionsBatch(Activity activity, List<String> permissions) {
        for (String permission : permissions) {
            requestPermission(activity, permission);
        }
    }
}
