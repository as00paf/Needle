package com.needletest.pafoid.needletest.haystack.task;

public class RetrieveLocationsParams {
    public String userName, userId, haystackId;

    public RetrieveLocationsParams(String userName, String userId, String haystackId){
        this.userName = userName;
        this.userId = userId;
        this.haystackId = haystackId;
    }
}
