package com.xjs.sdk.mydid.config;

import com.xjs.sdk.mydid.service.MydidService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;

@Configuration
@EnableConfigurationProperties(MydidProperties.class)
public class MydidConfig {

    @Resource(type=RestTemplate.class)
    private RestTemplate restTemplate;

    @Resource
    private MydidProperties properties;

    @Bean
    public MydidService mydidService(){
        return new MydidService(restTemplate, properties);
    }

    @Bean
    @ConditionalOnMissingBean
    public RestTemplate restTemplate() {
        return creatRestTemplate();
    }

    public static RestTemplate creatRestTemplate(){
        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
        requestFactory.setConnectionRequestTimeout(3000);
        requestFactory.setConnectTimeout(3000);
        requestFactory.setReadTimeout(3000);
        return new RestTemplate(requestFactory);
    }

}
