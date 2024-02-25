package com.heima.article.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.heima.model.article.pojos.ApArticleConfig;
import com.heima.model.comment.dtos.CommentConfigDto;
import com.heima.model.common.dtos.ResponseResult;

import java.util.Map;

/**
 * @author cys
 * @Date 2023-2023/7/12-11:35
 */
public interface ApArticleConfigService extends IService<ApArticleConfig> {
    /**
     * 更新文章配置
     * @param map
     */
    void updateMap(Map map);

    /**
     * 修改文章评论状态
     * @return
     */
    public ResponseResult updateCommentStatus(CommentConfigDto dto);
}
