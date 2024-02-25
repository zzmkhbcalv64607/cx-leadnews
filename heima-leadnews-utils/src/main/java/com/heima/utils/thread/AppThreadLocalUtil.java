package com.heima.utils.thread;

import com.heima.model.user.pojos.ApUser;
import com.heima.model.wemedia.pojos.WmUser;

/**
 * @author cys
 * @Date 2023/7/2 16:40
 */
public class AppThreadLocalUtil {

    private final static ThreadLocal<ApUser> WM_USER_THREAD_LOCAL = new ThreadLocal<>();

    //存入线程
    public static void setUser(ApUser apUser){
        WM_USER_THREAD_LOCAL.set(apUser);
    }
    //取出线程
    public static ApUser getUser(){
        return WM_USER_THREAD_LOCAL.get();
    }
    ///清理线程
    public static void clear(){
        WM_USER_THREAD_LOCAL.remove();
    }
}
