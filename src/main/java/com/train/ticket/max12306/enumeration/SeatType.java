package com.train.ticket.max12306.enumeration;

import lombok.Getter;

/**
 * @ClassName SeatType
 * @Author duxiaoyu
 * @Date 2020/8/27 16:09
 * @Version 1.0
 */
@Getter
public enum SeatType{

    /**
     * 一等座
     */
    FIRST_SEAT("M"),
    /**
     * 二等座
     */
    SECOND_SEAT("O"),
    /**
     * 商务座
     */
    BUSINESS_SEAT("9"),
    /**
     * 高级软卧
     */
    HIGH_SOFT_SLEEP("6"),
    /**
     * 软卧
     */
    SOFT_SLEEP("4"),
    /**
     * 硬卧
     */
    HARD_SLEEP("3"),
    /**
     * 软座
     */
    SOFT_SEAT("2"),
    /**
     * 硬座
     */
    HARD_SEAT("1"),
    /**
     * 无座
     */
    NONE_SEAT("1"),
    /**
     * 特等座
     */
    SPECIAL_SEAT("P");

    private String value;
    SeatType(String value){
        this.value=value;
    }
}
