package com.nemator.needle.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;

import com.nemator.needle.Needle;
import com.nemator.needle.R;
import com.nemator.needle.models.vo.HaystackVO;
import com.nemator.needle.models.vo.LocationSharingVO;
import com.nemator.needle.utils.AppConstants;

public class LocationSharingActivity extends AppCompatActivity {

    private static final String TAG = "LocationSharingActivity";

    //Children
    private TextView distanceLabel;
    private Toolbar toolbar;

    //Data
    private LocationSharingVO locationSharing;
    private Boolean isSent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(savedInstanceState != null){
            updateValuesFromBundle(savedInstanceState);
        }else{
            locationSharing = (LocationSharingVO) getIntent().getExtras().get(AppConstants.TAG_LOCATION_SHARING);
        }

        setContentView(R.layout.activity_location_sharing);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        String name = locationSharing.isSender() ? locationSharing.getReceiver().getReadableUserName() : locationSharing.getSender().getReadableUserName();
        String title = getString(R.string.location_of) + name;
        toolbar.setTitle(title);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        //Distance Indicator
        distanceLabel = (TextView) findViewById(R.id.location_sharing_distance_label);
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

    @Override
    protected void onDestroy() {
        Needle.networkController.unregister();
        Needle.serviceController.unbindService();

        super.onDestroy();
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
