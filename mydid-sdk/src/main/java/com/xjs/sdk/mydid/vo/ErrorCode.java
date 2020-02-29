package com.xjs.sdk.mydid.vo;

public enum ErrorCode {

    ID_URL_EMPTY(50500, "ID服务URL不能为空"),
    ID_SERVICE_ERROR(50501, "ID服务异常，请稍后重试"),
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
