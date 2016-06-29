package com.nemator.needle.activities;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.nemator.needle.Needle;
import com.nemator.needle.R;
import com.nemator.needle.activities.facebook.FacebookAlbumActivity;
import com.nemator.needle.adapter.UserProfileAdapter;
import com.nemator.needle.api.ApiClient;
import com.nemator.needle.api.callback.CreateHaystackCallback;
import com.nemator.needle.api.result.FriendsResult;
import com.nemator.needle.api.result.FriendshipResult;
import com.nemator.needle.api.result.ImageResult;
import com.nemator.needle.api.result.TaskResult;
import com.nemator.needle.api.result.UserResult;
import com.nemator.needle.controller.AuthenticationController;
import com.nemator.needle.interfaces.IUserProfileListener;
import com.nemator.needle.models.vo.FriendshipVO;
import com.nemator.needle.models.vo.UserVO;
import com.nemator.needle.tasks.imageUploader.ImageUploadParams;
import com.nemator.needle.tasks.imageUploader.ImageUploadResult;
import com.nemator.needle.tasks.imageUploader.ImageUploaderTask;
import com.nemator.needle.utils.AppConstants;
import com.nemator.needle.utils.BitmapUtils;
import com.nemator.needle.utils.CameraUtils;
import com.nemator.needle.utils.CropCircleTransformation;
import com.nemator.needle.utils.PermissionManager;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserProfileActivity extends AppCompatActivity implements IUserProfileListener {

    public static final String TAG = "UserProfileActivity";

    private Toolbar toolbar;
    private RecyclerView listView;
    private ImageButton editAvatar, editCover;
    private ImageView avatar, cover;
    private ProgressDialog dialog;
    private PopupMenu popup;

    private UserVO user;
    private ArrayList<UserVO> friends;
    private FriendshipVO friendship;
    private Boolean isMe = false;
    private boolean userLoaded = false;
    private boolean friendsLoaded = false;
    private boolean isCameraShown = false;
    private File captureFile = null;

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
        avatar = (ImageView) findViewById(R.id.avatar);
        String pictureURL = user.getPictureURL().replace("_normal", "");
        if(!TextUtils.isEmpty(pictureURL)){
            Picasso.with(this).load(pictureURL)
                    .noFade()
                    .error(R.drawable.person_placeholder)
                    .transform(new CropCircleTransformation(this, 2, Color.WHITE))
                    .into(avatar);
        }else{
            Picasso.with(this).load(R.drawable.person_placeholder)
                    .noFade()
                    .transform(new CropCircleTransformation(this, 2, Color.WHITE))
                    .into(avatar);
            Log.e(TAG, "Can't load profile pic");
        }

        //Cover Image
        String coverUrl = user.getCoverPictureURL();
        cover = (ImageView) findViewById(R.id.cover);
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
                    showEditPicturePopup(v, true);
                }
            });

            editCover.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(TAG, "Edit cover picture");
                    showEditPicturePopup(v, false);
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
        getMenuInflater().inflate(R.menu.menu_default, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case android.R.id.home:
                finish();
                return true;
            case R.id.menu_option_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            case R.id.menu_option_help:
                startActivity(new Intent(this, HelpSupportActivity.class));
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void showEditPicturePopup(View v, final boolean isAvatar) {
        int menuRes = user.getLoginType() == AuthenticationController.LOGIN_TYPE_FACEBOOK ? R.menu.menu_update_picture_fb : R.menu.menu_update_picture;
        PopupMenu popup = new PopupMenu(UserProfileActivity.this, v);

        popup.setOnMenuItemClickListener(new MenuListener(isAvatar));
        popup.inflate(menuRes);
        popup.show();
    }

    private class MenuListener implements PopupMenu.OnMenuItemClickListener{

        private boolean isAvatar;

        public MenuListener(boolean isAvatar) {
            this.isAvatar = isAvatar;
        }

        @Override
        public boolean onMenuItemClick(MenuItem item) {
            int id = item.getItemId();
            switch (id){
                case R.id.menu_take_picture:
                    takePicture(isAvatar ? AppConstants.TAKE_PICTURE_AVATAR : AppConstants.TAKE_PICTURE_COVER);
                    return true;
                case R.id.menu_upload_picture:
                    pickImageFromGallery(isAvatar ? AppConstants.SELECT_PICTURE_AVATAR : AppConstants.SELECT_PICTURE_COVER);
                    return true;
                case R.id.menu_select_fb_picture:
                    selectPictureFromFacebook(isAvatar);
                    return true;
                case R.id.menu_use_fb_picture:
                    useFacebookPicture(isAvatar);
                    return true;
            }

            return false;
        }
    }

    private void takePicture(int requestCode){
        if(PermissionManager.getInstance(this).isPermissionGranted(Manifest.permission.WRITE_EXTERNAL_STORAGE)){
            Intent intent = CameraUtils.getImageCaptureIntent();

            if (intent.resolveActivity(getPackageManager()) != null) {
                intent.putExtra(MediaStore.EXTRA_SCREEN_ORIENTATION, ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                try {
                    captureFile = createImageFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(captureFile));

                startActivityForResult(intent, requestCode);
            }
            isCameraShown = true;
        }else{
            PermissionManager.getInstance(this).requestPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  // prefix
                ".jpg",         // suffix
                storageDir      // directory
        );

        return image;
    }

    private void pickImageFromGallery(int requestCode){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), requestCode);//TODO : use strings
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //Show dialog
        dialog = ProgressDialog.show(this, getString(R.string.uploading_picutre),
                getString(R.string.uploading_picutre_msg), true);

        if(resultCode == RESULT_OK) {
            //Get Image Location
            Uri uri = null;
            if(requestCode == AppConstants.SELECT_PICTURE_COVER || requestCode == AppConstants.SELECT_PICTURE_AVATAR){
                uri = data.getData();
            }else{
                uri = Uri.fromFile(captureFile);
            }
            final Uri selectedImage = uri;

            //Define type
            String type = (requestCode == AppConstants.SELECT_PICTURE_COVER || requestCode == AppConstants.TAKE_PICTURE_COVER) ?
                    AppConstants.COVER_PICTURE :
                    AppConstants.PROFILE_PICTURE;

            //Upload
            ApiClient.getInstance().updateImage(this, Needle.userModel.getUserId(), selectedImage, type, new Callback<ImageResult>(){
                @Override
                public void onResponse(Call<ImageResult> call, Response<ImageResult> response) {
                    ImageResult result = response.body();
                    dialog.dismiss();
                    if(result.getSuccessCode() == 1){

                        if (requestCode == AppConstants.SELECT_PICTURE_COVER || requestCode == AppConstants.TAKE_PICTURE_COVER){
                            Log.d(TAG, "Cover image updated!");

                            Picasso.with(UserProfileActivity.this)
                                    .load(selectedImage)
                                    .error(R.drawable.mat)
                                    .placeholder(R.drawable.gradient_foreground)
                                    .fit()
                                    .into(cover);

                            Needle.userModel.getUser().setCoverPictureURL(result.getUrl());
                            Needle.userModel.saveUser();
                        }else{
                            Log.d(TAG, "Profile image updated!");

                            Picasso.with(UserProfileActivity.this)
                                    .load(selectedImage)
                                    .error(R.drawable.person_placeholder)
                                    .transform(new CropCircleTransformation(UserProfileActivity.this, 2, Color.WHITE))
                                    .into(avatar);

                            Needle.userModel.getUser().setPictureURL(result.getUrl());
                            Needle.userModel.saveUser();
                        }

                        Toast.makeText(UserProfileActivity.this, R.string.image_upload_success_msg, Toast.LENGTH_SHORT).show();
                    }else{
                        Log.d(TAG, "Cover image update failed! Error : " + result.getMessage()) ;
                        Toast.makeText(UserProfileActivity.this, R.string.image_upload_failed_msg, Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<ImageResult> call, Throwable t) {
                    Log.d(TAG, "Cover image update failed! Error : " + t.getMessage());
                    Toast.makeText(UserProfileActivity.this, R.string.image_upload_failed_msg, Toast.LENGTH_SHORT).show();

                    dialog.dismiss();
                }
            });
        }else{
            Log.d(TAG, "cancelled");
            if(dialog != null) dialog.dismiss();
        }
    }

    private void selectPictureFromFacebook(boolean isAvatar){
        Intent intent = new Intent(this, FacebookAlbumActivity.class);
        int requestCode = isAvatar ? AppConstants.SELECT_PICTURE_AVATAR : AppConstants.SELECT_PICTURE_COVER;
        startActivityForResult(intent, requestCode);
    }

    private void useFacebookPicture(boolean isAvatar){
        //Show dialog
        dialog = ProgressDialog.show(this, getString(R.string.uploading_picutre),
                getString(R.string.uploading_picutre_msg), true);

        final String url, type;
        if(isAvatar){
            url = Needle.userModel.getFbUser().getPicture().getData().getUrl();
            type = AppConstants.PROFILE_PICTURE;
        }else{
            url = Needle.userModel.getFbUser().getCover().getSource();
            type = AppConstants.COVER_PICTURE;
        }

        ApiClient.getInstance().updateImage(this, Needle.userModel.getUserId(), url, type, new Callback<ImageResult>(){
            @Override
            public void onResponse(Call<ImageResult> call, Response<ImageResult> response) {
                ImageResult result = response.body();
                if(dialog != null) dialog.dismiss();
                if(result.getSuccessCode() == 1){

                    if (type == AppConstants.COVER_PICTURE){
                        Log.d(TAG, "Cover image updated!");

                        Picasso.with(UserProfileActivity.this)
                                .load(url)
                                .error(R.drawable.mat)
                                .placeholder(R.drawable.gradient_foreground)
                                .fit()
                                .into(cover);

                        Needle.userModel.getUser().setCoverPictureURL(result.getUrl());
                        Needle.userModel.saveUser();
                    }else{
                        Log.d(TAG, "Profile image updated!");

                        Picasso.with(UserProfileActivity.this)
                                .load(url)
                                .error(R.drawable.person_placeholder)
                                .transform(new CropCircleTransformation(UserProfileActivity.this, 2, Color.WHITE))
                                .into(avatar);

                        Needle.userModel.getUser().setPictureURL(result.getUrl());
                        Needle.userModel.saveUser();
                    }

                    Toast.makeText(UserProfileActivity.this, R.string.image_upload_success_msg, Toast.LENGTH_SHORT).show();
                }else{
                    Log.d(TAG, "Cover image update failed! Error : " + result.getMessage()) ;
                    Toast.makeText(UserProfileActivity.this, R.string.image_upload_failed_msg, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ImageResult> call, Throwable t) {
                Log.d(TAG, "Cover image update failed! Error : " + t.getMessage());
                Toast.makeText(UserProfileActivity.this, R.string.image_upload_failed_msg, Toast.LENGTH_SHORT).show();

                if(dialog != null) dialog.dismiss();
            }
        });
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
