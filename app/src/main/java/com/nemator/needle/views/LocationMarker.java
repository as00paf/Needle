package com.nemator.needle.views;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nemator.needle.R;

/**
 * Created by Alex on 25/04/2016.
 */
public class LocationMarker extends LinearLayout {
    private TextView title;
    private ImageView image;

    public LocationMarker(Context context) {
        super(context);
        init();
    }

    public LocationMarker(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public LocationMarker(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public LocationMarker(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init(){
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.location_marker, this, true);

        title = (TextView) findViewById(R.id.locationMarkertext);
        image = (ImageView) findViewById(R.id.markerImage);
    }

    public void setTitle(String text){
        title.setText(text);
    }
}
