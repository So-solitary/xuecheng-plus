package com.xuecheng.content.model.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.ToString;

import javax.validation.constraints.NotBlank;

@Data
@ToString(callSuper = true)
@ApiModel(value = "AddCourseDto", description = "新增课程基本信息")
public class AddCourseDto {

    @NotBlank(message = "收费规则不能为空")
    @ApiModelProperty(value = "收费规则", required = true)
    private String charge;

    @ApiModelProperty(value = "课程介绍")
    private String description;

    @NotBlank(message = "课程等级不能为空")
    @ApiModelProperty(value = "课程等级", required = true)
    private String grade;

    @NotBlank(message = "大分类不能为空")
    @ApiModelProperty(value = "大分类", required = true)
    private String mt;

    @NotBlank(message = "课程名称不能为空")
    @ApiModelProperty(value = "课程名称", required = true)
    private String name;

    @NotBlank(message = "课程图片不能为空")
    @ApiModelProperty(value = "课程图片", required = true)
    private String pic;

    @ApiModelProperty(value = "价格")
    private Float price;

    @NotBlank(message = "小分类不能为空")
    @ApiModelProperty(value = "小分类", required = true)
    private String st;

    @ApiModelProperty(value = "课程签名")
    private String tags;

    @NotBlank(message = "教学模式(普通，录播，直播等)不能为空")
    @ApiModelProperty(value = "教学模式(普通，录播，直播等)", required = true)
    private String teachmode;

    @NotBlank(message = "试用人群不能为空")
    @ApiModelProperty(value = "试用人群", required = true)
    private String users;
}
