package com.nemator.needle.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.nemator.needle.R;
import com.nemator.needle.fragments.haystack.HaystackPinsTabFragment;
import com.nemator.needle.fragments.haystack.HaystackUsersTabFragment;

import java.util.ArrayList;

public class SlidingPanelPagerAdapter extends FragmentStatePagerAdapter {
    private Context context;
    private ArrayList<Fragment> fragmentList = new ArrayList<Fragment>();

    public SlidingPanelPagerAdapter(FragmentManager fm, Context context) {
        super(fm);
        this.context = context;
    }

    @Override
    public Fragment getItem(int position) {
        Fragment fragment = null;

        switch (position){
            case 0://Pins tab
                fragment = new HaystackPinsTabFragment();
                break;
            case 1://Users tab
                fragment = new HaystackUsersTabFragment();
                break;
        }

        if(fragment != null){
            fragmentList.add(position, fragment);
        }

        return fragment;
    }

    @Override
    public int getCount() {
        return 2;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        String title;
        switch (position){
            case 0:
                title = context.getString(R.string.title_pins);
                break;
            case 1:
                title = context.getString(R.string.title_users);
                break;
            default:
                title = "tab " + String.valueOf(position);
                break;
        }

        return title;
    }

    public Drawable getPageIcon(int position){
        Drawable icon;
        switch (position){
            case 0:
                icon = context.getResources().getDrawable(R.drawable.ic_action_place);
                break;
            case 1:
                icon= context.getResources().getDrawable(R.drawable.ic_action_group);
                break;
            default:
                icon = context.getResources().getDrawable(R.drawable.ic_action_directions);
                break;
        }

        return icon;
    }

    public Fragment getFragmentByType(Class type){
        for (int i = 0; i < fragmentList.size() ; i++) {
            if(type.isInstance(fragmentList.get(i))) return fragmentList.get(i);
        }

        return null;
    }

    @Override
    public void destroyItem(View container, int position, Object object) {
        super.destroyItem(container, position, object);
        fragmentList.remove(position);
    }
}