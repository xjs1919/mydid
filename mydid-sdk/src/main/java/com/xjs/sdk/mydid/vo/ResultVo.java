package com.xjs.sdk.mydid.vo;

public class ResultVo<T> {

    private int errcode;

    private String errmsg;

    private T data;

    public ResultVo(){}

    private ResultVo(int code, String msg, T data){
        this.errcode = code;
        this.errmsg = msg;
        this.data =data;
    }

    public boolean isOk(){
        return this.errcode == 0;
    }

    public static <T> ResultVo<T> success(T data){
        return new ResultVo(0, "成功", data);
    }

    public static <T> ResultVo<T> fail(ErrorCode ec){
        return new ResultVo(ec.getCode(), ec.getMsg(), null);
    }

    public static <T> ResultVo<T> fail(ErrorCode ec, T data){
        return new ResultVo(ec.getCode(), ec.getMsg(), data);
    }

    public int getErrcode() {
        return errcode;
    }

    public String getErrmsg() {
        return errmsg;
    }

    public T getData() {
        return data;
    }

    public void setErrcode(int errcode) {
        this.errcode = errcode;
    }

    public void setErrmsg(String errmsg) {
        this.errmsg = errmsg;
    }

    public void setData(T data) {
        this.data = data;
    }
}
