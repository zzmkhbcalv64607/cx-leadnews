package com.heima.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.heima.model.admin.dtos.ALoginDto;
import com.heima.model.admin.dtos.AUserDto;
import com.heima.model.admin.pojos.AdUser;
import com.heima.model.common.dtos.ResponseResult;

/**
 * @author cys
 * @Date 2023-2023/7/13-19:51
 */
public interface AdminService extends IService<AdUser> {
    /**
     * 登录功能
     * @param loginDto
     * @return
     */
    public ResponseResult login(ALoginDto loginDto);

}
