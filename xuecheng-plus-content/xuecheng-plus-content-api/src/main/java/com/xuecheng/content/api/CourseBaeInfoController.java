package com.xuecheng.content.api;

import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.base.model.Result;
import com.xuecheng.content.model.dto.QueryCourseParamsDto;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/content/course")
@Slf4j
@Api(tags = "课程信息编辑接口")
public class CourseBaeInfoController {

    @GetMapping("/list")
    @ApiOperation("查询课程")
    public Result<PageResult> list(
            PageParams pageParams,
            @RequestBody(required = false) QueryCourseParamsDto queryCourseParamsDto) {
        log.info("查询课程: pageParams {}, queryCourseParamsDto {}", pageParams, queryCourseParamsDto);

        return Result.success();
    }
}
