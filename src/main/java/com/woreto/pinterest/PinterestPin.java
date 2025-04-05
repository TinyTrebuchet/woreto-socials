package com.woreto.pinterest;

public class PinterestPin {

    private String id;
    private String keyword;
    private Type mediaType;

    private String imageUrl;
    private int imageWidth;
    private int imageHeight;

    private String title;
    private String description;
    private String autoAltText;

    private int reactionCount;
    private int publisherReach;

    private String createdAt;
    private boolean posted;

    public enum Type {
        IMAGE,
        VIDEO
    }

    public PinterestPin() {}

    public PinterestPin(String id, String keyword, Type mediaType, String imageUrl, int imageWidth, int imageHeight,
                        String title, String description, String autoAltText, int reactionCount, int publisherReach,
                        String createdAt, boolean posted) {
        this.id = id;
        this.keyword = keyword;
        this.mediaType = mediaType;
        this.imageUrl = imageUrl;
        this.imageWidth = imageWidth;
        this.imageHeight = imageHeight;
        this.title = title;
        this.description = description;
        this.autoAltText = autoAltText;
        this.reactionCount = reactionCount;
        this.publisherReach = publisherReach;
        this.createdAt = createdAt;
        this.posted = posted;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public Type getMediaType() {
        return mediaType;
    }

    public void setMediaType(Type mediaType) {
        this.mediaType = mediaType;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public int getImageWidth() {
        return imageWidth;
    }

    public void setImageWidth(int imageWidth) {
        this.imageWidth = imageWidth;
    }

    public int getImageHeight() {
        return imageHeight;
    }

    public void setImageHeight(int imageHeight) {
        this.imageHeight = imageHeight;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAutoAltText() {
        return autoAltText;
    }

    public void setAutoAltText(String autoAltText) {
        this.autoAltText = autoAltText;
    }

    public int getReactionCount() {
        return reactionCount;
    }

    public void setReactionCount(int reactionCount) {
        this.reactionCount = reactionCount;
    }

    public int getPublisherReach() {
        return publisherReach;
    }

    public void setPublisherReach(int publisherReach) {
        this.publisherReach = publisherReach;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isPosted() {
        return posted;
    }

    public void setPosted(boolean posted) {
        this.posted = posted;
    }
}
