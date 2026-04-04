package com.sovon9.activity_service.dto;

public class ActivityDto {
    private Long activityId;
    private String title;
    private int activityStatusId;
    private String activityStatusDesc;

    public ActivityDto() {
    }

    public ActivityDto(Long activityId, String title, int activityStatusId, String activityStatusDesc) {
        this.activityId = activityId;
        this.title = title;
        this.activityStatusId = activityStatusId;
        this.activityStatusDesc = activityStatusDesc;
    }

    public Long getId() {
        return activityId;
    }

    public void setId(Long activityId) {
        this.activityId = activityId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getActivityStatusId() {
        return activityStatusId;
    }

    public void setActivityStatusId(int activityStatusId) {
        this.activityStatusId = activityStatusId;
    }

    public String getActivityStatusDesc() {
        return activityStatusDesc;
    }

    public void setActivityStatusDesc(String activityStatusDesc) {
        this.activityStatusDesc = activityStatusDesc;
    }
}