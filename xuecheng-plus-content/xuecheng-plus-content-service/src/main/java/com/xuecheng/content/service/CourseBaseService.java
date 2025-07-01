package com.xuecheng.content.service;

import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.content.model.dto.QueryCourseParamsDto;

public interface CourseBaseService {

    PageResult pageQuery(PageParams pageParams, QueryCourseParamsDto queryCourseParamsDto);
}
