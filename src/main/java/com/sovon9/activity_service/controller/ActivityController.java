package com.sovon9.activity_service.controller;

import com.sovon9.activity_service.dto.ActivityDto;
import com.sovon9.activity_service.service.ActivityService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.util.annotation.NonNull;

import java.util.List;

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

    @GetMapping("/v1/activities")
    public ResponseEntity<List<ActivityDto>> getAllActivityData()
    {
        return ResponseEntity.ok(activityService.getAllActivityData());
    }

    @PostMapping("/v1/activities")
    public ResponseEntity<?> saveActivityData(@RequestBody ActivityDto activityDto)
    {
        try {
            activityService.saveActivityData(activityDto);
        }
        catch (Exception e)
        {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @GetMapping("/v1/activities/status/{status}")
    public ResponseEntity<?> getActivityByStatus(@PathVariable String status)
    {
        ActivityDto activityDto = activityService.getActivityDataByStatus(status);
        if(null==activityDto)
        {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.ok(activityDto);
    }
}
