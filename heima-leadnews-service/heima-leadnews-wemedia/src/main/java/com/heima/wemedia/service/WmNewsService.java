package com.heima.wemedia.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.wemedia.dtos.WmNewsDto;
import com.heima.model.wemedia.dtos.WmNewsPageReqDto;
import com.heima.model.wemedia.pojos.WmNews;
import org.apache.kafka.streams.processor.internals.PunctuationQueue;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author cys
 */
public interface WmNewsService extends IService<WmNews> {

    /**
     * 查询自媒体文章列表
     * @param dto
     * @return
     */
    public ResponseResult findList(WmNewsPageReqDto dto);

    /**
     * 查询文章详情
     * @param id
     * @return
     */
    public ResponseResult findOne( Integer id);

    /**
     * 删除文章
     * @param id
     * @return
     */
    public ResponseResult delNews( Integer id);

    /**
     * 文章上下架
     * @param dto
     * @return
     */
    public ResponseResult downOrUp(WmNewsDto dto);

    /**
     * 提交修改文章 保存为草稿
     * @param dto
     * @return
     */
    public ResponseResult submitNews( WmNewsDto dto);

    /**
     * 分页查询自媒体文章列表
     * @param dto
     * @return
     */
    public ResponseResult findListAndPage(WmNewsPageReqDto dto);

    /**
     * 查询文章详情
     * @param id
     * @return
     */
    public ResponseResult find(Integer id);

    /**
     * 修改文章状态
     * @param dto
     * @return
     */
    public ResponseResult updateStatus(WmNewsDto dto);
}
