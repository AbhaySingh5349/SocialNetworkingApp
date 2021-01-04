package com.example.instagramclone.model;

public class StoryModelClass {

    private String StoryPublisherId, ProfileName, StoryValue, StoryId, StoryDescription, StoryDate, StoryType;
    private long StoryTimeStart, StoryTimeEnd, StoryTimeStamp;

    public String getStoryPublisherId() {
        return StoryPublisherId;
    }

    public void setStoryPublisherId(String storyPublisherId) {
        StoryPublisherId = storyPublisherId;
    }

    public String getProfileName() {
        return ProfileName;
    }

    public void setProfileName(String profileName) {
        ProfileName = profileName;
    }

    public String getStoryValue() {
        return StoryValue;
    }

    public void setStoryValue(String storyValue) {
        StoryValue = storyValue;
    }

    public String getStoryId() {
        return StoryId;
    }

    public void setStoryId(String storyId) {
        StoryId = storyId;
    }

    public String getStoryDescription() {
        return StoryDescription;
    }

    public void setStoryDescription(String storyDescription) {
        StoryDescription = storyDescription;
    }

    public String getStoryDate() {
        return StoryDate;
    }

    public void setStoryDate(String storyDate) {
        StoryDate = storyDate;
    }

    public String getStoryType() {
        return StoryType;
    }

    public void setStoryType(String storyType) {
        StoryType = storyType;
    }

    public long getStoryTimeStart() {
        return StoryTimeStart;
    }

    public void setStoryTimeStart(long storyTimeStart) {
        StoryTimeStart = storyTimeStart;
    }

    public long getStoryTimeEnd() {
        return StoryTimeEnd;
    }

    public void setStoryTimeEnd(long storyTimeEnd) {
        StoryTimeEnd = storyTimeEnd;
    }

    public long getStoryTimeStamp() {
        return StoryTimeStamp;
    }

    public void setStoryTimeStamp(long storyTimeStamp) {
        StoryTimeStamp = storyTimeStamp;
    }

    public StoryModelClass(String storyPublisherId, String profileName, String storyValue, String storyId, String storyDescription, String storyDate, String storyType, long storyTimeStart, long storyTimeEnd, long storyTimeStamp) {
        StoryPublisherId = storyPublisherId;
        ProfileName = profileName;
        StoryValue = storyValue;
        StoryId = storyId;
        StoryDescription = storyDescription;
        StoryDate = storyDate;
        StoryType = storyType;
        StoryTimeStart = storyTimeStart;
        StoryTimeEnd = storyTimeEnd;
        StoryTimeStamp = storyTimeStamp;
    }

    public StoryModelClass() {
    }
}