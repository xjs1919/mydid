package com.xjs.sdk.mydid.id;

import com.xjs.sdk.mydid.util.ThreadPoolUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.concurrent.*;

@Slf4j
class IdBuffer {

    /**缓存的最小数量*/
    private static final int MIN_CACHE_SIZE = 50;
    /**缓存的最大数量*/
    private static final int MAX_CACHE_SIZE = 1000;
    /**缓存*/
    private static LinkedBlockingQueue<Long> QUEUE = new LinkedBlockingQueue<Long>(MAX_CACHE_SIZE);

    private RestTemplate restTemplate;
    private String url;
    private int cacheSize;

    public void init(RestTemplate restTemplate, String url, int cacheSize) {
        this.restTemplate = restTemplate;
        this.url = url;
        this.cacheSize = (cacheSize<=MIN_CACHE_SIZE?MIN_CACHE_SIZE:cacheSize>MAX_CACHE_SIZE?MAX_CACHE_SIZE:cacheSize);
        initLoad();
        checkPeriodic();
    }

    /**
     * 获取ID号
     * @param timeoutMillSeconds 等待的最大毫秒数
     * @return 如果超过了timeoutSeconds没有可用的id，就会返回0
     * */
    public long get(int timeoutMillSeconds) {
        try{
            LinkedBlockingQueue<Long> queue = QUEUE;
            if(queue.size() <= cacheSize/2){
                ThreadPoolUtil.execute(()->{loadFromRemote(cacheSize-queue.size());});
            }
            Long id = null;
            if(timeoutMillSeconds > 0){
                id = queue.poll(timeoutMillSeconds, TimeUnit.MILLISECONDS);
            }else{
                id = queue.take();
            }
            if(id == null){
                return 0;
            }
            return id;
        }catch(Exception e){
            log.error(e.getMessage(), e);
            return 0;
        }
    }

    /**初始化加载*/
    private void initLoad(){
        ThreadPoolUtil.execute(()->{loadFromRemote(cacheSize);});
    }

    /**
     * 每5分钟检查一下缓存数量
     * */
    private void checkPeriodic(){
        ThreadPoolUtil.scheduleWithFixedDelay(new CheckBufferSizeTask(), 5*60, 5*60);
    }

    private class CheckBufferSizeTask implements Runnable{
        @Override
        public void run(){
            log.info("CheckBufferSizeTask,size:{}", QUEUE.size());
            if(QUEUE.size() < cacheSize/2){
                log.info("CheckBufferSizeTask start load from remote...");
                loadFromRemote(cacheSize-QUEUE.size());
            }
        }
    }

    private void loadFromRemote(int count){
        log.info("调用ID服务接口，cacheSize:{}", count);
        try{
            List<Long> ids = IdUtil.getIds(restTemplate, url, count);
            if(ids != null && ids.size() >=0 ){
                for(Long id : ids){
                    QUEUE.put(id);
                }
            }
        }catch(Exception e){
            log.error("调用接口失败", e);
        }
    }

}
