package com.nemator.needle.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by Alex on 29/06/2015.
 */
public class AppUtils {

    private static String username = "";
    private static int userId = -1;

    public static String getUserName(Context context){
        if(username == ""){
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
            username = sp.getString("username", "");
        }

        return username;
    }

    public static String getUserId(Context context){
        if(userId==-1){
            SharedPreferences sp = PreferenceManager
                    .getDefaultSharedPreferences(context);

            userId = sp.getInt("userId", -1);
        }

        return String.valueOf(userId);
    }

}
