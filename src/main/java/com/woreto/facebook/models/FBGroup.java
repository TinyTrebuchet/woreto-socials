package com.woreto.facebook.models;

public class FBGroup {
    private String id;
    private String name;
    private String managerId;
    private Long lastPosted;

    public FBGroup(String id, String name, String managerId, Long lastPosted) {
        this.id = id;
        this.name = name;
        this.managerId = managerId;
        this.lastPosted = lastPosted;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getManagerId() {
        return managerId;
    }

    public void setManagerId(String managerId) {
        this.managerId = managerId;
    }

    public Long getLastPosted() {
        return lastPosted;
    }

    public void setLastPosted(long lastPosted) {
        this.lastPosted = lastPosted;
    }
}
