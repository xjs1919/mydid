package com.xjs.sdk.mydid.id;

import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.atomic.AtomicBoolean;

public class IdFactory {

    private static IdFactory instance = new IdFactory();
    private IdBuffer buffer;
    private AtomicBoolean init;

    private IdFactory(){
        buffer = new IdBuffer();
        init = new AtomicBoolean(false);
    }

    /**
     *  初始化
     * @param restTemplate
     * @param url id服务的地址
     * @param cacheSize 本地缓存的id的数量
     * */
    public static void init(RestTemplate restTemplate, String url, int cacheSize){
        if(restTemplate == null){
            throw new RuntimeException("初始化restTemplate不能为空");
        }
        if(StringUtils.isEmpty(url)){
            throw new RuntimeException("初始化url不能为空");
        }
        if(!instance.init.compareAndSet(false,true)){
            return;
        }
        instance.buffer.init(restTemplate, url, cacheSize);
    }

    /**
     *  获取一个ID号
     * */
    public static long getId(){
        return getId(0);
    }

    /**
     * 获取一个ID号
     * @param timeoutMillSeconds 等待的最大毫秒数
     * @return 如果超过了timeoutMillSeconds没有可用的id，就会返回0
     * */
    public static long getId(int timeoutMillSeconds) {
        if(!instance.init.get()){
            throw new RuntimeException("请先初始化IdFactory");
        }
        return instance.buffer.get(timeoutMillSeconds);
    }

}
