package com.nemator.needle.api.result;

import com.google.gson.annotations.SerializedName;
import com.nemator.needle.models.vo.FriendshipVO;
import com.nemator.needle.models.vo.UserVO;

public class FriendshipResult extends TaskResult{
    @SerializedName("friendship")
    private FriendshipVO friendship;

    @SerializedName("friend")
    private UserVO friend;

    public FriendshipResult(FriendshipVO friendship, UserVO friend) {
        this.friendship = friendship;
        this.friend = friend;
    }

    public FriendshipResult(int successCode, String message, FriendshipVO friendship, UserVO friend) {
        super(successCode, message);
        this.friendship = friendship;
        this.friend = friend;
    }

    //Getters/Setters
    public FriendshipVO getFriendship() {
        return friendship;
    }

    public void setFriendship(FriendshipVO friendship) {
        this.friendship = friendship;
    }

    public UserVO getFriend() {
        return friend;
    }

    public void setFriend(UserVO friend) {
        this.friend = friend;
    }
}
