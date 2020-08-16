package com.train.ticket.max12306.controller;

import com.train.ticket.max12306.common.RestResult;
import com.train.ticket.max12306.service.UserLoginService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @ClassName UserLoginController
 * @ClassExplain: 说明
 * @Author Duxiaoyu
 * @Date 2020/8/16 16:26
 * @Since V 1.0
 */
@RestController
public class UserLoginController {

    @Autowired
    private UserLoginService userLoginService;

    /**
     * 获取图片验证码
     *
     * @return
     */
    @GetMapping("/max/getImgCapthcha")
    public RestResult getImgCapthcha() {
        return userLoginService.getImgCaptcha();
    }

    /**
     * 校验图片验证码
     *
     * @param imgIndex
     * @return
     */
    @GetMapping("/max/checkImgCapthcha")
    public RestResult checkImgCapthcha(String imgIndex) {
        return userLoginService.checkImgCaptcha(imgIndex, false);
    }
}
