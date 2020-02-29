package com.xjs.sdk.mydid.service;

import com.xjs.sdk.mydid.config.MydidProperties;
import com.xjs.sdk.mydid.id.IdFactory;
import org.springframework.web.client.RestTemplate;

public class MydidService {

    /**
     * 初始化方法
     * @param template
     * @param properties sdk会注入这个bean
     * */
    public MydidService(RestTemplate template, MydidProperties properties){
        IdFactory.init(template, properties.getService_url(), properties.getCache_size());
    }

    /**
     *  获取一个ID号
     * */
    public long getId(){
        return IdFactory.getId();
    }

    /**
     * 获取一个ID号
     * @param timeoutMillSeconds 等待的最大毫秒数
     * @return 如果超过了timeoutMillSeconds没有可用的id，就会返回0
     * */
    public long getId(int timeoutMillSeconds) {
        return IdFactory.getId(timeoutMillSeconds);
    }

}
