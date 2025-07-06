package com.xuecheng.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.content.mapper.TeachplanMapper;
import com.xuecheng.content.mapper.TeachplanMediaMapper;
import com.xuecheng.content.model.dto.SaveTeachplanDto;
import com.xuecheng.content.model.dto.TeachplanDto;
import com.xuecheng.content.model.po.Teachplan;
import com.xuecheng.content.model.po.TeachplanMedia;
import com.xuecheng.content.service.TeachplanService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
@Transactional
public class TeachplanServiceImpl implements TeachplanService {

    @Autowired
    TeachplanMapper teachplanMapper;

    @Autowired
    TeachplanMediaMapper teachplanMediaMapper;

    @Override
    public List<TeachplanDto> getTreeNodes(Long courseId) {
        List<TeachplanDto> list = teachplanMapper.selectTreeNodes(courseId);
        return list;
    }

    /**
     * 删除课程计划
     * 1 删除第一级别的大章节时要求大章节下边没有小章节时方可删除。
     * 2 删除第二级别的小章节的同时需要将teachplan_media表关联的信息也删除。
     * @param teachplanId
     */
    @Override
    public void deleteTeachplan(Long teachplanId) {
        //Teachplan teachplan = teachplanMapper.selectById(teachplanId);
        LambdaQueryWrapper<Teachplan> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Teachplan::getParentid, teachplanId);
        Integer cnt = teachplanMapper.selectCount(wrapper);
        if (cnt > 0) {
            throw new XueChengPlusException("课程计划信息还有子集信息，无法操作");
        }
        teachplanMapper.deleteById(teachplanId);
        LambdaQueryWrapper<TeachplanMedia> w1 = new LambdaQueryWrapper<>();
        w1.eq(TeachplanMedia::getTeachplanId, teachplanId);
        teachplanMediaMapper.delete(w1);

    }

    /**
     * 课程计划排序-向下移动
     * @param teachplanId
     */
//    @Override
//    public void movedown(Long teachplanId) {
//        Teachplan teachplan = teachplanMapper.selectById(teachplanId);
//        //Teachplan frontTeachplan = teachplanMapper.getFrontTeachplan(teachplanId);
//        Teachplan behindTeachplan = teachplanMapper.getBehindTeachplan(teachplanId);
//        if (behindTeachplan != null) {
//            Integer teachplanOrderby = teachplan.getOrderby();
//            Integer behindTeachplanOrderby = behindTeachplan.getOrderby();
//            teachplan.setOrderby(behindTeachplanOrderby);
//            behindTeachplan.setOrderby(teachplanOrderby);
//            teachplanMapper.updateById(teachplan);
//            teachplanMapper.updateById(behindTeachplan);
//        }
//    }

    /**
     * 课程计划排序-向上移动
     * @param teachplanId
     */
//    @Override
//    public void moveup(Long teachplanId) {
//        Teachplan teachplan = teachplanMapper.selectById(teachplanId);
//        Teachplan frontTeachplan = teachplanMapper.getFrontTeachplan(teachplanId);
//        if (frontTeachplan != null) {
//            Integer teachplanOrderby = teachplan.getOrderby();
//            Integer frontTeachplanOrderby = frontTeachplan.getOrderby();
//            teachplan.setOrderby(frontTeachplanOrderby);
//            frontTeachplan.setOrderby(teachplanOrderby);
//            teachplanMapper.updateById(teachplan);
//            teachplanMapper.updateById(frontTeachplan);
//        }
//    }

    /**
     * 课程计划排序-向上移动
     * @param teachplanId
     */
    @Override
    public void moveup(Long teachplanId) {
        Teachplan teachplan = teachplanMapper.selectById(teachplanId);
        LambdaQueryWrapper<Teachplan> w = new LambdaQueryWrapper<>();
        w.eq(Teachplan::getParentid, teachplan.getParentid());
        List<Teachplan> list = teachplanMapper.selectList(w);
        if (list != null && !list.isEmpty()) {
            list.sort((p1, p2) -> Integer.compare(p1.getOrderby(), p2.getOrderby()));
            Teachplan frontTeachplan = teachplan;
            for (Teachplan t : list) {
                if (teachplan.equals(t)) {
                    break;
                }
                frontTeachplan = t;
            }
            if (!teachplan.equals(frontTeachplan)) {
                Integer teachplanOrderby = teachplan.getOrderby();
                Integer frontTeachplanOrderby = frontTeachplan.getOrderby();
                teachplan.setOrderby(frontTeachplanOrderby);
                frontTeachplan.setOrderby(teachplanOrderby);
                teachplanMapper.updateById(teachplan);
                teachplanMapper.updateById(frontTeachplan);
            }
        }
    }

    /**
     * 课程计划创建或修改
     * @param teachplanDto
     */
    @Override
    public void saveTeachplan(SaveTeachplanDto teachplanDto) {
        //课程计划id
        Long id = teachplanDto.getId();
        //修改课程计划
        if(id!=null){
            Teachplan teachplan = teachplanMapper.selectById(id);
            BeanUtils.copyProperties(teachplanDto,teachplan);
            teachplanMapper.updateById(teachplan);
        }else{
            //取出同父同级别的课程计划数量
            int count = getTeachplanCount(teachplanDto.getCourseId(), teachplanDto.getParentid());
            Teachplan teachplanNew = new Teachplan();
            //设置排序号
            teachplanNew.setOrderby(count+1);
            BeanUtils.copyProperties(teachplanDto,teachplanNew);

            teachplanMapper.insert(teachplanNew);

        }
    }

    /**
     * @description 获取最新的排序号
     * @param courseId  课程id
     * @param parentId  父课程计划id
     * @return int 最新排序号
     * @author Mr.M
     * @date 2022/9/9 13:43
     */
    private int getTeachplanCount(long courseId,long parentId){
        LambdaQueryWrapper<Teachplan> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Teachplan::getCourseId,courseId);
        queryWrapper.eq(Teachplan::getParentid,parentId);
        Integer count = teachplanMapper.selectCount(queryWrapper);
        return count;
    }


    /**
     * 课程计划排序-向下移动
     * @param teachplanId
     */
    @Override
    public void movedown(Long teachplanId) {
        Teachplan teachplan = teachplanMapper.selectById(teachplanId);
        LambdaQueryWrapper<Teachplan> w = new LambdaQueryWrapper<>();
        w.eq(Teachplan::getParentid, teachplan.getParentid());
        List<Teachplan> list = teachplanMapper.selectList(w);
        if (list != null && !list.isEmpty()) {
            list.sort((p1, p2) -> Integer.compare(p2.getOrderby(), p1.getOrderby()));
            Teachplan behindTeachplan = teachplan;
            for (Teachplan t : list) {
                if (teachplan.equals(t)) {
                    break;
                }
                behindTeachplan = t;
            }
            if (!teachplan.equals(behindTeachplan)) {
                Integer teachplanOrderby = teachplan.getOrderby();
                Integer bebindTeachplanOrderby = behindTeachplan.getOrderby();
                teachplan.setOrderby(bebindTeachplanOrderby);
                behindTeachplan.setOrderby(teachplanOrderby);
                teachplanMapper.updateById(teachplan);
                teachplanMapper.updateById(behindTeachplan);
            }
        }
    }



}
