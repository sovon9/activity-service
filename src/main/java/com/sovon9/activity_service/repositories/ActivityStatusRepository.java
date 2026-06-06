package com.sovon9.activity_service.repositories;

import com.sovon9.activity_service.entities.ActivityStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ActivityStatusRepository extends JpaRepository<ActivityStatus, Integer> {
}