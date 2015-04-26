package com.nemator.needle.home.createHaystack;

import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.view.View;

import com.nemator.needle.R;
import com.nemator.needle.home.HaystackListTabFragment;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * Created by Alex on 12/04/2015.
 */
public class CreateHaystackPagerAdapter extends FragmentStatePagerAdapter {
    private Fragment fragment;
    private ArrayList<Fragment> fragmentList = new ArrayList<Fragment>();

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
                fragment = new CreateHaystackMapFragment();
                break;
            case 2://Users
                fragment = new CreateHaystackUsersFragment();
                break;
            default:
                fragment = new HaystackListTabFragment();
                break;
        }

        fragmentList.add(position, fragment);

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

    @Override
    public void destroyItem(View container, int position, Object object) {
        super.destroyItem(container, position, object);
        fragmentList.remove(position);
    }

    public Fragment getFragmentAt(int position){
        return fragmentList.get(position);
    }
}