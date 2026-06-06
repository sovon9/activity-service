package com.sovon9.activity_service.repositories;

import com.sovon9.activity_service.entities.ActivityType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ActivityTypeRepository extends JpaRepository<ActivityType, Integer> {
}