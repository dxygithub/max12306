package com.train.ticket.max12306.controller;

import com.train.ticket.max12306.common.HttpURL12306;
import com.train.ticket.max12306.common.QueryTicketRequest;
import com.train.ticket.max12306.entity.TicketInfo;
import com.train.ticket.max12306.service.TicketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
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

    @GetMapping("/max/queryTicketInfo")
    public ResponseEntity queryTicketInfo(QueryTicketRequest ticketRequest){
        Map<String,Object> result=new HashMap<>(16);
        if(Objects.nonNull(ticketRequest)){
            List<TicketInfo> ticketInfos=ticketService.getTickets(ticketRequest);
            List<Map<String,String>> fromStations=new ArrayList<>();
            if(!CollectionUtils.isEmpty(ticketInfos)){
                List<TicketInfo> mapList=ticketInfos.stream().filter(HttpURL12306.distinctByKey(TicketInfo::getFromStationCode)).collect(Collectors.toList());
                mapList.forEach(x->{
                    Map<String,String> map=new HashMap<>(16);
                    map.put("fromStationCode",x.getFromStationCode());
                    map.put("fromStationName",x.getFromeStationName());
                    fromStations.add(map);
                });
            }
            result.put("ticketInfos",ticketInfos);
            result.put("fromStations",fromStations);
            return ResponseEntity.status(200).body(result);
        }
        return ResponseEntity.status(400).body("{\"message\":\"参数为空\"}");
    }
}
