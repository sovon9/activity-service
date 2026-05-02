package com.sovon9.activity_service.repositories;

import com.sovon9.activity_service.entities.Activity;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface ActivityRepository extends JpaRepository<Activity, Long>, JpaSpecificationExecutor<Activity> {

    Window<Activity> findBy(ScrollPosition position, Limit limit, Sort sort);

    Activity findByStatusActivityStatusDesc(String status);

}
