package com.train.ticket.max12306.service;

import com.train.ticket.max12306.common.RestResult;

/**
 * @ClassName UserLogin
 * @ClassExplain: 说明
 * @Author Duxiaoyu
 * @Date 2020/8/16 15:15
 * @Since V 1.0
 */
public interface UserLoginService {

    /**
     * 获取图片验证码
     *
     * @return
     */
    RestResult getImgCaptcha();

    /**
     * 图片验证码校验
     *
     * @param answer
     * @param autoCheck
     * @return
     */
    RestResult checkImgCaptcha(String answer, String timer, String img, boolean autoCheck);
}
