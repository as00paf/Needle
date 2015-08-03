package com.nemator.needle.tasks.location;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;

import com.nemator.needle.tasks.locationSharing.LocationSharingParams;
import com.nemator.needle.utils.AppConstants;
import com.nemator.needle.utils.JSONParser;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class LocationTask extends AsyncTask<Void, Void, LocationTaskResult> {

    private static final String LOCATION_SHARING_URL = AppConstants.PROJECT_URL +"location.php";
    private static final String TAG = "LocationTask";

    private Object delegate;

    private JSONParser jsonParser = new JSONParser(7000, 10000);
    private LocationTaskParams params;
    private ProgressDialog dialog;

    public LocationTask(LocationTaskParams params){
        this.params = params;
    }

    public LocationTask(LocationTaskParams params, Object delegate){
        this.params = params;
        this.delegate = delegate;
    }

    @Override
    protected void onPreExecute() {
        String message = null;
        Boolean showDialog = false;
        Boolean isCancellable = true;
        switch(params.type){
            case LocationTaskParams.TYPE_GET :
                break;
            case LocationSharingParams.TYPE_UPDATE :
                break;
            case LocationSharingParams.TYPE_CANCEL :
                break;
            default:
                message = "Location Task";
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
    protected void onPostExecute(LocationTaskResult result) {
        switch(params.type){
            case LocationTaskParams.TYPE_GET :
                ((GetLocationsResponseHandler) delegate).onLocationsFetched(result);
                break;
        }

        if(dialog !=null){
            dialog.dismiss();
        }
    }

    @Override
    protected LocationTaskResult doInBackground(Void... args) {
        Log.i(TAG, "Sending Location request of type " + params.type);

        LocationTaskResult result = new LocationTaskResult();
        int success;
        String message = "not set";

        try {
            //Request
            JSONObject json;
            if(params.type == LocationSharingParams.TYPE_GET || params.type == LocationSharingParams.TYPE_CANCEL){
                 //Params
                 List<NameValuePair> requestParams = (List<NameValuePair>) getRequestParams();
                 json = jsonParser.makeHttpRequest(LOCATION_SHARING_URL, params.type, requestParams);
            }else{
                JSONObject jsonObject = (JSONObject) getRequestParams();
                json = jsonParser.makeHttpRequest(LOCATION_SHARING_URL, params.type, null, jsonObject);
            }

            if(json == null){
                result.successMessage = message;
                result.successCode = 0;
                return result;
            }

            //Results
            success = json.getInt(AppConstants.TAG_SUCCESS);
            result.successCode = success;
            message = json.getString(AppConstants.TAG_MESSAGE);
            result.successMessage = message;

            if(result.successCode == 1){
                switch(params.type){
                    case LocationSharingParams.TYPE_GET:
                        return getHaystackLocations(json, result);
                    case LocationSharingParams.TYPE_UPDATE:
                        return getUpdatedLocation(json, result);
                }
            }else{
                Log.i(TAG, "error");
            }


            return result;
        } catch (Exception e) {
            String msg = "LocationSharing Failure ! message :" + message;
            Log.d(TAG, msg);
            e.printStackTrace();
            result.successCode = 0;
            result.successMessage = msg;
        }

        return result;
    }

    private Object getRequestParams() {
        List<NameValuePair> requestParams = new ArrayList<NameValuePair>();
        JSONObject jsonObject = new JSONObject();
        switch (params.type){
            case LocationSharingParams.TYPE_GET :
                requestParams.add(new BasicNameValuePair(AppConstants.TAG_HAYSTACK_ID, params.haystackId));
                return requestParams;
            case LocationSharingParams.TYPE_UPDATE :
                try{
                    jsonObject.put(AppConstants.TAG_USER_ID, String.valueOf(params.userId));
                    jsonObject.put(AppConstants.TAG_LAT, String.valueOf(params.location.latitude));
                    jsonObject.put(AppConstants.TAG_LNG, String.valueOf(params.location.longitude));
                }catch (Exception e){
                    Log.i(TAG, "Exception : "+e.getMessage());
                }

                return jsonObject;
        }

        return null;
    }

    private LocationTaskResult getHaystackLocations(JSONObject json, LocationTaskResult result){
        result.locationList = new ArrayList<HashMap<String, Object>>();
        try {
            if (result.successCode == 1) {
                result.successMessage = json.getString(AppConstants.TAG_MESSAGE);
                JSONArray locations = json.getJSONArray(AppConstants.TAG_LOCATIONS);

                for (int i = 0; i < locations.length(); i++) {
                    JSONObject c = locations.getJSONObject(i);

                    String id = c.getString(AppConstants.TAG_USER_ID);
                    String name = c.getString(AppConstants.TAG_USER_NAME);
                    Double lat = c.getDouble(AppConstants.TAG_LAT);
                    Double lng = c.getDouble(AppConstants.TAG_LNG);

                    HashMap<String, Object> map = new HashMap<String, Object>();

                    map.put(AppConstants.TAG_USER_ID, id);
                    map.put(AppConstants.TAG_USER_NAME, name);
                    map.put(AppConstants.TAG_LAT, lat);
                    map.put(AppConstants.TAG_LNG, lng);

                    result.locationList.add(map);
                }
                return result;
            }else{
                result.successMessage = "Error Retrieving Locations";
                return result;
            }
        } catch (JSONException e) {
            Log.e(TAG, "Location Task JSON Error");
            //e.printStackTrace();
            result.successCode = 0;
            result.successMessage = "Error Retrieving Locations";
            return  result;
        }
    }

    private LocationTaskResult getUpdatedLocation(JSONObject json, LocationTaskResult result) {
        try {
            if (result.successCode == 1) {
                result.successMessage = json.getString(AppConstants.TAG_MESSAGE);
                Log.d(TAG, "Location Updated Successfuly! " + json.getString(AppConstants.TAG_MESSAGE));

                return result;
            }else{
                result.successMessage = "Location Update Failed! " + result.successMessage;
            }
        }catch (JSONException e) {
            e.printStackTrace();
            result.successMessage = "Location could not be updated. Exception : " + e.getMessage();
            result.successCode = 0;
        }

        return result;
    }

    public interface GetLocationsResponseHandler {
        void onLocationsFetched(LocationTaskResult result);
    }
}
