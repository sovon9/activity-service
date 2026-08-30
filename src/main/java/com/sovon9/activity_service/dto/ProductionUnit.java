package com.sovon9.activity_service.dto;

import jakarta.persistence.*;

@Entity
@Table(name = "production_unit")
public class ProductionUnit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long productionUnitId;
}