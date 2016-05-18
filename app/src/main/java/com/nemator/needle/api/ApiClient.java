package com.nemator.needle.api;

import android.util.Log;

import com.nemator.needle.Needle;
import com.nemator.needle.api.result.NeedleResult;
import com.nemator.needle.api.result.NotificationResult;
import com.nemator.needle.api.result.PinResult;
import com.nemator.needle.api.result.PinsResult;
import com.nemator.needle.api.result.UserRegistrationResult;
import com.nemator.needle.api.result.UserResult;
import com.nemator.needle.api.result.UsersResult;
import com.nemator.needle.models.vo.HaystackUserVO;
import com.nemator.needle.models.vo.HaystackVO;
import com.nemator.needle.models.vo.NeedleVO;
import com.nemator.needle.models.vo.NotificationVO;
import com.nemator.needle.models.vo.PinVO;
import com.nemator.needle.models.vo.UserVO;
import com.nemator.needle.api.result.TaskResult;
import com.nemator.needle.api.result.HaystackResult;
import com.nemator.needle.api.result.LoginResult;
import com.nemator.needle.utils.AppConstants;

import java.io.IOException;
import java.util.ArrayList;

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

    public void login(UserVO user, Callback<LoginResult> callBack){
        Call<LoginResult> call = client.logIn(user);
        call.enqueue(callBack);
    }

    public void getUserInfos(UserVO user, Callback<LoginResult> callBack){
        Call<LoginResult> call = client.getUserInfos(user.getId());
        call.enqueue(callBack);
    }

    public void logout(Callback<TaskResult> callBack){
        Call<TaskResult> call = client.logOut();
        call.enqueue(callBack);
    }

    public void getUserById(int callerId, int userId,  Callback<UserResult> callBack) {
        Call<UserResult> call = client.getUserById(callerId, userId);
        call.enqueue(callBack);
    }

    //Haystacks
    public void fetchHaystacks(int userId,  Callback<HaystackResult> callBack) {
        Call<HaystackResult> call = client.getHaystacks(userId);
        call.enqueue(callBack);
    }

    public void createHaystack(HaystackVO vo,  Callback<HaystackResult> callBack){
        Call<HaystackResult> call = client.createHaystack(vo);
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


    public void cancelHaystack(UserVO user, HaystackVO haystack, Callback<TaskResult> callBack) {
        HaystackUserVO vo = new HaystackUserVO(haystack, user);
        Call<TaskResult> call = client.cancelHaystack(vo);
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

    //Pins
    public void getHaystackPins(int haystackId, Callback<PinsResult> callback){
        Call<PinsResult> call = client.getHaystackPins(haystackId);
        call.enqueue(callback);
    }

    public void createHaystackPin(PinVO pin, Callback<PinResult> callback){
        Call<PinResult> call = client.createPin(pin);
        call.enqueue(callback);
    }

    //Location Sharing
    public void createNeedle(NeedleVO needleVO, Callback<NeedleResult> callBack) {
        Call<NeedleResult> call = client.createNeedle(needleVO);
        call.enqueue(callBack);
    }

    public void fetchNeedles(Callback<NeedleResult> callBack) {
        Call<NeedleResult> call = client.getNeedles(Needle.userModel.getUserId());
        call.enqueue(callBack);
    }

    public void shareLocationBack(NeedleVO vo, Callback<NeedleResult> callBack){
        Call<NeedleResult> call = client.shareLocationBack(vo);
        call.enqueue(callBack);
    }

    public void cancelNeedle(NeedleVO vo, Callback<NeedleResult> callBack){
        Call<NeedleResult> call = client.cancelNeedle(vo);
        call.enqueue(callBack);
    }

    //Location
    public void updateLocation(UserVO user, Callback<UserResult> callBack) {
        Call<UserResult> call = client.updateLocation(user);
        call.enqueue(callBack);
    }

    public void retrieveSenderLocation(int userId, NeedleVO vo, Callback<UserResult> callBack) {
        Call<UserResult> call = client.retrieveUserLocation(vo.getId(), userId, "sender");
        call.enqueue(callBack);
    }

    public void retrieveReceiverLocation(int userId, NeedleVO vo, Callback<UserResult> callBack) {
        Call<UserResult> call = client.retrieveUserLocation(vo.getId(), userId, "receiver");
        call.enqueue(callBack);
    }

    //General
    public void fetchAllUsers(int userId,  Callback<UsersResult> callBack) {
        Call<UsersResult> call = client.fetchAllUsers(userId);
        call.enqueue(callBack);
    }

    //Notifications
    public void fetchNotifications(int userId, Callback<NotificationResult> callBack) {
        Call<NotificationResult> call = client.fetchNotifications(userId);
        call.enqueue(callBack);
    }

    public void seenNotifications(ArrayList<NotificationVO> notifications, Callback<NotificationResult> callBack) {
        Call<NotificationResult> call = client.seenNotifications(notifications);
        call.enqueue(callBack);
    }

    //Friends
    public void getFriendById(int callerId, int userId,  Callback<UsersResult> callBack) {
       /* Call<UsersResult> call = client.getUser(userId);
        call.enqueue(callBack);*/
    }

}
