package com.nemator.needle.api.result;

import com.google.gson.annotations.SerializedName;
import com.nemator.needle.models.vo.NotificationVO;
import com.nemator.needle.models.vo.UserVO;

import java.util.ArrayList;
import java.util.List;

public class NotificationResult extends TaskResult{
    @SerializedName("notification")
    private NotificationVO notification;

    @SerializedName("notifications")
    private ArrayList<NotificationVO> notifications;

    public NotificationResult(int successCode, String message, NotificationVO notification) {
        super(successCode, message);
        this.notification = notification;
    }

    public NotificationResult(int successCode, String message, ArrayList<NotificationVO> notifications) {
        super(successCode, message);
        this.notifications = notifications;
    }

    public NotificationResult() {
    }

    public NotificationVO getNotification() {
        return notification;
    }

    public void seNotification(NotificationVO user) {
        this.notification = user;
    }

    public ArrayList<NotificationVO> getNotifications() {
        return notifications;
    }

    public void setNotifications(ArrayList<NotificationVO> notifications) {
        this.notifications = notifications;
    }
}
