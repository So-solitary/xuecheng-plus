package com.xuecheng.content.api;

import com.xuecheng.content.model.dto.BindTeachplanMediaDto;
import com.xuecheng.content.model.dto.SaveTeachplanDto;
import com.xuecheng.content.model.dto.TeachplanDto;
import com.xuecheng.content.service.TeachplanService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/teachplan")
@RestController
@Api(tags = "课程计划编辑接口")
@Slf4j
public class TeachplanController {

    @Autowired
    TeachplanService teachplanService;

    @ApiOperation("查询课程计划属性结构")
    @GetMapping("/{courseId}/tree-nodes")
    public List<TeachplanDto> getTreeNodes(@PathVariable("courseId") Long courseId) {
        log.info("查询课程计划属性结构: {}", courseId);
        List<TeachplanDto> list = teachplanService.getTreeNodes(courseId);
        log.info("查询课程计划属性结构: list {}", list);
        return list;
    }

    @DeleteMapping("/{teachplanId}")
    @ApiOperation("删除课程计划")
    public void deleteTeachplan(@PathVariable("teachplanId")Long teachplanId) {
        log.info("删除课程计划: {}", teachplanId);
        teachplanService.deleteTeachplan(teachplanId);
    }

    @PostMapping("/movedown/{teachplanId}")
    @ApiOperation("课程计划排序-向下移动")
    public void movedown(@PathVariable("teachplanId") Long teachplanId) {
        log.info("课程计划排序-向下移动: {}", teachplanId);
        teachplanService.movedown(teachplanId);
    }

    @PostMapping("/moveup/{teachplanId}")
    @ApiOperation("课程计划排序-向上移动")
    public void moveup(@PathVariable("teachplanId") Long teachplanId) {
        log.info("课程计划排序-向上移动: {}", teachplanId);
        teachplanService.moveup(teachplanId);
    }

    @ApiOperation("课程计划创建或修改")
    @PostMapping
    public void saveTeachplan( @RequestBody SaveTeachplanDto teachplan){
        log.info("课程计划创建或修改: {}", teachplan);
        teachplanService.saveTeachplan(teachplan);
    }

    @ApiOperation("课程计划和媒资信息绑定")
    @PostMapping("/association/media")
    public void associationMedia(@RequestBody BindTeachplanMediaDto bindTeachplanMediaDto) {
        teachplanService.associationMedia(bindTeachplanMediaDto);
    }

}
