package com.heima.utils.thread;

import com.heima.model.wemedia.pojos.WmUser;

/**
 * @author cys
 * @Date 2023/7/2 16:40
 */
public class WmThreadLocalUtil {

    private final static ThreadLocal<WmUser> WM_USER_THREAD_LOCAL = new ThreadLocal<>();

    //存入线程
    public static void setUser(WmUser wmUser){
        WM_USER_THREAD_LOCAL.set(wmUser);
    }
    //取出线程
    public static WmUser getUser(){
        return WM_USER_THREAD_LOCAL.get();
    }
    ///清理线程
    public static void clear(){
        WM_USER_THREAD_LOCAL.remove();
    }
}
