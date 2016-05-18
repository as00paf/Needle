package com.nemator.needle.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.nemator.needle.Needle;
import com.nemator.needle.R;
import com.nemator.needle.controller.NeedleController;
import com.nemator.needle.fragments.needle.NeedleMapFragment;
import com.nemator.needle.models.vo.NeedleVO;
import com.nemator.needle.models.vo.UserVO;
import com.nemator.needle.utils.AppConstants;
import com.nemator.needle.utils.PermissionsConstants;

public class NeedleActivity extends AppCompatActivity implements View.OnClickListener, NeedleController.ShareLocationBackDelegate, NeedleController.CancelShareBackDelegate, NeedleController.CancelNeedleDelegate {

    private static final String TAG = "NeedleActivity";

    //Children
    private TextView distanceLabel;
    private ImageButton myLocationButton, markersButton, followButton;
    private Toolbar toolbar;
    private Menu menu;

    //Data
    private NeedleVO locationSharing;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(savedInstanceState != null){
            updateValuesFromBundle(savedInstanceState);
        }else{
            locationSharing = (NeedleVO) getIntent().getExtras().get(AppConstants.TAG_LOCATION_SHARING);
        }

        setContentView(R.layout.activity_needle);

        if(!Needle.userModel.isInitialized()){
            Needle.userModel.init(this);
        }
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        String name = locationSharing.getReceiver().getId() != Needle.userModel.getUserId() ?
                locationSharing.getReceiver().getReadableUserName() : locationSharing.getSender().getReadableUserName();
        toolbar.setTitle(name);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        //Distance Indicator
        distanceLabel = (TextView) findViewById(R.id.distance_label);

        //Controls
        myLocationButton = (ImageButton) findViewById(R.id.my_position_button);
        markersButton = (ImageButton) findViewById(R.id.markers_button);
        followButton = (ImageButton) findViewById(R.id.follow_button);

        myLocationButton.setOnClickListener(this);
        markersButton.setOnClickListener(this);
        followButton.setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        //Location Service
        Needle.serviceController.initServiceAndStartUpdates(this);
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
        getMenuInflater().inflate(R.menu.menu_needle, menu);
        this.menu = menu;

        if(locationSharing.getReceiver().getId() == Needle.userModel.getUserId()){
            if(locationSharing.isSharedBack()){
                menu.findItem(R.id.share_location_back).setIcon(R.drawable.ic_location_disabled_white_24dp);
            }else{
                menu.findItem(R.id.share_location_back).setIcon(R.drawable.ic_my_location_white_24dp);
            }

            menu.findItem(R.id.share_location_back).setVisible(true);
            menu.findItem(R.id.cancel_needle).setVisible(false);
        }else{
            menu.findItem(R.id.cancel_needle).setVisible(true);
            menu.findItem(R.id.share_location_back).setVisible(false);
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
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
            case R.id.cancel_needle:
                cancelLocationSharing();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Needle.serviceController.unbindService();
    }

    @Override
    protected void onDestroy() {
        Needle.networkController.unregister();

        super.onDestroy();
    }

    //Public Methods
    public void updateDistance(String distance){
        distanceLabel.setText(distance);
    }

    public void shareLocationBack(){
        NeedleController.shareLocationBack(locationSharing, this);
    }

    @Override
    public void onLocationShareBackSuccess(NeedleVO locationSharing) {
        //TODO : show share back icon
        Log.d(TAG, "Location shared back ? " + locationSharing.isSharedBack());
        Toast.makeText(NeedleActivity.this, getString(R.string.location_shared_back), Toast.LENGTH_SHORT).show();

        this.locationSharing = locationSharing;
        menu.findItem(R.id.share_location_back).setIcon(R.drawable.ic_location_disabled_white_24dp);
    }

    @Override
    public void onLocationShareBackFailed(String msg) {
        Toast.makeText(NeedleActivity.this, msg, Toast.LENGTH_SHORT).show();
    }

    public void cancelShareBack(){
        NeedleController.cancelShareBack(locationSharing, this);
    }

    @Override
    public void onShareBackCancelSuccess(NeedleVO locationSharing) {
        Log.d(TAG, "Location shared back ? " + locationSharing.isSharedBack());
        Toast.makeText(NeedleActivity.this, getString(R.string.location_shared_back), Toast.LENGTH_SHORT).show();

        this.locationSharing = locationSharing;
        menu.findItem(R.id.share_location_back).setIcon(R.drawable.ic_my_location_white_24dp);
    }

    @Override
    public void onShareBackCancelFailed(String result) {
        Toast.makeText(NeedleActivity.this, result, Toast.LENGTH_SHORT).show();
    }

    public void cancelLocationSharing(){
        NeedleController.cancelNeedle(locationSharing, this);
    }

    @Override
    public void onNeedleCancelSuccess(NeedleVO locationSharing) {
        Log.d(TAG, "Location sharing cancelled !");
        Toast.makeText(NeedleActivity.this, getString(R.string.needle_expired), Toast.LENGTH_SHORT).show();
        finish();
    }

    @Override
    public void onNeedleCancelFailed(String result) {
        Toast.makeText(NeedleActivity.this, result, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(     requestCode == PermissionsConstants.getRequestCodeForPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                && grantResults[0] == PackageManager.PERMISSION_GRANTED){

            Needle.serviceController.startLocationUpdates();
        }
    }

    //Getters/Setters
    public NeedleVO getLocationSharing() {
        return locationSharing;
    }

    public void setLocationSharing(NeedleVO locationSharing) {
        this.locationSharing = locationSharing;
    }

    @Override
    public void onClick(View v) {
        NeedleMapFragment fragment = (NeedleMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if(v.getId() == myLocationButton.getId()){
            fragment.focusOnMyPosition();
        }else if(v.getId() == markersButton.getId()){
            fragment.zoomOnMarkers(true);
        }else if(v.getId() == followButton.getId()){
            fragment.toggleFollowUser(v);
        }
    }

    public void showInfoWindow(LatLng position, UserVO user) {

    }
}
