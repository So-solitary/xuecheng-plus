package com.xuecheng.content.service;

import com.xuecheng.content.model.dto.AddCourseTeacherDto;
import com.xuecheng.content.model.po.CourseTeacher;

import java.util.List;

public interface CourseTeacherService {
    /**
     * 查询师资信息
     * @param courseId
     * @return
     */
    List<CourseTeacher> list(Long courseId) ;

    /**
     * 新增师资信息
     * @param addCourseTeacherDto
     * @return
     */
    CourseTeacher save(AddCourseTeacherDto addCourseTeacherDto);

    /**
     * 变更师资信息
     * @param courseTeacher
     * @return
     */
    CourseTeacher update(CourseTeacher courseTeacher);

    /**
     * 删除教师
     * @param courseId
     * @param teacherId
     */
    void delete(Long courseId, Long teacherId);
}
