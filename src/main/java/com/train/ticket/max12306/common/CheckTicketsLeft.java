package com.train.ticket.max12306.common;

import com.train.ticket.max12306.entity.PassengerInfo;
import com.train.ticket.max12306.entity.TicketInfo;
import com.train.ticket.max12306.enumeration.SeatType;
import com.train.ticket.max12306.requestvo.CheckTicketsLeftRequest;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @ClassName CheckTicketsLeft 监测余票处理
 * @Author duxiaoyu
 * @Date 2020/9/17 14:48
 * @Version 1.0
 */
@Service
public class CheckTicketsLeft {

    private static final Logger LOGGER = LoggerFactory.getLogger(CheckTicketsLeft.class);

    /**
     * 余票信息
     */
    private List<TicketInfo> ticketInfos = new ArrayList<>();

    /**
     * 座位类型
     */
    private List<SeatType> seatTypes = new ArrayList<>();

    /**
     * 乘车人
     */
    private List<PassengerInfo> passengerInfos = new ArrayList<>();

    private static int code = 0;

    private TicketInfo ticketInfo;

    /**
     * 余票监测线程池
     */
    private static ExecutorService executorService = Executors.newFixedThreadPool(2);

    public void setTicketInfos(List<TicketInfo> ticketInfos) {
        this.ticketInfos = ticketInfos;
    }

    public void setSeatTypes(List<SeatType> seatTypes) {
        this.seatTypes = seatTypes;
    }

    public void setPassengerInfos(List<PassengerInfo> passengerInfos) {
        this.passengerInfos = passengerInfos;
    }

    // 创建捡漏任务: 按照车次划分执行任务
    // 上海 - 太原
    // 日期: 2020-09-17/2020-09-18/2020-09-19
    // D1/D2/D3/D4
    // 张三/李四/王五
    // 商务座/一等座/二等座/高级软卧/软卧/硬卧/硬座/无座/动车二等卧/动车一等卧

    // 捡漏任务，只要有一个返回有余票，即视为捡漏成功，其余线程即失败

    /**
     * 开始余票捡漏任务
     *
     * @return Map<String, Object>
     */
    public Map<String, Object> startTicketsLfetTask() {
        return null;
    }

    /**
     * 捡漏任务
     *
     *
     * @param ticketInfo
     * @return
     */
    public Runnable createThreadTask(TicketInfo ticketInfo) {
        return new Runnable() {
            @Override
            public void run() {

            }
        };
    }


    public Map<String, Object> checkTicketInfo(TicketInfo ticketInfo) {
        TicketInfo result = null;
        SeatType seatType = null;
        String count = "";
        Map<String, Object> resultMap = new HashMap<>(16);
        for (SeatType item : seatTypes) {
            // 检查是否有余票
            switch (item) {
                // 一等座
                case FIRST_SEAT:
                    count = ticketInfo.getFirstSeatCount();
                    break;
                // 二等座
                case SECOND_SEAT:
                    count = ticketInfo.getSecondSeatCount();
                    break;
                // 商务座
                case BUSINESS_SEAT:
                    count = ticketInfo.getBusinessSeatCount();
                    break;
                // 高级软卧
                case HIGH_SOFT_SLEEP:
                    count = ticketInfo.getHighSoftSleepCount();
                    break;
                // 软卧
                case SOFT_SLEEP:
                    count = ticketInfo.getSoftSleepCount();
                    break;
                // 硬卧
                case HARD_SLEEP:
                    count = ticketInfo.getHardSleepCount();
                    break;
                // 软座
                case SOFT_SEAT:
                    count = ticketInfo.getSoftSeatCount();
                    break;
                // 硬座
                case HARD_SEAT:
                    count = ticketInfo.getHardSeatCount();
                    break;
                // 无座
                case NONE_SEAT:
                    count = ticketInfo.getNoneSeatCount();
                    break;
                // 动车: 二等卧
                case SECOND_SOFT_SLEEP:
                    count = ticketInfo.getHardSleepCount();
                    break;
                // 动车: 一等卧
                case FIRST_SOFT_SLEEP:
                    count = ticketInfo.getSoftSleepCount();
                    break;
                default:
                    break;
            }

            if (StringUtils.isNotBlank(count)) {
                // 余票分为: 有/无/number
                if (!StringUtils.equals("无", count)) {
                    if (StringUtils.equals("有", count)) {
                        // 有票，符合条件
                        result = ticketInfo;
                        seatType = item;
                    } else {
                        Integer ticketCount = Integer.parseInt(count);
                        // 检查余票数量是否大于乘车人数量
                        if (ticketCount >= passengerInfos.size()) {
                            // 有票，符合条件
                            result = ticketInfo;
                            seatType = item;
                        }
                    }
                }
            }

            if (result != null) {
                // 监测到有余票，退出监测
                resultMap.put("seatType", seatType);
                resultMap.put("ticket", result);
                break;
            }
        }
        return resultMap;
    }
}
