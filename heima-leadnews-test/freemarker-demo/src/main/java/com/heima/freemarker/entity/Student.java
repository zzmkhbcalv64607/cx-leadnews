package com.heima.freemarker.entity;

import lombok.Data;

/**
 * @author cys
 * @Date 2023/7/2 10:43
 */

import java.util.Date;
@Data
public class Student {
    private String name;//姓名
    private int age;//年龄
    private Date birthday;//生日
    private Float money;//钱包
}
