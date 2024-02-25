package com.heima.user.controller.v1;

import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.user.dtos.LoginDto;
import com.heima.user.service.ApUserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author cys
 * @Date 2023/7/1 15:45
 */
@RestController
@RequestMapping("/api/v1/login")
@Api(value = "app端登录功能",tags = "app端登录功能")
public class ApUserLoginController {

    @Autowired
    private ApUserService apUserService;
    /**
     * app端登录功能
     * @param dto 登录参数
     * @return 登录结果
     */
    @PostMapping("/login_auth")
    @ApiOperation("app端登录功能")
    public ResponseResult login(@RequestBody LoginDto dto) {
        return apUserService.login(dto);
    }
}
