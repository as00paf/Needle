package com.nemator.needle.tasks.locationSharing;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;

import com.nemator.needle.R;
import com.nemator.needle.data.LocationServiceDBHelper.PostLocationRequest;
import com.nemator.needle.models.vo.LocationSharingVO;
import com.nemator.needle.tasks.db.addPostLocationRequest.AddPostLocationRequestParams;
import com.nemator.needle.tasks.db.removePostLocationRequest.RemovePostLocationRequestTask;
import com.nemator.needle.utils.AppConstants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class LocationSharingTask extends AsyncTask<Void, Void, LocationSharingResult> {

    private static final String LOCATION_SHARING_URL = AppConstants.PROJECT_URL +"locationSharing.php";
    private static final String TAG = "LocationSharingTask";

    private Object delegate;

    private LocationSharingParams params;
    private ProgressDialog dialog;

    public LocationSharingTask(LocationSharingParams params, Object delegate){
        this.params = params;
        this.delegate = delegate;
    }

    @Override
    protected void onPreExecute() {
        String message = null;
        Boolean showDialog = false;
        Boolean isCancellable = true;
        switch(params.type){
            case LocationSharingParams.TYPE_GET :
                break;
            case LocationSharingParams.TYPE_CREATE :
                message = params.context.getResources().getString(R.string.creating_location_sharing);
                isCancellable = false;
                break;
            case LocationSharingParams.TYPE_UPDATE :
                message = params.context.getResources().getString(R.string.sharing_location);
                break;
            case LocationSharingParams.TYPE_CANCEL :
                message = params.context.getResources().getString(R.string.cancelling_location_sharing);
                break;
            default:
                message = "Location Sharing";
        }

        if(showDialog && message != null){
            dialog = new ProgressDialog(params.context);
            dialog.setMessage(message);
            dialog.setIndeterminate(false);
            dialog.setCancelable(isCancellable);
            dialog.show();
        }


        super.onPreExecute();
    }

    @Override
    protected void onPostExecute(LocationSharingResult result) {
        switch(params.type){
            case LocationSharingParams.TYPE_GET :
                ((FetchLocationSharingResponseHandler) delegate).onLocationSharingFetched(result);
                break;
            case LocationSharingParams.TYPE_CREATE :
                ((CreateLocationSharingResponseHandler) delegate).onLocationSharingCreated(result);
                break;
            case LocationSharingParams.TYPE_UPDATE :
                ((UpdateLocationSharingTaskHandler) delegate).onLocationSharingUpdated(result);
                break;
            case LocationSharingParams.TYPE_CANCEL :
                ((CancelLocationSharingResponseHandler) delegate).onLocationSharingCancelled(result);
                break;
        }

        if(dialog !=null){
            dialog.dismiss();
        }
    }

    @Override
    protected LocationSharingResult doInBackground(Void... args) {
        Log.i(TAG, "Sending Location Sharing request of type " + params.type);

        LocationSharingResult result = new LocationSharingResult();
        int success;
        String message = "not set";
/*
        try {
            //Request
            JSONObject json;
            if(params.type == LocationSharingParams.TYPE_GET || params.type == LocationSharingParams.TYPE_CREATE || params.type == LocationSharingParams.TYPE_CANCEL){
                 //Params
                 List<NameValuePair> requestParams = (List<NameValuePair>) getRequestParams();
                 json = jsonParser.makeHttpRequest(LOCATION_SHARING_URL, params.type, requestParams);
            }else{
                JSONObject jsonObject = (JSONObject) getRequestParams();
                json = jsonParser.makeHttpRequest(LOCATION_SHARING_URL, params.type, null, jsonObject);
            }

            //Results
            success = json.getInt(AppConstants.TAG_SUCCESS);
            result.successCode = success;
            message = json.getString(AppConstants.TAG_MESSAGE);
            result.successMessage = message;

            if(result.successCode == 1){
                switch(params.type){
                    case LocationSharingParams.TYPE_GET:
                        return getLocationSharingList(json, result);
                    case LocationSharingParams.TYPE_CREATE:
                        return getCreateLocationSharing(json, result);
                    case LocationSharingParams.TYPE_UPDATE:
                        return getUpdatedLocationSharing(json, result);
                    case LocationSharingParams.TYPE_CANCEL:
                        return getCancelledLocationSharing(json, result);

                }
            }else{
                Log.i(TAG, "error");
            }


            return result;
        } catch (Exception e) {
            Log.d(TAG, "LocationSharing Failure ! message :" + message);
            e.printStackTrace();
        }
*/
        return result;
    }

    private Object getRequestParams() {
        /*List<NameValuePair> requestParams = new ArrayList<NameValuePair>();
        JSONObject jsonObject = new JSONObject();
        switch (params.type){
            case LocationSharingParams.TYPE_GET :
                requestParams.add(new BasicNameValuePair(AppConstants.TAG_USER_ID, params.userId));
                return requestParams;
            case LocationSharingParams.TYPE_CREATE :
                requestParams.add(new BasicNameValuePair(AppConstants.TAG_SENDER_NAME, params.vo.getSenderName()));
                requestParams.add(new BasicNameValuePair(AppConstants.TAG_SENDER_ID, String.valueOf(params.vo.getSenderId())));
                requestParams.add(new BasicNameValuePair(AppConstants.TAG_RECEIVER_NAME, params.vo.getReceiverName()));
                requestParams.add(new BasicNameValuePair(AppConstants.TAG_RECEIVER_ID, String.valueOf(params.vo.getReceiverId())));
                requestParams.add(new BasicNameValuePair(AppConstants.TAG_TIME_LIMIT, params.vo.getTimeLimit()));
                String notificationMessage = params.vo.getSenderName() + " shared his location with you !";
                requestParams.add(new BasicNameValuePair(AppConstants.TAG_NOTIFICATION_MESSAGE, notificationMessage));
                requestParams.add(new BasicNameValuePair("regId", params.regId));
                return requestParams;
            case LocationSharingParams.TYPE_UPDATE :
                try{
                    jsonObject.put(AppConstants.TAG_RECEIVER_ID, String.valueOf(params.vo.getReceiverId()));
                    jsonObject.put(AppConstants.TAG_LOCATION_SHARING_ID, String.valueOf(params.vo.getId()));
                    jsonObject.put(AppConstants.TAG_SHARE_BACK, String.valueOf(!params.vo.getShareBack()));
                }catch (Exception e){
                    Log.i(TAG, "Exception : "+e.getMessage());
                }

                return jsonObject;
            case LocationSharingParams.TYPE_CANCEL :
                requestParams.add(new BasicNameValuePair(AppConstants.TAG_SENDER_ID, String.valueOf(params.vo.getSenderId())));
                requestParams.add(new BasicNameValuePair(AppConstants.TAG_LOCATION_SHARING_ID, String.valueOf(params.vo.getId())));

                return requestParams;
        }
*/
        return null;
    }

    private LocationSharingResult getLocationSharingList(JSONObject json, LocationSharingResult result){
        String receivedMessage = "not set";
        String sentMessage = "not set";
        int receivedSuccess, sentSuccess;

        try{
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

                    //TODO:use constants
                    LocationSharingVO locationSharingVO = new LocationSharingVO();
                    locationSharingVO.setId(locationSharingData.getInt("id"));
                    locationSharingVO.setSenderName(locationSharingData.getString("senderName"));
                    locationSharingVO.setSenderId(locationSharingData.getInt("senderId"));
                    locationSharingVO.setReceiverName(locationSharingData.getString("receiverName"));
                    locationSharingVO.setReceiverId(locationSharingData.getInt("receiverId"));
                    locationSharingVO.setTimeLimit(locationSharingData.getString("timeLimit"));
                    locationSharingVO.setShareBack(locationSharingData.getInt("shareBack") == 1);

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

                    //TODO:use constants
                    LocationSharingVO locationSharingVO = new LocationSharingVO();
                    locationSharingVO.setId(locationSharingData.getInt("id"));
                    locationSharingVO.setTimeLimit(locationSharingData.getString("timeLimit"));
                    locationSharingVO.setSenderName(locationSharingData.getString("senderName"));
                    locationSharingVO.setSenderId(locationSharingData.getInt("senderId"));
                    locationSharingVO.setReceiverName(locationSharingData.getString("receiverName"));
                    locationSharingVO.setReceiverId(locationSharingData.getInt("receiverId"));
                    locationSharingVO.setShareBack(locationSharingData.getInt("shareBack") == 1);

                    Log.e("parseJson", "Adding Location Sharing # " + i);
                    locationSharingList.add(locationSharingVO);
                }

                result.receivedLocationSharingList = locationSharingList;

                return result;
            }

        } catch (Exception e) {
            Log.d(TAG, "LocationSharing Failure ! message :" + result.successMessage);
            e.printStackTrace();
        }

        return result;
    }

    private LocationSharingResult getCreateLocationSharing(JSONObject json, LocationSharingResult result){
        try{
            params.vo.setId(json.getInt(AppConstants.TAG_LOCATION_SHARING_ID));

            result.vo = params.vo;
            result.successMessage = json.getString(AppConstants.TAG_MESSAGE);
        }catch (JSONException e) {
            e.printStackTrace();
            result.successMessage = "Location Sharing could not be created. Exception : " + e.getMessage();
            result.successCode = 0;
        }

        Log.d(TAG, "Location Sharing Created Successfuly! " + result.successMessage);

        return result;
    }

    private LocationSharingResult getUpdatedLocationSharing(JSONObject json, LocationSharingResult result) {
        result.viewHolderRef = params.viewHolderRef;

        try {
            if (result.successCode == 1) {
                result.successMessage = json.getString(AppConstants.TAG_MESSAGE);
                result.vo = params.vo;
                result.viewHolderRef = params.viewHolderRef;

                Log.d(TAG, "LocationSharing Updated Successfuly! " + json.getString(AppConstants.TAG_MESSAGE));

                result.vo.setShareBack(!params.vo.getShareBack());
                return result;
            }else{
                result.successMessage = "LocationSharing Update Failed! " + result.successMessage;
            }
        }catch (JSONException e) {
            e.printStackTrace();
            result.successMessage = "Location Sharing could not be updated. Exception : " + e.getMessage();
            result.successCode = 0;
        }

        return result;
    }

    private LocationSharingResult getCancelledLocationSharing(JSONObject json, LocationSharingResult result) {
        try {
            if (result.successCode == 1) {
                result.successMessage = json.getString(AppConstants.TAG_MESSAGE);

                Log.d(TAG, "Location Sharing Cancelled ! " + json.getString(AppConstants.TAG_MESSAGE));

                //Remove from db
                AddPostLocationRequestParams removeParams = new AddPostLocationRequestParams(params.context,
                        PostLocationRequest.POSTER_TYPE_LOCATION_SHARING, params.vo.getTimeLimit(), params.vo.getSenderId(),
                        String.valueOf(params.vo.getId()));

                new RemovePostLocationRequestTask(removeParams).execute();

                result.vo = params.vo;

                return result;
            }else{
                Log.d(TAG, "Location Sharing Could Not Be Cancelled! " + json.getString(AppConstants.TAG_MESSAGE));

                result.successMessage = json.getString(AppConstants.TAG_MESSAGE);
                return result;

            }
        }catch (JSONException e) {
            e.printStackTrace();
            result.successMessage = "Location Sharing could not be cancelled. Exception : " + e.getMessage();
            result.successCode = 0;
        }

        return result;
    }

    public interface FetchLocationSharingResponseHandler {
        void onLocationSharingFetched(LocationSharingResult result);
    }

    public interface CreateLocationSharingResponseHandler {
        void onLocationSharingCreated(LocationSharingResult result);
    }

    public interface UpdateLocationSharingTaskHandler{
        void onLocationSharingUpdated(LocationSharingResult result);
    }

    public interface CancelLocationSharingResponseHandler {
        void onLocationSharingCancelled(LocationSharingResult result);
    }
}
