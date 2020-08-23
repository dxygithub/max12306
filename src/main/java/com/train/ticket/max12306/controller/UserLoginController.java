package com.train.ticket.max12306.controller;

import com.train.ticket.max12306.common.*;
import com.train.ticket.max12306.service.UserLoginService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
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

    @Value("${autoCheck}")
    private boolean autoCheck;

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
     * 初始化滑块验证
     *
     * @param passPort
     * @return
     */
    @PostMapping("/max/initSlidePassport")
    public RestResult initSlidePassport(InitSlidePassPort passPort) {
        return userLoginService.initSlidePassport(passPort);
    }

    /**
     * 用户登录
     *
     * @param loginRequest
     * @return
     */
    @PostMapping("/max/userLogin")
    public RestResult userLogin(UserLoginRequest loginRequest) {
        return userLoginService.userLogin(loginRequest);
    }

    /**
     * 用户认证
     *
     * @param appId
     * @return
     */
    @PostMapping("/max/passPortUamtk")
    public RestResult passPortUamtk(String appId, String uamtk) {
        return userLoginService.userPassportUamtk(appId, uamtk);
    }

    /**
     * 获取用户名
     *
     * @param tk
     * @return
     */
    @PostMapping("/max/getUserName")
    public RestResult getUserName(String tk) {
        return userLoginService.getUserName(tk);
    }

    /**
     * 获取乘车人
     *
     * @return
     */
    @GetMapping("/max/getPassengers")
    public RestResult getPassengers() {
        return userLoginService.getPassengers();
    }

    /**
     * 获取订单信息
     *
     * @return
     */
    @GetMapping("/max/getOrderInfo")
    public RestResult getOrderInfo() {
        return userLoginService.getOrderInfo();
    }

    /**
     * 删除乘车人
     *
     * @param passengersVo
     * @return
     */
    @PostMapping("/max/delPassenger")
    public RestResult delPassenger(PassengersVo passengersVo) {
        return userLoginService.delPassenger(passengersVo);
    }

    /**
     * 用户退出
     *
     * @return
     */
    @GetMapping("/max/userLoginOut")
    public RestResult userLoginOut() {
        return userLoginService.loginOut();
    }

    /**
     * 校验图片验证码
     *
     * @param imgIndex
     * @return
     */
    @GetMapping("/max/checkImgCapthcha")
    public RestResult checkImgCapthcha(String imgIndex, String timer) {
        if (this.autoCheck) {
            return userLoginService.checkImgCaptcha(imgIndex, timer, HttpURL12306.IMG_CAPTHCHA_MAP.get(timer), this.autoCheck);
        } else {
            return userLoginService.checkImgCaptcha(imgIndex, timer, "", this.autoCheck);
        }
    }
}
