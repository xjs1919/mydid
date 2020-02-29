package com.xjs.server.leaf.snowflake;

public class CheckLastTimeException extends RuntimeException {
    public CheckLastTimeException(String msg){
        super(msg);
    }
}
