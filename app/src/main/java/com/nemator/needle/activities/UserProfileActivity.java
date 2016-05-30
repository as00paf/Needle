package com.nemator.needle.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
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
    private boolean userLoaded = false;
    private boolean friendsLoaded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle bundle = getIntent().getExtras();
        if(bundle != null){
            if(bundle.containsKey(AppConstants.TAG_USER)){
                user = bundle.getParcelable(AppConstants.TAG_USER);
                friendship = bundle.getParcelable(AppConstants.TAG_FRIENDSHIP);

                isMe = user.getId() == Needle.userModel.getUserId();
                if(isMe){
                    user = Needle.userModel.getUser();
                    userLoaded = true;
                }

                if(friendship == null){
                    ApiClient.getInstance().getFriendship(user.getId(), friendshipResultCallback);
                }
            }else if(bundle.containsKey(AppConstants.TAG_USER_ID)){
                int userId = bundle.getInt(AppConstants.TAG_USER_ID);
                isMe = false;
                user = new UserVO().setId(userId);

                ApiClient.getInstance().getFriendship(userId, friendshipResultCallback);
            }
        }else{
            isMe = true;
            userLoaded = true;
            user = Needle.userModel.getUser();
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

        if(userLoaded){
            if(friendsLoaded){
                initUser();
            }else{
                ApiClient.getInstance().getFriends(user.getId(), friendsResultCallback);
            }
        }

        //Notifications
        initNotificationListener();
    }

    private Callback<FriendshipResult> friendshipResultCallback = new Callback<FriendshipResult>() {
        @Override
        public void onResponse(Call<FriendshipResult> call, Response<FriendshipResult> response) {
            FriendshipResult result = response.body();
            if(result.getSuccessCode() == 1){
                friendship = result.getFriendship();
                if(!userLoaded && user.getId() == result.getFriend().getId()){
                    user = result.getFriend();
                }
                userLoaded = true;

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

        UserProfileAdapter adapter = new UserProfileAdapter(this, user, friends, isMe, friendship, this);
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

    private void refreshFriend() {
        userLoaded = false;
        friendsLoaded = false;

        ApiClient.getInstance().getFriendship(user.getId(), friendshipResultCallback);
    }

    //Menu
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
    public void acceptFriendRequest(UserVO user) {
        ApiClient.getInstance().acceptFriendRequest(user.getId(), acceptfriendRequestCallback);
    }

    @Override
    public void cancelFriendRequest(UserVO user) {
        ApiClient.getInstance().cancelFriendRequest(user.getId(), cancelFriendRequestCallback);
    }

    @Override
    public void rejectFriendRequest(UserVO user) {
        ApiClient.getInstance().rejectFriendRequest(user.getId(), rejectfriendRequestCallback);
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

    //Notifications
    private void initNotificationListener(){
        //TODO : stop listening ?
        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(this);

        localBroadcastManager.registerReceiver(notificationReceiver,
                new IntentFilter(AppConstants.TAG_NOTIFICATION));
    }

    private BroadcastReceiver notificationReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle extras = intent.getExtras();
            int type = extras.getInt(AppConstants.TAG_TYPE);

            if(type == AppConstants.FRIEND_REQUEST || type == AppConstants.ACCEPTED_FRIEND_REQUEST ||
                    type == AppConstants.REJECTED_FRIEND_REQUEST){
                UserVO notificationUser = new UserVO().setId(extras.getInt(AppConstants.TAG_ID));
                if(notificationUser.getId() == user.getId()){
                    refreshFriend();
                }
            }
        }
    };

    //Callbacks
    private Callback<TaskResult> unfriendCallback = new Callback<TaskResult>() {
        @Override
        public void onResponse(Call<TaskResult> call, Response<TaskResult> response) {
            TaskResult result = response.body();

            if(result.getSuccessCode() == 1){
                friendship = null;
                ApiClient.getInstance().getFriends(user.getId(), friendsResultCallback);

                Toast.makeText(UserProfileActivity.this, R.string.unfriend_successful, Toast.LENGTH_SHORT).show();
            }else{
                Log.e(TAG, "Could not unfriend user. Error : " + result.getMessage());
            }
        }

        @Override
        public void onFailure(Call<TaskResult> call, Throwable t) {
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

    private Callback<FriendshipResult> acceptfriendRequestCallback = new Callback<FriendshipResult>() {
        @Override
        public void onResponse(Call<FriendshipResult> call, Response<FriendshipResult> response) {
            FriendshipResult result = response.body();

            if(result.getSuccessCode() == 1){
                Log.e(TAG, "Friend Request Accepted");

                UserProfileActivity.this.friendship.setStatus(AppConstants.FRIEND);
                ApiClient.getInstance().getFriends(user.getId(), friendsResultCallback);

                Toast.makeText(UserProfileActivity.this, R.string.friend_request_accepted, Toast.LENGTH_SHORT).show();
            }else{
                Log.e(TAG, "Could not accept friend request. Error : " + result.getMessage());
            }
        }

        @Override
        public void onFailure(Call<FriendshipResult> call, Throwable t) {
            Log.e(TAG, "Could not send friend request. Error : " + t.getMessage());
        }
    };

    private Callback<FriendshipResult> rejectfriendRequestCallback = new Callback<FriendshipResult>() {
        @Override
        public void onResponse(Call<FriendshipResult> call, Response<FriendshipResult> response) {
            FriendshipResult result = response.body();

            if(result.getSuccessCode() == 1){
                Log.e(TAG, "Friend Request Rejected");

                friendship = null;
                initUser();

                Toast.makeText(UserProfileActivity.this, R.string.friend_request_rejected, Toast.LENGTH_SHORT).show();
            }else{
                Log.e(TAG, "Could not reject friend request. Error : " + result.getMessage());
            }
        }

        @Override
        public void onFailure(Call<FriendshipResult> call, Throwable t) {
            Log.e(TAG, "Could not reject friend request. Error : " + t.getMessage());
        }
    };

    private Callback<TaskResult> cancelFriendRequestCallback = new Callback<TaskResult>() {
        @Override
        public void onResponse(Call<TaskResult> call, Response<TaskResult> response) {
            TaskResult result = response.body();

            if(result.getSuccessCode() == 1){
                Toast.makeText(UserProfileActivity.this, R.string.friend_request_cancelled, Toast.LENGTH_SHORT).show();
                friendship = null;
                initUser();
            }else{
                Log.e(TAG, "Could not cancel friend request. Error : " + result.getMessage());
            }
        }

        @Override
        public void onFailure(Call<TaskResult> call, Throwable t) {
            Log.e(TAG, "Could not cancel friend request. Error : " + t.getMessage());
        }
    };
}
