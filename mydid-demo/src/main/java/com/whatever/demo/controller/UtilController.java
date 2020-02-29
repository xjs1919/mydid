package com.whatever.demo.controller;

import com.xjs.sdk.mydid.config.MydidConfig;
import com.xjs.sdk.mydid.id.IdFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@Slf4j
@RequestMapping("/util")
public class UtilController {

    @Value("${mydid.service_url}")
    private String idUrl;

    @Value("${mydid.cache_size}")
    private int cacheSize;

    @GetMapping("/hello")
    public String hello(){
        IdFactory.init(MydidConfig.creatRestTemplate(), idUrl, cacheSize);
        long id1 = IdFactory.getId();
        long id2 = IdFactory.getId();
        return ""+id1+","+id2;
    }


}
