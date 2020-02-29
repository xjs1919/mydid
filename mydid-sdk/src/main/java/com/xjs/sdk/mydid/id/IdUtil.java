package com.xjs.sdk.mydid.id;

import com.xjs.sdk.mydid.util.StringUtil;
import com.xjs.sdk.mydid.vo.ResultVo;
import org.springframework.web.client.RestTemplate;

import java.util.List;

class IdUtil {

    private IdUtil(){}

    /**
     *  批量获取id
     * @param count
     * @return 如果成功，返回count个id号，如果失败返回null
     * */
    public static List<Long> getIds(RestTemplate restTemplate, String uri, int count){
        String url = StringUtil.urlAppendParams(uri, "count="+count);
        ResultVo resultVo = restTemplate.getForObject(url, ResultVo.class);
        if(resultVo.isOk()){
            return (List<Long>)resultVo.getData();
        }else{
            return null;
        }
    }
}
