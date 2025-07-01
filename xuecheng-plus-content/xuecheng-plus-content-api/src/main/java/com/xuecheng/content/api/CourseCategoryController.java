package com.xuecheng.content.api;

import com.xuecheng.base.model.Result;
import com.xuecheng.content.model.dto.CourseCategoryTreeDto;
import com.xuecheng.content.service.CourseCategoryService;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/content/course-category")
@Slf4j
@Api(tags = "课程分类编辑接口")
public class CourseCategoryController {

    @Autowired
    CourseCategoryService courseCategoryService;

    @GetMapping("/tree-nodes")
    public Result treeNodes() {
        log.info("课程分类编辑接口");
        List<CourseCategoryTreeDto> courseCategoryTreeVO = courseCategoryService.queryTreeNodes("1");
        return Result.success(courseCategoryTreeVO);
    }
}
