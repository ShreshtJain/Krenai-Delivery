package com.delivery.fragment.task.entity;

public class HistoryEntity {
    private String siteShotId;
    private String googleLocation;
    private String customLocation;
    private double latitude;
    private double longitude;
    private String status;
    private String timeStamp;
    private String focusImage;
    private String fullImage;

    public String getSiteShotId() {
        return siteShotId;
    }

    public void setSiteShotId(String siteShotId) {
        this.siteShotId = siteShotId;
    }

    public String getGoogleLocation() {
        return googleLocation;
    }

    public void setGoogleLocation(String googleLocation) {
        this.googleLocation = googleLocation;
    }

    public String getCustomLocation() {
        return customLocation;
    }

    public void setCustomLocation(String customLocation) {
        this.customLocation = customLocation;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getFocusImage() {
        return focusImage;
    }

    public void setFocusImage(String focusImage) {
        this.focusImage = focusImage;
    }

    public String getFullImage() {
        return fullImage;
    }

    public void setFullImage(String fullImage) {
        this.fullImage = fullImage;
    }
}
