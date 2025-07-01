package com.xuecheng.content.model.vo;

import com.xuecheng.content.model.po.CourseCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class CourseCategoryTreeVO extends CourseCategory implements Serializable {
    List<CourseCategoryTreeVO> childrenTreeNodes;
}
