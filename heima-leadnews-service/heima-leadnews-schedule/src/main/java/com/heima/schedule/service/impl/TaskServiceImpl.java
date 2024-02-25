package com.heima.schedule.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.heima.common.constants.ScheduleConstants;
import com.heima.common.redis.CacheService;
import com.heima.model.schedule.dtos.Task;
import com.heima.model.schedule.pojos.Taskinfo;
import com.heima.model.schedule.pojos.TaskinfoLogs;
import com.heima.schedule.mapper.TaskinfoLogsMapper;
import com.heima.schedule.mapper.TaskinfoMapper;
import com.heima.schedule.service.TaskService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * @author cys
 * @Date 2023-2023/7/8-18:31
 */
@Service
@Transactional
@Slf4j
public class TaskServiceImpl implements TaskService {

    @Autowired
    private TaskinfoMapper taskinfoMapper;
    @Autowired
    private TaskinfoLogsMapper taskinfoLogsMapper;
    @Autowired
    private CacheService cacheService;
    /**
     * 添加延迟任务
     *
     * @param task
     * @return
     */
    @Override
    public long addTask(Task task) {
        // 1.添加任务到数据库中
        boolean success = addTaskToDb(task);
        // 2.添加任务到redis中
        if (success) {
            addTaskToRedis(task);
        }


        return task.getTaskId();
    }

    /**
     * 取消延迟任务
     *
     * @param taskId
     * @return
     */
    @Override
    public boolean cancelTask(Long taskId) {
        boolean flag = false;
        //删除任务 更新任务日志
        Task task = updateBb(taskId,ScheduleConstants.CANCELLED);
        //删除redis的数据
        if (task!=null){
            removeTaskFromCache(task);
            flag = true;
        }
        return flag;
    }


    /**
     * 按照类型和优先级 从redis中拉取任务
     * @param type
     * @param priority
     * @return
     */
    @Override
    public Task pull(int type, int priority) {
        Task task =null;
        try {
            String  key = type + "_" + priority;
            // 1.从redis中获取任务 pop
            String task_json = cacheService.lRightPop(ScheduleConstants.TOPIC + key);
            if (StringUtils.isNoneBlank(task_json)) {
                task = JSON.parseObject(task_json, Task.class);

                //修改数据库
                updateBb(task.getTaskId(),ScheduleConstants.EXECUTED);
            }
        }catch (Exception e){
            e.printStackTrace();
            log.error("拉取任务失败",e);
        }

        // 2.修改数据库信息
        return task;
    }

    /**
     * 删除redis的数据
     * @param task
     */
    private void removeTaskFromCache(Task task) {
        String key = task.getTaskType() + "_" + task.getPriority();
        // 2.1. 如果任务执行时间小于等于当前时间，存入list
        if (task.getExecuteTime() <= System.currentTimeMillis()) {
            cacheService.lRemove(ScheduleConstants.TOPIC+key,0,JSON.toJSONString(task));
        }else {
            cacheService.zRemove(ScheduleConstants.TOPIC+key,JSON.toJSONString(task));
        }
    }

    /**
     * 删除任务  更新任务日志
     * @param taskId
     * @param status
     * @return
     */
    private Task updateBb(Long taskId, int status) {

        Task task = null;
        try {
            //删除任务
            taskinfoMapper.deleteById(taskId);

            //更新任务日志
            TaskinfoLogs taskinfoLogs = taskinfoLogsMapper.selectById(taskId);
            taskinfoLogs.setStatus(status);
            taskinfoLogsMapper.updateById(taskinfoLogs);
            task = new Task();
            BeanUtils.copyProperties(taskinfoLogs,task);
            task.setExecuteTime(taskinfoLogs.getExecuteTime().getTime());
        }catch (Exception e){
            log.error("删除任务失败 taskid={}",taskId);

        }

        return  task;

    }

    /**
     * 添加任务到redis中
     *
     * @param task
     */
    private void addTaskToRedis(Task task) {
        String key = task.getTaskType() + "_" + task.getPriority();

        //获取五分钟之后的时间
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, 5);
        long futureTime = calendar.getTimeInMillis();
        // 2.1. 如果任务执行时间小于等于当前时间，存入list
        if (task.getExecuteTime() <= System.currentTimeMillis()) {
            cacheService.lLeftPush(ScheduleConstants.TOPIC+key,
                    JSON.toJSONString(task));
        }else if (task.getExecuteTime()<=futureTime){
            // 2.2. 如果任务执行时间大于当前时间 && 小于等于预设时间（未来五分钟）存入zset
            cacheService.zAdd(ScheduleConstants.FUTURE+key,
                    JSON.toJSONString(task),task.getExecuteTime());
        }

    }

        /**
         * 添加任务到数据库中
         * @param task
         * @return
         */
        private boolean addTaskToDb (Task task){
            boolean flag = false;
            try {
                //保存任务表
                Taskinfo taskinfo = new Taskinfo();
                BeanUtils.copyProperties(task, taskinfo);
                taskinfo.setExecuteTime(new Date(task.getExecuteTime()));
                taskinfoMapper.insert(taskinfo);

                //设置taskID
                task.setTaskId(taskinfo.getTaskId());

                //保存任务日志表
                TaskinfoLogs taskinfoLogs = new TaskinfoLogs();
                BeanUtils.copyProperties(task, taskinfoLogs);
                taskinfoLogs.setVersion(1);
                taskinfoLogs.setExecuteTime(new Date(task.getExecuteTime()));
                taskinfoLogs.setStatus(ScheduleConstants.SCHEDULED);
                taskinfoLogsMapper.insert(taskinfoLogs);
                flag = true;
            } catch (Exception e) {
                e.printStackTrace();
            }

            return flag;

        }

        /**
         * 未来数据定时刷新
         */
        @Scheduled(cron = "0 */1 * * * ?")
        public void refresh(){
            String token = cacheService.tryLock("FUTURE_TASK_SYNC", 1000 * 30);
            if (StringUtils.isBlank(token)) {
                System.out.println(System.currentTimeMillis() / 1000 + "执行了定时任务");
                log.info("执行了定时任务");

                // 获取所有未来数据集合的key值// future_*
                Set<String> futureKeys = cacheService.scan(ScheduleConstants.FUTURE + "*");
                // future_250_250
                for (String futureKey : futureKeys) {

                    String topicKey = ScheduleConstants.TOPIC + futureKey.split(ScheduleConstants.FUTURE)[1];
                    //获取该组key下当前需要消费的任务数据
                    Set<String> tasks = cacheService.zRangeByScore(futureKey, 0, System.currentTimeMillis());
                    if (!tasks.isEmpty()) {
                        //将这些任务数据添加到消费者队列中
                        cacheService.refreshWithPipeline(futureKey, topicKey, tasks);
                        System.out.println("成功的将" + futureKey + "下的当前需要执行的任务数据刷新到" + topicKey + "下");
                        log.info("执行了定时任务");
                    }
                }

            }


        }
        /**
         * 数据库任务定时同步到redis
         */
        @PostConstruct
        @Scheduled(cron = "0 */5 * * * ?")
        public  void reloadData(){
            //清理缓存中的数据 list zset
            clearCache();
            //查询符合条件的任务 小于未来五分钟的任务
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.MINUTE, 5);
            List<Taskinfo> allTasks = taskinfoMapper.
                    selectList(Wrappers.<Taskinfo>lambdaQuery().
                            lt(Taskinfo::getExecuteTime,calendar.getTime()));
            //把任务添加到redis中
            if (allTasks!=null && allTasks.size()>0){
                for (Taskinfo taskinfo : allTasks) {
                    Task task = new Task();
                    BeanUtils.copyProperties(taskinfo,task);
                    task.setExecuteTime(taskinfo.getExecuteTime().getTime());
                    addTaskToRedis(task);
                }
            }
            log.info("成功的将数据库中的任务数据同步到redis中");

        }


        /**
         * 清理缓存
         */
        public void clearCache(){
            //清理缓存中的数据 list zset
            Set<String> topiKeys = cacheService.scan(ScheduleConstants.TOPIC + "*");
            Set<String> futureKeys = cacheService.scan(ScheduleConstants.FUTURE + "*");
            cacheService.delete(topiKeys);
            cacheService.delete(futureKeys);
        }
    }

