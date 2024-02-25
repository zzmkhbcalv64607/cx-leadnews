package com.heima.admin.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * @author cys
 * @Date 2023-2023/7/13-11:22
 */

@SpringBootApplication
@EnableDiscoveryClient
public class AdminGateWayApplication {
    public static void main(String[] args) {
        SpringApplication.run(AdminGateWayApplication.class, args);
    }
}
