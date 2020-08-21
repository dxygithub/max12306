package com.train.ticket.max12306.service.impl;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.train.ticket.max12306.common.*;
import com.train.ticket.max12306.service.UserLoginService;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;

/**
 * @ClassName UserLoginServiceImpl
 * @ClassExplain: 说明
 * @Author Duxiaoyu
 * @Date 2020/8/16 15:23
 * @Since V 1.0
 */
@Service
public class UserLoginServiceImpl implements UserLoginService {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserLoginServiceImpl.class);

    /**
     * 验证码中心位置固定坐标
     **/
    private String yzm1Point = "35,72";
    private String yzm2Point = "111,72";
    private String yzm3Point = "181,72";
    private String yzm4Point = "250,72";
    private String yzm5Point = "35,148";
    private String yzm6Point = "111,148";
    private String yzm7Point = "181,148";
    private String yzm8Point = "250,148";


    /**
     * 获取图片验证码
     *
     * @return
     */
    @Override
    public RestResult getImgCaptcha() {
        String[] resultArr = null;
        try {
            String result = HttpURL12306.getImgCaptcha();
            resultArr = result.split("--");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return RestResult.SUCCESS().data(resultArr).build();
    }

    /**
     * 初始化滑块验证
     *
     * @param passPort
     * @return
     */
    @Override
    public RestResult initSlidePassport(InitSlidePassPort passPort) {
        if (Objects.nonNull(passPort)) {
            try {
                String ifCheckSlidePasscodeToken = HttpURL12306.initSlidePassPort(passPort);
                if (StringUtils.equals("5", ifCheckSlidePasscodeToken)) {
                    LOGGER.info("======> 初始化滑块验证失败...");
                    return RestResult.SERVER_ERROR().message("初始化滑块验证失败").build();
                }
                LOGGER.info("======> 初始化滑块验证成功...");
                return RestResult.SUCCESS().data(ifCheckSlidePasscodeToken).build();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return RestResult.ERROR_PARAMS().message("初始化参数为空").build();
    }

    /**
     * 图片验证码校验
     *
     * @param imgIndex
     * @param timer
     * @param autoCheck
     * @param img
     * @return
     */
    @Override
    public RestResult checkImgCaptcha(String imgIndex, String timer, String img, boolean autoCheck) {
        try {
            String resultCode = "";
            if (!autoCheck) {
                if (StringUtils.isBlank(imgIndex)) {
                    return RestResult.ERROR_PARAMS().message("验证码坐标为空").build();
                }
                LOGGER.info("======> 开始手动识别验证码...");
                // 手动识别验证码，坐标位置前端已经处理过，此处直接进行校验即可
                resultCode = HttpURL12306.checkImgCapthcha(imgIndex, timer);
            } else {
                LOGGER.info("======> 开始自动识别验证码...");
                try (CloseableHttpClient client = HttpURL12306.httpClientBuild()) {
                    HttpPost httpPost = new HttpPost(HttpURLConstant12306.OCR_AUTO_CHECK);
                    List<NameValuePair> formPail = new ArrayList<>();
                    formPail.add(new BasicNameValuePair("img", img));
                    UrlEncodedFormEntity formEntity = new UrlEncodedFormEntity(formPail, "UTF-8");
                    httpPost.setEntity(formEntity);
                    try (CloseableHttpResponse response = client.execute(httpPost)) {
                        HttpEntity entity = response.getEntity();
                        String result = EntityUtils.toString(entity);
                        // 释放资源
                        EntityUtils.consume(entity);
                        if (StringUtils.isNotBlank(result)) {
                            JSONObject json = JSONUtil.parseObj(result);
                            if ("200".equals(json.get("code", String.class))) {
                                // 自动验证，返回验证码下标位置，需要使用固定坐标位置进行转换
                                String[] autoCheckImgIndex = json.get("result", String[].class);
                                String answer = capthchaXYMatching(autoCheckImgIndex);
                                resultCode = HttpURL12306.checkImgCapthcha(answer, timer);
                            }
                        }
                    }
                }
            }
            if (resultCode.equals("4")) {
                return RestResult.SUCCESS().data("4").message("验证码校验成功").build();
            } else {
                return RestResult.SUCCESS().data("5").message("验证码校验失败").build();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return RestResult.ERROR_PARAMS().message("验证码校验异常").build();
    }

    /**
     * 用户登录
     *
     * @param loginRequest
     * @return
     */
    @Override
    public RestResult userLogin(UserLoginRequest loginRequest) {
        if (Objects.nonNull(loginRequest)) {
            try {
                String result = HttpURL12306.loginRequest(loginRequest);
                if (!StringUtils.equals(result, "5")) {
                    return RestResult.SUCCESS().message("登录成功").data(result).build();
                } else {
                    return RestResult.SUCCESS().message("登录失败").data(result).build();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return RestResult.ERROR_PARAMS().message("登录参数为空").build();
    }

    /**
     * 用户认证
     *
     * @param appId
     * @return
     */
    @Override
    public RestResult userPassportUamtk(String appId, String uamtk) {
        if (StringUtils.isNotBlank(appId)) {
            try {
                String result = HttpURL12306.loginSuccessPassportUamtk(appId, uamtk);
                if (StringUtils.isBlank(result)) {
                    return RestResult.SUCCESS().message("认证失败").data("5").build();
                }
                return RestResult.SUCCESS().message("认证成功").data(result).build();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return RestResult.ERROR_PARAMS().message("认证参数为空").build();
    }

    /**
     * 获取用户名
     *
     * @param tk
     * @return
     */
    @Override
    public RestResult getUserName(String tk) {
        if (StringUtils.isNotBlank(tk)) {
            try {
                String result = HttpURL12306.getUserName(tk);
                if (!StringUtils.equals(result, "5")) {
                    return RestResult.SUCCESS().message("获取用户名成功").data(result).build();
                } else {
                    return RestResult.SUCCESS().message("获取用户名失败").data(result).build();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return RestResult.ERROR_PARAMS().message("uamtk为空，无法获取用户名").build();
    }

    /**
     * 用户退出
     *
     * @return
     */
    @Override
    public RestResult loginOut() {
        try {
            HttpURL12306.loginOut();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return RestResult.SUCCESS().message("退出成功").build();
    }

    /**
     * 验证码坐标匹配
     *
     * @param imgIndexArr
     * @return
     */
    private String capthchaXYMatching(String[] imgIndexArr) {
        StringJoiner answer = new StringJoiner(",");
        if (imgIndexArr != null && imgIndexArr.length > 0) {
            for (String index : imgIndexArr) {
                switch (index) {
                    case "1":
                        answer.add(this.yzm1Point);
                        break;
                    case "2":
                        answer.add(this.yzm2Point);
                        break;
                    case "3":
                        answer.add(this.yzm3Point);
                        break;
                    case "4":
                        answer.add(this.yzm4Point);
                        break;
                    case "5":
                        answer.add(this.yzm5Point);
                        break;
                    case "6":
                        answer.add(this.yzm6Point);
                        break;
                    case "7":
                        answer.add(this.yzm7Point);
                        break;
                    case "8":
                        answer.add(this.yzm8Point);
                        break;
                    default:
                        break;
                }
            }
        }
        return answer.toString();
    }
}
