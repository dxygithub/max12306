package com.train.ticket.max12306.configuration;

import com.train.ticket.max12306.cdn.CdnUtil;
import com.train.ticket.max12306.request.HttpURL12306;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * @ClassName CheckCdnTasks
 * @ClassExplain: 说明
 * @Author Duxiaoyu
 * @Date 2020/9/6 10:22
 * @Since V 1.0
 */
@Configuration
@EnableScheduling
public class CheckCdnTask implements SchedulingConfigurer {

    private static final Logger LOGGER = LoggerFactory.getLogger(CheckCdnTask.class);

    /**
     * 配置定时任务
     *
     * @param taskRegistrar
     */
    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        taskRegistrar.addTriggerTask(() -> {
            // 项目初次启动，判断可用cdn集合是否为空，如果为空就开始检测可用cdn
            // 如果不为空，就开始检测可用cdn集合中的cdn是否继续可用，如果不可用，就移除
            // 当可用cdn集合中的数量低于20个时，开始添加可用cdn
            if (HttpURL12306.cdnList.size() <= 0) {
                LOGGER.info("======> cdn检测开始 <======");
                try {
                    List<String> availableCdn = CdnUtil.getAvailableCdn();
                    if (!CollectionUtils.isEmpty(availableCdn)) {
                        HttpURL12306.cdnList = availableCdn;
                        LOGGER.info("======> 实际可用cdn数量: {}个", HttpURL12306.cdnList.size());
                        HttpURL12306.getRandomIndex();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, triggerContext -> {
            // 每五秒执行一次
            String cron = "0/3 * * * * ?";
            return new CronTrigger(cron).nextExecutionTime(triggerContext);
        });
    }
}
