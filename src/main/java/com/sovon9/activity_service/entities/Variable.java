//package com.sovon9.activity_service.entities;
//
//import jakarta.persistence.*;
//
//import java.util.List;
//
//@Entity
//@Table(name = "variable")
//public class Variable {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//    @Column
//    private String name;
//    @Column
//    private String unit;
//    @Column
//    private Double specMin;
//    @Column
//    private Double specMax;
//
//    @ManyToOne
//    @JoinColumn(name = "activity_id")
//    private Activity activity;
//
//    @OneToMany(mappedBy = "variable", fetch = FetchType.LAZY)
//    private List<TestValue> testValues;
//
//    public Long getId() {
//        return id;
//    }
//
//    public void setId(Long id) {
//        this.id = id;
//    }
//
//    public String getName() {
//        return name;
//    }
//
//    public void setName(String name) {
//        this.name = name;
//    }
//
//    public String getUnit() {
//        return unit;
//    }
//
//    public void setUnit(String unit) {
//        this.unit = unit;
//    }
//
//    public Double getSpecMin() {
//        return specMin;
//    }
//
//    public void setSpecMin(Double specMin) {
//        this.specMin = specMin;
//    }
//
//    public Double getSpecMax() {
//        return specMax;
//    }
//
//    public void setSpecMax(Double specMax) {
//        this.specMax = specMax;
//    }
//
//    public Activity getActivity() {
//        return activity;
//    }
//
//    public void setActivity(Activity activity) {
//        this.activity = activity;
//    }
//
//    public List<TestValue> getTestValues() {
//        return testValues;
//    }
//
//    public void setTestValues(List<TestValue> testValues) {
//        this.testValues = testValues;
//    }
//}
