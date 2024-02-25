package com.heima.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.user.dtos.LoginDto;
import com.heima.model.user.pojos.ApUser;

/**
 * @author cys
 * @Date 2023/7/1 15:48
 */
public interface ApUserService extends IService<ApUser> {

    /**
     * app端登录功能
     * @param dto 登录参数
     * @return 登录结果
     */
    public ResponseResult login(LoginDto dto);
}
