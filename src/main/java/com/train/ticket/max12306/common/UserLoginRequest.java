package com.train.ticket.max12306.common;

import lombok.Data;

/**
 * @ClassName UserLoginRequest
 * @Author duxiaoyu
 * @Date 2020/8/19 10:46
 * @Version 1.0
 */
@Data
public class UserLoginRequest {

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
