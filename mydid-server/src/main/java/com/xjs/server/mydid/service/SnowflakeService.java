package com.xjs.server.mydid.service;

import com.xjs.server.leaf.common.Result;
import com.xjs.server.leaf.snowflake.InitException;
import com.xjs.server.leaf.snowflake.SnowflakeIDGenImpl;
import com.xjs.server.mydid.config.LeafProperties;
import com.xjs.server.mydid.vo.ErrorCode;
import com.xjs.server.mydid.vo.ResultVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class SnowflakeService implements InitializingBean {

    @Value("${server.port:8080}")
    private int port;

    @Autowired
    LeafProperties leafProperties;

    private SnowflakeIDGenImpl idGen;

    @Override
    public void afterPropertiesSet() throws Exception{
        String zkAddress = leafProperties.getZk().getAddress();
        idGen = new SnowflakeIDGenImpl(zkAddress, port);
        if(idGen.init()) {
            log.info("Snowflake Service Init Successfully");
        } else {
            throw new InitException("Snowflake Service Init Fail");
        }
    }

    public ResultVo getId(int cnt) {
        List<Long> ids = new ArrayList<Long>(cnt);
        for(int i=0; i<cnt; i++){
            Result ret = idGen.get();
            if(!ret.isOk()){
                return ResultVo.fail(ErrorCode.CLOCK_ERROR);
            }
            ids.add(ret.getId());
        }
        return ResultVo.success(ids);
    }
}
