package com.nemator.needle.api;

import com.nemator.needle.models.vo.UserVO;
import com.nemator.needle.tasks.TaskResult;
import com.nemator.needle.tasks.login.LoginTaskResult;

import retrofit.Call;
import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.PUT;
import retrofit.http.Path;

public interface NeedleApiClient {

    //User
    @FormUrlEncoded
    @POST("user.php")
    Call<UserVO> registerUser(@Field("loginType") int loginType, @Field("email") String email, @Field("gcmRegId") String gcmRegId);

    @GET("user.php?id={userId}")
    Call<UserVO> getUser(@Path("userId") int userId);

    @FormUrlEncoded
    @PUT("user.php")
    Call<TaskResult> updateGCMRegId(@Field("userId") int userId, @Field("gcmRegId") String gcmRegId);

    //Authentication
    @FormUrlEncoded
    @POST("login.php")
    Call<LoginTaskResult> logIn(@Field("loginType") int loginType, @Field("email") String email, @Field("gcmRegId") String gcmRegId, @Field("password") String password);

    @FormUrlEncoded
    @POST("login.php")
    Call<LoginTaskResult> socialLogIn(@Field("loginType") int loginType, @Field("email") String email, @Field("gcmRegId") String gcmRegId, @Field("socialNetworkUserId") String socialNetworkUserId);


}
