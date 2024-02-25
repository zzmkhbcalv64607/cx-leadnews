package com.heima.schedule.service;

import com.heima.model.schedule.dtos.Task;

/**
 * @author cys
 * @Date 2023-2023/7/8-18:29
 */
public interface TaskService {
    /**
     * 添加延迟任务
     * @param task
     * @return
     */
    public long addTask(Task task);

    /**
     * 取消延迟任务
     * @param taskId
     * @return
     */
    public boolean cancelTask(Long taskId);

    /**
     * 按照类型和优先级 从redis中拉取任务
     * @param type
     * @param priority
     * @return
     */
    public Task pull(int type,int priority);


}
