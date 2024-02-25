package com.heima.search.service;

import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.search.dtos.UserSearchDto;

import java.io.IOException;

/**
 * @author cys
 * @Date 2023-2023/7/13-14:40
 */
public interface ArticleSearchService {

    /**
     * es文章分页搜索
     * @param userSearchDto
     * @return
     */
    public ResponseResult search( UserSearchDto userSearchDto)  throws IOException;
}
