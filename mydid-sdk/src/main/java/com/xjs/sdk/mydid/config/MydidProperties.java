package com.xjs.sdk.mydid.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "mydid")
@Data
public class MydidProperties {

    private String service_url;

    private int cache_size;

}

