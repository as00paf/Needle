package com.nemator.needle.tasks.facebook;

import android.os.AsyncTask;
import android.util.Log;

import com.github.gorbin.asne.core.AccessToken;
import com.github.gorbin.asne.core.listener.OnRequestAccessTokenCompleteListener;
import com.github.gorbin.asne.facebook.FacebookSocialNetwork;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;
import com.nemator.needle.utils.AppConstants;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONObject;

/**
 * Created by Alex on 15/09/2015.
 */
public class GetFacebookCoverURLTask extends AsyncTask<Void, Void, String>{

    private static final String FB_COVER_URL = AppConstants.PROJECT_URL +"haystack.php";
    private static final String TAG = "GetFacebookCoverURL";

    private String url;
    private String coverUrl = "";

    public GetFacebookCoverURLTask(FacebookSocialNetwork socialNetwork, String userId){
        url = "https://graph.facebook.com/" +
                userId
                +"?fields=cover&access_token=" + socialNetwork.getAccessToken().token;

        Log.d(TAG, "URL : "+url);
    }

    @Override
    protected String doInBackground(Void... voids) {
        try {
            OkHttpClient client = new OkHttpClient();

            Request request = new Request.Builder()
                    .url(url)
                    .build();

            //Request
            Response response = client.newCall(request).execute();
            String jsonString = response.body().string();
            Gson gson = new Gson();

            coverUrl = gson.fromJson(jsonString, FacebookCoverResponse.class).cover.url;
        } catch (Exception e) {
            Log.d(TAG, "Exception : " + e.getMessage());

            e.printStackTrace();
        }

        return coverUrl;
    }

    public static class FacebookCoverObject{
        @SerializedName("source")
        private String url = null;
    }

    public static class FacebookCoverResponse{
        @SerializedName("cover")
        private FacebookCoverObject cover = null;
    }
}
