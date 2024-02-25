package com.heima.wemedia.controller.v1;

import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.wemedia.dtos.WmNewsDto;
import com.heima.model.wemedia.dtos.WmNewsPageReqDto;
import com.heima.wemedia.service.WmNewsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @author cys
 * @Date 2023/7/2 23:02
 */
@RestController
@RequestMapping("/api/v1/news")
public class WmNewsController {

    @Autowired
    private WmNewsService wmNewsService;
    /**
     * 查询自媒体文章列表
     * @return
     */
    @PostMapping("/list")
    public ResponseResult findList(@RequestBody WmNewsPageReqDto dto){
        return wmNewsService.findList(dto);
    }

    /**
     * 查询文章详情
     * @param id
     * @return
     */
    @GetMapping("/one/{id}")
    public ResponseResult findOne(@PathVariable Integer id){
        return wmNewsService.findOne(id);
    }

    /**
     * 删除文章
     * @param id
     * @return
     */
    @GetMapping("/del_news/{id}")
    public ResponseResult delNews(@PathVariable Integer id){
        return wmNewsService.delNews(id);
    }


    /**
     * 文章上下架
     * @param dto
     * @return
     */
    @PostMapping("/down_or_up")
    public ResponseResult downOrUp(@RequestBody WmNewsDto dto){
        return wmNewsService.downOrUp(dto);
    }

    /**
     * 提交文章
     * @param dto
     * @return
     */
    @PostMapping("/submit")
    public ResponseResult submitNews(@RequestBody WmNewsDto dto){
        return wmNewsService.submitNews(dto);
    }
    /**
     * 文章列表分页查询
     * @param dto
     * @return
     */
    @PostMapping("/list_vo")
    public ResponseResult findAdList(@RequestBody WmNewsPageReqDto dto){
        return wmNewsService.findListAndPage(dto);
    }
    /**
     * 查询文章详情
     * @param id
     * @return
     */
    @GetMapping("/one_vo/{id}")
    public ResponseResult find(@PathVariable("id") Integer id){
        return wmNewsService.find(id);
    }

    @PostMapping("auth_fail")
    public ResponseResult updateStatusFail(@RequestBody WmNewsDto dto){
        return wmNewsService.updateStatus(dto);
    }

    @PostMapping("auth_pass")
    public ResponseResult updateStatusPass(@RequestBody WmNewsDto dto){
        return wmNewsService.updateStatus(dto);
    }
}
