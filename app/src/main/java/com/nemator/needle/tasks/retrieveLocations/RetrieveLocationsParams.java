package com.nemator.needle.tasks.retrieveLocations;

public class RetrieveLocationsParams {
    public String userName, userId, haystackId;
    public Boolean verbose;

    public RetrieveLocationsParams(String userName, String userId, String haystackId, Boolean verbose){
        this.userName = userName;
        this.userId = userId;
        this.haystackId = haystackId;
        this.verbose = verbose;
    }
}
