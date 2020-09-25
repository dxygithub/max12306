package com.train.ticket.max12306.service.impl;

import com.train.ticket.max12306.request.HttpURL12306;
import com.train.ticket.max12306.requestvo.QueryTicketPriceRequest;
import com.train.ticket.max12306.requestvo.QueryTicketRequest;
import com.train.ticket.max12306.entity.TicketInfo;
import com.train.ticket.max12306.entity.TicketPrice;
import com.train.ticket.max12306.service.TicketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * @ClassName TicketServiceImpl
 * @Author duxiaoyu
 * @Date 2020/8/10 15:27
 * @Version 1.0
 */
@Service
public class TicketServiceImpl implements TicketService {

    @Autowired
    private HttpURL12306 url12306;

    @Override
    public List<TicketInfo> getTickets(QueryTicketRequest ticketRequest) {
        List<TicketInfo> ticketInfos = null;
        try {
            ticketInfos = url12306.parseTicketInfo(ticketRequest);
        } catch (Exception e) {
            // e.printStackTrace();
            return CollectionUtils.isEmpty(ticketInfos) ? Collections.emptyList() : ticketInfos;
        }
        return CollectionUtils.isEmpty(ticketInfos) ? Collections.emptyList() : ticketInfos;
    }


    @Override
    public TicketPrice getTicketPrice(QueryTicketPriceRequest ticketPriceRequest) {
        TicketPrice price=null;
        try {
            price=url12306.parseTicketPrice(ticketPriceRequest);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Optional.ofNullable(price).orElse(new TicketPrice());
    }
}
