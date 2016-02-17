package com.nemator.needle;

import android.os.Bundle;
import android.support.multidex.MultiDex;
import android.support.multidex.MultiDexApplication;

import com.nemator.needle.controller.AuthenticationController;
import com.nemator.needle.controller.GCMController;
import com.nemator.needle.controller.GoogleAPIController;
import com.nemator.needle.controller.NavigationController;
import com.nemator.needle.controller.NetworkController;
import com.nemator.needle.models.UserModel;
import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import io.fabric.sdk.android.Fabric;

/**
 * Created by Alex on 20/09/2015.
 */
public class Needle extends MultiDexApplication {

    // Note: Your consumer key and secret should be obfuscated in your source code before shipping.
    private static final String TWITTER_KEY = "9bUrtSd8rgy7wqAMmYkrKioy6";
    private static final String TWITTER_SECRET = "pSf4y1bnsLtqxZ939wwoIXZbEUzNxT4mHsr2lgChXAwR6O8QrU";


    //App Components
    public static final NetworkController networkController = NetworkController.getInstance();
    public static final UserModel userModel = UserModel.getInstance();
    public static final AuthenticationController authenticationController = AuthenticationController.getInstance();
    public static final NavigationController navigationController = NavigationController.getInstance();
    public static final GCMController gcmController = GCMController.getInstance();
    public static final GoogleAPIController googleApiController = GoogleAPIController.getInstance();

    public Needle() {
        super();
    }

    public void onCreate(Bundle arguments) {
        MultiDex.install(getApplicationContext());
        super.onCreate();
        TwitterAuthConfig authConfig = new TwitterAuthConfig(TWITTER_KEY, TWITTER_SECRET);
        Fabric.with(this, new Twitter(authConfig));

    }
}
