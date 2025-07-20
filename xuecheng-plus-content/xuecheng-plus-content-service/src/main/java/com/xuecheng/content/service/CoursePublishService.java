package com.xuecheng.content.service;

import com.xuecheng.content.model.dto.CoursePreviewDto;

public interface CoursePublishService {
    public CoursePreviewDto getCoursePreviewInfo(Long courseId);

    /**
     * 提交课程
     *
     * @param companyId
     * @param courseId
     */
    void commitAudit(Long companyId, Long courseId);
}
