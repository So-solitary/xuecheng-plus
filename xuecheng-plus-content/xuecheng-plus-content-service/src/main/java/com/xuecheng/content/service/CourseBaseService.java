package com.xuecheng.content.service;

import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.content.model.dto.AddCourseDto;
import com.xuecheng.content.model.dto.CourseBaseInfoDto;
import com.xuecheng.content.model.dto.EditCourseDto;
import com.xuecheng.content.model.dto.QueryCourseParamsDto;

public interface CourseBaseService {

    PageResult pageQuery(PageParams pageParams, QueryCourseParamsDto queryCourseParamsDto);

    CourseBaseInfoDto createCourseBase(Long companyID, AddCourseDto addCourseDto);

    CourseBaseInfoDto getCourseBaseInfo(Long id);

    CourseBaseInfoDto updateCourseBase(Long companyId, EditCourseDto editCourseDto);

    /**
     * 删除课程
     * @param courseId
     */
    void delete(Long courseId);

}
