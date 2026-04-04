//package com.sovon9.activity_service.entities;
//
//import jakarta.persistence.*;
//
//import java.time.LocalDateTime;
//
//@Entity
//@Table(name = "test_value")
//public class TestValue {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    @Column
//    private String value;
//    @Column(name = "recorded_at")
//    private LocalDateTime recordedAt;
//    @Column(name = "recorded_by")
//    private String recordedBy;
//
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "variable_id")
//    private Variable variable;
//
//    public Long getId() {
//        return id;
//    }
//
//    public void setId(Long id) {
//        this.id = id;
//    }
//
//    public String getValue() {
//        return value;
//    }
//
//    public void setValue(String value) {
//        this.value = value;
//    }
//
//    public LocalDateTime getRecordedAt() {
//        return recordedAt;
//    }
//
//    public void setRecordedAt(LocalDateTime recordedAt) {
//        this.recordedAt = recordedAt;
//    }
//
//    public String getRecordedBy() {
//        return recordedBy;
//    }
//
//    public void setRecordedBy(String recordedBy) {
//        this.recordedBy = recordedBy;
//    }
//
//    public Variable getVariable() {
//        return variable;
//    }
//
//    public void setVariable(Variable variable) {
//        this.variable = variable;
//    }
//}
