package com.heima.article.listener;

import com.alibaba.fastjson.JSON;
import com.heima.article.service.ApArticleConfigService;
import com.heima.common.constants.WmNewsMessageConstants;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @author cys
 * @Date 2023-2023/7/12-11:30
 */
@Component
@Slf4j
public class ArticleDownListener {

    @Autowired
    private ApArticleConfigService apArticleConfigService;

    /**
     * 监听文章下架的消息
     * @param message
     */
    @KafkaListener(topics = WmNewsMessageConstants.WM_NEWS_UP_OR_DOWN_TOPIC)
    public void handler(String message){
        log.info("文章下架监听器监听到消息：{}",message);
        if (StringUtils.isNoneBlank(message)){
            Map map = JSON.parseObject(message, Map.class);
            apArticleConfigService.updateMap(map);
            log.info("article端文章配置修改，articleId={}",map.get("articleId"));

        }

    }
}
