package com.nemator.needle.home.createHaystack;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.nemator.needle.R;
import com.nemator.needle.home.HaystackListTabFragment;

/**
 * Created by Alex on 12/04/2015.
 */
public class CreateHaystackPagerAdapter extends FragmentStatePagerAdapter {
    private Fragment fragment;

    public CreateHaystackPagerAdapter(FragmentManager fm, Fragment frag) {
        super(fm);
        fragment = frag;
    }

    @Override
    public Fragment getItem(int position) {
        Fragment fragment;

        switch (position){
            case 0://General infos
                fragment = new CreateHaystackGeneralInfosFragment();
                break;
            case 1://Map
                fragment = new CreateHaystackMap();
                break;
            case 2://Users
                fragment = new CreateHaystackUsersFragment();
                break;
            default:
                fragment = new HaystackListTabFragment();
                break;
        }

        return fragment;
    }

    @Override
    public int getCount() {
        return 3;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        String title;
        switch (position){
            case 0:
                title = fragment.getString(R.string.general_infos);
                break;
            case 1:
                title = fragment.getString(R.string.map_view);
                break;
            case 2:
                title = fragment.getString(R.string.users);
                break;
            default:
                title = "tab " + String.valueOf(position);
                break;
        }

        return title;
    }
}