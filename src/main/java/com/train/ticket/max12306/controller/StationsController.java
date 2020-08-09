package com.train.ticket.max12306.controller;

import com.train.ticket.max12306.entity.StationInfo;
import com.train.ticket.max12306.service.StationInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

/**
 * @ClassName AddressController
 * @ClassExplain: 说明
 * @Author Duxiaoyu
 * @Date 2020/8/8 17:57
 * @Since V 1.0
 */
@RestController
public class StationsController {

    @Autowired
    private StationInfoService stationInfoService;

    /**
     * 获取所有车站信息
     * @return
     */
    @GetMapping("/max/getStations")
    public ResponseEntity getStations(){
        List<StationInfo> stationInfos=stationInfoService.directGetStationInfo();
        return ResponseEntity.status(200).body(stationInfos);
    }
}
