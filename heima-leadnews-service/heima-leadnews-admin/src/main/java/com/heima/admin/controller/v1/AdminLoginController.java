package com.heima.admin.controller.v1;

import com.heima.admin.service.AdminService;
import com.heima.model.admin.dtos.ALoginDto;
import com.heima.model.common.dtos.ResponseResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author cys
 * @Date 2023/7/2 0:03
 */
@RestController
@RequestMapping("/login")
public class AdminLoginController {
    @Autowired
    private AdminService adminService;
    /**
     * 登录功能
     * @param dto
     * @return
     */
    @PostMapping("/in")
    public ResponseResult login(@RequestBody ALoginDto dto){
        return adminService.login(dto);
    }

}
