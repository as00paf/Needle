package com.nemator.needle.fragments.locationSharing.locationSharing;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.nemator.needle.R;
import com.nemator.needle.models.vo.LocationSharingVO;
import com.nemator.needle.utils.AppConstants;


public class LocationSharingFragment extends Fragment {
    private static final String TAG = "LocationSharingFragment";

    private View rootView;

    //Children
    private TextView distanceLabel;

    //Data
    private LocationSharingVO locationSharing;
    private Boolean isSent;

    public static LocationSharingFragment newInstance() {
        LocationSharingFragment fragment = new LocationSharingFragment();
        return fragment;
    }

    public LocationSharingFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(savedInstanceState != null){
            updateValuesFromBundle(savedInstanceState);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_location_sharing, container, false);

        //Distance Indicator
        distanceLabel = (TextView) rootView.findViewById(R.id.location_sharing_distance_label);

        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putParcelable(AppConstants.LOCATION_SHARING_DATA_KEY, locationSharing);
        savedInstanceState.putBoolean(AppConstants.IS_SENT_DATA_KEY, isSent);
    }

    private void updateValuesFromBundle(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            if (savedInstanceState.keySet().contains(AppConstants.LOCATION_SHARING_DATA_KEY)) {
                locationSharing = savedInstanceState.getParcelable(AppConstants.LOCATION_SHARING_DATA_KEY);
            }

            if (savedInstanceState.keySet().contains(AppConstants.IS_SENT_DATA_KEY)) {
                isSent = savedInstanceState.getBoolean(AppConstants.IS_SENT_DATA_KEY);
            }
        }
    }

    //Public Methods
    public void updateDistance(String distance){
        distanceLabel.setText(distance);
    }

    //Getters/Setters
    public LocationSharingVO getLocationSharing() {
        return locationSharing;
    }

    public void setLocationSharing(LocationSharingVO locationSharing) {
        this.locationSharing = locationSharing;
    }

    public Boolean getIsSent() {
        return isSent;
    }

    public void setIsSent(Boolean isSent) {
        this.isSent = isSent;
    }
}