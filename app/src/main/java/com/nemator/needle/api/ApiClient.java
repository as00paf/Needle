package com.nemator.needle.api;

import android.util.Log;

import com.nemator.needle.api.result.LocationSharingTaskResult;
import com.nemator.needle.api.result.UserRegistrationResult;
import com.nemator.needle.api.result.UserTaskResult;
import com.nemator.needle.api.result.UsersTaskResult;
import com.nemator.needle.models.vo.HaystackUserVO;
import com.nemator.needle.models.vo.HaystackVO;
import com.nemator.needle.models.vo.UserVO;
import com.nemator.needle.tasks.TaskResult;
import com.nemator.needle.api.result.HaystackTaskResult;
import com.nemator.needle.api.result.LoginTaskResult;
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

    public void registerUser(UserVO userVO, Callback<UserRegistrationResult> callBack){
        Call<UserRegistrationResult> call = client.registerUser(userVO.getLoginType(), userVO.getEmail(), userVO.getUserName(),
                userVO.getPassword(), userVO.getGcmRegId(), userVO.getPictureURL(), userVO.getCoverPictureURL(), userVO.getSocialNetworkUserId());
        call.enqueue(callBack);
    }

    public void login(int loginType, String email, String username, String gcmRegId, String password, String socialNetworkUserId, Callback<LoginTaskResult> callBack){
        Call<LoginTaskResult> call = client.logIn(loginType, email, username, gcmRegId, password, socialNetworkUserId);
        call.enqueue(callBack);
    }

    public void logout(Callback<TaskResult> callBack){
        Call<TaskResult> call = client.logOut();
        call.enqueue(callBack);
    }

    public void fetchHaystacks(int userId,  Callback<HaystackTaskResult> callBack) {
        Call<HaystackTaskResult> call = client.getHaystacks(userId);
        call.enqueue(callBack);
    }

    public void createHaystack(HaystackVO vo,  Callback<HaystackTaskResult> callBack){
        Call<HaystackTaskResult> call = client.createHaystack(vo.getName(),
                vo.getOwner(), vo.getIsPublic(), vo.getZoneRadius(), vo.getIsCircle(), vo.getPosition().getLongitude(),
                vo.getPosition().getLatitude(), vo.getPictureURL(), vo.getTimeLimit(), vo.getUserIds());
        call.enqueue(callBack);
    }

    public void fetchLocationSharings(int userId,  Callback<LocationSharingTaskResult> callBack) {
        Call<LocationSharingTaskResult> call = client.getLocationSharings(String.valueOf(userId));
        call.enqueue(callBack);
    }

    public void fetchAllUsers(int userId,  Callback<UsersTaskResult> callBack) {
        Call<UsersTaskResult> call = client.fetchAllUsers(String.valueOf(userId));
        call.enqueue(callBack);
    }

    public void updateLocation(UserVO user, Callback<UserTaskResult> callBack) {
        Call<UserTaskResult> call = client.updateLocation(user);
        call.enqueue(callBack);
    }

    public void retrieveHaystackUserLocations(int haystackId, Callback<UsersTaskResult> callBack){
        Call<UsersTaskResult> call = client.retrieveHaystackLocations(haystackId);
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

    public void fetchHaystackUsers(UserVO user, HaystackVO haystack, Callback<UsersTaskResult> callBack){
        Call<UsersTaskResult> call = client.fetchHaystackUsers(user.getId(), haystack.getId());
        call.enqueue(callBack);
    }

    public void addUsersToHaystack(HaystackVO haystack, Callback<HaystackTaskResult> callback) {
        Call<HaystackTaskResult> call = client.addUsersToHaystack(haystack);
        call.enqueue(callback);
    }

    public void fetchUsersNotInHaystack(int haystackId, Callback<UsersTaskResult> callback) {
        Call<UsersTaskResult> call = client.fetchUsersNotInHaystack(haystackId);
        call.enqueue(callback);
    }
}
