package com.train.ticket.max12306.common;

import lombok.Data;

/**
 * @ClassName QueryTicketPriceRequest
 * @Author duxiaoyu
 * @Date 2020/7/31 10:32
 * @Version 1.0
 */
@Data
public class QueryTicketPriceRequest {

    /**
     * 车次号
     */
    private String trainCode;

    /**
     * 列车号
     */
    private String trainNo;

    /**
     * 出发站序号
     */
    private String fromStationNo;

    /**
     * 到达站序号
     */
    private String toStationNo;

    /**
     * 座位类型
     */
    private String seatTypes;

    /**
     * 出发日期
     */
    private String trainDate;
}
