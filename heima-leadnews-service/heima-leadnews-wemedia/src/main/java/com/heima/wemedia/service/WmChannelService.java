package com.heima.wemedia.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.wemedia.dtos.WmChannelDto;
import com.heima.model.wemedia.pojos.WmChannel;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * @author cys
 */
public interface WmChannelService extends IService<WmChannel> {
    /**
     * 查询所有自媒体频道
     * @return
     */
    public ResponseResult findAll();

    /**
     * 分页查询频道列表
     * @param dto
     * @return
     */
    public ResponseResult findList( WmChannelDto dto);

    /**
     * 保存自媒体频道
     * @param dto
     * @return
     */
    public ResponseResult saveChannel(WmChannel dto);

    /**
     * 删除自媒体频道
     * @param id
     * @return
     */
    public ResponseResult delChannel(Integer id);

    /**
     * 修改自媒体频道
     * @param wmChannel
     * @return
     */
    public ResponseResult updateChannel(WmChannel wmChannel);
}