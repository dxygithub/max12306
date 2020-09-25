package com.train.ticket.max12306.service.impl;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.train.ticket.max12306.common.*;
import com.train.ticket.max12306.constant.HttpURLConstant12306;
import com.train.ticket.max12306.entity.MyOrder;
import com.train.ticket.max12306.entity.PassengerInfo;
import com.train.ticket.max12306.request.HttpURL12306;
import com.train.ticket.max12306.requestvo.InitSlidePassPort;
import com.train.ticket.max12306.requestvo.PassengersVo;
import com.train.ticket.max12306.requestvo.UserLoginRequest;
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
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    private HttpURL12306 url12306;

    /**
     * 验证码中心位置固定坐标
     **/
    private static final String YZM1_POS = "35,72";
    private static final String YZM2_POS = "111,72";
    private static final String YZM3_POS = "181,72";
    private static final String YZM4_POS = "250,72";
    private static final String YZM5_POS = "35,148";
    private static final String YZM6_POS = "111,148";
    private static final String YZM7_POS = "181,148";
    private static final String YZM8_POS = "250,148";


    /**
     * 获取图片验证码
     *
     * @return
     */
    @Override
    public RestResult getImgCaptcha() {
        String[] resultArr = null;
        try {
            String result = url12306.getImgCaptcha();
            resultArr = result.split("--");
            // 登录前获取浏览器请求标识参数
            if (!(HttpURL12306.COOKIE_CACHE_MAP.containsKey("RAIL_DEVICEID") && HttpURL12306.COOKIE_CACHE_MAP.containsKey("RAIL_EXPIRATION"))) {
                HttpURL12306.getDeviceIdParams();
            }
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
                String ifCheckSlidePasscodeToken = url12306.initSlidePassPort(passPort);
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
            String answer = "";
            String resultCode = "";
            if (!autoCheck) {
                if (StringUtils.isBlank(imgIndex)) {
                    return RestResult.ERROR_PARAMS().message("验证码坐标为空").build();
                }
                LOGGER.info("======> 开始手动识别验证码...");
                // 手动识别验证码，坐标位置前端已经处理过，此处直接进行校验即可
                resultCode = url12306.checkImgCapthcha(imgIndex, timer);
            } else {
                LOGGER.info("======> 开始自动识别验证码...");
                String[] autoCheckImgIndex = autoCheckImgCaptcha(img);
                answer = capthchaXYMatching(autoCheckImgIndex);
                resultCode = url12306.checkImgCapthcha(answer, timer);
            }
            if (resultCode.equals("4")) {
                return RestResult.SUCCESS().data("4-" + answer).message("验证码校验成功").build();
            } else {
                return RestResult.SUCCESS().data("5-").message("验证码校验失败").build();
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
                String result = url12306.loginRequest(loginRequest);
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
                String result = url12306.loginSuccessPassportUamtk(appId, uamtk);
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
                String result = url12306.getUserName(tk);
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
     * 获取乘车人
     *
     * @return
     */
    @Override
    public RestResult getPassengers() {
        try {
            List<PassengerInfo> passengerInfos = url12306.getPassengersInfo();
            return RestResult.SUCCESS().data(passengerInfos).build();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return RestResult.SERVER_ERROR().build();
    }

    /**
     * 获取订单信息
     *
     * @return
     */
    @Override
    public RestResult getOrderInfo(String queryStartDate, String queryEndDate, String queryWhere) {

        try {
            List<MyOrder> orderList = url12306.getOrderInfo(queryStartDate, queryEndDate, queryWhere);
            return RestResult.SUCCESS().data(orderList).build();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return RestResult.ERROR_PARAMS().build();
    }

    /**
     * 获取未完成的订单
     *
     * @return
     */
    @Override
    public RestResult getOrderNoComplete() {
        try {
            List<MyOrder> orderList = url12306.getOrderNoComplete();
            return RestResult.SUCCESS().data(orderList).build();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return RestResult.SERVER_ERROR().build();
    }

    /**
     * 删除乘车人
     *
     * @param passengersVo
     * @return
     */
    @Override
    public RestResult delPassenger(PassengersVo passengersVo) {
        if (Objects.nonNull(passengersVo)) {
            try {
                String result = url12306.delPassenger(passengersVo);
                return RestResult.SUCCESS().data(result).build();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return RestResult.ERROR_PARAMS().message("参数为空").build();
    }

    /**
     * 新增乘车人
     *
     * @param passengersVo
     * @return
     */
    @Override
    public RestResult addPassengers(PassengersVo passengersVo) {
        if (Objects.nonNull(passengersVo)) {
            try {
                String result = url12306.addPassenger(passengersVo);
                return RestResult.SUCCESS().data(result).build();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return RestResult.ERROR_PARAMS().message("参数为空").build();
    }

    /**
     * 用户退出
     *
     * @return
     */
    @Override
    public RestResult loginOut() {
        try {
            url12306.loginOut();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return RestResult.SUCCESS().message("退出成功").build();
    }

    /**
     * 自动识别图片验证码
     *
     * @param img 验证码
     * @return
     */
    public static String[] autoCheckImgCaptcha(String img) throws Exception {
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
                        return autoCheckImgIndex;
                    }
                }
            }
        }
        return null;
    }

    /**
     * 验证码坐标匹配
     *
     * @param imgIndexArr
     * @return
     */
    public static String capthchaXYMatching(String[] imgIndexArr) {
        StringJoiner answer = new StringJoiner(",");
        if (imgIndexArr != null && imgIndexArr.length > 0) {
            for (String index : imgIndexArr) {
                switch (index) {
                    case "1":
                        answer.add(YZM1_POS);
                        break;
                    case "2":
                        answer.add(YZM2_POS);
                        break;
                    case "3":
                        answer.add(YZM3_POS);
                        break;
                    case "4":
                        answer.add(YZM4_POS);
                        break;
                    case "5":
                        answer.add(YZM5_POS);
                        break;
                    case "6":
                        answer.add(YZM6_POS);
                        break;
                    case "7":
                        answer.add(YZM7_POS);
                        break;
                    case "8":
                        answer.add(YZM8_POS);
                        break;
                    default:
                        break;
                }
            }
        }
        return answer.toString();
    }
}
