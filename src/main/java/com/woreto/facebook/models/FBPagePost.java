package com.woreto.facebook.models;

import java.util.ArrayList;
import java.util.List;

public class FBPagePost {
    private String storyId;
    private String actorId;
    private String url;
    private List<String> sharedWith;
    private Long createdTime;
    private Long modifiedTime;

    public FBPagePost(String storyId, String actorId, String url) {
        this.storyId = storyId;
        this.actorId = actorId;
        this.url = url;
        this.sharedWith = new ArrayList<>();
        this.createdTime = this.modifiedTime = System.currentTimeMillis();
    }

    public FBPagePost(String storyId, String actorId, String url, List<String> sharedWith, Long createdTime, Long modifiedTime) {
        this.storyId = storyId;
        this.actorId = actorId;
        this.url = url;
        this.sharedWith = sharedWith;
        this.createdTime = createdTime;
        this.modifiedTime = modifiedTime;
    }

    public String getStoryId() {
        return storyId;
    }

    public void setStoryId(String storyId) {
        this.storyId = storyId;
    }

    public String getActorId() {
        return actorId;
    }

    public void setActorId(String actorId) {
        this.actorId = actorId;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public List<String> getSharedWith() {
        return sharedWith;
    }

    public void setSharedWith(List<String> sharedWith) {
        this.sharedWith = sharedWith;
    }

    public Long getModifiedTime() {
        return modifiedTime;
    }

    public void setModifiedTime(Long modifiedTime) {
        this.modifiedTime = modifiedTime;
    }

    public Long getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Long createdTime) {
        this.createdTime = createdTime;
    }

    @Override
    public String toString() {
        return "FBPagePost {"
                + "storyId=" + storyId
                + ",actorId=" + actorId
                + ",url=" + url
                + ",sharedWith=" + sharedWith
                + ",createdTime=" + createdTime
                + ",modifiedTime=" + modifiedTime
                + "}";
    }
}
