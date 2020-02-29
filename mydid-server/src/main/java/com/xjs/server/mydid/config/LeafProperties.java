package com.xjs.server.mydid.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "leaf")
@Data
public class LeafProperties {

    private Zk zk;

    @Data
    public static class Zk{
        private String address;
    }
}
