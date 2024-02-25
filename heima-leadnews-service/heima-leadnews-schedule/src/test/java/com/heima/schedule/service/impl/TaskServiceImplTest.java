package com.heima.schedule.service.impl;

import com.heima.model.schedule.dtos.Task;
import com.heima.schedule.ScheduleApplication;
import com.heima.schedule.service.TaskService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;

import static org.junit.Assert.*;

/**
 * @author cys
 * @Date 2023-2023/7/9-9:24
 */
@SpringBootTest(classes = ScheduleApplication.class)
@RunWith(SpringRunner.class)
@EnableScheduling
public class TaskServiceImplTest {


    @Autowired
    private TaskService taskService;
    @Test
    public void addTask() {
        for (int i = 0; i < 5; i++) {
            Task task = new Task();
            task.setTaskType(100+i);
            task.setPriority(50);
            task.setParameters("task test".getBytes());
            task.setExecuteTime(new Date().getTime()+500*i);

            long taskId = taskService.addTask(task);
        }
    }


    @Test
    public void cancelTask(){
        boolean b = taskService.cancelTask(1677856027920158721L);
        System.out.println(b);
    }




    @Test
    public void pullTask(){
        Task pull = taskService.pull(101, 51);
        System.out.println(pull);
    }
}