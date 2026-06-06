package com.sovon9.activity_service.entities;

import com.sovon9.activity_service.util.GlobalUtil;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "activity")
public class Activity implements Node{

    @Transient
    private String id;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long activityId;

    @Column(name = "activity_status_id")
    private int activityStatusId;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name = "activity_status_id", insertable=false, updatable=false)
    private ActivityStatus status;

    @Column(name = "activity_type_id")
    private int activityTypeId;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name = "activity_type_id", insertable=false, updatable=false)
    private ActivityType type;

    @Column
    private String title;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "production_unit_id")
    private Long productionUnitId;

    @Column(name = "process_order_id")
    private Long processOrderId;

//    @OneToMany(mappedBy = "activity", fetch = FetchType.LAZY)
//    private List<Variable> variables;


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

//    public List<Variable> getVariables() {
//        return variables;
//    }
//
//    public void setVariables(List<Variable> variables) {
//        this.variables = variables;
//    }

    public ActivityType getType() {
        return type;
    }

    public void setType(ActivityType type) {
        this.type = type;
    }

    public int getActivityStatusId() {
        return activityStatusId;
    }

    public void setActivityStatusId(int activityStatusId) {
        this.activityStatusId = activityStatusId;
    }

    public int getActivityTypeId() {
        return activityTypeId;
    }

    public void setActivityTypeId(int activityTypeId) {
        this.activityTypeId = activityTypeId;
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
    public String getId() {
        return id;
    }
    @Override
    public void setId(String id) {
        this.id = id;
    }
    @PostLoad
    public void postLoad() {
        this.id = GlobalUtil.toGlobalId("Activity", activityId);
    }
}
