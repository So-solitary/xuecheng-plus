package com.xuecheng.base.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 错误响应参数包装
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class RestErrorResponse implements Serializable {

    private String errMessage;
}
