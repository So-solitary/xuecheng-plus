package com.xuecheng.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.content.mapper.CourseTeacherMapper;
import com.xuecheng.content.model.dto.AddCourseTeacherDto;
import com.xuecheng.content.model.po.CourseTeacher;
import com.xuecheng.content.service.CourseTeacherService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class CourseTeacherServiceImpl implements CourseTeacherService {

    @Autowired
    CourseTeacherMapper courseTeacherMapper;

    /**
     * 查询师资信息
     * @param courseId
     * @return
     */
    @Override
    public List<CourseTeacher> list(Long courseId) {
        LambdaQueryWrapper<CourseTeacher> w = new LambdaQueryWrapper<>();
        w.eq(CourseTeacher::getCourseId, courseId);
        List<CourseTeacher> courseTeachers = courseTeacherMapper.selectList(w);
        return courseTeachers;
    }

    /**
     * 新增师资信息
     * @param addCourseTeacherDto
     * @return
     */
    @Override
    public CourseTeacher save(AddCourseTeacherDto addCourseTeacherDto) {
        CourseTeacher courseTeacher = new CourseTeacher();
        BeanUtils.copyProperties(addCourseTeacherDto, courseTeacher);
        courseTeacherMapper.insert(courseTeacher);
        log.info("新增师资信息: {}", courseTeacher);
        return courseTeacher;
    }

    /**
     * 变更师资信息
     * @param courseTeacher
     * @return
     */
    @Override
    public CourseTeacher update(CourseTeacher courseTeacher) {
        courseTeacherMapper.updateById(courseTeacher);
        return courseTeacher;
    }

    /**
     * 删除教师
     * @param courseId
     * @param teacherId
     */
    @Override
    public void delete(Long courseId, Long teacherId) {
        LambdaQueryWrapper<CourseTeacher> w = new LambdaQueryWrapper<>();
        w.eq(CourseTeacher::getCourseId, courseId);
        w.eq(CourseTeacher::getId, teacherId);
        courseTeacherMapper.delete(w);
    }


}
