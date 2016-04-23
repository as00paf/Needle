package com.nemator.needle.activities;

import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.nemator.needle.Needle;
import com.nemator.needle.R;
import com.nemator.needle.adapter.UserProfileAdapter;
import com.nemator.needle.api.ApiClient;
import com.nemator.needle.api.result.UserResult;
import com.nemator.needle.models.vo.UserVO;
import com.nemator.needle.utils.AppConstants;
import com.nemator.needle.utils.CropCircleTransformation;
import com.squareup.picasso.Picasso;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserProfileActivity extends AppCompatActivity{

    public static final String TAG = "UserProfileActivity";

    private Toolbar toolbar;
    private RecyclerView listView;
    private ImageButton editAvatar, editCover;

    private UserVO user;
    private Boolean isMe = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Boolean isUserLoading = false;

        Bundle bundle = getIntent().getExtras();
        if(bundle != null){
            if(bundle.containsKey(AppConstants.TAG_USER)){
                user = bundle.getParcelable(AppConstants.TAG_USER);
            }else if(bundle.containsKey(AppConstants.TAG_USER_ID)){
                //Load user details
                isUserLoading = true;

                int userId = bundle.getInt(AppConstants.TAG_USER_ID);
                isMe = Needle.userModel.getUserId() == userId;

                ApiClient.getInstance().getUserById(Needle.userModel.getUserId(), userId, new Callback<UserResult>() {
                    @Override
                    public void onResponse(Call<UserResult> call, Response<UserResult> response) {
                        UserResult result = response.body();
                        if(result.getSuccessCode() == 1){
                            user = result.getUser();
                            initUser();
                        }else{
                            Log.e(TAG, "Could not load user : " + result.getMessage() + "\n" + response.message());
                        }
                    }

                    @Override
                    public void onFailure(Call<UserResult> call, Throwable t) {
                        Log.e(TAG, "Could not load user : " + t.getMessage());
                    }
                });
            }
        }

        setContentView(R.layout.activity_user_profile);

        //Toolbar
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        //List
        listView =  (RecyclerView) findViewById(R.id.listView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        listView.setLayoutManager(layoutManager);

        editAvatar = (ImageButton) findViewById(R.id.edit_avatar_button);
        editCover = (ImageButton) findViewById(R.id.edit_cover_button);

        if(!isUserLoading){
            if(user == null){
                user = Needle.userModel.getUser();
                isMe = true;
            }
            initUser();
        }
    }

    private void initUser() {
        //Username
        CollapsingToolbarLayout collapsingToolbar = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        collapsingToolbar.setTitle(user.getReadableUserName());
        collapsingToolbar.setStatusBarScrimColor(ContextCompat.getColor(this, R.color.primary_dark));

        toolbar.setTitle(user.getReadableUserName());
        getSupportActionBar().setTitle(user.getReadableUserName());
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        //Profile Image
        ImageView avatarImageView = (ImageView) findViewById(R.id.avatar);
        String pictureURL = user.getPictureURL().replace("_normal", "");
        if(!TextUtils.isEmpty(pictureURL)){
            Picasso.with(this).load(pictureURL)
                    .noFade()
                    .error(R.drawable.person_placeholder)
                    .transform(new CropCircleTransformation(this, 2, Color.WHITE))
                    .into(avatarImageView);
        }else{
            Log.e(TAG, "Can't load profile pic");
        }

        //Cover Image
        String coverUrl = user.getCoverPictureURL();
        ImageView cover = (ImageView) findViewById(R.id.cover);
        if(!TextUtils.isEmpty(coverUrl)){
            Picasso.with(this)
                    .load(coverUrl)
                    .error(R.drawable.mat)
                    .placeholder(R.drawable.gradient_foreground)
                    .fit()
                    .into(cover);
        }else{
            Log.e(TAG, "Can't load cover pic");
        }

        UserProfileAdapter adapter = new UserProfileAdapter(this, user, isMe);
        listView.setAdapter(adapter);

        //Edit buttons
        if(isMe){
            editAvatar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(TAG, "Edit avatar picture");
                }
            });

            editCover.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(TAG, "Edit cover picture");
                }
            });
        }else{
            editAvatar.setVisibility(View.INVISIBLE);
            editCover.setVisibility(View.INVISIBLE);
        }
    }


    private void refreshUser() {
        if(isMe){
            ApiClient.getInstance().getUserById(Needle.userModel.getUserId(), user.getId(), new Callback<UserResult>() {
                @Override
                public void onResponse(Call<UserResult> call, Response<UserResult> response) {
                    UserResult result = response.body();
                    if (result.getSuccessCode() == 1) {
                        user = result.getUser();
                        initUser();
                    } else {
                        Log.e(TAG, "Error getting user : " + result.getMessage());
                    }
                }

                @Override
                public void onFailure(Call<UserResult> call, Throwable t) {
                    Log.e(TAG, "Error getting user : " + t.getMessage());
                }
            });
        }else{

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        int menuRes = isMe ? R.menu.menu_user_profile : R.menu.menu_friend_profile;
        getMenuInflater().inflate(menuRes, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case android.R.id.home:
                finish();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
