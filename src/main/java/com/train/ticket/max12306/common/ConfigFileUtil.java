package com.train.ticket.max12306.common;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

/**
 * @ClassName ConfigFileUtil
 * @Author duxiaoyu
 * @Date 2020/8/27 9:50
 * @Version 1.0
 */
@Component
@PropertySource(value = {"classpath:config12306.properties"})
@Getter
public class ConfigFileUtil {

    @Value("${RAIL_EXPIRATION}")
    private String RAIL_EXPIRATION;

    @Value("${RAIL_DEVICEID}")
    private String RAIL_DEVICEID;

    @Value("${autoCheck}")
    private boolean autoCheck;
}
