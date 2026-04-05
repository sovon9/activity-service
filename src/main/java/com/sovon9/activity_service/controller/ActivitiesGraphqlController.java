package com.sovon9.activity_service.controller;

import com.sovon9.activity_service.entities.Activity;
import com.sovon9.activity_service.entities.ActivityStatus;
import com.sovon9.activity_service.entities.ActivityType;
import com.sovon9.activity_service.repositories.ActivityRepository;
import com.sovon9.activity_service.repositories.ActivityStatusRepository;
import com.sovon9.activity_service.repositories.ActivityTypeRepository;
import com.sovon9.activity_service.util.QueryBuilderUtil;
import org.springframework.data.domain.Limit;
import org.springframework.data.domain.ScrollPosition;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Window;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.BatchMapping;
import org.springframework.graphql.data.query.ScrollSubrange;
import org.springframework.stereotype.Controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Controller
public class ActivitiesGraphqlController {

    private final ActivityRepository activityRepository;
    private final ActivityStatusRepository activityStatusRepository;
    private final ActivityTypeRepository activityTypeRepository;

    public ActivitiesGraphqlController(ActivityRepository activityRepository, 
                                       ActivityStatusRepository activityStatusRepository, 
                                       ActivityTypeRepository activityTypeRepository) {
        this.activityRepository = activityRepository;
        this.activityStatusRepository = activityStatusRepository;
        this.activityTypeRepository = activityTypeRepository;
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

    @BatchMapping(typeName = "Activity", field = "status")
    public Map<Activity, ActivityStatus> status(List<Activity> activities) {
        List<Integer> statusIds = activities.stream()
                .map(Activity::getActivityStatusId)
                .distinct()
                .collect(Collectors.toList());

        List<ActivityStatus> statuses = activityStatusRepository.findAllById(statusIds);

        Map<Integer, ActivityStatus> statusMap = statuses.stream()
                .collect(Collectors.toMap(ActivityStatus::getActivityStatusId, Function.identity()));

        Map<Activity, ActivityStatus> resultMap = new HashMap<>();
        for(Activity activity : activities) {
            ActivityStatus status = statusMap.get(activity.getActivityStatusId());
            if(status != null) {
                resultMap.put(activity, status);
            }
        }
        return resultMap;
    }

    @BatchMapping(typeName = "Activity", field = "type")
    public Map<Activity, ActivityType> type(List<Activity> activities) {
        List<Integer> typeIds = activities.stream()
                .map(Activity::getActivityTypeId)
                .distinct()
                .collect(Collectors.toList());

        List<ActivityType> types = activityTypeRepository.findAllById(typeIds);

        Map<Integer, ActivityType> typeMap = types.stream()
                .collect(Collectors.toMap(ActivityType::getActivityTypeId, Function.identity()));

        Map<Activity, ActivityType> resultMap = new HashMap<>();
        for(Activity activity : activities) {
            ActivityType type = typeMap.get(activity.getActivityTypeId());
            if(type != null) {
                resultMap.put(activity, type);
            }
        }
        return resultMap;
    }
}