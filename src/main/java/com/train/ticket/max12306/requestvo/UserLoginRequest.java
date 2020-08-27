package com.train.ticket.max12306.requestvo;

import lombok.Data;

/**
 * @ClassName UserLoginRequest 登录form
 * @Author duxiaoyu
 * @Date 2020/8/19 10:46
 * @Version 1.0
 */
@Data
public class UserLoginRequest {

    /**
     * 以下参数需要前端通过校验返回参数: 图片验证+滑块校验
     */
    private String sessionId;

    private String sig;

    private String ifCheckSlidePasscodeToken;

    private String scene;

    private String tk;

    private String username;

    private String password;

    private String appid;

    private String answer;
}
