package com.train.ticket.max12306.service.impl;

import cloud.gouyiba.core.constructor.QueryWrapper;
import com.train.ticket.max12306.entity.StationInfo;
import com.train.ticket.max12306.mapper.StationInfoMapper;
import com.train.ticket.max12306.service.StationInfoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

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
     * 获取所有车站信息
     *
     * @return
     */
    @Override
    public List<StationInfo> getAllStationInfo() {
        List<StationInfo> stationInfos = stationInfoMapper.selectList(new QueryWrapper<>().orderBy("station_sort", QueryWrapper.ASC));
        return CollectionUtils.isEmpty(stationInfos) ? Collections.emptyList() : stationInfos;
    }
}
