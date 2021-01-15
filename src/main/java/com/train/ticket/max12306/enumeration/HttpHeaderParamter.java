package com.train.ticket.max12306.enumeration;


import lombok.Getter;

/**
 * @ClassName HttpHeaderParamter 请求头参数
 * @Author duxiaoyu
 * @Date 2020/7/28 14:37
 * @Version 1.0
 */
@Getter
public enum HttpHeaderParamter {

    /**
     * GET/POST请求均需设置以下请求参数
     */

    ACCEPT("Accept","*/*"),

    ACCEPT_ENCODING("Accept-Encoding","gzip, deflate, br"),

    ACCEPT_LANGUAGE("Accept-Language","zh-CN,zh;q=0.9,en-US;q=0.8,en;q=0.7"),
    /**
     * 浏览器标识，后期考虑换成动态切换，防止12306拦截
     * Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/86.0.4240.75 Safari/537.36
     * Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/70.0.3538.25 Safari/537.36 Core/1.70.3775.400 QQBrowser/10.6.4208.400
     *
     */
    USER_AGENT("User-Agent","Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/86.0.4240.75 Safari/537.36"),

    X_REQUESTED_WITH("X-Requested-With","XMLHttpRequest"),

    /**
     * 2020-07-28
     * 目前12306查询余票也必须携带cookie
     */
    COOKIE("Cookie","_passport_session={1};RAIL_EXPIRATION={2};RAIL_DEVICEID={3};_passport_ct={4};BIGipServerpassport={5};BIGipServerpool_passport={6};BIGipServerotn={7};route={8}");

    private String value;

    private String key;

    HttpHeaderParamter(String key,String value){
        this.value=value;
        this.key=key;
    }
}
