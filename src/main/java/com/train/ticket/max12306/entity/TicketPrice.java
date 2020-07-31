package com.train.ticket.max12306.entity;

import lombok.Data;

/**
 * @ClassName TicketPrice 车票价格
 * @Author duxiaoyu
 * @Date 2020/7/31 9:46
 * @Version 1.0
 */
@Data
public class TicketPrice {

    /**
     * 列车号
     */
    private String trainNo;

    /**
     * 车次号: G/D/Z/T/K
     */
    private String trainCode;

    /**
     * G/D/Z/K/T: G/D二等座--Z/T/K:无座价格
     */
    private String WZ;

    /**
     * G/D: 一等座价格
     */
    private String M;

    /**
     * G: 商务座/特等座
     */
    private String A9;

    /**
     * D: 软卧价格
     */
    private String AI;

    /**
     * D: 硬卧价格
     */
    private String AJ;

    /**
     * G/D: 无座价格
     */
    private String O;

    /**
     * Z: 高级软卧价格
     */
    private String A6;

    /**
     * Z/T/K: 软卧价格
     */
    private String A4;

    /**
     * Z/T/K: 硬卧价格
     */
    private String A3;

    /**
     * Z/T/K: 硬座价格
     */
    private String A1;



    /**
     * 商务座/特等座价格
     */
    private String businessSeatPrice;

    /**
     * 一等座价格
     */
    private String firstSeatPrice;

    /**
     * 二等座价格
     */
    private String secondSeatPrice;

    /**
     * 高级软卧价格
     */
    private String highSoftSleepPrice;

    /**
     * 软卧/一等卧价格
     */
    private String softSleepPrice;

    /**
     * 动卧价格
     */
    private String motorSleepPrice;

    /**
     * 硬卧/二等卧价格
     */
    private String hardSleepPrice;

    /**
     * 软座价格
     */
    private String softSeatPrice;

    /**
     * 硬座价格
     */
    private String hardSeatPrice;

    /**
     * 无座价格
     */
    private String noneSeatPrice;
}
