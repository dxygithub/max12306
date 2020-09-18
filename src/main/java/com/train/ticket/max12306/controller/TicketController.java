package com.train.ticket.max12306.controller;

import com.train.ticket.max12306.common.CheckTicketsLeft;
import com.train.ticket.max12306.common.HttpURL12306;
import com.train.ticket.max12306.requestvo.CheckTicketsLeftRequest;
import com.train.ticket.max12306.requestvo.QueryTicketPriceRequest;
import com.train.ticket.max12306.requestvo.QueryTicketRequest;
import com.train.ticket.max12306.common.RestResult;
import com.train.ticket.max12306.entity.TicketInfo;
import com.train.ticket.max12306.entity.TicketPrice;
import com.train.ticket.max12306.service.TicketService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @ClassName TicketController
 * @Author duxiaoyu
 * @Date 2020/8/10 15:23
 * @Version 1.0
 */
@RestController
public class TicketController {

    @Autowired
    private TicketService ticketService;

    @Autowired
    private CheckTicketsLeft ticketsLeft;

    /**
     * 查询车票信息
     *
     * @param ticketRequest
     * @return
     */
    @GetMapping("/max/queryTicketInfo")
    public RestResult queryTicketInfo(QueryTicketRequest ticketRequest) {
        Map<String, Object> result = new HashMap<>(16);
        if (Objects.nonNull(ticketRequest)) {
            List<TicketInfo> ticketInfos = ticketService.getTickets(ticketRequest);
            List<Map<String, String>> fromStations = new ArrayList<>();
            if (!CollectionUtils.isEmpty(ticketInfos)) {
                List<TicketInfo> mapList = ticketInfos.stream().filter(HttpURL12306.distinctByKey(TicketInfo::getFromStationCode)).collect(Collectors.toList());
                mapList.forEach(x -> {
                    Map<String, String> map = new HashMap<>(16);
                    map.put("fromStationCode", x.getFromStationCode());
                    map.put("fromStationName", x.getFromStationName());
                    fromStations.add(map);
                });
            }
            result.put("ticketInfos", ticketInfos);
            result.put("fromStations", fromStations);
            return RestResult.SUCCESS().data(result).build();
        }
        return RestResult.ERROR_PARAMS().build();
    }

    /**
     * 实时监测余票信息
     *
     * @param ticketsLeftRequest
     * @return
     */
    @GetMapping("/max/monitorTicketsLeft")
    public RestResult monitorTicketsLeft(CheckTicketsLeftRequest ticketsLeftRequest) {
        if (Objects.nonNull(ticketsLeftRequest)) {
            QueryTicketRequest ticketRequest = new QueryTicketRequest();
            // 目前只监测单日期余票
            ticketRequest.setFromDate(ticketsLeftRequest.getFromDateList().get(0));
            ticketRequest.setFromStationCode(ticketsLeftRequest.getFromStationCode());
            ticketRequest.setToStationCode(ticketsLeftRequest.getToStationCode());
            ticketRequest.setTicketType(ticketsLeftRequest.getTicketType());
            List<TicketInfo> ticketInfos = ticketService.getTickets(ticketRequest);
            if (!CollectionUtils.isEmpty(ticketInfos)) {
                // 过滤已选车次
                List<TicketInfo> selectTicket = new ArrayList<>();
                ticketsLeftRequest.getTrainCodeList().forEach(trainCode -> {
                    TicketInfo temp = ticketInfos.stream().filter(ticket -> StringUtils.equals(ticket.getTrainCode(), trainCode)).findAny().orElse(null);
                    if (Objects.nonNull(temp)) {
                        selectTicket.add(temp);
                    }
                });
                ticketsLeft.setTicketInfos(selectTicket);
                ticketsLeft.setSeatTypes(ticketsLeftRequest.getSeatTypes());
                ticketsLeft.setPassengerInfos(ticketsLeftRequest.getPassengerInfos());
                // 开始监测余票信息，如果存在则返回余票信息
            }
        }
        return RestResult.ERROR_PARAMS().build();
    }

    /**
     * 查询车票价格
     *
     * @param ticketPriceRequest
     * @return
     */
    @GetMapping("/max/queryTicketPrice")
    public RestResult queryTicketPrice(QueryTicketPriceRequest ticketPriceRequest) {
        TicketPrice ticketPrice = ticketService.getTicketPrice(ticketPriceRequest);
        return RestResult.SUCCESS().data(ticketPrice).build();
    }
}
