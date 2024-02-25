package com.heima.model.admin.dtos;


import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author cys
 */
@Data
public class ALoginDto {

    /**
     * 登录用户名
     */
    @ApiModelProperty(value = "账号",required = true)
    private String name;
    /**
     * 密码
     */
    @ApiModelProperty(value = "密码",required = true)
    private String password;
}
