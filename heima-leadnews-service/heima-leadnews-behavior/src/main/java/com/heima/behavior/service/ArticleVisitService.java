package com.heima.behavior.service;

import com.heima.model.behavior.LikesBehaviorDto;
import com.heima.model.common.dtos.ResponseResult;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * @author cys
 * @Date 2023-2023/7/17-9:54
 */
public interface ArticleVisitService {

    /**
     * 点赞
     * @param dto
     * @return
     */
    public ResponseResult liked(LikesBehaviorDto dto);
}
