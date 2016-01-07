package com.nemator.needle.api;

import android.util.Log;

import com.nemator.needle.models.vo.HaystackVO;
import com.nemator.needle.models.vo.UserVO;
import com.nemator.needle.tasks.haystack.HaystackTaskResult;
import com.nemator.needle.tasks.login.LoginTaskResult;
import com.nemator.needle.utils.AppConstants;
import com.squareup.okhttp.Interceptor;
import com.squareup.okhttp.ResponseBody;

import java.io.IOException;
import java.util.List;

import retrofit.Call;
import retrofit.Callback;
import retrofit.GsonConverterFactory;
import retrofit.Retrofit;

/**
 * Created by Alex on 06/10/2015.
 */
public class ApiClient {

    private static ApiClient instance;

    private NeedleApiClient client;

    public ApiClient() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(AppConstants.PROJECT_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        client = retrofit.create(NeedleApiClient.class);

        retrofit.client().interceptors().add(new Interceptor() {
            @Override
            public com.squareup.okhttp.Response intercept(Chain chain) throws IOException {
                com.squareup.okhttp.Response response = chain.proceed(chain.request());
                String content = response.body().string();
                Log.d("interceptor", "intercepted res = " + content);
                return response.newBuilder()
                        .body(ResponseBody.create(response.body().contentType(), content))
                        .build();
            }
        });
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

    public void fetchHaystacks(int userId,  Callback<HaystackTaskResult> callBack) {
        Call<HaystackTaskResult> call = client.getHaystacks(String.valueOf(userId));
        call.enqueue(callBack);
    }

    public void createHaystack(HaystackVO vo,  Callback<HaystackTaskResult> callBack){
        Call<HaystackTaskResult> call = client.createHaystack(vo.getName(),
                vo.getOwner(), vo.getIsPublic(), vo.getZoneRadius(), vo.getIsCircle(), vo.getPosition().latitude,
                vo.getPosition().longitude, vo.getPictureURL(), vo.getTimeLimit(), vo.getUserIds());
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
}
