package com.nemator.needle.api;

import android.graphics.Bitmap;

import com.nemator.needle.api.result.FriendshipResult;
import com.nemator.needle.api.result.FriendsResult;
import com.nemator.needle.api.result.HaystackResult;
import com.nemator.needle.api.result.ImageResult;
import com.nemator.needle.api.result.NeedleResult;
import com.nemator.needle.api.result.LoginResult;
import com.nemator.needle.api.result.NotificationResult;
import com.nemator.needle.api.result.PinResult;
import com.nemator.needle.api.result.PinsResult;
import com.nemator.needle.api.result.UserRegistrationResult;
import com.nemator.needle.api.result.UserResult;
import com.nemator.needle.api.result.UsersResult;
import com.nemator.needle.models.vo.FriendshipVO;
import com.nemator.needle.models.vo.HaystackUserVO;
import com.nemator.needle.models.vo.HaystackVO;
import com.nemator.needle.models.vo.ImageVO;
import com.nemator.needle.models.vo.NeedleVO;
import com.nemator.needle.models.vo.NotificationVO;
import com.nemator.needle.models.vo.PinVO;
import com.nemator.needle.models.vo.UserVO;
import com.nemator.needle.api.result.TaskResult;

import java.util.ArrayList;

import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.HTTP;
import retrofit2.http.PATCH;
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
    Call<UserVO> getUser(@Path("id") int userId);

    @GET("user.php")
    Call<UserResult> getUserById(@Query("callerId") int callerId, @Query("id") int userId);

    @GET("user.php")
    Call<UsersResult> fetchAllUsers(@Query("except") int userId);

    @FormUrlEncoded
    @PUT("user.php")
    Call<TaskResult> updateGCMRegId(@Field("userId") int userId, @Field("gcmRegId") String gcmRegId);

    @PUT("user.php")
    Call<ImageResult> updatePicture(@Body ImageVO imageVO);

    //Authentication
    @POST("login.php")
    Call<LoginResult> logIn(@Body UserVO user);

    @GET("login.php")
    Call<LoginResult> getUserInfos(@Query("userId") int userId);

    @GET("logout.php")
    Call<TaskResult> logOut();

    //Haystacks
    @GET("haystack.php")
    Call<HaystackResult> getHaystacks(@Query("userId") int userId);

    @POST("haystack.php")
    Call<HaystackResult> createHaystack(@Body HaystackVO haystack);

    @HTTP(method = "DELETE", path = "haystack.php", hasBody = true)
    Call<TaskResult> cancelHaystack(@Body HaystackUserVO vo);

    //Pins
    @GET("pin.php")
    Call<PinsResult> getHaystackPins(@Query("haystackId") int haystackId);

    @POST("pin.php")
    Call<PinResult> createPin(@Body PinVO pin);

    //Location
    @PUT("location.php")
    Call<UserResult> updateLocation(@Body UserVO vo);

    @GET("location.php")
    Call<UsersResult> retrieveHaystackLocations(@Query("haystackId") int haystackId);

    @GET("location.php")
    Call<UserResult> retrieveUserLocation(@Query("locationSharingId") int locationSharingId, @Query("userId") int userId, @Query("who") String who);

    //HaystackUser
    @PUT("haystackUser.php")
    Call<TaskResult> toggleUserActivation(@Body HaystackUserVO vo);

    @HTTP(method = "DELETE", path = "haystackUser.php", hasBody = true)
    Call<TaskResult> leaveHaystack(@Body HaystackUserVO vo);

    @GET("haystackUser.php")
    Call<UsersResult> fetchHaystackUsers(@Query("userId") int userId, @Query("haystackId") int haystackId);

    @POST("haystackUser.php")
    Call<HaystackResult> addUsersToHaystack(@Body HaystackVO haystack);

    //LocationSharing
    @GET("locationSharing.php")
    Call<NeedleResult> getNeedles(@Query("userId") int userId);

    @POST("locationSharing.php")
    Call<NeedleResult> createNeedle(@Body NeedleVO needleVO);

    @PUT("locationSharing.php")
    Call<NeedleResult> shareLocationBack(@Body NeedleVO vo);

    @HTTP(method = "DELETE", path = "locationSharing.php", hasBody = true)
    Call<NeedleResult> cancelNeedle(@Body NeedleVO vo);

    //Notifications
    @GET("notification.php")
    Call<NotificationResult> fetchNotifications(@Query("userId") int userId);

    @PUT("notification.php")
    Call<NotificationResult> seenNotifications(@Body ArrayList<NotificationVO> notifications);

    //Friends
    @GET("friends.php")
    Call<FriendsResult> getFriends(@Query("userId") int userId);

    @GET("friends.php")
    Call<FriendsResult> getPotentialFriends(@Query("exceptions") int... exceptions);

    @GET("friends.php")
    Call<FriendshipResult> getFriendship(@Query("userId") int userId, @Query("friendId") int friendId);

    @GET("friends.php")
    Call<UsersResult> getFriendsNotInHaystack(@Query("userId") int userId, @Query("haystackId") int haystackId);

    @POST("friends.php")
    Call<FriendshipResult> sendFriendRequest(@Body FriendshipVO friendshipVO);

    @PUT("friends.php")
    Call<FriendshipResult> acceptFriendRequest(@Body FriendshipVO friendshipVO);

    @PUT("friends.php")
    Call<FriendshipResult> rejectFriendRequest(@Body FriendshipVO friendshipVO);

    @HTTP(method = "DELETE", path = "friends.php", hasBody = true)
    Call<TaskResult> unFriend(@Body FriendshipVO friendshipVO);

    @HTTP(method = "DELETE", path = "friends.php", hasBody = true)
    Call<TaskResult> cancelFriendRequest(@Body FriendshipVO friendshipVO);
}
