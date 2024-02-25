package com.heima.article.controller.v1;

import com.heima.article.service.ApArticleService;
import com.heima.common.constants.ArticleConstants;
import com.heima.model.article.dtos.ArticleHomeDto;
import com.heima.model.common.dtos.ResponseResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author cys
 * @Date 2023/7/2 0:03
 */
@RestController
@RequestMapping("/api/v1/article")
public class ArticleHomeController {


    @Autowired
    private ApArticleService apArticleService;
    /**
     * 加载首页文章
     * @param dto 参数封装对象
     * @return 文章列表
     */
    @RequestMapping("/load")
    public ResponseResult load(@RequestBody ArticleHomeDto dto) {
        return  apArticleService.load(dto, ArticleConstants.LOADTYPE_LOAD_MORE);
    }

    /**
     * 加载首页更多
     * @param dto 参数封装对象
     * @return 文章列表
     */
    @RequestMapping("/loadmore")
    public ResponseResult loadmore(@RequestBody ArticleHomeDto dto) {
        return  apArticleService.load(dto, ArticleConstants.LOADTYPE_LOAD_MORE);
    }

    /**
     * 加载首页最新的文章信息
     * @param dto 参数封装对象
     * @return 文章列表
     */
    @RequestMapping("/loadnew")
    public ResponseResult loadnew(@RequestBody ArticleHomeDto dto) {
        return  apArticleService.load(dto, ArticleConstants.LOADTYPE_LOAD_NEW);
    }
}
