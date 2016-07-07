package com.nemator.needle.activities.facebook;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;

import com.facebook.AccessToken;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.appevents.AppEventsLogger;
import com.google.gson.Gson;
import com.nemator.needle.Needle;
import com.nemator.needle.R;
import com.nemator.needle.adapter.FacebookAlbumAdapter;
import com.nemator.needle.models.vo.facebook.FacebookAlbumVO;
import com.nemator.needle.models.vo.facebook.FacebookAlbumsVO;
import com.nemator.needle.utils.AppConstants;

public class FacebookAlbumsActivity extends AppCompatActivity implements FacebookAlbumAdapter.ClickListener {

    private static final String TAG = "FbAlbumsActivity";
    private Toolbar toolbar;
    private RecyclerView list;
    private FacebookAlbumAdapter albumAdapter;

    private int requestCode;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_list);

        Intent intent = getIntent();
        if(intent != null){
           requestCode = intent.getExtras().getInt(AppConstants.TAG_REQUEST_CODE, -1);
        }

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        list = (RecyclerView) findViewById(R.id.list);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        list.setLayoutManager(layoutManager);
    }

    @Override
    protected void onResume() {
        super.onResume();
        initFacebook();
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

            initAlbums();
        }
    };

    private void initAlbums() {
        String userId = Needle.userModel.getFbUser().getId();
        if(!TextUtils.isEmpty(userId)){

            GraphRequest request = GraphRequest.newGraphPathRequest(
                    AccessToken.getCurrentAccessToken(),
                     "me/albums",
                    new GraphRequest.Callback() {
                        public void onCompleted(GraphResponse response) {
                            Log.d(TAG, "Success : " + response.getRawResponse());

                            Gson gson = new Gson();
                            FacebookAlbumsVO albums = gson.fromJson(response.getRawResponse(), FacebookAlbumsVO.class);
                            if (albums != null) {
                                albumAdapter = new FacebookAlbumAdapter(albums, FacebookAlbumsActivity.this);
                                list.setAdapter(albumAdapter);
                            }else{
                                Log.e(TAG, "Error : " + response.getRawResponse());
                            }
                        }
                    }
            );

            Bundle parameters = new Bundle();
            parameters.putString("fields", "link, can_tag, name, count, picture, photos, source");
            request.setParameters(parameters);
            request.executeAsync();
        }else{
            Log.e(TAG, "UserId is null");
        }

    }

    @Override
    public void onClick(FacebookAlbumVO album) {
        Intent intent = new Intent(this, FacebookAlbumActivity.class);
        intent.putExtra(AppConstants.TAG_ALBUM, album);

        startActivityForResult(intent, requestCode);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(data != null && data.getExtras() != null){
            finishWithResult(data);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                super.onBackPressed();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void finishWithResult(Intent data){
        setResult(RESULT_OK, data);
        finish();
    }
}
