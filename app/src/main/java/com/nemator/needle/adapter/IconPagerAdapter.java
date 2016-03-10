package com.nemator.needle.adapter;

/**
 * Created by Alex on 10/03/2016.
 */
public interface IconPagerAdapter {
    String getPageTitle(int index);
    int getDrawableId(int index);
    int getCount();
}
