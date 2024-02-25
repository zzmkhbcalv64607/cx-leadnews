package com.heima.wemedia.controller.v1;

import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.wemedia.dtos.WmChannelDto;
import com.heima.model.wemedia.pojos.WmChannel;
import com.heima.wemedia.service.WmChannelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @author cys
 * @Date 2023/7/2 22:49
 */
@RestController
@RequestMapping("/api/v1/channel")
public class WmchannelController {

    @Autowired
    private WmChannelService wmChannelService;

    /**
     * 查询所有自媒体频道
     * @return
     */
    @GetMapping("/channels")
    public ResponseResult findAll(){
        return wmChannelService.findAll();
    }

    /**
     * 分页查询频道列表
     * @param dto
     * @return
     */
    @PostMapping("/list")
    public ResponseResult findList(@RequestBody WmChannelDto dto){
        return wmChannelService.findList(dto);
    }

    /**
     * 保存改频道
     * @param dto
     * @return
     */
    @PostMapping("/save")
    public  ResponseResult saveChannel(@RequestBody WmChannel dto){
        return wmChannelService.saveChannel(dto);
    }

    @GetMapping("/del/{id}")
    public ResponseResult delChannel(@PathVariable("id") Integer id){
        return wmChannelService.delChannel(id);
    }
    @PostMapping("/update")
    public ResponseResult updateChannel(@RequestBody WmChannel wmChannel){
        return wmChannelService.updateChannel(wmChannel);
    }

}
