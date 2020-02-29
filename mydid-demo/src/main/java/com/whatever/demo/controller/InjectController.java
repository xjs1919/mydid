package com.whatever.demo.controller;

import com.xjs.sdk.mydid.config.EnableIdService;
import com.xjs.sdk.mydid.service.MydidService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@EnableIdService
@RequestMapping("/inject")
public class InjectController {

    @Autowired
    MydidService mydidService;

    @GetMapping("/hello")
    public String hello(){
        long id1 = mydidService.getId();
        long id2 = mydidService.getId();
        return ""+id1+","+id2;
    }

}
