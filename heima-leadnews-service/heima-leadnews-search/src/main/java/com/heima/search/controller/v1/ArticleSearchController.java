package com.heima.search.controller.v1;

import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.search.dtos.UserSearchDto;
import com.heima.search.service.ArticleSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

/**
 * @author cys
 * @Date 2023-2023/7/13-14:36
 */
@RestController
@RequestMapping("/api/v1/article/search/")
public class ArticleSearchController {

    @Autowired
    private ArticleSearchService articleSearchService;
    /**
     * es文章分页搜索
     * @param userSearchDto
     * @return
     */
    @PostMapping("search")
    public ResponseResult search(@RequestBody UserSearchDto userSearchDto) throws IOException {
        return articleSearchService.search(userSearchDto);
    }
}
