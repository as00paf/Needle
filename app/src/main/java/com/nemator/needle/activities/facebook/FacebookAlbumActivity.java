package com.nemator.needle.activities.facebook;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;

import com.facebook.AccessToken;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.appevents.AppEventsLogger;
import com.google.gson.Gson;
import com.nemator.needle.Needle;
import com.nemator.needle.R;
import com.nemator.needle.adapter.FacebookAlbumAdapter;
import com.nemator.needle.models.vo.facebook.FacebookAlbumsVO;
import com.nemator.needle.models.vo.facebook.FacebookUserVO;

/**
 * Created by Alex on 28/06/2016.
 */
public class FacebookAlbumActivity extends AppCompatActivity{

    private static final String TAG = "FbAlbumActivity";
    private Toolbar toolbar;
    private RecyclerView list;
    private FacebookAlbumAdapter albumAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_list);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        list = (RecyclerView) findViewById(R.id.list);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        list.setLayoutManager(layoutManager);
    }

    @Override
    protected void onResume() {
        super.onResume();
        //initFacebook();
        initAlbums();
    }

    private void initFacebook(){
        Log.d(TAG, "initFacebook");
        AppEventsLogger.activateApp(this);
        FacebookSdk.sdkInitialize(getApplicationContext(), facebookInitializedCallback);
    }

    private final FacebookSdk.InitializeCallback facebookInitializedCallback = new FacebookSdk.InitializeCallback() {
        @Override
        public void onInitialized() {
            Log.d(TAG, "facebook sdk initialized");

            FacebookSdk.setIsDebugEnabled(true);
            FacebookSdk.setApplicationName(getResources().getString(R.string.app_name));
            FacebookSdk.setApplicationId(getResources().getString(R.string.facebook_app_id));

            //initAlbums();
        }
    };

    private void initAlbums() {
        String userId = Needle.userModel.getFbUser().getId();
        if(!TextUtils.isEmpty(userId)){

            new GraphRequest(
                    AccessToken.getCurrentAccessToken(),
                    "/" + userId + "/albums",
                    null,
                    HttpMethod.GET,
                    new GraphRequest.Callback() {
                        public void onCompleted(GraphResponse response) {
                            Log.d(TAG, "Success : " + response.getRawResponse());

                            Gson gson = new Gson();
                            FacebookAlbumsVO albums = gson.fromJson(response.getRawResponse(), FacebookAlbumsVO.class);
                            albums.generateCoverUrls(Needle.userModel.getFbUser().getId(), AccessToken.getCurrentAccessToken().getToken());

                            albumAdapter = new FacebookAlbumAdapter(albums);
                            list.setAdapter(albumAdapter);
                        }
                    }
            ).executeAsync();
        }else{
            Log.e(TAG, "UserId is null");
        }

    }
}
