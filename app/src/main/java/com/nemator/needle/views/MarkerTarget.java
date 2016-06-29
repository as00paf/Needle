package com.nemator.needle.views;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.util.Log;

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Marker;
import com.nemator.needle.utils.BitmapUtils;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

public class MarkerTarget implements Target {
    private static final String TAG = "PicassoMarker";

    protected Marker marker;

    public MarkerTarget(Marker marker)
    {
        this.marker = marker;
    }

    public Marker getMarker(){
        return marker;
    }

    @Override
    public int hashCode() {
        return marker.hashCode();
    }

    @Override
    public void onBitmapLoaded(final Bitmap bitmap, Picasso.LoadedFrom from) {
        marker.setIcon(BitmapDescriptorFactory.fromBitmap(bitmap));
    }

    @Override
    public void onBitmapFailed(Drawable errorDrawable)
    {
        if(errorDrawable != null){
            Bitmap placeHolderBitmap = BitmapUtils.drawableToBitmap(errorDrawable);
            marker.setIcon(BitmapDescriptorFactory.fromBitmap(placeHolderBitmap));
        }
    }

    @Override
    public void onPrepareLoad(Drawable placeHolderDrawable)
    {
        try{
            Bitmap placeHolderBitmap = BitmapUtils.drawableToBitmap(placeHolderDrawable);
            BitmapDescriptor descriptor = BitmapDescriptorFactory.fromBitmap(placeHolderBitmap);
            marker.setIcon(descriptor);
        }catch (Exception e){

        }
    }


    public void setMarker(Marker marker) {
        this.marker = marker;
    }
}