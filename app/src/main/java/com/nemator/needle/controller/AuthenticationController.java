package com.nemator.needle.controller;


import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.nemator.needle.MainActivity;
import com.nemator.needle.R;
import com.nemator.needle.models.UserModel;
import com.nemator.needle.tasks.AuthenticationResult;
import com.nemator.needle.tasks.login.LoginTask;
import com.nemator.needle.tasks.login.LoginTask.LoginResponseHandler;
import com.nemator.needle.tasks.login.LoginTaskParams;
import com.nemator.needle.tasks.register.RegisterTask.RegisterResponseHandler;
import com.nemator.needle.utils.AppConstants;

import it.neokree.materialnavigationdrawer.elements.MaterialAccount;

public class AuthenticationController implements LoginResponseHandler, RegisterResponseHandler {

    private MainActivity activity;
    private UserModel userModel;
    private NavigationController navigationController;
    private SharedPreferences mSharedPreferences;

    public AuthenticationController(MainActivity activity, UserModel userModel, NavigationController navigationController){
        this.activity = activity;
        this.userModel = userModel;
        this.navigationController = navigationController;

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity);
    }

    //Handlers
    @Override
    public void onRegistrationComplete(AuthenticationResult result, String password, Boolean rememberMe) {
        LoginTaskParams params = new LoginTaskParams(result.user.getUserName(), password, activity, rememberMe, false);
        new LoginTask(params, this).execute();
    }

    @Override
    public void onLoginComplete(AuthenticationResult result) {
        if(!userModel.isLoggedIn()) {
            if (result.successCode == 1) {
                userModel.setLoggedIn(true);

                setAccount();

                //Save infos
                mSharedPreferences.edit().putString(AppConstants.TAG_USER_NAME, result.user.getUserName()).
                        putInt(AppConstants.TAG_USER_ID, result.user.getUserId()).
                        putString(AppConstants.TAG_GCM_REG_ID, result.user.getGcmRegId()).
                        commit();

                //Add/Remove Sections
                navigationController.removeSection(AppConstants.SECTION_LOGIN);
                navigationController.removeSection(AppConstants.SECTION_REGISTER);
                navigationController. createMainSections();
                navigationController.showSection(AppConstants.SECTION_HAYSTACKS);
            } else {
                Toast.makeText(activity, "An Error Occured\n Please Try Again!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    //Public Mehtods
    public void setAccount(){
        if(activity.getAccountList().size() > 0){
            activity.removeAccount((MaterialAccount) activity.getAccountList().get(0));
        }

        activity.addAccount(new MaterialAccount(activity.getResources(), userModel.getUserName(), "e-mail", R.drawable.me, R.drawable.mat));
        activity.setFirstAccountPhoto(activity.getResources().getDrawable(R.drawable.me));//TODO:Get picture from cache
    }
}
