package com.train.ticket.max12306.entity;

import lombok.Data;

/**
 * @ClassName PassengerInfo 乘车人
 * @ClassExplain: 说明
 * @Author Duxiaoyu
 * @Date 2020/8/22 16:45
 * @Since V 1.0
 */
@Data
public class PassengerInfo {

    /**
     * 地址
     */
    private String address;

    /**
     * 加密字符串
     */
    private String allEncStr;

    /**
     * 出生日期
     */
    private String bornDate;

    /**
     * 姓名简拼
     */
    private String firstLetter;

    /**
     * 手机号码
     */
    private String mobileNo;

    /**
     * 证件号码
     */
    private String passengerIdNo;

    /**
     * 证件类型编号
     */
    private String passengerIdTypeCode;

    /**
     * 证件证类型名称
     */
    private String passengeridTypeName;

    /**
     * 乘车人姓名
     */
    private String passengerName;

    /**
     * 旅客类型编号
     */
    private String passengerType;

    /**
     * 旅客类型名称
     */
    private String passengerTypeName;

    /**
     * 乘车人uuid
     */
    private String passengerUUid;

    /**
     * 性别编号
     */
    private String sexCode;

    /**
     * 性别名称
     */
    private String sexName;

    private String isUserSelf;
}
