package com.nemator.needle.utils;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Point;
import android.location.Location;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v4.content.ContextCompat;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.nemator.needle.R;
import com.nemator.needle.models.vo.HaystackVO;

import java.util.ArrayList;

/**
 * Created by Alex on 09/12/2015.
 */
public class GoogleMapDrawingUtils {
    //TODO : move everything at the same place
    public static void animateMarker(GoogleMap map, final Marker marker, final LatLng toPosition, final boolean hideMarker) {
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
                    marker.setPosition(new LatLng(lat, lng));

                    if (t < 1.0) {
                        // Post again 16ms later.
                        handler.postDelayed(this, 16);
                    } else {
                        if (hideMarker) {
                            marker.setVisible(false);
                        } else {
                            marker.setVisible(true);
                        }
                    }
                }
            });
        }
    }

    public static Circle drawMarkerWithCircle(GoogleMap map, LatLng position, float scaleFactor, Resources resources, Marker marker, Circle circle, String title){
        double radiusInMeters = 50.0 * scaleFactor; //50 meters is max for now
        int strokeColor = resources.getColor(R.color.primary);
        int shadeColor = resources.getColor(R.color.circleColor);

        //Remove old Polygon
        if(circle != null){
            circle.remove();
        }

        if(marker != null){
            marker.remove();
        }

        if(map != null){
            CircleOptions circleOptions = new CircleOptions().center(position).radius(radiusInMeters).fillColor(shadeColor).strokeColor(strokeColor).strokeWidth(8);
            circle = map.addCircle(circleOptions);

            MarkerOptions markerOptions = new MarkerOptions().position(position);
            marker = map.addMarker(markerOptions);

            marker.setTitle(title);
        }

        return circle;
    }

    public static Circle updateMarkerWithCircle(GoogleMap map, Marker marker, Circle circle, LatLng position, float scaleFactor) {
        if(!circle.getCenter().equals(position)){
            circle.setCenter(position);
        }

        double radiusInMeters = 40.0 * scaleFactor;
        circle.setRadius(radiusInMeters);

        if(!marker.getPosition().equals(position)){
            animateMarker(map, marker, position, false);
        }

        return circle;
    }

    public static Polygon drawMarkerWithPolygon(GoogleMap map, LatLng position, float scaleFactor, Resources resources, Marker marker, Polygon polygon, String title){
        double sizeInMeters = 50.0 * scaleFactor;

        int strokeColor = resources.getColor(R.color.primary);
        int shadeColor = resources.getColor(R.color.circleColor);

        //Remove old Polygon
        if(polygon != null){
            polygon.remove();
        }

        if(marker != null){
            marker.remove();
        }

        ArrayList<LatLng> vertices = new ArrayList<>(4);
        vertices.add(0, SphericalUtil.computeOffset(position, sizeInMeters, 0));
        vertices.add(1, SphericalUtil.computeOffset(position, sizeInMeters, 90));
        vertices.add(2, SphericalUtil.computeOffset(position, sizeInMeters, 180));
        vertices.add(3, SphericalUtil.computeOffset(position, sizeInMeters, 270));

        PolygonOptions polygonOptions = new PolygonOptions().fillColor(shadeColor).strokeColor(strokeColor).strokeWidth(8).addAll(vertices);
        polygon = map.addPolygon(polygonOptions);

        MarkerOptions markerOptions = new MarkerOptions().position(position);
        marker = map.addMarker(markerOptions);
        marker.setTitle(title);

        return polygon;
    }

    public static Polygon updateMarkerWithPolygon(GoogleMap map, Marker marker, Polygon polygon, LatLng position, float scaleFactor) {
        double scale = 50.0 * scaleFactor;

        ArrayList<LatLng> vertices = new ArrayList<>(4);
        vertices.add(0, SphericalUtil.computeOffset(position, scale, 0));
        vertices.add(1, SphericalUtil.computeOffset(position, scale, 90));
        vertices.add(2, SphericalUtil.computeOffset(position, scale, 180));
        vertices.add(3, SphericalUtil.computeOffset(position, scale, 270));

        polygon.setPoints(vertices);

        if(!marker.getPosition().equals(position)){
            animateMarker(map, marker, position, false);
        }

        return polygon;
    }

    public static Circle drawCircle(GoogleMap googleMap, LatLng position, Float scaleFactor, Resources resources, Circle circle) {
        double radiusInMeters = 50.0 * scaleFactor; //50 meters is max for now
        int strokeColor = resources.getColor(R.color.primary);
        int shadeColor = resources.getColor(R.color.circleColor);

        //Remove old Polygon
        if(circle != null){
            circle.remove();
        }

        if(googleMap != null){
            CircleOptions circleOptions = new CircleOptions().center(position).radius(radiusInMeters).fillColor(shadeColor).strokeColor(strokeColor).strokeWidth(8);
            circle = googleMap.addCircle(circleOptions);
        }

        return circle;
    }

    public static Polygon drawPolygon(GoogleMap googleMap, LatLng position, Float mScaleFactor, Resources resources, Polygon polygon) {
        double sizeInMeters = 50.0 * mScaleFactor;

        int strokeColor = resources.getColor(R.color.primary);
        int shadeColor = resources.getColor(R.color.circleColor);

        //Remove old Polygon
        if(polygon != null){
            polygon.remove();
        }

        ArrayList<LatLng> vertices = new ArrayList<>(4);
        vertices.add(0, SphericalUtil.computeOffset(position, sizeInMeters, 0));
        vertices.add(1, SphericalUtil.computeOffset(position, sizeInMeters, 90));
        vertices.add(2, SphericalUtil.computeOffset(position, sizeInMeters, 180));
        vertices.add(3, SphericalUtil.computeOffset(position, sizeInMeters, 270));

        PolygonOptions polygonOptions = new PolygonOptions().fillColor(shadeColor).strokeColor(strokeColor).strokeWidth(8).addAll(vertices);
        polygon = googleMap.addPolygon(polygonOptions);

        return polygon;
    }


    public static Circle updateCircle(Circle circle, LatLng position, Float mScaleFactor) {
        if(!circle.getCenter().equals(position)){
            circle.setCenter(position);
        }

        double radiusInMeters = 40.0 * mScaleFactor;
        circle.setRadius(radiusInMeters);

        return circle;
    }

    public static Polygon updatePolygon(Polygon polygon, LatLng position, Float mScaleFactor) {
        double scale = 50.0 * mScaleFactor;

        ArrayList<LatLng> vertices = new ArrayList<>(4);
        vertices.add(0, SphericalUtil.computeOffset(position, scale, 0));
        vertices.add(1, SphericalUtil.computeOffset(position, scale, 90));
        vertices.add(2, SphericalUtil.computeOffset(position, scale, 180));
        vertices.add(3, SphericalUtil.computeOffset(position, scale, 270));

        polygon.setPoints(vertices);

        return polygon;
    }

    public static Polygon drawHaystackPolygon(Context context, GoogleMap mMap, HaystackVO haystack) {
        int strokeColor = ContextCompat.getColor(context, R.color.primary_dark);
        int shadeColor = ContextCompat.getColor(context, R.color.circleColor);

        ArrayList<LatLng> vertices = new ArrayList<>(4);
        vertices.add(0, SphericalUtil.computeOffset(haystack.getPositionLatLng(), haystack.getZoneRadius(), 0));
        vertices.add(1, SphericalUtil.computeOffset(haystack.getPositionLatLng(), haystack.getZoneRadius(), 90));
        vertices.add(2, SphericalUtil.computeOffset(haystack.getPositionLatLng(), haystack.getZoneRadius(), 180));
        vertices.add(3, SphericalUtil.computeOffset(haystack.getPositionLatLng(), haystack.getZoneRadius(), 270));

        PolygonOptions polygonOptions = new PolygonOptions().fillColor(shadeColor).strokeColor(strokeColor).strokeWidth(8).addAll(vertices);
        Polygon polygon = mMap.addPolygon(polygonOptions);

        return polygon;
    }

    public static Polygon updateHaystackPolygon(Polygon polygon, HaystackVO haystackVO) {
        ArrayList<LatLng> vertices = new ArrayList<>(4);
        vertices.add(0, SphericalUtil.computeOffset(haystackVO.getPositionLatLng(), haystackVO.getZoneRadius(), 0));
        vertices.add(1, SphericalUtil.computeOffset(haystackVO.getPositionLatLng(), haystackVO.getZoneRadius(), 90));
        vertices.add(2, SphericalUtil.computeOffset(haystackVO.getPositionLatLng(), haystackVO.getZoneRadius(), 180));
        vertices.add(3, SphericalUtil.computeOffset(haystackVO.getPositionLatLng(), haystackVO.getZoneRadius(), 270));

        polygon.setPoints(vertices);

        return polygon;
    }
}
