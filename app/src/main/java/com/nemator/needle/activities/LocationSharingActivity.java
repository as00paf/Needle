package com.nemator.needle.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.nemator.needle.Needle;
import com.nemator.needle.R;
import com.nemator.needle.api.ApiClient;
import com.nemator.needle.api.result.LocationSharingResult;
import com.nemator.needle.models.vo.HaystackVO;
import com.nemator.needle.models.vo.LocationSharingVO;
import com.nemator.needle.utils.AppConstants;
import com.nemator.needle.viewHolders.LocationSharingCardHolder;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LocationSharingActivity extends AppCompatActivity {

    private static final String TAG = "LocationSharingActivity";

    //Children
    private TextView distanceLabel;
    private Toolbar toolbar;
    private Menu menu;

    //Data
    private LocationSharingVO locationSharing;

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
        String name = locationSharing.getReceiver().getId() != Needle.userModel.getUserId() ?
                locationSharing.getReceiver().getReadableUserName() : locationSharing.getSender().getReadableUserName();
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
        savedInstanceState.putParcelable(AppConstants.TAG_LOCATION_SHARING, locationSharing);
    }

    private void updateValuesFromBundle(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            if (savedInstanceState.keySet().contains(AppConstants.TAG_LOCATION_SHARING)) {
                locationSharing = savedInstanceState.getParcelable(AppConstants.TAG_LOCATION_SHARING);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.location_sharing, menu);
        this.menu = menu;

        if(locationSharing.getReceiver().getId() == Needle.userModel.getUserId()){
            if(locationSharing.isSharedBack()){
                menu.findItem(R.id.share_location_back).setIcon(R.drawable.ic_location_disabled_white_24dp);
            }else{
                menu.findItem(R.id.share_location_back).setIcon(R.drawable.ic_my_location_white_24dp);
            }

            menu.findItem(R.id.share_location_back).setVisible(true);
            menu.findItem(R.id.cancel_location_sharing).setVisible(false);
        }else{
            menu.findItem(R.id.cancel_location_sharing).setVisible(true);
            menu.findItem(R.id.share_location_back).setVisible(false);
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.share_location_back:
                if(locationSharing.isSharedBack()){
                    cancelShareBack();
                }else{
                    shareLocationBack();
                }

                break;
            case R.id.cancel_location_sharing:
                cancelLocationSharing();
                break;
        }

        return super.onOptionsItemSelected(item);
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

    public void shareLocationBack(){
        Log.i(TAG, "Trying to share location back");
        LocationSharingVO vo = locationSharing.clone();
        vo.setShareBack(true);
        ApiClient.getInstance().shareLocationBack(vo, shareLocationBackCallback);
    }

    private Callback<LocationSharingResult> shareLocationBackCallback = new Callback<LocationSharingResult>() {
        @Override
        public void onResponse(Call<LocationSharingResult> call, Response<LocationSharingResult> response) {
            //TODO : show share back icon
            LocationSharingResult result = response.body();
            if(result.getSuccessCode() == 1){
                Log.d(TAG, "Location shared back ? " + result.getLocationSharing().isSharedBack());
                Toast.makeText(LocationSharingActivity.this, getString(R.string.location_shared_back), Toast.LENGTH_SHORT).show();

                locationSharing.setShareBack(result.getLocationSharing().isSharedBack());
                if(locationSharing.isSharedBack()){
                    menu.findItem(R.id.share_location_back).setIcon(R.drawable.ic_location_disabled_white_24dp);
                }else{
                    menu.findItem(R.id.share_location_back).setIcon(R.drawable.ic_my_location_white_24dp);
                }
            }else{
                Log.d(TAG, "Failed to share location back !");
            }
        }

        @Override
        public void onFailure(Call<LocationSharingResult> call, Throwable t) {
            Log.d(TAG, "Failed to share location back !");
        }
    };

    public void cancelShareBack(){
        Log.i(TAG, "Trying to cancel share back");
        LocationSharingVO vo = locationSharing.clone();
        vo.setShareBack(false);
        ApiClient.getInstance().shareLocationBack(vo, shareLocationBackCallback);
    }

    public void cancelLocationSharing(){
        Log.i(TAG, "Trying to cancel location sharing");
        ApiClient.getInstance().cancelLocationSharing(locationSharing, cancelLocationSharingCallback);
    }

    private Callback<LocationSharingResult> cancelLocationSharingCallback = new Callback<LocationSharingResult>() {
        @Override
        public void onResponse(Call<LocationSharingResult> call, Response<LocationSharingResult> response) {
            LocationSharingResult result = response.body();
            if(result.getSuccessCode() == 1){
                Log.d(TAG, "Location sharing cancelled !");
                Toast.makeText(LocationSharingActivity.this, getString(R.string.location_sharing_ended), Toast.LENGTH_SHORT).show();
                finish();
            }else{
                Log.d(TAG, "Failed to cancel location sharing !");
            }
        }

        @Override
        public void onFailure(Call<LocationSharingResult> call, Throwable t) {
            Log.d(TAG, "Failed to cancel location sharing !");
        }
    };

    //Getters/Setters
    public LocationSharingVO getLocationSharing() {
        return locationSharing;
    }

    public void setLocationSharing(LocationSharingVO locationSharing) {
        this.locationSharing = locationSharing;
    }
}
