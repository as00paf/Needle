package com.nemator.needle.api;

import com.nemator.needle.api.result.HaystackResult;
import com.nemator.needle.api.result.LocationSharingResult;
import com.nemator.needle.api.result.LoginResult;
import com.nemator.needle.api.result.UserRegistrationResult;
import com.nemator.needle.api.result.UserResult;
import com.nemator.needle.api.result.UsersResult;
import com.nemator.needle.models.vo.HaystackUserVO;
import com.nemator.needle.models.vo.HaystackVO;
import com.nemator.needle.models.vo.LocationSharingVO;
import com.nemator.needle.models.vo.UserVO;
import com.nemator.needle.api.result.TaskResult;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.HTTP;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface NeedleApiClient {

    //User
    @FormUrlEncoded
    @POST("user.php")
    Call<UserRegistrationResult> registerUser(@Field("loginType") int loginType, @Field("email") String email,
                              @Field("userName") String userName, @Field("password") String password,
                              @Field("gcmRegId") String gcmRegId,  @Field("pictureURL") String pictureURL,
                              @Field("coverPictureURL") String coverPictureURL, @Field("socialNetworkUserId") String socialNetworkUserId);

    @GET("user.php")
    Call<UserVO> getUser(@Path("userId") int userId);

    @FormUrlEncoded
    @PUT("user.php")
    Call<TaskResult> updateGCMRegId(@Field("userId") int userId, @Field("gcmRegId") String gcmRegId);

    //Authentication
    @FormUrlEncoded
    @POST("login.php")
    Call<LoginResult> logIn(@Field("type") int loginType, @Field("email") String email, @Field("username") String username,
                                @Field("regId") String gcmRegId, @Field("password") String password, @Field("socialNetworkUserId") String socialNetworkUserId);

    @FormUrlEncoded
    @POST("login.php")
    Call<LoginResult> socialLogIn(@Field("loginType") int loginType, @Field("email") String email,
                                      @Field("gcmRegId") String gcmRegId, @Field("socialNetworkUserId")
                                      String socialNetworkUserId);

    @GET("logout.php")
    Call<TaskResult> logOut();

    //Haystacks
    @GET("haystack.php")
    Call<HaystackResult> getHaystacks(@Query("userId") int userId);

    @FormUrlEncoded
    @POST("haystack.php")
    Call<HaystackResult> createHaystack(@Field("name") String name, @Field("owner") int owner,
                              @Field("isPublic") Boolean isPublic ,@Field("zoneRadius") int zoneRadius,
                              @Field("isCircle") Boolean isCircle,  @Field("lat") double lat,
                              @Field("lng") double lng, @Field("pictureURL")  String pictureURL,
                              @Field("timeLimit") String timeLimit, @Field("haystack_user[]") List<Integer> haystackUsers);

    //Users
    //TODO : replace by better use of user.php
    @GET("retrieveAllUsers.php")
    Call<UsersResult> fetchAllUsers(@Query("userId") String userId);

    //Location
    @PUT("location.php")
    Call<UserResult> updateLocation(@Body UserVO vo);

    @GET("location.php")
    Call<UsersResult> retrieveHaystackLocations(@Query("haystackId") int haystackId);

    @GET("location.php")
    Call<UserResult> retrieveUserLocation(@Query("locationSharingId") int locationSharingId, @Query("userId") int userId);

    //HaystackUser
    @PUT("haystackUser.php")
    Call<TaskResult> toggleUserActivation(@Body HaystackUserVO vo);

    @HTTP(method = "DELETE", path = "haystackUser.php", hasBody = true)
    Call<TaskResult> leaveHaystack(@Body HaystackUserVO vo);

    @GET("haystackUser.php")
    Call<UsersResult> fetchHaystackUsers(@Query("userId") int userId, @Query("haystackId") int haystackId);

    @POST("haystackUser.php")
    Call<HaystackResult> addUsersToHaystack(@Body HaystackVO haystack);

    @GET("fetchUsersNotInHaystack.php")
    Call<UsersResult> fetchUsersNotInHaystack(@Query("haystackId") int haystackId);

    //LocationSharing
    @GET("locationSharing.php")
    Call<LocationSharingResult> getLocationSharings(@Query("userId") int userId);

    @POST("locationSharing.php")
    Call<LocationSharingResult> createLocationSharing(@Body LocationSharingVO locationSharingVO);

    @PUT("locationSharing.php")
    Call<LocationSharingResult> shareLocationBack(@Body LocationSharingVO vo);

    @HTTP(method = "DELETE", path = "locationSharing.php", hasBody = true)
    Call<LocationSharingResult> cancelLocationSharing(@Body LocationSharingVO vo);
}
