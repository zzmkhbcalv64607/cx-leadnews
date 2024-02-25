package com.heima.wemedia.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.heima.model.common.dtos.PageResponseResult;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.wemedia.dtos.WmChannelDto;
import com.heima.model.wemedia.pojos.WmChannel;
import com.heima.model.wemedia.pojos.WmNews;
import com.heima.wemedia.mapper.WmChannelMapper;
import com.heima.wemedia.service.WmChannelService;
import com.heima.wemedia.service.WmNewsService;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

/**
 * @author cys
 */
@Service
@Transactional
@Slf4j
public class WmChannelServiceImpl extends ServiceImpl<WmChannelMapper, WmChannel> implements WmChannelService {


    @Autowired
    private WmNewsService wmNewsService;
    /**
     * 查询所有自媒体频道
     * @return
     */
    @Override
    public ResponseResult findAll() {
        return ResponseResult.okResult(list());
    }

    /**
     * 分页查询频道列表
     * @param dto
     * @return
     */
    @Override
    public ResponseResult findList(WmChannelDto dto) {
        //参数检查
        if (dto ==null ){
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }

        //页面参数检查
        dto.checkParam();
        //分页查询
        Page<WmChannel> page1 =null;
        Page<WmChannel> page = new Page<WmChannel>(dto.getPage(),dto.getSize());
        if (dto.getName() != null){
            LambdaQueryWrapper<WmChannel> wrapper = new LambdaQueryWrapper<>();
            wrapper.like(WmChannel::getName,dto.getName());
            page1 = page(page, wrapper);
        }else{
            page1 = page(page);
        }
        ResponseResult responseResult = new PageResponseResult(dto.getPage(), dto.getSize(), (int)page.getTotal());
        responseResult.setData(page1.getRecords());
        return responseResult;
    }

    @Override
    public ResponseResult saveChannel(WmChannel dto) {
        //参数检查
        if (dto == null){
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }
        //查询有没有重复的频道 重复的频道不能添加
        LambdaQueryWrapper<WmChannel> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WmChannel::getName,dto.getName());
        WmChannel one = getOne(wrapper);
        if (one != null){
            return ResponseResult.errorResult(AppHttpCodeEnum.DATA_EXIST,"频道名称不能重复");
        }
        //保存频道
        dto.setCreatedTime(new Date());
        dto.setIsDefault(Boolean.TRUE);
        boolean save = save(dto);
        if (!save){
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID,"保存频道失败");
        }
        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
    }

    /**
     * 删除自媒体频道
     * @param id
     * @return
     */
    @Override
    public ResponseResult delChannel(Integer id) {
        //参数检查
        if (id == null){
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }
        //查询频道是否存在
        WmChannel one = getById(id);
        if (one == null){
            return ResponseResult.errorResult(AppHttpCodeEnum.DATA_NOT_EXIST,"频道不存在");
        }
        //检查是否有文章使用该频道
        LambdaQueryWrapper<WmNews> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WmNews::getChannelId,id);
        int count = wmNewsService.count(wrapper);
        if (count > 0){
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID,"频道下有文章，不能删除");
        }
        //删除频道
        boolean b = removeById(id);
        if (!b){
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID,"删除频道失败");
        }
        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
    }

    @Override
    public ResponseResult updateChannel(WmChannel wmChannel) {
        if (wmChannel == null){
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }
        LambdaQueryWrapper<WmNews> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WmNews::getChannelId,wmChannel.getId());
        int count = wmNewsService.count(wrapper);
        if (count > 0){
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID,"频道下有文章，不能删除");
        }
        boolean b = updateById(wmChannel);
        if (!b){
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID,"更新频道失败");
        }
        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
    }
}