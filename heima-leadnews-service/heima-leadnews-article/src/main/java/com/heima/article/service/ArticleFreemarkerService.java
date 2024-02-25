package com.heima.article.service;

import com.heima.model.article.pojos.ApArticle;

/**
 * @author cys
 * @Date 2023-2023/7/6-16:33
 */
public interface ArticleFreemarkerService {
    /**
     * 生成静态文件上传到minIO中
     * @param apArticle
     * @param content
     */
    public void buildArticleToMinIO(ApArticle apArticle, String content);
}
