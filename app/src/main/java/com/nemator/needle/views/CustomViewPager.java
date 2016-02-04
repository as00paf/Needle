package com.nemator.needle.views;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.View;

import com.google.android.gms.maps.MapView;

/**
 * Created by Alex on 09/12/2015.
 */
public class CustomViewPager extends ViewPager {
    public CustomViewPager(Context context) {
        super(context);
    }

    public CustomViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected boolean canScroll(View v, boolean checkV, int dx, int x, int y) {
        if(v instanceof MapView){
            return true;
        }

        if (v.getClass().getPackage().getName().startsWith("maps.")) {
            return true;
        }

        return super.canScroll(v, checkV, dx, x, y);
    }
}
