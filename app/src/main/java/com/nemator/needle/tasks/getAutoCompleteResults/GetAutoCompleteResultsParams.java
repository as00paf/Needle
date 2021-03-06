package com.nemator.needle.tasks.getAutoCompleteResults;

import android.content.Context;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.maps.model.LatLng;

/**
 * Created by Alex on 14/05/2015.
 */
public class GetAutoCompleteResultsParams {
    private String mSearchTerm;
    private AutocompleteFilter mFilter;
    private GoogleApiClient mGoogleApiClient;
    private LatLng mCurrentPosition;
    private float mZoneRadius;
    private Context mContext;

    public GetAutoCompleteResultsParams(Context context,  GoogleApiClient googleApiClient, LatLng currentPosition, float zoneRadius, String searchTerm ){
        mContext = context;
        mGoogleApiClient = googleApiClient;
        mCurrentPosition = currentPosition;
        mZoneRadius = zoneRadius;
        mSearchTerm = searchTerm;
    }

    public GetAutoCompleteResultsParams(Context context, GoogleApiClient googleApiClient, LatLng currentPosition, float zoneRadius, String searchTerm, AutocompleteFilter filter ){
        mContext = context;
        mGoogleApiClient = googleApiClient;
        mCurrentPosition = currentPosition;
        mZoneRadius = zoneRadius;
        mSearchTerm = searchTerm;
        mFilter = filter;
    }

    public GoogleApiClient getGoogleApiClient() {
        return mGoogleApiClient;
    }

    public void setGoogleApiClient(GoogleApiClient mGoogleApiClient) {
        this.mGoogleApiClient = mGoogleApiClient;
    }

    public LatLng getCurrentPosition() {
        return mCurrentPosition;
    }

    public void setCurrentPosition(LatLng mCurrentPosition) {
        this.mCurrentPosition = mCurrentPosition;
    }

    public float getZoneRadius() {
        return mZoneRadius;
    }

    public void setZoneRadius(float mZoneRadius) {
        this.mZoneRadius = mZoneRadius;
    }

    public String getSearchTerm() {
        return mSearchTerm;
    }

    public void setSearchTerm(String mSearchTerm) {
        this.mSearchTerm = mSearchTerm;
    }

    public AutocompleteFilter getFilter() {
        return mFilter;
    }

    public void setFilter(AutocompleteFilter mFilter) {
        this.mFilter = mFilter;
    }

    public Context getContext() {
        return mContext;
    }

    public void setContext(Context mContext) {
        this.mContext = mContext;
    }
}

