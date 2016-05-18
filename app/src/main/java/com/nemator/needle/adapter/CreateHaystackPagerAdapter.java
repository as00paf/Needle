package com.nemator.needle.adapter;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.View;

import com.nemator.needle.R;
import com.nemator.needle.fragments.haystacks.createHaystack.CreateHaystackGeneralInfosFragment;
import com.nemator.needle.fragments.haystacks.createHaystack.CreateHaystackMapFragment;
import com.nemator.needle.fragments.haystacks.createHaystack.CreateHaystackUsersFragment;

import java.util.ArrayList;

public class CreateHaystackPagerAdapter extends FragmentPagerAdapter implements IconPagerAdapter {
    public final int GENERAL_INFOS = 0;
    public final int MAP = 1;
    public final int USERS = 2;

    private Context context;
    private ArrayList<Fragment> fragmentList = new ArrayList<Fragment>();

    private Boolean isPublic = false;

    private int[] drawablesIds = {
            R.drawable.haystack_icon_selector,
            R.drawable.map_icon_selector,
            R.drawable.users_icon_selector
    };

    public CreateHaystackPagerAdapter(FragmentManager fm, Context context) {
        super(fm);
        this.context = context;
    }

    @Override
    public Fragment getItem(int position) {
        Fragment fragment;

        switch (position){
            case GENERAL_INFOS://General infos
                fragment = CreateHaystackGeneralInfosFragment.newInstance();
                break;
            case MAP://Map
                fragment = CreateHaystackMapFragment.newInstance();
                break;
            case USERS://Users
                fragment = CreateHaystackUsersFragment.newInstance();
                break;
            default:
                fragment = CreateHaystackGeneralInfosFragment.newInstance();
                break;
        }

        fragmentList.add(position, fragment);

        return fragment;
    }

    @Override
    public int getCount() {
        return (isPublic) ? 2 : 3;
    }

    @Override
    public String getPageTitle(int position) {
        String title;
        switch (position){
            case GENERAL_INFOS:
                title = context.getString(R.string.general_infos);
                break;
            case MAP:
                title = context.getString(R.string.map_view);
                break;
            case USERS:
                title = context.getString(R.string.users);
                break;
            default:
                title = "tab " + String.valueOf(position);
                break;
        }

        return title;
    }

    public Boolean getIsPublic() {
        return isPublic;
    }

    public void setIsPublic(Boolean isPublic) {
        this.isPublic = isPublic;
        this.notifyDataSetChanged();
    }

    @Override
    public void destroyItem(View container, int position, Object object) {
        super.destroyItem(container, position, object);
        fragmentList.remove(position);
    }

    public Fragment getFragmentAt(int position){
        return fragmentList.get(position);
    }

    public Fragment getFragmentByType(Class type){
        for (int i = 0; i < fragmentList.size() ; i++) {
            if(type.isInstance(fragmentList.get(i))) return fragmentList.get(i);
        }

        return null;
    }

    public int getDrawableId(int position){
        return drawablesIds[position];
    }
}