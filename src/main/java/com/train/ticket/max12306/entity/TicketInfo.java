package com.train.ticket.max12306.entity;

import lombok.Data;

/**
 * @ClassName TicketInfo 车票信息
 * @Author duxiaoyu
 * @Date 2020/7/29 15:32
 * @Version 1.0
 */
@Data
public class TicketInfo {

    /**
     * 车票密钥: 下单时使用
     */
    private String ticketSecretKey;

    /**
     * 备注: 预定/列车停运/几点起售
     */
    private String remark;

    /**
     * 列车号: 查询票价时使用
     */
    private String trainNo;

    /**
     * 车次号
     */
    private String trainCode;

    /**
     * 起始站编码
     */
    private String startStationCode;

    /**
     * 终点站编码
     */
    private String endStationCode;

    /**
     * 出发站编码
     */
    private String fromStationCode;

    /**
     * 出发站
     */
    private String fromeStationName;

    /**
     * 到达站编码
     */
    private String toStationCode;

    /**
     * 到达站
     */
    private String toStationMame;

    /**
     * 出发时间
     */
    private String fromTime;

    /**
     * 到达时间
     */
    private String toTime;

    /**
     * 历时时间: 06h:45m
     */
    private String lastTime;

    /**
     * 是否可购买: Y/N
     */
    private String canBuy;

    /**
     * 列车起始站发车日期: 20200729
     */
    private String startDate;

    /**
     * 列车位置
     */
    private String trainLocation;

    /**
     * 出发站站序: 01表示始发站，大于1表示过站
     */
    private String fromStationNo;

    /**
     * 到达站序: 对应火车经停的站序
     */
    private String toStationNo;

    /**
     * 可否使用二代身份证进出站: 0不可以，1可以
     */
    private String isSupportCard;

    /**
     * 座位类型: 查询车票价格时使用
     */
    private String seatType;

    /**
     * 是否支持候补: 0不支持，1支持
     */
    private String canBackup;

    /**
     * 其他
     */
    private String other;

    /**
     * 暂时不知: 待知晓
     */
    private String ybCount;

    /**
     * 高级软卧数量
     */
    private String highSoftSleepCount;

    /**
     * 软卧/一等卧数量
     */
    private String softSleepCount;

    /**
     * 软座数量
     */
    private String softSeatCount;

    /**
     * 特等座数量
     */
    private String specialSeatCount;

    /**
     * 无座数量
     */
    private String noneSeatCount;

    /**
     * 硬卧/二等卧数量
     */
    private String hardSleepCount;

    /**
     * 硬座数量
     */
    private String hardSeatCount;

    /**
     * 二等座数量
     */
    private String secondSeatCount;

    /**
     * 一等座数量
     */
    private String firstSeatCount;

    /**
     * 商务座/特等座数量
     */
    private String businessSeatCount;

    /**
     * 动卧数量
     */
    private String motorSleepCount;
}
