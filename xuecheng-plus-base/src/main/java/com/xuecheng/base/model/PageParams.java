package com.xuecheng.base.model;

import lombok.*;

@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PageParams {

    private Long PageNo;

    private Long PageSize;
}
