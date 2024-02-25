package com.heima.article.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.heima.model.article.dtos.ArticleCommentDto;
import com.heima.model.article.dtos.ArticleHomeDto;
import com.heima.model.article.pojos.ApArticle;
import com.heima.model.article.vos.ArticleCommnetVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author cys
 * @Date 2023/7/2 0:13
 */
@Mapper
public interface ApArticleMapper extends BaseMapper<ApArticle> {
    /**
     * 加载文章首页数据
     * @param dto 参数封装对象
     * @param type 1 加载更多 2 加载最新
     * @return 文章列表
     */
    public List<ApArticle> loadArticleList(ArticleHomeDto dto, Short type);


    public List<ApArticle> findArticleListByLast5days(@Param("dayParam") Date dayParam);

    public Map queryLikesAndConllections(@Param("wmUserId") Integer wmUserId, @Param("beginDate") Date beginDate, @Param("endDate")  Date endDate);

    List<ArticleCommnetVo> findNewsComments(@Param("dto")  ArticleCommentDto dto);

    int findNewsCommentsCount(@Param("dto") ArticleCommentDto dto);
}
