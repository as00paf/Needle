package com.nemator.needle.tasks.facebook;

import android.content.res.Resources;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.github.gorbin.asne.core.AccessToken;
import com.github.gorbin.asne.core.listener.OnRequestAccessTokenCompleteListener;
import com.github.gorbin.asne.facebook.FacebookSocialNetwork;
import com.nemator.needle.R;
import com.nemator.needle.tasks.haystack.HaystackTaskParams;
import com.nemator.needle.utils.AppConstants;
import com.nemator.needle.utils.JSONParser;

import org.apache.http.NameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Alex on 15/09/2015.
 */
public class GetFacebookCoverURLTask extends AsyncTask<Void, Void, String>{

    private static final String FB_COVER_URL = AppConstants.PROJECT_URL +"haystack.php";
    private static final String TAG = "GetFacebookCoverURL";
    private FacebookSocialNetwork socialNetwork;

    private JSONParser jsonParser = new JSONParser();
    private String url;
    private String coverUrl = "";
    private JSONObject json;

    public GetFacebookCoverURLTask(FacebookSocialNetwork socialNetwork, String userId){
        this.socialNetwork = socialNetwork;
        url = "https://graph.facebook.com/" +
                userId
                +"?fields=cover&access_token=";

        Log.d(TAG, "URL : "+url);
    }

    @Override
    protected String doInBackground(Void... voids) {
        socialNetwork.setOnRequestAccessTokenCompleteListener(new OnRequestAccessTokenCompleteListener() {
            @Override
            public void onRequestAccessTokenComplete(int socialNetworkID, AccessToken accessToken) {
                try {
                    //Request
                    json = jsonParser.makeHttpRequest(url+accessToken.token, "GET", new ArrayList<NameValuePair>());
                    coverUrl = json.getJSONObject("cover").getString("source");
                } catch (Exception e) {
                    Log.d(TAG, "Exception : " + e.getMessage());

                    e.printStackTrace();
                }
            }

            @Override
            public void onError(int socialNetworkID, String requestID, String errorMessage, Object data) {

            }
        });
        socialNetwork.requestAccessToken();




        return coverUrl;
    }

}
