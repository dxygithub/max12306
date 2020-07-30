package com.train.ticket.max12306.entity;

import lombok.Data;

/**
 * @ClassName StationInfo 车站信息
 * @Author duxiaoyu
 * @Date 2020/7/28 17:50
 * @Version 1.0
 */
@Data
public class StationInfo {

    /**
     * 车站名称
     */
    private String stationName;

    /**
     * 车站编号
     */
    private String stationCode;

    /**
     * 车站拼音
     */
    private String stationSpell;

    /**
     * 车站简拼
     */
    private String stationLogogram;

    /**
     * 车站排序
     */
    private Integer stationSort;
}
