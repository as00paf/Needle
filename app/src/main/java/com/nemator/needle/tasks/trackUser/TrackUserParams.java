package com.nemator.needle.tasks.trackUser;

public class TrackUserParams {
    public String locationSharingId, userId;

    public TrackUserParams(String locationSharingId, String userId){
        this.locationSharingId = locationSharingId;
        this.userId = userId;
    }
}
