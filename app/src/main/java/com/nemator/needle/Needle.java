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

/**
 * Created by Alex on 20/09/2015.
 */
public class Needle extends MultiDexApplication {

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

    }
}
