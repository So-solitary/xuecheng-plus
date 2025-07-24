package com.xuecheng.content.service.impl;

import com.alibaba.fastjson.JSON;
import com.xuecheng.base.constant.AuditConstant;
import com.xuecheng.base.constant.PublishConstant;
import com.xuecheng.base.enumeration.CommonError;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.content.mapper.*;
import com.xuecheng.content.model.dto.CourseBaseInfoDto;
import com.xuecheng.content.model.dto.CoursePreviewDto;
import com.xuecheng.content.model.dto.TeachplanDto;
import com.xuecheng.content.model.po.CourseBase;
import com.xuecheng.content.model.po.CourseMarket;
import com.xuecheng.content.model.po.CoursePublish;
import com.xuecheng.content.model.po.CoursePublishPre;
import com.xuecheng.content.service.CourseBaseService;
import com.xuecheng.content.service.CoursePublishService;
import com.xuecheng.content.service.TeachplanService;
import com.xuecheng.messagesdk.model.po.MqMessage;
import com.xuecheng.messagesdk.service.MqMessageService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CoursePublishServiceImpl implements CoursePublishService {
    @Autowired
    CourseBaseService courseBaseService;

    @Autowired
    TeachplanService teachplanService;

    @Autowired
    CourseMarketMapper courseMarketMapper;

    @Autowired
    TeachplanMapper teachplanMapper;

    @Autowired
    CoursePublishPreMapper coursePublishPreMapper;

    @Autowired
    CourseBaseMapper courseBaseMapper;

    @Autowired
    CoursePublishMapper coursePublishMapper;

    @Autowired
    MqMessageService mqMessageService;



    @Override
    public CoursePreviewDto getCoursePreviewInfo(Long courseId) {

        //课程基本信息、营销信息
        CourseBaseInfoDto courseBaseInfo = courseBaseService.getCourseBaseInfo(courseId);

        //课程计划信息
        List<TeachplanDto> teachplanTree= teachplanService.getTreeNodes(courseId);

        CoursePreviewDto coursePreviewDto = new CoursePreviewDto();
        coursePreviewDto.setCourseBase(courseBaseInfo);
        coursePreviewDto.setTeachplans(teachplanTree);
        return coursePreviewDto;
    }

    /**
     * 提交课程
     *
     * @param companyId
     * @param courseId
     */
    @Override
    public void commitAudit(Long companyId, Long courseId) {
        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        if (AuditConstant.COMMITED.equals(courseBase.getAuditStatus())) {
            new XueChengPlusException("对已提交审核的课程不允许提交审核");
        }

        if (companyId.equals(courseBase.getCompanyId())) {
            new XueChengPlusException("本机构只允许提交本机构的课程");
        }

        if (courseBase.getPic() == null) {
            new XueChengPlusException("没有上传图片不允许提交审核");
        }

        List<TeachplanDto> treeNodes = teachplanService.getTreeNodes(courseId);
        if (treeNodes == null) {
            new XueChengPlusException("没有添加课程计划不允许提交审核");
        }

        CoursePublishPre coursePublishPre = new CoursePublishPre();
        BeanUtils.copyProperties(courseBase, coursePublishPre);
        CourseMarket courseMarket = courseMarketMapper.selectById(courseId);
        if (courseMarket == null) {
            new XueChengPlusException("课程营销信息不存在");
        }
        String marketJson = JSON.toJSONString(courseMarket);

        coursePublishPre.setMarket(marketJson);

        if (treeNodes.size() <= 0) {
            new XueChengPlusException("还没有添加课程计划");
        }

        String teachplanJson = JSON.toJSONString(treeNodes);
        coursePublishPre.setTeachplan(teachplanJson);

        // todo 设置教师信息
        coursePublishPre.setTeachers(null);

        coursePublishPre.setUsername(null);
        coursePublishPre.setMtName(null);
        coursePublishPre.setStName(null);

        coursePublishPre.setCreateDate(LocalDateTime.now());
        coursePublishPre.setStatus(AuditConstant.COMMITED);
        coursePublishPre.setRemark(null);

        CoursePublishPre c = coursePublishPreMapper.selectById(courseId);
        if (c == null) {
            coursePublishPreMapper.insert(coursePublishPre);
        } else {
            coursePublishPreMapper.updateById(coursePublishPre);
        }

        courseBase.setAuditStatus(AuditConstant.COMMITED);
        courseBaseMapper.updateById(courseBase);

    }

    @Transactional
    @Override
    public void publish(Long companyId, Long courseId) {
        //整合课程发布信息
        //查询课程预发布表
        CoursePublishPre coursePublishPre = coursePublishPreMapper.selectById(courseId);
        if(coursePublishPre == null){
            throw new XueChengPlusException("课程预发布数据为空");
        }

        if (!companyId.equals(coursePublishPre.getCompanyId())) {
            throw new XueChengPlusException("不允许提交其他机构的课程");
        }

        if (!AuditConstant.AUDITED.equals(coursePublishPre.getStatus())) {
            throw new XueChengPlusException("操作失败，课程审核通过方可发布");
        }

        // 保存课程发布信息
        saveCoursePublish(courseId);

        // 保存消息表
        saveCoursePublishMessage(courseId);

        coursePublishPreMapper.deleteById(courseId);

    }

    /**
     * 保存消息表
     * @param courseId
     */
    private void saveCoursePublishMessage(Long courseId) {
        MqMessage mqMessage = mqMessageService.addMessage("course_publish", String.valueOf(courseId), null, null);
        if(mqMessage==null){
            throw new XueChengPlusException(CommonError.UNKOWN_ERROR.getErrMessage());
        }
    }

    /**
     * 保存课程发布信息
     * @param courseId
     */
    private void saveCoursePublish(Long courseId) {

        CoursePublishPre coursePublishPre = coursePublishPreMapper.selectById(courseId);
        CoursePublish coursePublish = new CoursePublish();

        //拷贝到课程发布对象
        BeanUtils.copyProperties(coursePublishPre,coursePublish);
        coursePublish.setStatus(PublishConstant.PUBLISHED);
        CoursePublish coursePublishUpdate = coursePublishMapper.selectById(courseId);
        if(coursePublishUpdate == null){
            coursePublishMapper.insert(coursePublish);
        }else{
            coursePublishMapper.updateById(coursePublish);
        }
        //更新课程基本表的发布状态
        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        courseBase.setStatus(PublishConstant.PUBLISHED);
        courseBaseMapper.updateById(courseBase);
    }
}
