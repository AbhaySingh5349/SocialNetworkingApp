package com.example.instagramclone.model;

public class ShowUsersModelClass {

    private String ProfileName, UserName, UserId;

    public String getProfileName() {
        return ProfileName;
    }

    public void setProfileName(String profileName) {
        ProfileName = profileName;
    }

    public String getUserName() {
        return UserName;
    }

    public void setUserName(String userName) {
        UserName = userName;
    }

    public String getUserId() {
        return UserId;
    }

    public void setUserId(String userId) {
        UserId = userId;
    }

    public ShowUsersModelClass(String profileName, String userName, String userId) {
        ProfileName = profileName;
        UserName = userName;
        UserId = userId;
    }

    public ShowUsersModelClass() {
    }
}
