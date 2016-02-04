package com.nemator.needle.utils;

import android.Manifest;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class PermissionsConstants {

    public static final int MULTIPLE_PERMISSIONS = 255;

    public static Map<String, Integer> permissionsRequestCodes;

    static {
        Map<String, Integer> map = new HashMap<String, Integer>();
        map.put(Manifest.permission.READ_CALENDAR, 0);
        map.put(Manifest.permission.WRITE_CALENDAR, 1);

        map.put(Manifest.permission.CAMERA, 2);

        map.put(Manifest.permission.READ_CONTACTS, 3);
        map.put(Manifest.permission.WRITE_CONTACTS, 4);

        map.put(Manifest.permission.ACCESS_COARSE_LOCATION, 5);
        map.put(Manifest.permission.ACCESS_FINE_LOCATION, 6);

        map.put(Manifest.permission.RECORD_AUDIO, 7);

        map.put(Manifest.permission.READ_PHONE_STATE, 8);
        map.put(Manifest.permission.CALL_PHONE, 9);
        map.put(Manifest.permission.READ_CALL_LOG, 10);
        map.put(Manifest.permission.WRITE_CALL_LOG, 11);
        map.put(Manifest.permission.ADD_VOICEMAIL, 12);
        map.put(Manifest.permission.USE_SIP, 13);
        map.put(Manifest.permission.PROCESS_OUTGOING_CALLS, 14);

        map.put(Manifest.permission.BODY_SENSORS, 15);

        map.put(Manifest.permission.SEND_SMS, 16);
        map.put(Manifest.permission.RECEIVE_SMS, 17);
        map.put(Manifest.permission.READ_SMS, 18);
        map.put(Manifest.permission.RECEIVE_WAP_PUSH, 19);
        map.put(Manifest.permission.RECEIVE_SMS, 20);

        map.put(Manifest.permission.READ_EXTERNAL_STORAGE, 21);
        map.put(Manifest.permission.WRITE_EXTERNAL_STORAGE, 22);

        map.put(Manifest.permission.GET_ACCOUNTS, 23);

        permissionsRequestCodes = Collections.unmodifiableMap(map);
    }

    public static int getRequestCodeForPermission(String permission) {
        return permissionsRequestCodes.get(permission);
    }

}
