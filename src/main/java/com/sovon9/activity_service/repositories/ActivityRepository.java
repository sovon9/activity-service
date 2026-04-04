package com.sovon9.activity_service.repositories;

import com.sovon9.activity_service.entities.Activity;
import org.springframework.data.domain.Limit;
import org.springframework.data.domain.ScrollPosition;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Window;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ActivityRepository extends JpaRepository<Activity, Long> {

    Window<Activity> findBy(ScrollPosition position, Limit limit, Sort sort);

}
