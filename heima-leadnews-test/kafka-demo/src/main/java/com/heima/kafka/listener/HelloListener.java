package com.heima.kafka.listener;

import com.alibaba.fastjson.JSON;
import com.heima.kafka.pojo.User;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * @author cys
 * @Date 2023-2023/7/12-10:41
 */
@Component
public class HelloListener {
    @KafkaListener(topics = "user-test")
    public void onMessage(String message){
        User user = JSON.parseObject(message, User.class);
        System.out.println("接收到的消息："+user);
    }
}
