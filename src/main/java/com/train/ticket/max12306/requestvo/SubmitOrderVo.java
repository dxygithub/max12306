package com.train.ticket.max12306.requestvo;

import com.train.ticket.max12306.entity.PassengerInfo;
import com.train.ticket.max12306.entity.StationInfo;
import com.train.ticket.max12306.entity.TicketInfo;
import com.train.ticket.max12306.enumeration.SeatType;
import com.train.ticket.max12306.enumeration.TicketType;
import lombok.Data;

import java.util.List;

/**
 * @ClassName SubmitOrderVo
 * @ClassExplain: 订单提交form
 * @Author Duxiaoyu
 * @Date 2020/8/29 10:04
 * @Since V 1.0
 */
@Data
public class SubmitOrderVo {

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

    /**
     * 乘车人信息
     */
    private List<PassengerInfo> passengerInfoList;

    /**
     * 车票信息
     */
    private TicketInfo ticketInfo;

    /**
     * 座位类型
     */
    private SeatType seatType;
}
