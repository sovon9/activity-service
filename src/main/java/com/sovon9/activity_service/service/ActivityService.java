package com.sovon9.activity_service.service;

import com.sovon9.activity_service.dto.ActivityDto;
import com.sovon9.activity_service.entities.Activity;
import com.sovon9.activity_service.repositories.ActivityRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ActivityService {

    private ActivityRepository activityRepository;

    public ActivityService(ActivityRepository activityRepository)
    {
        this.activityRepository=activityRepository;
    }

    public ActivityDto getActivityData(Long activityId)
    {
        Optional<Activity> optionalActivity = activityRepository.findById(activityId);
        if(!optionalActivity.isPresent())
        {
            return null;
        }
        Activity activity = optionalActivity.get();
        ActivityDto activityDto = new ActivityDto(activity.getActivityId(), activity.getStatus(), activity.getType(), activity.getTitle(),
                activity.getCreatedAt(), activity.getProductionUnitId(), activity.getProcessOrderId());
        return activityDto;
    }

    public List<ActivityDto> getAllActivityData() {
        List<Activity> activities = activityRepository.findAll();
        return activities.stream().map(activity -> new ActivityDto(activity.getActivityId(),
                activity.getStatus(), activity.getType(), activity.getTitle(), activity.getCreatedAt(), activity.getProductionUnitId(),
                activity.getProcessOrderId())).toList();
    }

    public void saveActivityData(ActivityDto activityDto) {
        Activity activity = new Activity();
        activity.setActivityId(activityDto.getActivityId());
        activity.setStatus(activityDto.getStatus());
        activity.setType(activityDto.getType());
        activity.setTitle(activityDto.getTitle());
        activity.setCreatedAt(activityDto.getCreatedAt());
        activity.setProductionUnitId(activityDto.getProductionUnitId());
        activity.setProcessOrderId(activityDto.getProcessOrderId());
        activityRepository.save(activity);
    }

    public ActivityDto getActivityDataByStatus(String status) {
        Activity activity = activityRepository.findByStatus(status);
        if(null==activity)
        {
            return null;
        }
        ActivityDto activityDto = new ActivityDto(activity.getActivityId(), activity.getStatus(), activity.getType(),
                activity.getTitle(), activity.getCreatedAt(), activity.getProductionUnitId(), activity.getProcessOrderId());
        return activityDto;
    }
}
