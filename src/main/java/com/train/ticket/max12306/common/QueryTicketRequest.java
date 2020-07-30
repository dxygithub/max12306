package com.train.ticket.max12306.common;

import com.train.ticket.max12306.enumeration.TicketType;
import lombok.Data;

/**
 * @ClassName QueryTicketRequest
 * @Author duxiaoyu
 * @Date 2020/7/29 17:48
 * @Version 1.0
 */
@Data
public class QueryTicketRequest {

    /**
     * 出发地点/出发站编码
     */
    private String fromStationCode;

    /**
     * 目的地/到达站编码
     */
    private String toStationCode;

    /**
     * 出发日
     */
    private String fromDate;

    /**
     * 车票类型
     */
    private TicketType ticketType;
}
