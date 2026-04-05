package com.sovon9.activity_service.controller;

import com.sovon9.activity_service.entities.Activity;
import com.sovon9.activity_service.repositories.ActivityRepository;
import com.sovon9.activity_service.util.QueryBuilderUtil;
import org.springframework.data.domain.Limit;
import org.springframework.data.domain.ScrollPosition;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Window;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.query.ScrollSubrange;
import org.springframework.stereotype.Controller;

import java.util.Map;

@Controller
public class ActivitiesGraphqlController {

    private final ActivityRepository activityRepository;

    public ActivitiesGraphqlController(ActivityRepository activityRepository) {
        this.activityRepository = activityRepository;
    }

    @QueryMapping
    public Window<Activity> activities(ScrollSubrange subrange, @Argument Map<String, Object> where, @Argument Map<String, Object> order) {
        ScrollPosition scrollPosition = subrange.position().orElse(ScrollPosition.offset());
        int limit = subrange.count().orElse(10);

        Sort sort = QueryBuilderUtil.buildSort(order, "activityId", Sort.Direction.ASC);
        Specification<Activity> spec = QueryBuilderUtil.buildSpecification(where);

        if (spec == null) {
            return activityRepository.findBy(scrollPosition, Limit.of(limit), sort);
        }

        return activityRepository.findBy(spec, q -> q.limit(limit).sortBy(sort).scroll(scrollPosition));
    }
}
