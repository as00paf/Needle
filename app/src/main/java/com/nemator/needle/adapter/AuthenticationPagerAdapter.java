package com.nemator.needle.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.nemator.needle.fragments.authentication.LoginFragment;
import com.nemator.needle.fragments.authentication.RegisterFragment;

public class AuthenticationPagerAdapter extends FragmentPagerAdapter {

    private LoginFragment loginFragment;
    private RegisterFragment registerFragment;

    public AuthenticationPagerAdapter(FragmentManager fm) {
        super(fm);
        loginFragment = LoginFragment.newInstance();
    }

    @Override
    public Fragment getItem(int position) {
        if(position == 0){
            return loginFragment;
        }else{
            if(registerFragment == null)
                registerFragment = RegisterFragment.newInstance();
            return registerFragment;
        }
    }

    @Override
    public int getCount() {
        return 2;
    }
}
