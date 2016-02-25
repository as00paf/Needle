package com.nemator.needle.api;

import com.nemator.needle.models.vo.HaystackUserVO;
import com.nemator.needle.models.vo.UserVO;
import com.nemator.needle.tasks.TaskResult;
import com.nemator.needle.tasks.haystack.HaystackTaskResult;
import com.nemator.needle.tasks.login.LoginTaskResult;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
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
    Call<LoginTaskResult> logIn(@Field("type") int loginType, @Field("email") String email, @Field("username") String username,
                                @Field("regId") String gcmRegId, @Field("password") String password, @Field("socialNetworkUserId") String socialNetworkUserId);

    @FormUrlEncoded
    @POST("login.php")
    Call<LoginTaskResult> socialLogIn(@Field("loginType") int loginType, @Field("email") String email,
                                      @Field("gcmRegId") String gcmRegId, @Field("socialNetworkUserId")
                                      String socialNetworkUserId);

    @GET("logout.php")
    Call<TaskResult> logOut();

    //Haystacks
    @GET("haystack.php")
    Call<HaystackTaskResult> getHaystacks(@Query("userId") int userId);

    @FormUrlEncoded
    @POST("haystack.php")
    Call<HaystackTaskResult> createHaystack(@Field("name") String name, @Field("owner") int owner,
                              @Field("isPublic") Boolean isPublic ,@Field("zoneRadius") int zoneRadius,
                              @Field("isCircle") Boolean isCircle,  @Field("lat") double lat,
                              @Field("lng") double lng, @Field("pictureURL")  String pictureURL,
                              @Field("timeLimit") String timeLimit, @Field("haystack_user[]") List<Integer> haystackUsers);

    //LocationSharing
    @GET("locationSharing.php")
    Call<LocationSharingTaskResult> getLocationSharings(@Query("userId") String userId);

    //Users
    @GET("retrieveAllUsers.php")
    Call<UsersTaskResult> fetchAllUsers(@Query("userId") String userId);

    //Location
    @PUT("location.php")
    Call<UserTaskResult> updateLocation(@Body UserVO vo);

    @GET("location.php")
    Call<UsersTaskResult> retrieveHaystackLocations(@Query("haystackId") int haystackId);

    //HaystackUser
    @PUT("haystackUser.php")
    Call<TaskResult> toggleUserActivation(@Body HaystackUserVO vo);
}
