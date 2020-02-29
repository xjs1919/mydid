package com.xjs.server.mydid.controller;


import com.xjs.server.mydid.service.SnowflakeService;
import com.xjs.server.mydid.vo.ResultVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/id")
@Slf4j
public class IdController {

    private static final int MAX_COUNT = 1000;

    @Autowired
    SnowflakeService snowflakeService;

    /**
     * 批量获取ID号
     * @param  request request
     * @param  count   要获取的数量
     * */
    @GetMapping("/get")
    public ResultVo getId(HttpServletRequest request, @RequestParam(value="count", defaultValue = "1")int count){
        int cnt = (count<=0?1:count>MAX_COUNT?MAX_COUNT:count);
        return snowflakeService.getId(cnt);
    }

}
