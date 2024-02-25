package com.heima.article.test;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.heima.article.ArticleApplication;
import com.heima.article.mapper.ApArticleContentMapper;
import com.heima.article.service.ApArticleService;
import com.heima.file.service.FileStorageService;
import com.heima.model.article.pojos.ApArticle;
import com.heima.model.article.pojos.ApArticleContent;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * @author cys
 * @Date 2023/7/2 14:44
 */
@SpringBootTest(classes = ArticleApplication.class)
@RunWith(SpringRunner.class)
public class ArticleFreemarketTest {

    @Autowired
    private ApArticleContentMapper apArticleContentMapper;
    @Autowired
    private Configuration configuration;
    @Autowired
    private FileStorageService fileStorageService;
    @Autowired
    private ApArticleService apArticleService;


    @Test
    public void createStaticUrlTest() throws Exception{
        // 4.1 获取文章对象
        ApArticleContent apArticleContent =apArticleContentMapper.selectOne(Wrappers.<ApArticleContent>lambdaQuery()
                .eq(ApArticleContent::getArticleId, "1383827911810011137L"));
        if (apArticleContent != null && StringUtils.isNoneBlank(apArticleContent.getContent())) {
            // 4.2 文章内容通过freemarket生成静态化Html页面
            Template template = configuration.getTemplate("article.ftl");
            //数据模型
            Map<String,Object> content =new HashMap<>();

            content.put("content", JSONArray.parseArray( apArticleContent.getContent()) );

            StringWriter out =new StringWriter();
            //上传
            template.process(content,out);

            // 4.3 上传到minio中
            ByteArrayInputStream in = new ByteArrayInputStream(out.toString().getBytes());
            String path = fileStorageService.uploadHtmlFile("", apArticleContent.getArticleId() + ".html", in);

            // 4.4 生成静态化的url地址 修改ap_article表中的static_url字段
            apArticleService.update(Wrappers.<ApArticle>lambdaUpdate()
                    .eq(ApArticle::getId,apArticleContent.getArticleId())
                    .set(ApArticle::getStaticUrl,path));
        }

    }
}
