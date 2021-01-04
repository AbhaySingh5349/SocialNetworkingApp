package com.example.instagramclone.model;

import java.util.ArrayList;

public class PostsInfoModelClass {

    private String PostDescription, ProfileName, PostId, PostValue, PostPublisherId, PostTime, PostDate, PostType;
    private long PostTimeStamp;

    public String getPostDescription() {
        return PostDescription;
    }

    public void setPostDescription(String postDescription) {
        PostDescription = postDescription;
    }

    public String getProfileName() {
        return ProfileName;
    }

    public void setProfileName(String profileName) {
        ProfileName = profileName;
    }

    public String getPostId() {
        return PostId;
    }

    public void setPostId(String postId) {
        PostId = postId;
    }

    public String getPostValue() {
        return PostValue;
    }

    public void setPostValue(String postValue) {
        PostValue = postValue;
    }

    public String getPostPublisherId() {
        return PostPublisherId;
    }

    public void setPostPublisherId(String postPublisherId) {
        PostPublisherId = postPublisherId;
    }

    public String getPostTime() {
        return PostTime;
    }

    public void setPostTime(String postTime) {
        PostTime = postTime;
    }

    public String getPostDate() {
        return PostDate;
    }

    public void setPostDate(String postDate) {
        PostDate = postDate;
    }

    public String getPostType() {
        return PostType;
    }

    public void setPostType(String postType) {
        PostType = postType;
    }

    public long getPostTimeStamp() {
        return PostTimeStamp;
    }

    public void setPostTimeStamp(long postTimeStamp) {
        PostTimeStamp = postTimeStamp;
    }

    public PostsInfoModelClass(String postDescription, String profileName, String postId, String postValue, String postPublisherId, String postTime, String postDate, String postType, long postTimeStamp) {
        PostDescription = postDescription;
        ProfileName = profileName;
        PostId = postId;
        PostValue = postValue;
        PostPublisherId = postPublisherId;
        PostTime = postTime;
        PostDate = postDate;
        PostType = postType;
        PostTimeStamp = postTimeStamp;
    }

    public PostsInfoModelClass() {
    }
}
