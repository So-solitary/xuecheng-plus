package com.xuecheng.content.api;

import com.xuecheng.content.model.dto.AddCourseTeacherDto;
import com.xuecheng.content.model.po.CourseTeacher;
import com.xuecheng.content.service.CourseTeacherService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/content/courseTeacher")
@Api(tags = "课程师资")
@Slf4j
public class CourseTeacherController {

    @Autowired
    CourseTeacherService courseTeacherService;

    @GetMapping("/list/{courseId}")
    @ApiOperation("查询师资信息")
    public List<CourseTeacher> list(@PathVariable("courseId") Long courseId) {
        log.info("查询师资信息: {}", courseId);
        List<CourseTeacher> list = courseTeacherService.list(courseId);
        return list;
    }


    @PostMapping
    @ApiOperation("新增师资信息")
    public CourseTeacher save(@RequestBody AddCourseTeacherDto addCourseTeacherDto) {
        log.info("新增师资信息: {}", addCourseTeacherDto);
        CourseTeacher courseTeacher = courseTeacherService.save(addCourseTeacherDto);
        return courseTeacher;
    }

    @PutMapping
    @ApiOperation("变更师资信息")
    public CourseTeacher update(@RequestBody CourseTeacher courseTeacher) {
        log.info("变更师资信息: {}", courseTeacher);
        CourseTeacher teacher = courseTeacherService.update(courseTeacher);
        return teacher;
    }

    @DeleteMapping("/course/{courseId}/{teacherId}")
    @ApiOperation("删除教师")
    public void delete(@PathVariable("courseId")Long courseId, @PathVariable("teacherId") Long teacherId) {
        log.info("删除教师: courseId {}, teacherId {}", courseId, teacherId);
        courseTeacherService.delete(courseId, teacherId);
    }
}
