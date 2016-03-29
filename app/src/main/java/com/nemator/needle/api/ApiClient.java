package com.nemator.needle.api;

import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;

import com.nemator.needle.Needle;
import com.nemator.needle.api.result.LocationSharingResult;
import com.nemator.needle.api.result.NotificationResult;
import com.nemator.needle.api.result.UserRegistrationResult;
import com.nemator.needle.api.result.UserResult;
import com.nemator.needle.api.result.UsersResult;
import com.nemator.needle.models.vo.HaystackUserVO;
import com.nemator.needle.models.vo.HaystackVO;
import com.nemator.needle.models.vo.LocationSharingVO;
import com.nemator.needle.models.vo.UserVO;
import com.nemator.needle.api.result.TaskResult;
import com.nemator.needle.api.result.HaystackResult;
import com.nemator.needle.api.result.LoginResult;
import com.nemator.needle.utils.AppConstants;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by Alex on 06/10/2015.
 */
public class ApiClient {

    private static ApiClient instance;

    private NeedleApiClient client;
    private OkHttpClient httpClient;

    public ApiClient() {
        httpClient = new OkHttpClient.Builder().addInterceptor(new Interceptor() {
            @Override
            public okhttp3.Response intercept(Chain chain) throws IOException {
                okhttp3.Response response = chain.proceed(chain.request());

                String content = response.body().string();
                Log.d("interceptor", "intercepted res = " + content);
                return response.newBuilder()
                        .body(ResponseBody.create(response.body().contentType(), content))
                        .build();
            }
        }).build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(AppConstants.PROJECT_URL)
                .client(httpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        client = retrofit.create(NeedleApiClient.class);
    }

    public static ApiClient getInstance() {
        if(instance == null){
            instance = new ApiClient();
        }

        return instance;
    }

    //Authentication
    public void registerUser(UserVO userVO, Callback<UserRegistrationResult> callBack){
        Call<UserRegistrationResult> call = client.registerUser(userVO.getLoginType(), userVO.getEmail(), userVO.getUserName(),
                userVO.getPassword(), userVO.getGcmRegId(), userVO.getPictureURL(), userVO.getCoverPictureURL(), userVO.getSocialNetworkUserId());
        call.enqueue(callBack);
    }

    public void login(int loginType, String email, String username, String gcmRegId, String password, String socialNetworkUserId, Callback<LoginResult> callBack){
        Call<LoginResult> call = client.logIn(loginType, email, username, gcmRegId, password, socialNetworkUserId);
        call.enqueue(callBack);
    }

    public void logout(Callback<TaskResult> callBack){
        Call<TaskResult> call = client.logOut();
        call.enqueue(callBack);
    }

    //Haystacks
    public void fetchHaystacks(int userId,  Callback<HaystackResult> callBack) {
        Call<HaystackResult> call = client.getHaystacks(userId);
        call.enqueue(callBack);
    }

    public void createHaystack(HaystackVO vo,  Callback<HaystackResult> callBack){
        Call<HaystackResult> call = client.createHaystack(vo.getName(),
                vo.getOwner(), vo.getIsPublic(), vo.getZoneRadius(), vo.getIsCircle(), vo.getPosition().getLongitude(),
                vo.getPosition().getLatitude(), vo.getPictureURL(), vo.getTimeLimit(), vo.getUserIds());
        call.enqueue(callBack);
    }

    public void retrieveHaystackUserLocations(int haystackId, Callback<UsersResult> callBack){
        Call<UsersResult> call = client.retrieveHaystackLocations(haystackId);
        call.enqueue(callBack);
    }

    public void activateUser(UserVO user, HaystackVO haystack, Callback<TaskResult> callBack) {
        user.setIsActive(true);
        Call<TaskResult> call = client.toggleUserActivation(new HaystackUserVO(haystack, user));
        call.enqueue(callBack);
    }

    public void deactivateUser(UserVO user, HaystackVO haystack, Callback<TaskResult> callBack) {
        Call<TaskResult> call = client.toggleUserActivation(new HaystackUserVO(haystack, user));
        call.enqueue(callBack);
    }

    public void leaveHaystack(UserVO user, HaystackVO haystack, Callback<TaskResult> callBack){
        HaystackUserVO vo = new HaystackUserVO(haystack, user);
        Call<TaskResult> call = client.leaveHaystack(vo);
        call.enqueue(callBack);
    }

    public void fetchHaystackUsers(UserVO user, HaystackVO haystack, Callback<UsersResult> callBack){
        Call<UsersResult> call = client.fetchHaystackUsers(user.getId(), haystack.getId());
        call.enqueue(callBack);
    }

    public void addUsersToHaystack(HaystackVO haystack, Callback<HaystackResult> callback) {
        Call<HaystackResult> call = client.addUsersToHaystack(haystack);
        call.enqueue(callback);
    }

    public void fetchUsersNotInHaystack(int haystackId, Callback<UsersResult> callback) {
        Call<UsersResult> call = client.fetchUsersNotInHaystack(haystackId);
        call.enqueue(callback);
    }

    //Location Sharing
    public void createLocationSharing(LocationSharingVO locationSharingVO, Callback<LocationSharingResult> callBack) {
        Call<LocationSharingResult> call = client.createLocationSharing(locationSharingVO);
        call.enqueue(callBack);
    }

    public void fetchLocationSharings(Callback<LocationSharingResult> callBack) {
        Call<LocationSharingResult> call = client.getLocationSharings(Needle.userModel.getUserId());
        call.enqueue(callBack);
    }

    public void shareLocationBack(LocationSharingVO vo, Callback<LocationSharingResult> callBack){
        Call<LocationSharingResult> call = client.shareLocationBack(vo);
        call.enqueue(callBack);
    }

    public void cancelLocationSharing(LocationSharingVO vo, Callback<LocationSharingResult> callBack){
        Call<LocationSharingResult> call = client.cancelLocationSharing(vo);
        call.enqueue(callBack);
    }

    //Location
    public void updateLocation(UserVO user, Callback<UserResult> callBack) {
        Call<UserResult> call = client.updateLocation(user);
        call.enqueue(callBack);
    }

    public void retrieveSenderLocation(int userId, LocationSharingVO vo, Callback<UserResult> callBack) {
        Call<UserResult> call = client.retrieveUserLocation(vo.getId(), userId, "sender");
        call.enqueue(callBack);
    }

    public void retrieveReceiverLocation(int userId, LocationSharingVO vo, Callback<UserResult> callBack) {
        Call<UserResult> call = client.retrieveUserLocation(vo.getId(), userId, "receiver");
        call.enqueue(callBack);
    }

    //General
    public void fetchAllUsers(int userId,  Callback<UsersResult> callBack) {
        Call<UsersResult> call = client.fetchAllUsers(String.valueOf(userId));
        call.enqueue(callBack);
    }

    //Notifications
    public void fetchNotifications(int userId,  Callback<NotificationResult> callBack) {
        Call<NotificationResult> call = client.fetchNotifications(userId);
        call.enqueue(callBack);
    }

    //Friends

}
