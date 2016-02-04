package com.nemator.needle.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.nemator.needle.R;

public class PermissionManager {

    public static final String TAG = "PermissionManager";

    private static PermissionManager instance;

    private Context context;
    private Activity activity;

    public PermissionManager(Context context) {
        super();
        this.context = context;
    }

    public static PermissionManager getInstance(Context context) {
        if(instance == null){
            instance = new PermissionManager(context);
        }
        return instance;
    }

    public Boolean isPermissionGranted(String permission) {
        return ContextCompat.checkSelfPermission(context, permission)
                    == PackageManager.PERMISSION_GRANTED;
    }

    public void requestPermission(Activity act, String permission) {
        if (ContextCompat.checkSelfPermission(context, permission)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(act, permission)) {
                Log.i(TAG, "We need use this permission");
            }

            ActivityCompat.requestPermissions(act, new String[]{permission},
                    PermissionsConstants.getRequestCodeForPermission(permission));
        }
    }

    public void requestPermissions(Activity act, String[] permissions) {
        for (int i = 0; i < permissions.length; i++) {
            String permission = permissions[i];

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(act, permission)) {
                Log.i(TAG, "We need use this permission");
            }

            ActivityCompat.requestPermissions(act, permissions,
                    PermissionsConstants.MULTIPLE_PERMISSIONS);
        }
    }
}
