package com.heima.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.heima.model.user.pojos.ApUserRealname;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.user.dtos.AuthDto;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * @author cys
 */
public interface ApUserRealnameService extends IService<ApUserRealname> {

    /**
     * 按照状态分页查询用户列表
     * @param dto
     * @return
     */
    public ResponseResult loadListByStatus(AuthDto dto );


    /**
     * 审核失败
     * @param dto
     * @return
     */
    public ResponseResult authFail( AuthDto dto, Short status);

}