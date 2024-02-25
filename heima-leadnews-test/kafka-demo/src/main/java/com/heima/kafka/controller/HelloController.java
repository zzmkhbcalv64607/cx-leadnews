package com.heima.kafka.controller;

import com.alibaba.fastjson.JSON;
import com.heima.kafka.pojo.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author cys
 * @Date 2023-2023/7/12-10:39
 */
@RestController
public class HelloController {

    @Autowired
    private KafkaTemplate<String,String> kafkaTemplate;

    @GetMapping("/hello")
    public String hello(){
//        kafkaTemplate.send("itcast-test","hello kafka");
        User user = new User();
        user.setUsername("张三");
        user.setAge(18);
        String users = JSON.toJSONString(user);
        kafkaTemplate.send("user-test",users);
        return "success";
    }
}
