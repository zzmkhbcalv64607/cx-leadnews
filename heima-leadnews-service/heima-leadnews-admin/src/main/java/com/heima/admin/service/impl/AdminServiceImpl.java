package com.heima.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.base.Strings;
import com.heima.admin.mapper.AdUserMapper;
import com.heima.admin.service.AdminService;
import com.heima.model.admin.dtos.ALoginDto;
import com.heima.model.admin.dtos.AUserDto;
import com.heima.model.admin.pojos.AdUser;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.user.pojos.ApUser;
import com.heima.utils.common.AppJwtUtil;
import javassist.expr.NewArray;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * @author cys
 * @Date 2023-2023/7/13-19:53
 */
@Service
@Slf4j
public class AdminServiceImpl extends ServiceImpl<AdUserMapper, AdUser> implements AdminService {
    /**
     * 登录功能
     * @param loginDto
     * @return
     */
    @Override
    public ResponseResult login(ALoginDto loginDto) {
        //1.校验参数
        if(Strings.isNullOrEmpty(loginDto.getName()) || Strings.isNullOrEmpty(loginDto.getPassword())){
            return ResponseResult.errorResult(AppHttpCodeEnum.DATA_NOT_EXIST,"参数失效");
        }
        //2.根据用户名查询用户
        LambdaQueryWrapper<AdUser> wrapper = new LambdaQueryWrapper<AdUser>();
        wrapper.eq(AdUser::getName,loginDto.getName());
        AdUser adUser = this.getOne(wrapper);
        if (adUser == null){
            return ResponseResult.errorResult(AppHttpCodeEnum.DATA_NOT_EXIST,"用户不存在");
        }

        //3.校验密码
        //获取加密岩
        String salt = adUser.getSalt();
        String password = adUser.getPassword();
        password = DigestUtils.md5DigestAsHex((loginDto.getPassword() + salt).getBytes());
        if (!password.equals(adUser.getPassword())){
            return ResponseResult.errorResult(AppHttpCodeEnum.LOGIN_PASSWORD_ERROR);
        }else{
            Map<String, Object> map = new HashMap<>();
            map.put("token", AppJwtUtil.getToken(adUser.getId().longValue()));
            adUser.setPassword("");
            adUser.setSalt("");
            map.put("user",adUser);
            return ResponseResult.okResult(map);
        }
    }


}
