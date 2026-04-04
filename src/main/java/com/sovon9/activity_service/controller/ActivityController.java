package com.sovon9.activity_service.controller;

import com.sovon9.activity_service.service.ActivityService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/activity")
public class ActivityController {

    private ActivityService activityService;

    public ActivityController(ActivityService activityService)
    {
        this.activityService=activityService;
    }

    @GetMapping("/v1/activities/{activityId}")
    public ResponseEntity<?> getActivityData(@PathVariable("activityId") Long activityId) {
        if(null==activityId || activityId==0)
        {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        return ResponseEntity.ok(activityService.getActivityData(activityId));
    }
}
