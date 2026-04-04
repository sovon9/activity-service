package com.sovon9.activity_service.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "activity_status")
public class ActivityStatus {

    @Id
    @Column(name = "activity_status_id")
    private int activityStatusId;
    @Column(name = "activity_status_desc")
    private String activityStatusDesc;

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
