package com.train.ticket.max12306.enumeration;

import lombok.Getter;

/**
 * @ClassName OrderStatus 订单状态-主要用于订单流程
 * @Author duxiaoyu
 * @Date 2020/8/27 16:34
 * @Version 1.0
 */
@Getter
public enum OrderStatus {

    /**
     * 成功
     */
    SUCCESS(1),
    /**
     * 失败
     */
    FAIL(2),
    /**
     * 接口请求为空
     */
    EMPTY(3),
    /**
     * 执行错误
     */
    ERROR(4),
    /**
     * 参数为空
     */
    PARM_EMPTY(5);

    private int value;
    OrderStatus(int value){
        this.value=value;
    }
}
