package com.woreto.facebook.models;

import java.util.List;

public class FBPage {

    private String id;
    private String name;
    private List<String> keywords;
    private Long lastPosted;
    private String managerId;
    private List<String> groupsToShare;

    public FBPage(String id, String name, List<String> keywords, Long lastPosted, String managerId,
                  List<String> groupsToShare) {
        this.id = id;
        this.name = name;
        this.keywords = keywords;
        this.lastPosted = lastPosted;
        this.managerId = managerId;
        this.groupsToShare = groupsToShare;
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

    public List<String> getKeywords() {
        return keywords;
    }

    public void setKeywords(List<String> keywords) {
        this.keywords = keywords;
    }

    public Long getLastPosted() {
        return lastPosted;
    }

    public void setLastPosted(Long lastPosted) {
        this.lastPosted = lastPosted;
    }

    public String getManagerId() {
        return managerId;
    }

    public void setManagerId(String managerId) {
        this.managerId = managerId;
    }

    public List<String> getGroupsToShare() {
        return groupsToShare;
    }

    public void setGroupsToShare(List<String> groupsToShare) {
        this.groupsToShare = groupsToShare;
    }
}
