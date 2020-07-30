package com.train.ticket.max12306.enumeration;

import lombok.Getter;

/**
 * @ClassName TicketType 车票类型
 * @Author duxiaoyu
 * @Date 2020/7/29 13:43
 * @Version 1.0
 */
@Getter
public enum TicketType {

    /**
     * 普通票
     */
    TICKETS("ADULT"),

    /**
     * 学生票
     */
    STUDENT_TICKET("0X00");

    private String value;

    TicketType(String value){
        this.value=value;
    }
}
