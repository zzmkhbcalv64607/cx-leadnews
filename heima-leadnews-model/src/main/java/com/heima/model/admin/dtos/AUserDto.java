package com.heima.model.admin.dtos;

import lombok.Data;

/**
 * @author cys
 * @Date 2023-2023/7/15-11:39
 */
@Data
public class AUserDto {
    /**
     * id
     */
    private String id;
    /**
     * 信息
     */
    private String msg;
    /**
     * 页数
     */
    private int   page;
    /**
     * 大小
     */
    private int   size;
    /**
     * 状态
     */
    private int status;
}
