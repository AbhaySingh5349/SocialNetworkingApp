package com.example.instagramclone.model;

public class SearchModelClass {

    String UserName, ProfileName, Bio, UserId;

    public String getUserName() {
        return UserName;
    }

    public void setUserName(String userName) {
        UserName = userName;
    }

    public String getProfileName() {
        return ProfileName;
    }

    public void setProfileName(String profileName) {
        ProfileName = profileName;
    }

    public String getBio() {
        return Bio;
    }

    public void setBio(String bio) {
        Bio = bio;
    }

    public String getUserId() {
        return UserId;
    }

    public void setUserId(String userId) {
        UserId = userId;
    }

    public SearchModelClass(String userName, String profileName, String bio, String userId) {
        UserName = userName;
        ProfileName = profileName;
        Bio = bio;
        UserId = userId;
    }

    public SearchModelClass() {
    }
}
