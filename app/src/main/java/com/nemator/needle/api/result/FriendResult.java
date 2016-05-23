package com.nemator.needle.api.result;

import com.google.gson.annotations.SerializedName;
import com.nemator.needle.models.vo.FriendshipVO;
import com.nemator.needle.models.vo.UserVO;

public class FriendResult extends TaskResult{
    @SerializedName("userId")
    private int userId;

    @SerializedName("friendId")
    private int friendId;

    @SerializedName("status")
    private int status;

    @SerializedName("acceptDate")
    private String acceptDate;

    public FriendResult(int successCode, String message, int userId, int friendId, int status, String acceptDate) {
        super(successCode, message);

        this.userId = userId;
        this.friendId = friendId;
        this.status = status;
        this.acceptDate = acceptDate;
    }

    public FriendResult() {
    }

    //Getters/Setters
    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getFriendId() {
        return friendId;
    }

    public void setFriendId(int friendId) {
        this.friendId = friendId;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getAcceptDate() {
        return acceptDate;
    }

    public void setAcceptDate(String acceptDate) {
        this.acceptDate = acceptDate;
    }

    public FriendshipVO getFriendship(){
        return new FriendshipVO(userId, friendId, status, acceptDate);
    }
}
