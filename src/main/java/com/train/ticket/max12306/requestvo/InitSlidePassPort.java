package com.train.ticket.max12306.requestvo;

import lombok.Data;

/**
 * @ClassName InitSlidePassPort 初始化阿里云滑块验证
 * @Author duxiaoyu
 * @Date 2020/8/18 15:28
 * @Version 1.0
 */
@Data
public class InitSlidePassPort {

    private String appid;

    private String username;

    private String slideMode;
}
