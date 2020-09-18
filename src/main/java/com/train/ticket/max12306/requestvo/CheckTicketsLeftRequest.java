package com.train.ticket.max12306.requestvo;

import com.train.ticket.max12306.entity.PassengerInfo;
import com.train.ticket.max12306.enumeration.SeatType;
import com.train.ticket.max12306.enumeration.TicketType;
import lombok.Data;

import java.util.List;

/**
 * @ClassName CheckTicketsLeftRequest
 * @Author duxiaoyu
 * @Date 2020/9/17 16:10
 * @Version 1.0
 */
@Data
public class CheckTicketsLeftRequest {

    /**
     * 出发地点/出发站编码
     */
    private String fromStationCode;

    /**
     * 目的地/到达站编码
     */
    private String toStationCode;

    /**
     * 车票类型
     */
    private TicketType ticketType;

    /**
     * 出发日期: 可多选
     */
    private List<String> fromDateList;

    /**
     * 车次号: 可多选
     */
    private List<String> trainCodeList;

    /**
     * 乘车人: 可多选
     */
    private List<PassengerInfo> passengerInfos;

    /**
     * 座位: 可多选
     */
    private List<SeatType> seatTypes;
}
