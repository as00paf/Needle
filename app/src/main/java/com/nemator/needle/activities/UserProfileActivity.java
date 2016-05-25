package com.nemator.needle.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.content.ContextCompat;
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
import android.widget.Toast;

import com.nemator.needle.Needle;
import com.nemator.needle.R;
import com.nemator.needle.adapter.UserProfileAdapter;
import com.nemator.needle.api.ApiClient;
import com.nemator.needle.api.result.FriendsResult;
import com.nemator.needle.api.result.FriendshipResult;
import com.nemator.needle.api.result.TaskResult;
import com.nemator.needle.api.result.UserResult;
import com.nemator.needle.interfaces.IUserProfileListener;
import com.nemator.needle.models.vo.FriendshipVO;
import com.nemator.needle.models.vo.UserVO;
import com.nemator.needle.utils.AppConstants;
import com.nemator.needle.utils.CropCircleTransformation;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserProfileActivity extends AppCompatActivity implements IUserProfileListener{

    public static final String TAG = "UserProfileActivity";

    private Toolbar toolbar;
    private RecyclerView listView;
    private ImageButton editAvatar, editCover;

    private UserVO user;
    private ArrayList<UserVO> friends;
    private FriendshipVO friendship;
    private Boolean isMe = false;
    private boolean friendsLoaded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Boolean isUserLoading = false;

        Bundle bundle = getIntent().getExtras();
        if(bundle != null){
            if(bundle.containsKey(AppConstants.TAG_USER)){
                user = bundle.getParcelable(AppConstants.TAG_USER);
                friendship = bundle.getParcelable(AppConstants.TAG_FRIENDSHIP);

                isMe = user.getId() == Needle.userModel.getUserId();

                if(friendship == null){
                    isUserLoading = true;
                    ApiClient.getInstance().getFriendship(user.getId(), friendshipResultCallback);
                }
            }else if(bundle.containsKey(AppConstants.TAG_USER_ID)){
                //Load user details
                isUserLoading = true;

                int userId = bundle.getInt(AppConstants.TAG_USER_ID);
                isMe = Needle.userModel.getUserId() == userId;

                ApiClient.getInstance().getFriendship(userId, friendshipResultCallback);
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

            if(friendsLoaded){
                initUser();
            }else{
                ApiClient.getInstance().getFriends(user.getId(), friendsResultCallback);
            }
        }
    }

    private Callback<FriendshipResult> friendshipResultCallback = new Callback<FriendshipResult>() {
        @Override
        public void onResponse(Call<FriendshipResult> call, Response<FriendshipResult> response) {
            FriendshipResult result = response.body();
            if(result.getSuccessCode() == 1){
                if(user == null) user = result.getFriend();
                friendship = result.getFriendship();

                if(friendsLoaded){
                    initUser();
                }else{
                    ApiClient.getInstance().getFriends(user.getId(), friendsResultCallback);
                }
            }else{
                Log.e(TAG, "Could not load user : " + result.getMessage() + "\n" + response.message());
            }
        }

        @Override
        public void onFailure(Call<FriendshipResult> call, Throwable t) {
            Log.e(TAG, "Could not load user : " + t.getMessage());
        }
    };

    private Callback<FriendsResult> friendsResultCallback = new Callback<FriendsResult>() {
        @Override
        public void onResponse(Call<FriendsResult> call, Response<FriendsResult> response) {
            FriendsResult result = response.body();
            if(result.getSuccessCode() == 1){
                friends = result.getFriends();
                initUser();
            }else{
                Log.e(TAG, "Could not load friends : " + result.getMessage() + "\n" + response.message());
            }
        }

        @Override
        public void onFailure(Call<FriendsResult> call, Throwable t) {
            Log.e(TAG, "Could not load friends : " + t.getMessage());
        }
    };

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

        Boolean isFriend = friendship != null ? friendship.getStatus() == AppConstants.FRIEND : false;
        UserProfileAdapter adapter = new UserProfileAdapter(this, user, friends, isMe, isFriend, this);
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

    //Listener
    @Override
    public void unfriend(UserVO user) {
        ApiClient.getInstance().unfriend(user.getId(), unfriendCallback);
    }

    @Override
    public void sendFriendRequest(UserVO user) {
        ApiClient.getInstance().sendFriendRequest(user.getId(), friendRequestCallback);
    }

    @Override
    public void addUserToGroup(UserVO user) {
        //TODO: implement this
    }

    @Override
    public void sendNeedle(UserVO user) {
        Intent intent = new Intent(this, CreateNeedleActivity.class);
        intent.putExtra(AppConstants.TAG_USER, (Parcelable) user);
        startActivity(intent);
    }

    //Callbacks
    private Callback<FriendshipResult> unfriendCallback = new Callback<FriendshipResult>() {
        @Override
        public void onResponse(Call<FriendshipResult> call, Response<FriendshipResult> response) {
            FriendshipResult result = response.body();

            if(result.getSuccessCode() == 1){
                FriendshipVO friendship = result.getFriendship();
                UserProfileActivity.this.friendship = friendship;
                initUser();

                Toast.makeText(UserProfileActivity.this, R.string.unfriend_successful, Toast.LENGTH_SHORT).show();
            }else{
                Log.e(TAG, "Could not unfriend user. Error : " + result.getMessage());
            }
        }

        @Override
        public void onFailure(Call<FriendshipResult> call, Throwable t) {
            Log.e(TAG, "Could not unfriend user. Error : " + t.getMessage());
        }
    };

    private Callback<FriendshipResult> friendRequestCallback = new Callback<FriendshipResult>() {
        @Override
        public void onResponse(Call<FriendshipResult> call, Response<FriendshipResult> response) {
            FriendshipResult result = response.body();

            if(result.getSuccessCode() == 1){
                Log.e(TAG, "Friend Request Sent");

                FriendshipVO friendship = result.getFriendship();
                UserProfileActivity.this.friendship = friendship;
                initUser();

                Toast.makeText(UserProfileActivity.this, R.string.sent_friend_requests, Toast.LENGTH_SHORT).show();
            }else{
                Log.e(TAG, "Could not send friend request. Error : " + result.getMessage());
            }
        }

        @Override
        public void onFailure(Call<FriendshipResult> call, Throwable t) {
            Log.e(TAG, "Could not send friend request. Error : " + t.getMessage());
        }
    };
}
