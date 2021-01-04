package com.example.instagramclone.model;

public class NotificationModelClass {

    private String NotificationId, NotificationSenderId, NotificationSenderProfileName, NotificationText, NotificationPostId, NotificationPostType, NotificationDate, NotificationTime, isPost;

    public String getNotificationId() {
        return NotificationId;
    }

    public void setNotificationId(String notificationId) {
        NotificationId = notificationId;
    }

    public String getNotificationSenderId() {
        return NotificationSenderId;
    }

    public void setNotificationSenderId(String notificationSenderId) {
        NotificationSenderId = notificationSenderId;
    }

    public String getNotificationSenderProfileName() {
        return NotificationSenderProfileName;
    }

    public void setNotificationSenderProfileName(String notificationSenderProfileName) {
        NotificationSenderProfileName = notificationSenderProfileName;
    }

    public String getNotificationText() {
        return NotificationText;
    }

    public void setNotificationText(String notificationText) {
        NotificationText = notificationText;
    }

    public String getNotificationPostId() {
        return NotificationPostId;
    }

    public void setNotificationPostId(String notificationPostId) {
        NotificationPostId = notificationPostId;
    }

    public String getNotificationPostType() {
        return NotificationPostType;
    }

    public void setNotificationPostType(String notificationPostType) {
        NotificationPostType = notificationPostType;
    }

    public String getNotificationDate() {
        return NotificationDate;
    }

    public void setNotificationDate(String notificationDate) {
        NotificationDate = notificationDate;
    }

    public String getNotificationTime() {
        return NotificationTime;
    }

    public void setNotificationTime(String notificationTime) {
        NotificationTime = notificationTime;
    }

    public String getIsPost() {
        return isPost;
    }

    public void setIsPost(String isPost) {
        this.isPost = isPost;
    }

    public NotificationModelClass(String notificationId, String notificationSenderId, String notificationSenderProfileName, String notificationText, String notificationPostId, String notificationPostType, String notificationDate, String notificationTime, String isPost) {
        NotificationId = notificationId;
        NotificationSenderId = notificationSenderId;
        NotificationSenderProfileName = notificationSenderProfileName;
        NotificationText = notificationText;
        NotificationPostId = notificationPostId;
        NotificationPostType = notificationPostType;
        NotificationDate = notificationDate;
        NotificationTime = notificationTime;
        this.isPost = isPost;
    }

    public NotificationModelClass() {
    }
}
