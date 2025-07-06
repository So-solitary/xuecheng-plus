package com.xuecheng.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.content.mapper.*;
import com.xuecheng.content.model.dto.AddCourseDto;
import com.xuecheng.content.model.dto.CourseBaseInfoDto;
import com.xuecheng.content.model.dto.EditCourseDto;
import com.xuecheng.content.model.dto.QueryCourseParamsDto;
import com.xuecheng.content.model.po.*;
import com.xuecheng.content.service.CourseBaseService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Transactional
public class CourseBaseServiceImpl implements CourseBaseService {

    @Autowired
    CourseBaseMapper courseBaseMapper;

    @Autowired
    CourseMarketMapper courseMarketMapper;

    @Autowired
    CourseCategoryMapper courseCategoryMapper;

    @Autowired
    TeachplanMapper teachplanMapper;

    @Autowired
    private CourseTeacherMapper courseTeacherMapper;

    @Override
    public PageResult pageQuery(PageParams pageParams, QueryCourseParamsDto queryCourseParamsDto) {

        LambdaQueryWrapper<CourseBase> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(
                StringUtils.isNotEmpty(queryCourseParamsDto.getCourseName()),
                CourseBase::getName,
                queryCourseParamsDto.getCourseName()
        );

        queryWrapper.eq(
                StringUtils.isNotEmpty(queryCourseParamsDto.getAuditStatus()),
                CourseBase::getAuditStatus,
                queryCourseParamsDto.getAuditStatus()
        );

        Page<CourseBase> page = new Page<>(pageParams.getPageNo(), pageParams.getPageSize());

        Page<CourseBase> res = courseBaseMapper.selectPage(page, queryWrapper);

        return new PageResult(res.getRecords(), res.getTotal(), page.getCurrent(), page.getSize());
    }

    @Override
    public CourseBaseInfoDto createCourseBase(Long companyId, AddCourseDto dto) {
        //合法性校验
        if (StringUtils.isBlank(dto.getName())) {
            throw new XueChengPlusException("课程名称为空");
        }

        if (StringUtils.isBlank(dto.getMt())) {
            throw new XueChengPlusException("课程分类为空");
        }

        if (StringUtils.isBlank(dto.getSt())) {
            throw new XueChengPlusException("课程分类为空");
        }

        if (StringUtils.isBlank(dto.getGrade())) {
            throw new XueChengPlusException("课程等级为空");
        }

        if (StringUtils.isBlank(dto.getTeachmode())) {
            throw new XueChengPlusException("教育模式为空");
        }

        if (StringUtils.isBlank(dto.getUsers())) {
            throw new XueChengPlusException("适应人群为空");
        }

        if (StringUtils.isBlank(dto.getCharge())) {
            throw new XueChengPlusException("收费规则为空");
        }

        CourseBase base = new CourseBase();
        BeanUtils.copyProperties(dto, base);

        base.setAuditStatus("202002");
        base.setStatus("203001");
        base.setCompanyId(companyId);
        base.setCreateDate(LocalDateTime.now());

        int r1 = courseBaseMapper.insert(base);
        if (r1 <= 0) {
            throw new XueChengPlusException("新增课程基本信息失败");
        }

        // 向课程营销表保存课程营销信息
        CourseMarket courseMarket = new CourseMarket();
        Long courseId = base.getId();

        BeanUtils.copyProperties(dto, courseMarket);
        courseMarket.setId(courseId);

        int r2 = saveCourseMarket(courseMarket);

        if (r2 <= 0) {
            throw new XueChengPlusException("保存课程营销信息失败");
        }

        return getCourseBaseInfo(courseId);
    }




    @Override
    public CourseBaseInfoDto updateCourseBase(Long companyId, EditCourseDto dto) {
        //课程id
        Long courseId = dto.getId();
        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        if(courseBase==null){
            new XueChengPlusException("课程不存在");
        }

        //校验本机构只能修改本机构的课程
        if(!courseBase.getCompanyId().equals(companyId)){
            new XueChengPlusException("本机构只能修改本机构的课程");
        }

        //封装基本信息的数据
        BeanUtils.copyProperties(dto,courseBase);
        courseBase.setChangeDate(LocalDateTime.now());

        //更新课程基本信息
        int i = courseBaseMapper.updateById(courseBase);

        //封装营销信息的数据
        CourseMarket courseMarket = new CourseMarket();
        BeanUtils.copyProperties(dto,courseMarket);
        saveCourseMarket(courseMarket);
        //查询课程信息
        CourseBaseInfoDto courseBaseInfo = this.getCourseBaseInfo(courseId);
        return courseBaseInfo;
    }

    @Override
    public void delete(Long courseId) {
        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        String auditStatus = courseBase.getAuditStatus();
        if (!"202002".equals(auditStatus)) {
            throw new XueChengPlusException("课程审核状态为已提交，不能删除");
        }
        courseBaseMapper.deleteById(courseId);
        courseMarketMapper.deleteById(courseId);
        LambdaQueryWrapper<Teachplan> w = new LambdaQueryWrapper<>();
        w.eq(Teachplan::getCourseId, courseId);
        teachplanMapper.delete(w);
        LambdaQueryWrapper<CourseTeacher> w1 = new LambdaQueryWrapper<>();
        w1.eq(CourseTeacher::getCourseId, courseId);
        courseTeacherMapper.delete(w1);
    }

    private int saveCourseMarket(CourseMarket courseMarket) {
        // 收费规则
        String charge = courseMarket.getCharge();
        if (StringUtils.isBlank(charge)) {
            throw new XueChengPlusException("收费规则没有选择");
        }

        // 收费规则未收费
        if ("201001".equals(charge)) {
            if (courseMarket.getPrice() == null
            || courseMarket.getPrice().longValue() < 0) {
                throw new XueChengPlusException("课程未收费价格不能为空且必须大于0");
            }
        }
        CourseMarket courseMarket1 = courseMarketMapper.selectById(courseMarket.getId());
        if (courseMarket1 == null) {
            return courseMarketMapper.insert(courseMarket);
        } else {
            BeanUtils.copyProperties(courseMarket, courseMarket1);
            courseMarket1.setId(courseMarket.getId());
            return courseMarketMapper.updateById(courseMarket1);
        }
    }


    public CourseBaseInfoDto getCourseBaseInfo(Long courseId) {
        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        if (courseBase == null) {
            return null;
        }
        CourseBaseInfoDto courseBaseInfoDto = new CourseBaseInfoDto();
        BeanUtils.copyProperties(courseBase, courseBaseInfoDto);
        CourseMarket courseMarket = courseMarketMapper.selectById(courseId);
        if (courseMarket != null) {
            BeanUtils.copyProperties(courseMarket, courseBaseInfoDto);
        }

        CourseCategory courseCategoryBySt = courseCategoryMapper.selectById(courseBase.getSt());
        courseBaseInfoDto.setStName(courseCategoryBySt.getName());

        CourseCategory courseCategoryByMt = courseCategoryMapper.selectById(courseBase.getMt());
        courseBaseInfoDto.setMtName(courseCategoryByMt.getName());

        return courseBaseInfoDto;

    }



}
