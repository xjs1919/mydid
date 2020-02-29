package com.xjs.server.mydid.vo;

public enum ErrorCode {

    SERVER_ERROR(40000, "服务端异常"),
    CLOCK_ERROR(40001, "服务器时钟回退太多，请联系管理员"),

    ;

    private int code;
    private String msg;

    private ErrorCode(int code, String msg){
        this.code = code;
        this.msg = msg;
    }

    public int getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }
}
