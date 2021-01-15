package com.train.ticket.max12306.controller;

import com.train.ticket.max12306.common.*;
import com.train.ticket.max12306.request.HttpURL12306;
import com.train.ticket.max12306.requestvo.InitSlidePassPort;
import com.train.ticket.max12306.requestvo.PassengersVo;
import com.train.ticket.max12306.requestvo.UserLoginRequest;
import com.train.ticket.max12306.service.UserLoginService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

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

    @Autowired
    private ConfigFileUtil config;

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
     * 获取登录校验的AppKey
     * @return
     */
    @GetMapping("/max/getLoginAppKey")
    public RestResult getLoginAppKey(){
        return userLoginService.getLoginAppKey();
    }

    /**
     * 初始化滑块验证
     *
     * @param passPort
     * @return
     */
    @PostMapping("/max/initSlidePassport")
    public RestResult initSlidePassport(@RequestBody InitSlidePassPort passPort) {
        return userLoginService.initSlidePassport(passPort);
    }

    /**
     * 用户登录
     *
     * @param loginRequest
     * @return
     */
    @PostMapping("/max/userLogin")
    public RestResult userLogin(@RequestBody UserLoginRequest loginRequest) {
        return userLoginService.userLogin(loginRequest);
    }

    /**
     * 用户认证
     *
     * @return
     */
    @PostMapping("/max/passPortUamtk")
    public RestResult passPortUamtk(@RequestBody Map<String,String> params) {
        return userLoginService.userPassportUamtk(params.get("appId"), params.get("uamtk"));
    }

    /**
     * 获取用户名
     *
     * @param tk
     * @return
     */
    @GetMapping("/max/getUserName")
    public RestResult getUserName(@RequestParam("tk") String tk) {
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
    public RestResult getOrderInfo(String queryStartDate,String queryEndDate,String queryWhere) {
        return userLoginService.getOrderInfo(queryStartDate,queryEndDate,queryWhere);
    }

    /**
     * 获取未完成的订单信息
     * @return
     */
    @GetMapping("/max/getOrderNoComplete")
    public RestResult getOrderNoComplete(){
        return userLoginService.getOrderNoComplete();
    }

    /**
     * 删除乘车人
     *
     * @param passengersVo
     * @return
     */
    @PostMapping("/max/delPassenger")
    public RestResult delPassenger(@RequestBody PassengersVo passengersVo) {
        return userLoginService.delPassenger(passengersVo);
    }

    /**
     * 新增乘车人
     * @param passengersVo
     * @return
     */
    @PostMapping("/max/addPassenger")
    public RestResult addPassenger(@RequestBody PassengersVo passengersVo){
        return userLoginService.addPassengers(passengersVo);
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
        if (config.isAutoCheck()) {
            return userLoginService.checkImgCaptcha(imgIndex, timer, HttpURL12306.IMG_CAPTHCHA_MAP.get(timer), config.isAutoCheck());
        } else {
            return userLoginService.checkImgCaptcha(imgIndex, timer, "", config.isAutoCheck());
        }
    }
}
