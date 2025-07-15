package com.xuecheng.content.service;

import com.xuecheng.content.model.dto.BindTeachplanMediaDto;
import com.xuecheng.content.model.dto.SaveTeachplanDto;
import com.xuecheng.content.model.dto.TeachplanDto;

import java.util.List;

public interface TeachplanService {

    List<TeachplanDto> getTreeNodes(Long courseId);

    /**
     * 删除课程计划
     * 1 删除第一级别的大章节时要求大章节下边没有小章节时方可删除。
     * 2 删除第二级别的小章节的同时需要将teachplan_media表关联的信息也删除。
     * @param teachplanId
     */
    void deleteTeachplan(Long teachplanId);

    /**
     * 课程计划排序-向下移动
     * @param teachplanId
     */
    void movedown(Long teachplanId);

    /**
     * 课程计划排序-向上移动
     * @param teachplanId
     */
    void moveup(Long teachplanId);

    /**
     * 课程计划创建或修改
     * @param teachplan
     */
    void saveTeachplan(SaveTeachplanDto teachplan);

    /**
     * 教学计划关联视频
     * @param bindTeachplanMediaDto
     */
    void associationMedia(BindTeachplanMediaDto bindTeachplanMediaDto);

}
