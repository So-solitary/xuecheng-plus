package com.xuecheng.content.api;

import com.xuecheng.base.exception.ValidationGroups;
import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.content.model.dto.AddCourseDto;
import com.xuecheng.content.model.dto.CourseBaseInfoDto;
import com.xuecheng.content.model.dto.EditCourseDto;
import com.xuecheng.content.model.dto.QueryCourseParamsDto;
import com.xuecheng.content.service.CourseBaseService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/content/course")
@Slf4j
@Api(tags = "课程信息编辑接口")
public class CourseBaeInfoController {


    @Autowired
    CourseBaseService courseBaseService;

    @PostMapping("/list")
    @ApiOperation("查询课程")
    public PageResult list(
            PageParams pageParams,
            @RequestBody(required = false) QueryCourseParamsDto queryCourseParamsDto) {
        log.info("查询课程: pageParams {}, queryCourseParamsDto {}", pageParams, queryCourseParamsDto);
        PageResult pageResult = courseBaseService.pageQuery(pageParams, queryCourseParamsDto);
        return pageResult;
    }

    @PostMapping
    @ApiOperation("新增课程")
    public CourseBaseInfoDto createCourseBase(@RequestBody @Validated({ValidationGroups.Inster.class}) AddCourseDto addCourseDto) {
        log.info("新增课程: {}", addCourseDto);
        Long companyId = 1232141425L;
        CourseBaseInfoDto courseBaseInfoDto = courseBaseService.createCourseBase(companyId,addCourseDto);
        return courseBaseInfoDto;
    }

    @GetMapping("/{id}")
    public CourseBaseInfoDto getCourseById(@PathVariable("id") Long id) {
        log.info("查看课程: {}",id);
        CourseBaseInfoDto courseBaseInfoDto = courseBaseService.getCourseBaseInfo(id);
        return courseBaseInfoDto;
    }

    @ApiOperation("修改课程基础信息")
    @PutMapping
    public CourseBaseInfoDto modifyCourseBase(@RequestBody @Validated EditCourseDto editCourseDto){
        log.info("修改课程基础信息: {}", editCourseDto);
        //机构id，由于认证系统没有上线暂时硬编码
        Long companyId = 1232141425L;
        CourseBaseInfoDto courseBaseInfoDto = courseBaseService.updateCourseBase(companyId,editCourseDto);
        return courseBaseInfoDto;
    }


    @DeleteMapping("/{courseId}")
    public void delete(@PathVariable("courseId") Long courseId) {
        log.info("删除课程: {}", courseId);
        courseBaseService.delete(courseId);
    }
}
