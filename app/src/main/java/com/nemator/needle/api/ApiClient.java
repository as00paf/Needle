package com.nemator.needle.api;

import com.nemator.needle.models.vo.UserVO;
import com.nemator.needle.tasks.login.LoginTaskResult;
import com.nemator.needle.utils.AppConstants;

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
    }

    public static ApiClient getInstance() {
        if(instance == null){
            instance = new ApiClient();
        }

        return instance;
    }

    public void registerUser(UserVO userVO, Callback<UserVO> callBack){
        Call<UserVO> call = client.registerUser(userVO.getLoginType(), userVO.getEmail(), userVO.getGcmRegId());
        call.enqueue(callBack);
    }

    public void login(int loginType, String email, String gcmRegId, String password, Callback<LoginTaskResult> callBack){
        Call<LoginTaskResult> call = client.logIn(loginType, email, gcmRegId, password);
        call.enqueue(callBack);
    }

}
