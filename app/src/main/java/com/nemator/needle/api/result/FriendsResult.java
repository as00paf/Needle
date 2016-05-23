package com.nemator.needle.api.result;

import com.google.gson.annotations.SerializedName;
import com.nemator.needle.models.vo.FriendshipVO;
import com.nemator.needle.models.vo.UserVO;

import java.util.ArrayList;

public class FriendsResult extends TaskResult{
    @SerializedName("friends")
    private ArrayList<UserVO> friends;

    @SerializedName("receivedFriendRequests")
    private ArrayList<UserVO> receivedFriendRequests;

    @SerializedName("sentFriendRequests")
    private ArrayList<UserVO> sentFriendRequests;

    public FriendsResult(int successCode, String message) {
        super(successCode, message);
    }

    public FriendsResult() {
    }

    public FriendsResult(int successCode, String message, ArrayList<UserVO> friends) {
        super(successCode, message);
        this.friends = friends;
    }

    public FriendsResult(int successCode, String message, ArrayList<UserVO> friends, ArrayList<UserVO> receivedFriendRequests, ArrayList<UserVO> sentFriendRequests) {
        super(successCode, message);
        this.friends = friends;
        this.receivedFriendRequests = receivedFriendRequests;
        this.sentFriendRequests = sentFriendRequests;
    }

    public ArrayList<UserVO> getFriends() {
        return friends;
    }

    public void setFriends(ArrayList<UserVO> friends) {
        this.friends = friends;
    }

    public ArrayList<UserVO> getReceivedFriendRequests() {
        return receivedFriendRequests;
    }

    public void setReceivedFriendRequests(ArrayList<UserVO> receivedFriendRequests) {
        this.receivedFriendRequests = receivedFriendRequests;
    }

    public ArrayList<UserVO> getSentFriendRequests() {
        return sentFriendRequests;
    }

    public void setSentFriendRequests(ArrayList<UserVO> sentFriendRequests) {
        this.sentFriendRequests = sentFriendRequests;
    }
}
