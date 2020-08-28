package com.train.ticket.max12306.order;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.train.ticket.max12306.common.ConfigFileUtil;
import com.train.ticket.max12306.common.HttpURL12306;
import com.train.ticket.max12306.common.HttpURLConstant12306;
import com.train.ticket.max12306.entity.PassengerInfo;
import com.train.ticket.max12306.entity.TicketInfo;
import com.train.ticket.max12306.enumeration.ISPassCode;
import com.train.ticket.max12306.enumeration.OrderStatus;
import com.train.ticket.max12306.enumeration.SeatType;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.URLDecoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @ClassName OrderManage 订单处理
 * @Author duxiaoyu
 * @Date 2020/8/27 14:59
 * @Version 1.0
 */
@Component
public class OrderManage {

    private static final Logger LOGGER = LoggerFactory.getLogger(OrderManage.class);

    private static final int SUCCESS = 200;

    private static final boolean STATUS_SUCCESS = true;

    /**
     * 小黑屋: 车次号 -> 加入时间
     */
    public static final Map<String, Long> SMALL_DARK_ROOM = new HashMap<>(16);

    @Autowired
    private ConfigFileUtil config;

    @Autowired
    private HttpURL12306 url12306;

    // 开始预定车票

    /**
     * 提交预定请求
     *
     * @param secretStr 车次加密串
     * @return OrderStatus
     */
    public OrderStatus submitOrderRequest(String secretStr, String trainDate, String backTrainDate, String queryFromStationCode, String queryToStationCode) throws Exception {
        if (StringUtils.isBlank(secretStr)) {
            return OrderStatus.PARM_EMPTY;
        }
        // 解码加密串
        String secretStrDeCode = URLDecoder.decode(secretStr, "UTF-8");
        List<NameValuePair> formPail = new ArrayList<>();
        formPail.add(new BasicNameValuePair("secretStr", secretStrDeCode));
        formPail.add(new BasicNameValuePair("train_date", trainDate));
        formPail.add(new BasicNameValuePair("back_train_date", backTrainDate));
        formPail.add(new BasicNameValuePair("tour_flag", "dc"));
        formPail.add(new BasicNameValuePair("purpose_codes", "purpose_codes"));
        formPail.add(new BasicNameValuePair("query_from_station_name", HttpURL12306.STATION_MAP.get(queryFromStationCode)));
        formPail.add(new BasicNameValuePair("query_to_station_name", HttpURL12306.STATION_MAP.get(queryToStationCode)));
        formPail.add(new BasicNameValuePair("undefined", ""));
        HttpPost post = HttpURL12306.httpPostBuild(HttpURLConstant12306.SUBMIT_ORDER_REQUEST, formPail, url12306.getCookieStr(null));
        try (CloseableHttpClient client = HttpURL12306.httpClientBuild()) {
            try (CloseableHttpResponse response = client.execute(post, HttpURL12306.context)) {
                HttpEntity entity = response.getEntity();
                String result = EntityUtils.toString(entity);
                // 释放资源
                EntityUtils.consume(entity);
                if (StringUtils.isNotBlank(result)) {
                    JSONObject json = JSONUtil.parseObj(result);
                    String message = json.get("messages", String.class);
                    if (json.get("status", Boolean.class)) {
                        LOGGER.info("======> 预定车票成功...");
                        return OrderStatus.SUCCESS;
                    } else {
                        LOGGER.info("======> 预定车票失败，原因: {}...", message);
                    }
                } else {
                    return OrderStatus.EMPTY;
                }
            }
        }
        return OrderStatus.FAIL;
    }


    // 进入下单页面获取submitToken

    /**
     * 获取订单token
     *
     * @return token
     */
    public String getSubmitToken() throws Exception {
        List<NameValuePair> formPail = new ArrayList<>();
        formPail.add(new BasicNameValuePair("_json_att", ""));
        HttpPost post = HttpURL12306.httpPostBuild(HttpURLConstant12306.INIT_DC, formPail, url12306.getCookieStr(null));
        post.setHeader("Referer", "https://kyfw.12306.cn/otn/confirmPassenger/initDc");
        try (CloseableHttpClient client = HttpURL12306.httpClientBuild()) {
            try (CloseableHttpResponse response = client.execute(post, HttpURL12306.context)) {
                HttpEntity entity = response.getEntity();
                String result = EntityUtils.toString(entity);
                // 释放资源
                EntityUtils.consume(entity);
                if (StringUtils.isNotBlank(result)) {
                    String token = "";
                    Pattern p = Pattern.compile("globalRepeatSubmitToken \\= '(.*?)';");
                    Matcher m = p.matcher(result);
                    while (m.find()) {
                        token = m.group(1);
                    }
                    Pattern p1 = Pattern.compile("'key_check_isChange':'(.*?)',");
                    Matcher m1 = p1.matcher(result);
                    while (m1.find()) {
                        token += "," + m1.group(1);
                    }
                    Pattern p2 = Pattern.compile("'leftTicketStr':'(.*?)',");
                    Matcher m2 = p2.matcher(result);
                    while (m2.find()) {
                        token += "," + m2.group(1);
                    }
                    if (StringUtils.isNotBlank(token)) {
                        LOGGER.info("======> 订单token获取成功...");
                        return token;
                    }
                    LOGGER.info("======> 订单token获取失败...");
                }
            }
        }
        return "";
    }

    // 获取乘车人和席位信息->提交订单->检查是否需要验证码或者滑块验证

    /**
     * 获取乘车人和席位信息 -> 提交订单 -> 检查是否需要验证码或者滑块验证
     *
     * @param passengerInfos 乘车人信息
     * @param submitToken    订单token
     * @return ISPassCode
     * @throws Exception
     */
    public ISPassCode startSubmitOrder(List<PassengerInfo> passengerInfos, String submitToken) throws Exception {
        StringJoiner passengerTicketStr = new StringJoiner(",");
        StringJoiner oldPassengerStr = new StringJoiner(",");
        passengerInfos.forEach(passengerInfo -> {
            // 乘车人车票字符串
            passengerTicketStr.add(passengerInfo.getSeatType().getValue());
            passengerTicketStr.add("0");
            passengerTicketStr.add("1");
            passengerTicketStr.add(passengerInfo.getPassengerName());
            passengerTicketStr.add("1");
            passengerTicketStr.add(passengerInfo.getPassengerIdNo());
            passengerTicketStr.add(passengerInfo.getMobileNo());
            passengerTicketStr.add("N");
            passengerTicketStr.add(passengerInfo.getAllEncStr() + "_");

            // 原有乘车人字符串
            oldPassengerStr.add(passengerInfo.getPassengerName());
            oldPassengerStr.add("1");
            oldPassengerStr.add(passengerInfo.getPassengerIdNo());
            oldPassengerStr.add("1_");
        });

        String afterPassengerTicketStr = passengerTicketStr.toString();
        afterPassengerTicketStr = afterPassengerTicketStr.substring(0, afterPassengerTicketStr.lastIndexOf("_"));
        String afterOldPassengerStr = oldPassengerStr.toString();

        List<NameValuePair> formPail = new ArrayList<>();
        formPail.add(new BasicNameValuePair("cancel_flag", "2"));
        // 这个参数不知道干什么的:000000000000000000000000000000
        formPail.add(new BasicNameValuePair("bed_level_order_num", "000000000000000000000000000000"));
        formPail.add(new BasicNameValuePair("passengerTicketStr", afterPassengerTicketStr));
        formPail.add(new BasicNameValuePair("oldPassengerStr", afterOldPassengerStr));
        formPail.add(new BasicNameValuePair("tour_flag", "dc"));
        formPail.add(new BasicNameValuePair("randCode", ""));
        formPail.add(new BasicNameValuePair("whatsSelect", "1"));
        formPail.add(new BasicNameValuePair("sessionId", ""));
        formPail.add(new BasicNameValuePair("sig", ""));
        formPail.add(new BasicNameValuePair("scene", "nc_login"));
        formPail.add(new BasicNameValuePair("_json_att", ""));
        formPail.add(new BasicNameValuePair("REPEAT_SUBMIT_TOKEN", submitToken));

        HttpPost post = HttpURL12306.httpPostBuild(HttpURLConstant12306.CHECK_ORDER_INFO, formPail, url12306.getCookieStr(null));
        try (CloseableHttpClient client = HttpURL12306.httpClientBuild()) {
            try (CloseableHttpResponse response = client.execute(post, HttpURL12306.context)) {
                HttpEntity entity = response.getEntity();
                String result = EntityUtils.toString(entity);
                // 释放资源
                EntityUtils.consume(entity);
                if (StringUtils.isNotBlank(result)) {
                    JSONObject json = JSONUtil.parseObj(result);
                    String message = json.get("messages", String.class);
                    if (SUCCESS == json.get("httpstatus", Integer.class) && json.get("status", Boolean.class)) {
                        JSONObject data = json.get("data", JSONObject.class);
                        if (data.get("submitStatus", Boolean.class)) {
                            LOGGER.info("======> 订单提交成功...");
                            // 是否需要验证码: Y 需要 / N 不需要
                            String ifShowPassCode = data.get("ifShowPassCode", String.class);
                            // 安全期时间: 单位 -> 秒
                            String ifShowPassCodeTime = data.get("ifShowPassCodeTime", String.class);
                            if (StringUtils.equals("Y", ifShowPassCode)) {
                                LOGGER.info("======> 本次订单提交需要验证码...");
                                LOGGER.info("======> 本次订单提交需要等待安全期: {}/s...", StringUtils.isBlank(ifShowPassCodeTime) ? "empty" : Integer.parseInt(ifShowPassCodeTime));
                                return ISPassCode.YES;
                            } else if (StringUtils.equals("N", ifShowPassCode)) {
                                LOGGER.info("======> 本次订单提交不需要验证码...");
                                LOGGER.info("======> 本次订单提交需要等待安全期: {}/s...", StringUtils.isBlank(ifShowPassCodeTime) ? "empty" : Integer.parseInt(ifShowPassCodeTime));
                                return ISPassCode.NO;
                            } else if (StringUtils.equals("X", ifShowPassCode)) {
                                LOGGER.info("======> 本次订单提交预定失败...");
                                return ISPassCode.ERR;
                            } else {
                                LOGGER.info("======> 本次订单提交不需要验证码...");
                                LOGGER.info("======> 本次订单提交需要等待安全期: {}/s...", StringUtils.isBlank(ifShowPassCodeTime) ? "empty" : Integer.parseInt(ifShowPassCodeTime));
                                return ISPassCode.NO;
                            }
                        } else {
                            LOGGER.info("======> 本次订单提交预定失败...");
                        }
                    } else {
                        LOGGER.info("======> 本次订单提交预定失败，原因: {}...", message);
                    }
                }
            }
        }
        return ISPassCode.ERR;
    }

    // 获取当前车票排队人数和余票信息

    /**
     * 检查当前车票排队人数和余票信息
     *
     * @param submitToken
     * @param ticketInfo
     * @return
     */
    public OrderStatus getQueueCount(String submitToken, String leftTicket, TicketInfo ticketInfo, SeatType seatType) throws Exception {
        List<NameValuePair> formPail = new ArrayList<>();
        formPail.add(new BasicNameValuePair("train_date", getGMT(ticketInfo.getStartDate())));
        formPail.add(new BasicNameValuePair("train_no", ticketInfo.getTrainNo()));
        formPail.add(new BasicNameValuePair("stationTrainCode", ticketInfo.getTrainCode()));
        formPail.add(new BasicNameValuePair("seatType", seatType.getValue()));
        formPail.add(new BasicNameValuePair("fromStationTelecode", HttpURL12306.STATION_MAP.get(ticketInfo.getFromStationCode())));
        formPail.add(new BasicNameValuePair("toStationTelecode", HttpURL12306.STATION_MAP.get(ticketInfo.getToStationCode())));
        formPail.add(new BasicNameValuePair("leftTicket", leftTicket));
        formPail.add(new BasicNameValuePair("purpose_codes", "00"));
        formPail.add(new BasicNameValuePair("train_location", ticketInfo.getTrainLocation()));
        formPail.add(new BasicNameValuePair("_json_att", ""));
        formPail.add(new BasicNameValuePair("REPEAT_SUBMIT_TOKEN", submitToken));
        HttpPost post = HttpURL12306.httpPostBuild(HttpURLConstant12306.GET_QUEUE_COUNT, formPail, url12306.getCookieStr(null));
        try (CloseableHttpClient client = HttpURL12306.httpClientBuild()) {
            try (CloseableHttpResponse response = client.execute(post, HttpURL12306.context)) {
                HttpEntity entity = response.getEntity();
                String result = EntityUtils.toString(entity);
                // 释放资源
                EntityUtils.consume(entity);
                if (StringUtils.isNotBlank(result)) {
                    JSONObject json = JSONUtil.parseObj(result);
                    if (SUCCESS == json.get("httpstatus", Integer.class) && json.get("status", Boolean.class)) {
                        LOGGER.info("======> 查询当前车票排队和余票信息成功...");
                        JSONObject data = json.get("data", JSONObject.class);
                        if (data.get("op_2", Boolean.class)) {
                            return OrderStatus.SUCCESS;
                        } else {
                            return OrderStatus.FAIL;
                        }
                    } else {
                        LOGGER.info("======> 查询当前车票排队和余票信息失败...");
                        return OrderStatus.FAIL;
                    }
                }
            }
        }
        return OrderStatus.ERROR;
    }

    // 确认订单提交是否成功

    /**
     * 确认订单提交
     *
     * @param submitToken      订单token
     * @param leftTicket       车票加密串
     * @param keyCheckIsChange 校验key
     * @param passengerInfos   乘车人
     */
    public void confirmSingleForQueue(String submitToken, String leftTicket, String keyCheckIsChange, String trainLocation, List<PassengerInfo> passengerInfos) throws Exception {
        StringJoiner passengerTicketStr = new StringJoiner(",");
        StringJoiner oldPassengerStr = new StringJoiner(",");
        passengerInfos.forEach(passengerInfo -> {
            // 乘车人车票字符串
            passengerTicketStr.add(passengerInfo.getSeatType().getValue());
            passengerTicketStr.add("0");
            passengerTicketStr.add("1");
            passengerTicketStr.add(passengerInfo.getPassengerName());
            passengerTicketStr.add("1");
            passengerTicketStr.add(passengerInfo.getPassengerIdNo());
            passengerTicketStr.add(passengerInfo.getMobileNo());
            passengerTicketStr.add("N");
            passengerTicketStr.add(passengerInfo.getAllEncStr() + "_");

            // 原有乘车人字符串
            oldPassengerStr.add(passengerInfo.getPassengerName());
            oldPassengerStr.add("1");
            oldPassengerStr.add(passengerInfo.getPassengerIdNo());
            oldPassengerStr.add("1_");
        });

        String afterPassengerTicketStr = passengerTicketStr.toString();
        afterPassengerTicketStr = afterPassengerTicketStr.substring(0, afterPassengerTicketStr.lastIndexOf("_"));
        String afterOldPassengerStr = oldPassengerStr.toString();

        List<NameValuePair> formPail = new ArrayList<>();
        formPail.add(new BasicNameValuePair("passengerTicketStr", afterPassengerTicketStr));
        formPail.add(new BasicNameValuePair("oldPassengerStr", afterOldPassengerStr));
        formPail.add(new BasicNameValuePair("randCode", ""));
        formPail.add(new BasicNameValuePair("purpose_codes", "00"));
        formPail.add(new BasicNameValuePair("key_check_isChange", keyCheckIsChange));
        formPail.add(new BasicNameValuePair("leftTicketStr", leftTicket));
        formPail.add(new BasicNameValuePair("train_location", trainLocation));
        formPail.add(new BasicNameValuePair("choose_seats", ""));
        formPail.add(new BasicNameValuePair("seatDetailType", "000"));
        formPail.add(new BasicNameValuePair("whatsSelect", "1"));
        formPail.add(new BasicNameValuePair("dwAll", "N"));
        formPail.add(new BasicNameValuePair("_json_att", ""));
        formPail.add(new BasicNameValuePair("REPEAT_SUBMIT_TOKEN", submitToken));

        HttpPost post = HttpURL12306.httpPostBuild(HttpURLConstant12306.CONFIRM_SINGLE_FOR_QUEUE, formPail, url12306.getCookieStr(null));
        try (CloseableHttpClient client = HttpURL12306.httpClientBuild()) {
            try (CloseableHttpResponse response = client.execute(post, HttpURL12306.context)) {
                HttpEntity entity = response.getEntity();
                String result = EntityUtils.toString(entity);
                // 释放资源
                EntityUtils.consume(entity);
                if (StringUtils.isNotBlank(result)) {
                    JSONObject json = JSONUtil.parseObj(result);
                    String message = json.get("messages", String.class);
                    if (SUCCESS == json.get("httpstatus", Integer.class) && json.get("status", Boolean.class)) {
                        JSONObject data = json.get("data", JSONObject.class);
                        if (data.get("submitStatus", Boolean.class)) {
                            LOGGER.info("======> 确认订单提交成功...");
                        } else {
                            String errMsg = data.get("errMsg", String.class);
                            LOGGER.info("======> 确认订单提交失败，原因: {}...", errMsg);
                        }
                    } else {
                        LOGGER.info("======> 确认订单提交失败，原因: {}...", message);
                    }
                }
            }
        }
    }

    // 进入订单等待

    /**
     * 进入订单等待处理
     *
     * @param submitToken 订单token
     * @return 订单号
     */
    public String orderWait(String submitToken) throws Exception {
        String sequenceNo = "";
        Integer waitTime = 0;
        Integer waitCount = 0;
        String message = "";
        int sleepCount = 0;
        // 开始进入订单等待时间
        while (waitTime >= 0) {
            if (sleepCount <= 20) {
                HttpGet get = HttpURL12306.httpGetBuild(
                        HttpURLConstant12306.ORDER_WAIT.
                                replace("{1}", String.valueOf(System.currentTimeMillis())).
                                replace("{2}", submitToken), url12306.getCookieStr(null));
                try (CloseableHttpClient client = HttpURL12306.httpClientBuild()) {
                    try (CloseableHttpResponse response = client.execute(get, HttpURL12306.context)) {
                        HttpEntity entity = response.getEntity();
                        String result = EntityUtils.toString(entity);
                        // 释放资源
                        EntityUtils.consume(entity);
                        if (StringUtils.isNotBlank(result)) {
                            JSONObject json = JSONUtil.parseObj(result);
                            if (SUCCESS == json.get("httpstatus", Integer.class) && json.get("status", Boolean.class)) {
                                JSONObject data = json.get("data", JSONObject.class);
                                waitTime = StringUtils.isBlank(data.get("waitTime", String.class)) ? 0 : Integer.parseInt(data.get("waitTime", String.class));
                                waitCount = StringUtils.isBlank(data.get("waitCount", String.class)) ? 0 : Integer.parseInt(data.get("waitCount", String.class));
                                sequenceNo = data.get("orderId", String.class);
                                LOGGER.info("======> 目前排队人数: {} -> 排队等待时间预计还剩: {}/ms...", waitCount, waitTime);
                                message = data.get("msg", String.class);
                                if (StringUtils.isNotBlank(message)) {
                                    LOGGER.info("======> 订单异常，处理结果: {}...", message);
                                    break;
                                }
                                // 休眠3秒后继续获取订单等待信息，最高请求次数20次，超过20次视为订单失败
                                sleepCount++;
                                Thread.sleep(3000L);
                            }
                        }
                    }
                }
            }else {
                // 订单失败
            }
        }
        return sequenceNo;
    }

    // 获取订单号 -> 订票成功


    /**
     * 获取标准时间字符串
     *
     * @param date
     * @return
     */
    public String getGMT(String date) {
        String str = "";
        TimeZone tz = TimeZone.getTimeZone("ETC/GMT-8");
        TimeZone.setDefault(tz);
        Calendar cal = Calendar.getInstance(new SimpleTimeZone(0, "GMT"));
        Date dd;
        SimpleDateFormat shortSdf = new SimpleDateFormat("yyyy-MM-dd");
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd yyyy HH:mm:ss 'GMT'", Locale.US);
            sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
            dd = shortSdf.parse(date);
            cal.setTime(dd);
            str = sdf.format(cal.getTime());
            return str + "+0800 (中国标准时间)";
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void main(String[] args) {
        OrderManage orderManage = new OrderManage();
        System.out.println(orderManage.getGMT("2020-08-28"));
    }
}