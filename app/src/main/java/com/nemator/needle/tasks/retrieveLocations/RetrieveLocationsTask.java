package com.nemator.needle.tasks.retrieveLocations;

import android.os.AsyncTask;
import android.util.Log;

import com.nemator.needle.utils.AppConstants;
import com.nemator.needle.utils.JSONParser;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class RetrieveLocationsTask extends AsyncTask<Void, Void, RetrieveLocationsResult> {
    private static final String LOCATION_URL = AppConstants.PROJECT_URL + "retrieveLocations.php";
    private static final String TAG = "RetrieveLocationsTask";

    private RetrieveLocationsResponseHandler delegate;
    private RetrieveLocationsParams params;
    private JSONParser jParser = new JSONParser();

    public RetrieveLocationsTask(RetrieveLocationsParams params, RetrieveLocationsResponseHandler delegate){
        this.params = params;
        this.delegate = delegate;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected RetrieveLocationsResult doInBackground(Void... arg) {
        RetrieveLocationsResult result = new RetrieveLocationsResult();
        int success;

        result.locationList = new ArrayList<HashMap<String, Object>>();
        try {
            List<NameValuePair> requestParams = new ArrayList<NameValuePair>();
            requestParams.add(new BasicNameValuePair("username", params.userName));
            requestParams.add(new BasicNameValuePair("userId", params.userId));
            requestParams.add(new BasicNameValuePair("haystackId", params.haystackId));

            if(params.verbose) Log.d(TAG, "Retrieving Locations...");

            JSONObject json = jParser.makeHttpRequest(LOCATION_URL, "POST", requestParams);
            success = json.getInt(AppConstants.TAG_SUCCESS);
            result.successCode = success;
            result.locationList = new ArrayList<HashMap<String, Object>>();

            if (success == 1) {
                if(params.verbose) Log.d(TAG, "RetrieveLocationsTask success");

                result.message = json.getString(AppConstants.TAG_MESSAGE);
                result.locations = json.getJSONArray(AppConstants.TAG_LOCATIONS);

                for (int i = 0; i < result.locations.length(); i++) {
                    JSONObject c = result.locations.getJSONObject(i);

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
                if(params.verbose) Log.d(TAG, "RetrieveLocationsTask failed : "+json.getString(AppConstants.TAG_MESSAGE));
                result.message = "Error Retrieving Locations";
                return result;
            }
        } catch (JSONException e) {
            Log.e(TAG,"RetrieveLocationsTask JSON Error");
            //e.printStackTrace();
            result.successCode = 0;
            result.message = "Error Retrieving Locations";
            return  result;
        }
    }

    @Override
    protected void onPostExecute(RetrieveLocationsResult result) {
        super.onPostExecute(result);
        delegate.onLocationsRetrieved(result);
    }

    public interface RetrieveLocationsResponseHandler {
        void onLocationsRetrieved(RetrieveLocationsResult result);
    }

}
