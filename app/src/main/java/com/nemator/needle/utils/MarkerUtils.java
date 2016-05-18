package com.nemator.needle.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.location.Location;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v4.content.ContextCompat;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.nemator.needle.R;
import com.nemator.needle.models.vo.UserVO;
import com.nemator.needle.views.UserMarker;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

public class MarkerUtils {

    public static UserMarker createUserMarker(Context context, GoogleMap map, UserVO user, LatLng location, String snippet){
        int strokeColor = ContextCompat.getColor(context, android.R.color.black);
        int shadeColor = ContextCompat.getColor(context, R.color.transparent_grey);

        return createUserMarker(context, map, user, location, snippet, strokeColor, shadeColor);
    }

    public static UserMarker createUserMarker(Context context, GoogleMap map, UserVO user, LatLng location, String snippet, int strokeColor, int shadeColor){
        //Create map marker with options
        MarkerOptions myMarkerOptions = new MarkerOptions().position(location);
        myMarkerOptions.flat(true);
        myMarkerOptions.anchor(1.0f, 0.6f);

        //Circle
        double radiusInMeters = 10.0;

        CircleOptions circleOptions = new CircleOptions().center(location).radius(radiusInMeters).fillColor(shadeColor).strokeColor(strokeColor).strokeWidth(8);
        Circle circle = map.addCircle(circleOptions);

        //Set Background
        Bitmap pinBitmap = BitmapUtils.drawableToBitmap(context.getResources().getDrawable(R.drawable.ic_place_black_24dp));
        Bitmap scaledBitmap = BitmapUtils.scaleBitmap(pinBitmap, pinBitmap.getWidth() * 2, true);

        Marker marker = map.addMarker(myMarkerOptions);
        marker.setIcon(BitmapDescriptorFactory.fromBitmap(scaledBitmap));
        marker.setSnippet(snippet);

        //Create marker target
        final UserMarker userMarker = new UserMarker(user, marker);
        userMarker.setCircle(circle);
        String pictureURL = user.getPictureURL();

        final MarkerImageTransform transform = new MarkerImageTransform(context);

        //Load and transform picture into marker
        Picasso.with(context).load(pictureURL)
                .placeholder(R.drawable.ic_place_black_48dp)
                .transform(transform)
                .into(userMarker);

        return userMarker;
    }

    private static class MarkerImageTransform implements Transformation {
        private Context context;

        public MarkerImageTransform(Context context)
        {
            this.context = context;
        }

        @Override
        public Bitmap transform(Bitmap source) {
            Bitmap pinBitmap = BitmapUtils.drawableToBitmap(this.context.getResources().getDrawable(R.drawable.ic_place_black_48dp));
            Bitmap scaledSource = BitmapUtils.scaleBitmap(source, pinBitmap.getWidth() / 2 - 4, true);

            Bitmap circularBitmap = BitmapUtils.addCircularBorderToBitmap(scaledSource, 2, context.getResources().getColor(android.R.color.white));
            Bitmap markerBitmap = BitmapUtils.overlay(pinBitmap, circularBitmap, (pinBitmap.getWidth() - scaledSource.getWidth()) / 2 - 4, pinBitmap.getHeight() / 8);

            source.recycle();

            return markerBitmap;
        }

        @Override
        public String key(){
            return "MarkerImageTransform";
        }
    }

    public static void animateUserMarker(final GoogleMap map, final Marker marker, final Circle circle, final LatLng toPosition, final boolean hideMarker) {
        final Handler handler = new Handler();
        final long start = SystemClock.uptimeMillis();
        Projection proj = map.getProjection();
        Point startPoint = proj.toScreenLocation(marker.getPosition());
        final LatLng startLatLng = proj.fromScreenLocation(startPoint);
        final long duration = 500;
        final Interpolator interpolator = new LinearInterpolator();

        Location startLocation = new Location("");
        startLocation.setLatitude(startLatLng.latitude);
        startLocation.setLongitude(startLatLng.longitude);

        Location endLocation = new Location("");
        endLocation.setLatitude(toPosition.latitude);
        endLocation.setLongitude(toPosition.longitude);

        float distance = startLocation.distanceTo(endLocation);

        if(distance > 1){
            handler.post(new Runnable() {
                @Override
                public void run() {
                    long elapsed = SystemClock.uptimeMillis() - start;
                    float t = interpolator.getInterpolation((float) elapsed / duration);
                    double lng = t * toPosition.longitude + (1 - t) * startLatLng.longitude;
                    double lat = t * toPosition.latitude + (1 - t) * startLatLng.latitude;
                    LatLng currentPosition = new LatLng(lat, lng);
                    marker.setPosition(currentPosition);
                    circle.setCenter(currentPosition);

                    if (t < 1.0) {
                        // Post again 16ms later.
                        handler.postDelayed(this, 16);
                    } else {
                        if (hideMarker) {
                            marker.setVisible(false);
                            circle.setVisible(false);
                        } else {
                            marker.setVisible(true);
                            circle.setVisible(true);
                        }
                    }
                }
            });
        }
    }
}
