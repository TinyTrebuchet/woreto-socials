package com.woreto.facebook.models;

import java.util.ArrayList;
import java.util.List;

public class FBPagePost {
    private String storyId;
    private String actorId;
    private String url;
    private List<String> sharedWith = new ArrayList<>();
    private Long createdTime;
    private Long modifiedTime;
    private Type type;

    public enum Type {
        IMAGE,
        REEL
    }

    public FBPagePost(String storyId, Type type) {
        this.storyId = storyId;
        this.type = type;
        this.createdTime = this.modifiedTime = System.currentTimeMillis();
    }

    public FBPagePost(String storyId, String actorId, String url, Type type, List<String> sharedWith, Long createdTime, Long modifiedTime) {
        this.storyId = storyId;
        this.actorId = actorId;
        this.url = url;
        this.type = type;
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

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "FBPagePost {"
                + "storyId=" + storyId
                + ",actorId=" + actorId
                + ",url=" + url
                + ",type=" + type
                + ",sharedWith=" + sharedWith
                + ",createdTime=" + createdTime
                + ",modifiedTime=" + modifiedTime
                + "}";
    }
}
