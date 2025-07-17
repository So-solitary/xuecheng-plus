package com.xuecheng.content.api;

import com.xuecheng.content.model.dto.CoursePreviewDto;
import com.xuecheng.content.service.CoursePublishService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@Api(tags = "课程公开查询接口")
@RequestMapping("/open")
public class ContentOpenController {

    @Autowired
    CoursePublishService coursePublishService;

    @ApiOperation("获取课程信息")
    @GetMapping("/course/whole/{courseId}")
    public CoursePreviewDto getCourseInfo(@PathVariable("courseId") Long courseId) {
        log.info("获取课程信息: {}", courseId);
        CoursePreviewDto coursePreviewInfo = coursePublishService.getCoursePreviewInfo(courseId);
        return coursePreviewInfo;
    }
}
