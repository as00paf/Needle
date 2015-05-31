package com.nemator.needle.models.vo;


import com.google.android.gms.maps.model.LatLng;

public class LocationSharingVO {
    public int id;
    public String senderName;
    public int senderId;
    public String receiverName;
    public int receiverId;
    public LatLng location;
    public String timeLimit;
    public String pictureURL;
}
