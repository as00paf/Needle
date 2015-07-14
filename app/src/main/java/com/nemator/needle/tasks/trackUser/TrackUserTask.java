package com.nemator.needle.tasks.trackUser;

import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.nemator.needle.tasks.retrieveLocations.RetrieveLocationsParams;
import com.nemator.needle.tasks.retrieveLocations.RetrieveLocationsResult;
import com.nemator.needle.utils.AppConstants;
import com.nemator.needle.utils.JSONParser;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TrackUserTask extends AsyncTask<Void, Void, TrackUserResult> {
    private static final String TRACK_USER_URL = AppConstants.PROJECT_URL + "trackUser.php";
    private static final String TAG = "TrackUserTask";

    private TrackUserResponseHandler delegate;
    private TrackUserParams params;
    private JSONParser jParser = new JSONParser();

    public TrackUserTask(TrackUserParams params, TrackUserResponseHandler delegate){
        this.params = params;
        this.delegate = delegate;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected TrackUserResult doInBackground(Void... arg) {
        TrackUserResult result = new TrackUserResult();
        int success;

        try {
            List<NameValuePair> requestParams = new ArrayList<NameValuePair>();
            requestParams.add(new BasicNameValuePair("userId", params.userId));
            requestParams.add(new BasicNameValuePair("locationSharingId", params.locationSharingId));

            Log.d(TAG, "Tracking User ...");

            JSONObject json = jParser.makeHttpRequest(TRACK_USER_URL, "GET", requestParams);
            success = json.getInt(AppConstants.TAG_SUCCESS);
            result.successCode = success;
            if (success == 1) {
                Log.d(TAG, "Tracking User Task success");
                result.location = new LatLng(json.getDouble("lat"), json.getDouble("lng"));
                result.message = json.getString(AppConstants.TAG_MESSAGE);

                return result;
            }else{
                Log.d(TAG, "RetrieveLocationsTask failed : "+json.getString(AppConstants.TAG_MESSAGE));
                result.message = "Error Tracking User";
                return result;
            }
        } catch (JSONException e) {
            Log.e(TAG,"Track User JSON Error");
            //e.printStackTrace();
            result.successCode = 0;
            result.message = "Error Tracking User";
            return  result;
        }
    }

    @Override
    protected void onPostExecute(TrackUserResult result) {
        super.onPostExecute(result);
        delegate.onUserTracked(result);
    }

    public interface TrackUserResponseHandler {
        void onUserTracked(TrackUserResult result);
    }

}
