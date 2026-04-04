package com.sovon9.activity_service.controller;

import com.sovon9.activity_service.entities.Activity;
import com.sovon9.activity_service.repositories.ActivityRepository;
import org.springframework.data.domain.Limit;
import org.springframework.data.domain.ScrollPosition;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Window;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.query.ScrollSubrange;
import org.springframework.stereotype.Controller;

import java.util.Optional;

@Controller
public class ActivitiesGraphqlController {

    private final ActivityRepository activityRepository;

    public ActivitiesGraphqlController(ActivityRepository activityRepository) {
        this.activityRepository = activityRepository;
    }

    @QueryMapping
    public Window<Activity> activities(ScrollSubrange subrange, @Argument Optional<Object> where, @Argument Optional<Object> order) {
        ScrollPosition scrollPosition = subrange.position().orElse(ScrollPosition.offset());
        Limit limit = Limit.of(subrange.count().orElse(10));

        Sort sort = Sort.by(Sort.Direction.ASC, "activityId");

        return activityRepository.findBy(scrollPosition, limit, sort);
    }
}