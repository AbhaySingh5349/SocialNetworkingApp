package com.example.instagramclone.model;

public class HashTagModelClass {

    String PostHashTag, PostId;

    public String getPostHashTag() {
        return PostHashTag;
    }

    public void setPostHashTag(String postHashTag) {
        PostHashTag = postHashTag;
    }

    public String getPostId() {
        return PostId;
    }

    public void setPostId(String postId) {
        PostId = postId;
    }

    public HashTagModelClass(String postHashTag, String postId) {
        PostHashTag = postHashTag;
        PostId = postId;
    }

    public HashTagModelClass() {
    }
}
