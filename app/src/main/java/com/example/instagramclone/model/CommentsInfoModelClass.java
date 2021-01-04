package com.example.instagramclone.model;

public class CommentsInfoModelClass {

    private String CommentDate, CommentId, CommentPublisherId, CommentPublisherName, CommentTime, CommentValue;
    private long CommentTimeStamp;

    public String getCommentDate() {
        return CommentDate;
    }

    public void setCommentDate(String commentDate) {
        CommentDate = commentDate;
    }

    public String getCommentId() {
        return CommentId;
    }

    public void setCommentId(String commentId) {
        CommentId = commentId;
    }

    public String getCommentPublisherId() {
        return CommentPublisherId;
    }

    public void setCommentPublisherId(String commentPublisherId) {
        CommentPublisherId = commentPublisherId;
    }

    public String getCommentPublisherName() {
        return CommentPublisherName;
    }

    public void setCommentPublisherName(String commentPublisherName) {
        CommentPublisherName = commentPublisherName;
    }

    public String getCommentTime() {
        return CommentTime;
    }

    public void setCommentTime(String commentTime) {
        CommentTime = commentTime;
    }

    public String getCommentValue() {
        return CommentValue;
    }

    public void setCommentValue(String commentValue) {
        CommentValue = commentValue;
    }

    public long getCommentTimeStamp() {
        return CommentTimeStamp;
    }

    public void setCommentTimeStamp(long commentTimeStamp) {
        CommentTimeStamp = commentTimeStamp;
    }

    public CommentsInfoModelClass(String commentDate, String commentId, String commentPublisherId, String commentPublisherName, String commentTime, String commentValue, long commentTimeStamp) {
        CommentDate = commentDate;
        CommentId = commentId;
        CommentPublisherId = commentPublisherId;
        CommentPublisherName = commentPublisherName;
        CommentTime = commentTime;
        CommentValue = commentValue;
        CommentTimeStamp = commentTimeStamp;
    }

    public CommentsInfoModelClass() {
    }
}
