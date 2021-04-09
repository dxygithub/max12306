package com.train.ticket.max12306.enumeration;

import lombok.Getter;


/**
 * @ClassName UseCdnProxy
 * @Author duxiaoyu
 * @Date 2021/4/9 14:23
 * @Version 1.0
 */
@Getter
public enum UseCdnProxy {

    /**
     * 不使用cdn代理请求
     */
    NO(1),

    /**
     * 使用cdn代理请求
     */
    YES(2);

    private int value;

    UseCdnProxy(int value){
        this.value=value;
    }

}
