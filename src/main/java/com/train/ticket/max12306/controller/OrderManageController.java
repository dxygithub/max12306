package com.train.ticket.max12306.controller;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.train.ticket.max12306.request.HttpURL12306;
import com.train.ticket.max12306.constant.HttpURLConstant12306;
import com.train.ticket.max12306.common.RestResult;
import com.train.ticket.max12306.entity.PassengerInfo;
import com.train.ticket.max12306.entity.TicketInfo;
import com.train.ticket.max12306.enumeration.ISPassCode;
import com.train.ticket.max12306.enumeration.OrderStatus;
import com.train.ticket.max12306.order.OrderManage;
import com.train.ticket.max12306.requestvo.SubmitOrderVo;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @ClassName OrderManageController
 * @ClassExplain: 订单处理
 * @Author Duxiaoyu
 * @Date 2020/8/29 9:58
 * @Since V 1.0
 */
@RestController
public class OrderManageController {

    private static final Logger LOGGER = LoggerFactory.getLogger(OrderManageController.class);

    @Autowired
    private OrderManage order;

    @Autowired
    private HttpURL12306 url12306;

    private static SubmitOrderVo orderVoST;

    /**
     * 提交订单
     *
     * @param orderVo
     * @return
     */
    @PostMapping("/max/submitOrder")
    public RestResult submitOrder(@RequestBody SubmitOrderVo orderVo) {
        if (Objects.nonNull(orderVo)) {
            orderVoST = orderVo;
            List<PassengerInfo> passengerInfoList = orderVoST.getPassengerInfoList();
            passengerInfoList.forEach(passengerInfo -> {
                passengerInfo.setSeatType(orderVo.getSeatType());
            });
            TicketInfo ticket = orderVoST.getTicketInfo();

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            String backDate = sdf.format(new Date());

            try {
                // 检查用户登录是否失效
                String isLogin = "";
                while (!StringUtils.equals("Y", isLogin)) {
                    isLogin = url12306.checkUser();
                    if (!StringUtils.equals("Y", isLogin)) {
                        String tk = HttpURL12306.COOKIE_CACHE_MAP.get("tk");
                        // 快速自动刷新用户
                        url12306.getUserName(tk);
                        // 等待500毫秒继续执行，防止请求被封
                        Thread.sleep(500L);
                    }
                }
                if (StringUtils.equals("Y", isLogin)) {
                    // 开始提交预定订单
                    OrderStatus subOrderStatus = order.submitOrderRequest(ticket.getTicketSecretKey(), orderVoST.getFromDate(), backDate, orderVoST.getFromStationCode(), orderVoST.getToStationCode());
                    if (subOrderStatus == OrderStatus.SUCCESS) {
                        // 开始获取订单token相关参数
                        Map<String, String> tokenMap = order.getSubmitToken();
                        if (!CollectionUtils.isEmpty(tokenMap)) {
                            // 判断是否需要滑块验证，目前12306在提交订单时，尤其是准点开售时，不再需要图片验证
                            if (StringUtils.equals("1", tokenMap.get("ifCheckSlidePasscode"))) {
                                LOGGER.info("======> 本次订单提交需要滑块验证，请注意浏览器...");
                                // 滑块token
                                String ifCheckSlidePasscodeToken = tokenMap.get("ifCheckSlidePasscodeToken");
                                // 交给前端完成滑块验证，目前为手动验证
                                Map<String, String> result = new HashMap<>(16);
                                result.put("ifCheckSlidePasscodeToken", ifCheckSlidePasscodeToken);
                                return RestResult.SUCCESS().data(result).isSlidePassCode(1).message("本次订单提交需要滑块验证").build();
                            } else {
                                // 不需要滑块验证
                                LOGGER.info("======> 本次订单提交不需要验证码...");
                                return submitConfirmOrder("", "");
                            }
                            // 需要图片验证码提交订单，暂时弃用
                        /*ISPassCode passCode = order.startSubmitOrder(passengerInfoList, tokenMap.get("submitToken"),"","");
                        if (passCode == ISPassCode.YES) {
                            while (true) {
                                // 获取图片验证码
                                String imgCaptcha = url12306.getImgCaptcha();
                                String[] imgCaptchaArr = imgCaptcha.split("--");
                                // 开始自动校验
                                String[] autoCheckImgIndex = UserLoginServiceImpl.autoCheckImgCaptcha(imgCaptchaArr[1]);
                                String answer = UserLoginServiceImpl.capthchaXYMatching(autoCheckImgIndex);
                                if (StringUtils.isNotBlank(answer)) {
                                    LOGGER.info("======> 图片验证码识别成功...");
                                    int code = checkRandCodeAnsyn(answer, tokenMap.get("submitToken"));
                                    if (code == 4) {
                                        LOGGER.info("======> 图片验证码校验通过...");
                                        break;
                                    } else {
                                        LOGGER.info("======> 图片验证码校验未通过...");
                                    }
                                } else {
                                    LOGGER.info("======> 图片验证码识别失败...");
                                }
                            }
                            // 开始检查当前车票排队人数和余票信息
                        }*/
                        } else {
                            return RestResult.SERVER_ERROR().message("获取订单token相关参数失败").build();
                        }
                    } else {
                        // 提交订单失败
                        return RestResult.SERVER_ERROR().message("提交订单失败").build();
                    }
                } else {
                    // 快速自动登录-后面实现
                    return RestResult.SERVER_ERROR().message("登录状态失效，请重新登录").build();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return RestResult.ERROR_PARAMS().message("订单提交参数为空").build();
    }


    /**
     * 滑块验证 -> 检查订单 -> 确认订单
     *
     * @return
     */
    @PostMapping("/max/checkSlidePassCodeForOrder")
    public RestResult checkSlidePassCodeForOrder(@RequestBody Map<String, String> params) {
        return submitConfirmOrder(params.get("sessionId"), params.get("sig"));
    }

    /**
     * 提交并确认订单
     *
     * @param sessionId
     * @param sig
     * @return
     */
    public RestResult submitConfirmOrder(String sessionId, String sig) {
        List<PassengerInfo> passengerInfoList = orderVoST.getPassengerInfoList();
        passengerInfoList.forEach(passengerInfo -> {
            passengerInfo.setSeatType(orderVoST.getSeatType());
        });
        // 开始尝试提交订单
        try {
            ISPassCode passCode = order.startSubmitOrder(passengerInfoList, OrderManage.TOKEN_MAP.get("submitToken"), sessionId, sig);
            if (passCode != ISPassCode.ERR) {
                // 开始检查当前车票排队人数和余票信息
                TicketInfo ticketInfo = orderVoST.getTicketInfo();
                ticketInfo.setStartDate(orderVoST.getFromDate());// 发车日期
                OrderStatus orderQueueRes = order.getQueueCount(ticketInfo, passengerInfoList.get(0).getSeatType());
                if (orderQueueRes == OrderStatus.SUCCESS) {
                    // 开始确认订单
                    OrderStatus confirmOrderRes = order.confirmSingleForQueue(orderVoST.getTicketInfo().getTrainLocation(), passengerInfoList);
                    if (confirmOrderRes == OrderStatus.SUCCESS) {
                        // 开始进入订单等待期
                        String sequenceNo = order.orderWait();
                        if (StringUtils.isNotBlank(sequenceNo)) {
                            LOGGER.info("======> 恭喜您订票成功，订单号为：{}, 请立即打开浏览器登录12306，访问‘未完成订单’，在30分钟内完成支付！", sequenceNo);
                            return RestResult.SUCCESS().data(sequenceNo).isSlidePassCode(0).message(String.format("恭喜您订票成功，订单号为：%s, 请立即打开浏览器登录12306，访问‘未完成订单’，在30分钟内完成支付！", sequenceNo)).build();
                        } else {
                            return RestResult.SERVER_ERROR().data(orderQueueRes).message("订单等待失败或超时").build();
                        }
                    } else {
                        return RestResult.SERVER_ERROR().data(orderQueueRes).message("确认订单失败").build();
                    }
                } else {
                    return RestResult.SERVER_ERROR().data(orderQueueRes).message("排队失败").build();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return RestResult.SERVER_ERROR().message("订单检查失败").build();
    }

    /**
     * 校验订单图片验证码: 暂时弃用
     *
     * @param answer      坐标
     * @param submitToken 订单token
     * @return
     * @throws Exception
     */
    public int checkRandCodeAnsyn(String answer, String submitToken) throws Exception {
        List<NameValuePair> formPail = new ArrayList<>();
        formPail.add(new BasicNameValuePair("randCode", answer));
        formPail.add(new BasicNameValuePair("REPEAT_SUBMIT_TOKEN", submitToken));
        formPail.add(new BasicNameValuePair("rand", "randp"));
        HttpPost post = HttpURL12306.httpPostBuild(HttpURLConstant12306.ORDER_IMG_CAPTCHA_CHECK, formPail, url12306.getCookieStr(null));
        try (CloseableHttpClient client = HttpURL12306.httpClientBuild()) {
            try (CloseableHttpResponse response = client.execute(post, HttpURL12306.context)) {
                HttpEntity entity = response.getEntity();
                String result = EntityUtils.toString(entity);
                // 释放资源
                EntityUtils.consume(entity);
                if (StringUtils.isNotBlank(result)) {
                    JSONObject json = JSONUtil.parseObj(result);
                    if (200 == json.get("httpstatus", Integer.class) && json.get("status", Boolean.class)) {
                        String message = json.get("msg", String.class);
                        if (message.equalsIgnoreCase("TRUE")) {
                            return 4;
                        }
                    } else {
                        return 5;
                    }
                } else {
                    LOGGER.info("======> 接口返回结果为空: 302...");
                }
            }
        }
        return 5;
    }
}
