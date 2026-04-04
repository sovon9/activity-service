package com.sovon9.activity_service.service;

import com.sovon9.activity_service.dto.ActivityDto;
import com.sovon9.activity_service.entities.Activity;
import com.sovon9.activity_service.repositories.ActivityRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        ActivityDto activityDto = new ActivityDto(activity.getActivityId(), activity.getTitle(), activity.getActivityStatusId(), activity.getStatus().getActivityStatusDesc());
        return activityDto;
    }
}
