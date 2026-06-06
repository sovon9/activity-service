package com.sovon9.activity_service.dto;

import com.sovon9.activity_service.entities.ActivityStatus;
import com.sovon9.activity_service.entities.ActivityType;

import java.time.LocalDateTime;
import java.util.Objects;

public class ActivityDto {
    private Long activityId;
    private ActivityStatus status;
    private ActivityType type;
    private String title;
    private LocalDateTime createdAt;
    private Long productionUnitId;
    private Long processOrderId;

    public ActivityDto() {
    }

    /**
     *
     * @param activityId
     * @param status
     * @param type
     * @param title
     * @param createdAt
     * @param productionUnitId
     * @param processOrderId
     */
    public ActivityDto(Long activityId, ActivityStatus status, ActivityType type, String title, LocalDateTime createdAt, Long productionUnitId, Long processOrderId) {
        this.activityId = activityId;
        this.status = status;
        this.type = type;
        this.title = title;
        this.createdAt = createdAt;
        this.productionUnitId = productionUnitId;
        this.processOrderId = processOrderId;
    }

    public Long getActivityId() {
        return activityId;
    }

    public void setActivityId(Long activityId) {
        this.activityId = activityId;
    }

    public ActivityStatus getStatus() {
        return status;
    }

    public void setStatus(ActivityStatus status) {
        this.status = status;
    }

    public ActivityType getType() {
        return type;
    }

    public void setType(ActivityType type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Long getProductionUnitId() {
        return productionUnitId;
    }

    public void setProductionUnitId(Long productionUnitId) {
        this.productionUnitId = productionUnitId;
    }

    public Long getProcessOrderId() {
        return processOrderId;
    }

    public void setProcessOrderId(Long processOrderId) {
        this.processOrderId = processOrderId;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        ActivityDto that = (ActivityDto) o;
        return Objects.equals(activityId, that.activityId) && Objects.equals(status, that.status) && Objects.equals(type, that.type) && Objects.equals(title, that.title) && Objects.equals(createdAt, that.createdAt) && Objects.equals(productionUnitId, that.productionUnitId) && Objects.equals(processOrderId, that.processOrderId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(activityId, status, type, title, createdAt, productionUnitId, processOrderId);
    }
}