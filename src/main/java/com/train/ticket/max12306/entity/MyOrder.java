package com.train.ticket.max12306.entity;

import lombok.Data;

/**
 * @ClassName MyOrder 我的订单
 * @ClassExplain: 说明
 * @Author Duxiaoyu
 * @Date 2020/8/23 16:43
 * @Since V 1.0
 */
@Data
public class MyOrder {

    /**
     * 乘车人姓名
     */
    private String arrayPassserNamePage;

    /**
     * 出发站
     */
    private String fromStationNamePage;

    /**
     * 到达站
     */
    private String toStationNamePage;

    /**
     * 订单生成日期
     */
    private String orderDate;

    /**
     * 订单号
     */
    private String sequenceNo;

    /**
     * 发车日期
     */
    private String startTrainDatePage;

    /**
     * 出发车次
     */
    private String trainCodePage;

    /**
     * 车票总价
     */
    private String ticketTotalPricePage;

    /**
     * 车票数量
     */
    private String ticketTotalnum;

    /**
     * 车厢号
     */
    private String coachNo;

    /**
     * 座位号
     */
    private String seatName;

    /**
     * 座位类型
     */
    private String seatTypeName;

    /**
     * 车票单价
     */
    private String strTicketPricePage;

    /**
     * 车票状态
     */
    private String ticketStatusName;

    /**
     * 车票类型
     */
    private String ticketTypeName;


    /*********************************  未完成订单字段 *********************************/

    /**
     * 发车时间: time
     */
    private String startTimePage;

    /**
     * 到达时间: time
     */
    private String arriveTimePage;

    /*********************************  未完成订单字段 *********************************/

}
