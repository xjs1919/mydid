package com.xjs.sdk.mydid.config;

import org.springframework.context.annotation.Import;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * @Description 业务方可以引用这个注解，注入CaptchaService
 * */
@Retention(value = java.lang.annotation.RetentionPolicy.RUNTIME)
@Target(value = { java.lang.annotation.ElementType.TYPE })
@Import({MydidConfig.class})
public @interface EnableIdService {

}