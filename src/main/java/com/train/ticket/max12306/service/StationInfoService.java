package com.train.ticket.max12306.service;

import com.train.ticket.max12306.entity.StationInfo;

import java.util.List;

/**
 * @ClassName StationInfoService
 * @Author duxiaoyu
 * @Date 2020/7/29 17:30
 * @Version 1.0
 */
public interface StationInfoService {

    /**
     * 保存车站信息
     * @param stationInfos
     * @return
     */
    int stationInfoSave(List<StationInfo> stationInfos);

    /**
     * 获取所有车站信息
     * @return
     */
    List<StationInfo> getAllStationInfo();
}
