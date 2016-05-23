package com.nemator.needle.adapter;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.nemator.needle.R;
import com.nemator.needle.fragments.needle.createNeedle.CreateNeedleExpirationFragment;
import com.nemator.needle.fragments.needle.createNeedle.CreateNeedleUsersFragment;
import com.nemator.needle.models.vo.UserVO;

public class CreateNeedlePagerAdapter extends FragmentPagerAdapter implements IconPagerAdapter{
    public final int EXPIRATION = 0;
    public final int USERS = 1;

    private Context context;

    private Boolean isPublic = false;

    private CreateNeedleExpirationFragment expirationFragment;
    private CreateNeedleUsersFragment usersFragment;

    private int[] drawablesIds = {
            R.drawable.expiration_icon_selector,
            R.drawable.users_icon_selector
    };

    public CreateNeedlePagerAdapter(FragmentManager fm, Context context) {
        super(fm);
        this.context = context;
    }

    @Override
    public Fragment getItem(int position) {
        Fragment fragment = null;

        switch (position){
            case EXPIRATION:
                if(expirationFragment == null){
                    expirationFragment = CreateNeedleExpirationFragment.newInstance();
                }
                fragment = expirationFragment;
                break;
            case USERS:
                if(usersFragment == null){
                    usersFragment = CreateNeedleUsersFragment.newInstance();
                }
                fragment = usersFragment;
                break;
        }

        return fragment;
    }

    @Override
    public int getCount() {
        return 2;
    }

    //TODO : localize
    @Override
    public String getPageTitle(int position) {
        String title = null;
        switch (position){
            case EXPIRATION:
                title = context.getString(R.string.expiration);
                break;
            case USERS:
                title = context.getString(R.string.friend);
                break;
        }

        return title;
    }

    public int getDrawableId(int position){
        return drawablesIds[position];
    }

    public UserVO getSelectedUser() {
        return usersFragment.getSelectedUser();
    }

    public String getDateLimit() {
        return expirationFragment.getDateLimit();
    }


    public String getTimeLimit() {
        return expirationFragment.getTimeLimit();
    }
}