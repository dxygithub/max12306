package com.train.ticket.max12306.service;

import com.train.ticket.max12306.requestvo.QueryTicketPriceRequest;
import com.train.ticket.max12306.requestvo.QueryTicketRequest;
import com.train.ticket.max12306.entity.TicketInfo;
import com.train.ticket.max12306.entity.TicketPrice;

import java.util.List;

/**
 * @ClassName TicketService
 * @Author duxiaoyu
 * @Date 2020/8/10 15:26
 * @Version 1.0
 */
public interface TicketService {

    List<TicketInfo> getTickets(QueryTicketRequest ticketRequest);

    TicketPrice getTicketPrice(QueryTicketPriceRequest ticketPriceRequest);
}
