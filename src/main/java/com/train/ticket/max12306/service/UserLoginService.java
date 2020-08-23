package com.train.ticket.max12306.service;

import com.train.ticket.max12306.common.InitSlidePassPort;
import com.train.ticket.max12306.common.PassengersVo;
import com.train.ticket.max12306.common.RestResult;
import com.train.ticket.max12306.common.UserLoginRequest;

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


    /**
     * 初始化滑块验证
     * @param passPort
     * @return
     */
    RestResult initSlidePassport(InitSlidePassPort passPort);

    /**
     * 用户登录
     * @param loginRequest
     * @return
     */
    RestResult userLogin(UserLoginRequest loginRequest);

    /**
     * 用户认证
     * @param appId
     * @return
     */
    RestResult userPassportUamtk(String appId,String uamtk);

    /**
     * 获取用户名
     * @param tk
     * @return
     */
    RestResult getUserName(String tk);

    /**
     * 用户退出
     * @return
     */
    RestResult loginOut();

    /**
     * 获取乘车人
     * @return
     */
    RestResult getPassengers();

    /**
     * 删除乘车人
     * @param passengersVo
     * @return
     */
    RestResult delPassenger(PassengersVo passengersVo);

    /**
     * 获取订单信息
     * @return
     */
    RestResult getOrderInfo();
}