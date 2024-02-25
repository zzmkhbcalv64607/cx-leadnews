package com.heima.wemedia.service;

import java.util.Date;

/**
 * @author cys
 * @Date 2023-2023/7/9-22:04
 */
public  interface WmNewsTaskService {
    /**
     * 添加任务到延迟队列中
     * @param id  文章的id
     * @param publishTime  发布的时间  可以做为任务的执行时间
     */
    public void addNewsToTask(Integer id, Date publishTime);


    /**
     * 消费延迟队列数据
     */
    public void scanNewsByTask();
}
