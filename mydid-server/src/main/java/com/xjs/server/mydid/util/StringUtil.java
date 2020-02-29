package com.xjs.server.mydid.util;

import org.springframework.util.StringUtils;

public class StringUtil {

    public static byte[] toBytes(String src){
        return toBytes(src, "UTF-8");
    }

    public static byte[] toBytes(String src, String charset){
        try{
            return src.getBytes(charset);
        }catch(Exception e){
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }


    public static String toString(byte[] data){
        return toString(data, "UTF-8");
    }

    public static String toString(byte[] data, String charset){
        try{
            return new String(data, charset);
        }catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }

    public static int toInt(String src, int defValue){
        if(StringUtils.isEmpty(src)){
            return defValue;
        }
        try{
            return Integer.parseInt(src);
        }catch(Exception e){
            return defValue;
        }
    }

    /***
     * 参考： https://developer.mozilla.org/zh-CN/docs/Web/HTTP/Headers/Origin
     * */
    public static String normalizeOrigin(String origin){
        if(StringUtils.isEmpty(origin)){
            return "";
        }
        String old = origin;
        origin = origin.toLowerCase();
        if(!origin.startsWith("http://") && !origin.startsWith("https://")){
            return "";
        }
        if(origin.indexOf("?") >= 0 || origin.indexOf("&") >= 0 ||
           origin.indexOf("'")>=0 || origin.indexOf("\"") >= 0 ||
           origin.indexOf("(")>=0 || origin.indexOf(")") >= 0 ||
           origin.indexOf("\r")>=0 || origin.indexOf("\n") >= 0){
            return "";
        }
        return old;
    }
}
