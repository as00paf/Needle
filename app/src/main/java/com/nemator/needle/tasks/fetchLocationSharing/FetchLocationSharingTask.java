package com.nemator.needle.tasks.fetchLocationSharing;

import android.os.AsyncTask;
import android.util.Log;
import android.view.View;

import com.google.android.gms.maps.model.LatLng;
import com.nemator.needle.models.vo.LocationSharingVO;
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

    public FetchLocationSharingTask(FetchLocationSharingParams params, FetchLocationSharingResponseHandler delegate){
        this.params = params;
        this.delegate = delegate;
    }

    @Override
    protected void onPostExecute(FetchLocationSharingResult result) {
        delegate.onLocationSharingFetched(result);
    }

    @Override
    protected FetchLocationSharingResult doInBackground(Void... args) {
        FetchLocationSharingResult result = new FetchLocationSharingResult();
        int success, receivedSuccess, sentSuccess;
        String message = "not set";
        String receivedMessage = "not set";
        String sentMessage = "not set";

        try {
            //Params
            List<NameValuePair> requestParams = new ArrayList<NameValuePair>();
            requestParams.add(new BasicNameValuePair("userId", params.userId));

            //Request
            JSONObject json = jsonParser.makeHttpRequest(GET_LOCATION_SHARING_URL, "POST", requestParams);

            //Results
            success = json.getInt(AppConstants.TAG_SUCCESS);
            result.successCode = success;
            message = json.getString(AppConstants.TAG_MESSAGE);
            result.successMessage = message;
            receivedSuccess = json.getInt("received_success");
            result.receivedSuccessCode = receivedSuccess;
            receivedMessage = json.getString("received_message");
            result.receivedSuccessMessage = receivedMessage;
            sentSuccess = json.getInt("sent_success");
            result.sentSuccessCode = sentSuccess;
            sentMessage = json.getString("sent_message");
            result.sentSuccessMessage = sentMessage;

            Log.d(TAG, "FetchLocationSharing Successful!\n" + json.toString());

            //Sent Locations
            JSONArray sentLocationSharings = json.getJSONArray("sent");

            if (sentLocationSharings != null) {
                ArrayList<LocationSharingVO> locationSharingList = new ArrayList<LocationSharingVO>();

                int count = sentLocationSharings.length();
                for (int i = 0; i < count; i++) {
                    JSONObject locationSharingData = sentLocationSharings.getJSONObject(i);

                    LocationSharingVO locationSharingVO = new LocationSharingVO();
                    locationSharingVO.setId(locationSharingData.getInt("id"));
                    locationSharingVO.setSenderName(locationSharingData.getString("senderName"));
                    locationSharingVO.setSenderId(locationSharingData.getInt("senderId"));
                    locationSharingVO.setReceiverName(locationSharingData.getString("receiverName"));
                    locationSharingVO.setReceiverId(locationSharingData.getInt("receiverId"));
                    locationSharingVO.setTimeLimit(locationSharingData.getString("timeLimit"));

                    Log.e("parseJson", "Adding Location Sharing # " + i);
                    locationSharingList.add(locationSharingVO);
                }

                result.sentLocationSharingList = locationSharingList;
            }

            //Received Locations
            JSONArray receivedLocationSharings = json.getJSONArray("received");

            if (receivedLocationSharings != null) {
                ArrayList<LocationSharingVO> locationSharingList = new ArrayList<LocationSharingVO>();

                int count = receivedLocationSharings.length();
                for (int i = 0; i < count; i++) {
                    JSONObject locationSharingData = receivedLocationSharings.getJSONObject(i);

                    LocationSharingVO locationSharingVO = new LocationSharingVO();
                    locationSharingVO.setId(locationSharingData.getInt("id"));
                    locationSharingVO.setTimeLimit(locationSharingData.getString("timeLimit"));
                    locationSharingVO.setSenderName(locationSharingData.getString("senderName"));
                    locationSharingVO.setSenderId(locationSharingData.getInt("senderId"));
                    locationSharingVO.setReceiverName(locationSharingData.getString("receiverName"));
                    locationSharingVO.setReceiverId(locationSharingData.getInt("receiverId"));

                    Log.e("parseJson", "Adding Location Sharing # " + i);
                    locationSharingList.add(locationSharingVO);
                }

                result.receivedLocationSharingList = locationSharingList;

                return result;
            }
        } catch (Exception e) {
            Log.d(TAG, "FetchLocationSharing Failure ! message :" + message +"\nreceivedMessage : " + receivedMessage + "\nsentMessage : " + sentMessage);
            e.printStackTrace();
        }

        return result;
    }

    public interface FetchLocationSharingResponseHandler {
        void onLocationSharingFetched(FetchLocationSharingResult result);
    }
}
