package com.xjs.server.mydid.vo;

/**API响应结果*/
public class ResultVo<T> {
    /**响应码*/
    private int errcode;
    /**错误信息*/
    private String errmsg;
    /**响应数据*/
    private T data;

    public ResultVo(){}

    private ResultVo(int code, String msg, T data){
        this.errcode = code;
        this.errmsg = msg;
        this.data =data;
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
}
