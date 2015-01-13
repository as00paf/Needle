package com.needletest.pafoid.needletest.asynctask;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

import com.needletest.pafoid.needletest.AppConstants;
import com.needletest.pafoid.needletest.models.PostLocationParams;
import com.needletest.pafoid.needletest.utils.JSONParser;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class PostLocation extends AsyncTask<PostLocationParams, String, String> {

    private static final String TAG_SUCCESS = "success";
    private static final String TAG_MESSAGE = "message";

    private static final String POST_LOCATION_URL = AppConstants.PROJECT_URL + "updateLocation.php";

    private JSONParser jsonParser = new JSONParser();

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected String doInBackground(PostLocationParams... params) {
        int success;

        String lat = Double.toString(params[0].location.getLatitude());
        String lng = Double.toString(params[0].location.getLongitude());

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(params[0].context);
        String username = sp.getString("username", "anon");

        try {
            List<NameValuePair> requestParams = new ArrayList<NameValuePair>();
            requestParams.add(new BasicNameValuePair("username", username));
            requestParams.add(new BasicNameValuePair("lat", lat));
            requestParams.add(new BasicNameValuePair("lng", lng));

            Log.d("request!", "starting");

            JSONObject json = jsonParser.makeHttpRequest(POST_LOCATION_URL, "POST", requestParams);

            Log.d("Post Location attempt", json.toString());

            success = json.getInt(TAG_SUCCESS);
            if (success == 1) {
                Log.d("Location Added!", json.toString());
                return json.getString(TAG_MESSAGE);
            }else{
                Log.d("Location Failure!", json.getString(TAG_MESSAGE));
                return json.getString(TAG_MESSAGE);

            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;

    }

    protected void onPostExecute(String file_url) {

    }

}
