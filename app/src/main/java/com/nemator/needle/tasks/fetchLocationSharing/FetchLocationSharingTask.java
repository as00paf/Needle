package com.nemator.needle.tasks.fetchLocationSharing;

import android.os.AsyncTask;
import android.util.Log;
import android.view.View;

import com.google.android.gms.maps.model.LatLng;
import com.nemator.needle.models.vo.HaystackVO;
import com.nemator.needle.models.vo.LocationSharingVO;
import com.nemator.needle.models.vo.UserVO;
import com.nemator.needle.tasks.fetchHaystack.FetchHaystacksParams;
import com.nemator.needle.tasks.fetchHaystack.FetchHaystacksResult;
import com.nemator.needle.utils.AppConstants;
import com.nemator.needle.utils.JSONParser;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class FetchLocationSharingTask extends AsyncTask<Void, Void, FetchLocationSharingResult> {

    private static final String GET_LOCATION_SHARING_URL = AppConstants.PROJECT_URL +"getLocationSharing.php";
    private static final String TAG = "FetchLocationSTask";

    private FetchLocationSharingResponseHandler delegate;

    private JSONParser jsonParser = new JSONParser();
    private FetchLocationSharingParams params;

    private ArrayList<LocationSharingVO> locationSharingList = null;

    public FetchLocationSharingTask(FetchLocationSharingParams params, FetchLocationSharingResponseHandler delegate){
        this.params = params;
        this.delegate = delegate;
    }

    @Override
    protected void onPostExecute(FetchLocationSharingResult result) {
        params.progressbar.setVisibility(View.GONE);
        delegate.onLocationSharingFetched(result);
    }

    @Override
    protected FetchLocationSharingResult doInBackground(Void... args) {
        FetchLocationSharingResult result = new FetchLocationSharingResult();
        int success;

        try {
            //Params
            List<NameValuePair> requestParams = new ArrayList<NameValuePair>();
            requestParams.add(new BasicNameValuePair("userId", params.userId));

            //Request
            JSONObject json = jsonParser.makeHttpRequest(GET_LOCATION_SHARING_URL, "POST", requestParams);

            //Results
            success = json.getInt(AppConstants.TAG_SUCCESS);
            result.successCode = success;

            Log.d(TAG, "FetchLocationSharing Successful!\n" + json.toString());

            JSONArray locationSharings = json.getJSONArray("location_sharings");

            if (locationSharings != null) {
                locationSharingList = new ArrayList<LocationSharingVO>();

                int count = locationSharings.length();
                for (int i = 0; i < count; i++) {
                    JSONObject locationSharingData = locationSharings.getJSONObject(i);

                    LocationSharingVO locationSharingVO = new LocationSharingVO();
                    locationSharingVO.id = locationSharingData.getInt("id");
                    locationSharingVO.timeLimit = locationSharingData.getString("timeLimit");
                    locationSharingVO.senderName = locationSharingData.getString("senderName");
                    locationSharingVO.senderId = locationSharingData.getInt("senderId");
                    locationSharingVO.receiverName = locationSharingData.getString("receiverName");
                    locationSharingVO.receiverId = locationSharingData.getInt("receiverId");
                    locationSharingVO.location = new LatLng(locationSharingData.getDouble("lat"), locationSharingData.getDouble("lng"));

                    //Picture
                    try{
                        String pictureURL = locationSharingData.getString("pictureURL");
                        if (pictureURL != null)
                            locationSharingVO.pictureURL = pictureURL;
                    }catch(Exception e){
                        Log.e("parseJson", "No pictureURL for #" + i );
                    }

                    Log.e("parseJson", "Adding Location Sharing # " + i);
                    locationSharingList.add(locationSharingVO);
                }

                result.locationSharingList = locationSharingList;

                return result;
            }else{
                Log.d(TAG, "FetchLocationSharing Failure!" + json.getString(AppConstants.TAG_MESSAGE));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    public interface FetchLocationSharingResponseHandler {
        void onLocationSharingFetched(FetchLocationSharingResult result);
    }
}
