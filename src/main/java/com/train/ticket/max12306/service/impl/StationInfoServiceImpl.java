package com.train.ticket.max12306.service.impl;

import cloud.gouyiba.core.constructor.QueryWrapper;
import com.train.ticket.max12306.common.HttpURL12306;
import com.train.ticket.max12306.entity.StationInfo;
import com.train.ticket.max12306.mapper.StationInfoMapper;
import com.train.ticket.max12306.service.StationInfoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @ClassName StationInfoServiceImpl
 * @Author duxiaoyu
 * @Date 2020/7/29 17:34
 * @Version 1.0
 */
@Service
public class StationInfoServiceImpl implements StationInfoService {

    private static final Logger LOGGER = LoggerFactory.getLogger(StationInfoServiceImpl.class);

    @Autowired
    private StationInfoMapper stationInfoMapper;

    /**
     * 保存车站信息
     *
     * @param stationInfos
     * @return
     */
    @Override
    public int stationInfoSave(List<StationInfo> stationInfos) {
        if (CollectionUtils.isEmpty(stationInfos)) {
            LOGGER.info("======> 车站信息为空...");
            return 0;
        }
        return Optional.ofNullable(stationInfoMapper.insertBatch(stationInfos)).orElse(0);
    }

    /**
     * 获取所有车站信息：DB获取，已去重,已排序
     *
     * @return
     */
    @Override
    public List<StationInfo> getAllStationInfo() {
        List<StationInfo> stationInfos = stationInfoMapper.selectList(new QueryWrapper<>().orderBy("station_sort", QueryWrapper.ASC));
        return CollectionUtils.isEmpty(stationInfos) ? Collections.emptyList() : stationInfos;
    }

    /**
     * 直接获取车站信息: 请求12306
     *
     * @return
     */
    public List<StationInfo> directGetStationInfo() {
        List<StationInfo> stationInfos = null;
        try {
            stationInfos = HttpURL12306.parseStationInfo();
            if (!CollectionUtils.isEmpty(stationInfos)) {
                // 车站信息去重排序，直接请求12306获取到的车站信息可能会有重复的
                stationInfos = stationInfos.stream().filter(HttpURL12306.distinctByKey(StationInfo::getStationCode)).sorted(Comparator.comparing(StationInfo::getStationSort)).collect(Collectors.toList());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return CollectionUtils.isEmpty(stationInfos) ? Collections.emptyList() : stationInfos;
    }
}
