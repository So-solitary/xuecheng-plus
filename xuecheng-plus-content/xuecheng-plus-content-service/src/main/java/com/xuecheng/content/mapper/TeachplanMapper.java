package com.xuecheng.content.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xuecheng.content.model.dto.TeachplanDto;
import com.xuecheng.content.model.po.Teachplan;

import java.util.List;

/**
 * <p>
 * 课程计划 Mapper 接口
 * </p>
 *
 * @author itcast
 */
public interface TeachplanMapper extends BaseMapper<Teachplan> {

    List<TeachplanDto> selectTreeNodes(long courseId);

    /**
     * 根据orderby排序，找到courseId的上一个对象
     * @param courseId
     * @return
     */
    Teachplan getFrontTeachplan(Long courseId);

    /**
     * 根据orderby排序，找到courseId的下一个对象
     * @param teachplanId
     * @return
     */
    Teachplan getBehindTeachplan(Long teachplanId);
}
