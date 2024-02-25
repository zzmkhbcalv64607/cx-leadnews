package com.heima.search.service;

import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.search.dtos.HistorySearchDto;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * @author cys
 * @Date 2023-2023/7/14-14:48
 */
public interface ApUserSearchService {
    /**
     * 保存用户搜索记录
     * @param keyword
     * @param userId
     */
    public void insert(String keyword, Integer userId);

    /**
     * 加载用户搜索记录
     * @return
     */
    public ResponseResult findUserSearch();

    /**
     * 删除用户搜索记录
     * @param historySearchDto
     * @return
     */
    public ResponseResult delUserSearch( HistorySearchDto historySearchDto);
}
