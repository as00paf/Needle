package com.nemator.needle.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.support.v4.content.ContextCompat;
import android.text.format.DateUtils;
import android.util.Base64;
import android.util.Log;

import com.nemator.needle.R;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Alex on 23/08/2015.
 */
public class AppUtils {

    public static void printApplicationHash(Context context){
        try {
            PackageInfo info = context.getPackageManager().getPackageInfo("com.nemator.needle", PackageManager.GET_SIGNATURES);
            for (android.content.pm.Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                String sign= Base64.encodeToString(md.digest(), Base64.DEFAULT);
                Log.e("MY KEY HASH:", sign);
                //  Toast.makeText(getApplicationContext(),sign,     Toast.LENGTH_LONG).show();
            }
        } catch (PackageManager.NameNotFoundException e) {
        } catch (NoSuchAlgorithmException e) {
        }
    }

    public static CharSequence formatDateUntil(Context context, String activeUntil) {
        CharSequence result = null;

        //2016-04-27 20:47:00
        SimpleDateFormat srcDf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

        try {
            Date date = srcDf.parse(activeUntil);
            long now = System.currentTimeMillis();

            result = context.getString(R.string.ends, DateUtils.getRelativeTimeSpanString(date.getTime(), now, 0L, DateUtils.FORMAT_ABBREV_ALL));

        } catch (ParseException e) {
            e.printStackTrace();
        }

        return result;
    }

    public static CharSequence formatDateRelative(String activeUntil) {
        CharSequence result = null;

        //2016-04-27 20:47:00
        SimpleDateFormat srcDf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

        try {
            Date date = srcDf.parse(activeUntil);
            long now = System.currentTimeMillis();

            result = DateUtils.getRelativeTimeSpanString(date.getTime(), now, 0L, DateUtils.FORMAT_ABBREV_ALL);

        } catch (ParseException e) {
            e.printStackTrace();
        }

        return result;
    }

    public static String formatDistance(float meters) {
        if (meters < 1000) {
            return ((int) meters) + "m";
        } else if (meters < 10000) {
            return formatDec(meters / 1000f, 1) + "km";
        } else {
            return ((int) (meters / 1000f)) + "km";
        }
    }

    private static String formatDec(float val, int dec) {
        int factor = (int) Math.pow(10, dec);

        int front = (int) (val);
        int back = (int) Math.abs(val * (factor)) % factor;

        return front + "." + back;
    }

    public static boolean isDateAfterNow(String date, String format) {
        SimpleDateFormat srcDf = new SimpleDateFormat(format);

        Date strDate = null;
        try {
            strDate = srcDf.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return System.currentTimeMillis() > strDate.getTime();
    }

    public static boolean isDateBeforeNow(String date, String format) {
        SimpleDateFormat srcDf = new SimpleDateFormat(format);

        Date strDate = null;
        try {
            strDate = srcDf.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return System.currentTimeMillis() < strDate.getTime();
    }
}
