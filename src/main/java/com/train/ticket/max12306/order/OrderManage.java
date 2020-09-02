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
import org.springframework.stereotype.Service;

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
@Service
public class OrderManage {

    private static final Logger LOGGER = LoggerFactory.getLogger(OrderManage.class);

    private static final int SUCCESS = 200;

    private static final boolean STATUS_SUCCESS = true;

    /**
     * 小黑屋: 车次号 -> 加入时间
     */
    public static final Map<String, Long> SMALL_DARK_ROOM = new HashMap<>(16);

    /**
     * 订单token参数
     */
    public static final Map<String, String> TOKEN_MAP = new HashMap<>(16);

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
        formPail.add(new BasicNameValuePair("purpose_codes", "ADULT"));
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
                    LOGGER.info("======> 接口返回结果为空: 302...");
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
    public Map<String, String> getSubmitToken() throws Exception {
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
                    String param = "";

                    Pattern p1 = Pattern.compile("globalRepeatSubmitToken \\= '(.*?)';");
                    Matcher m1 = p1.matcher(result);
                    while (m1.find()) {
                        param = m1.group(1);
                    }
                    if (StringUtils.isNotBlank(param)) {
                        TOKEN_MAP.put("submitToken", param);
                    }

                    param = "";
                    // 获取提交订单时的滑块passCode -> 0: 不需要滑块验证 1: 需要滑块验证
                    Pattern p2 = Pattern.compile("if_check_slide_passcode\\='(.*?)';");
                    Matcher m2 = p2.matcher(result);
                    while (m2.find()) {
                        param = m2.group(1);
                    }
                    if (StringUtils.isNotBlank(param)) {
                        TOKEN_MAP.put("ifCheckSlidePasscode", param);
                    }

                    param = "";
                    // 获取提交订单时的滑块token
                    Pattern p3 = Pattern.compile("if_check_slide_passcode_token\\='(.*?)';");
                    Matcher m3 = p3.matcher(result);
                    while (m3.find()) {
                        param = m3.group(1);
                    }
                    if (StringUtils.isNotBlank(param)) {
                        TOKEN_MAP.put("ifCheckSlidePasscodeToken", param);
                    }

                    param = "";
                    Pattern p4 = Pattern.compile("'key_check_isChange':'(.*?)',");
                    Matcher m4 = p4.matcher(result);
                    while (m4.find()) {
                        param = m4.group(1);
                    }
                    if (StringUtils.isNotBlank(param)) {
                        TOKEN_MAP.put("keyCheckIsChange", param);
                    }

                    param = "";
                    Pattern p5 = Pattern.compile("'leftTicketStr':'(.*?)',");
                    Matcher m5 = p5.matcher(result);
                    while (m5.find()) {
                        param = m5.group(1);
                    }
                    if (StringUtils.isNotBlank(param)) {
                        TOKEN_MAP.put("leftTicketStr", param);
                    }

                    if (!TOKEN_MAP.isEmpty()) {
                        LOGGER.info("======> 订单token参数获取成功 -> {}...", JSONUtil.toJsonStr(TOKEN_MAP));
                        return TOKEN_MAP;
                    }
                    LOGGER.info("======> 订单token参数获取失败...");
                } else {
                    LOGGER.info("======> 接口返回结果为空: 302...");
                }
            }
        }
        return null;
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
    public ISPassCode startSubmitOrder(List<PassengerInfo> passengerInfos, String submitToken, String sessionId, String sig) throws Exception {
        StringBuilder passengerTicketStrBUilder=new StringBuilder();
        StringBuilder oldPassengerStrBUilder=new StringBuilder();
        passengerInfos.forEach(passengerInfo -> {
            // 乘车人车票字符串
            passengerTicketStrBUilder.append(passengerInfo.getSeatType().getValue()+",");
            passengerTicketStrBUilder.append("0"+",");
            passengerTicketStrBUilder.append("1"+",");
            passengerTicketStrBUilder.append(passengerInfo.getPassengerName()+",");
            passengerTicketStrBUilder.append("1"+",");
            passengerTicketStrBUilder.append(passengerInfo.getPassengerIdNo()+",");
            passengerTicketStrBUilder.append(passengerInfo.getMobileNo()+",");
            passengerTicketStrBUilder.append("N"+",");
            passengerTicketStrBUilder.append(passengerInfo.getAllEncStr()+"_");

            // 原有乘车人字符串
            oldPassengerStrBUilder.append(passengerInfo.getPassengerName()+",");
            oldPassengerStrBUilder.append("1"+",");
            oldPassengerStrBUilder.append(passengerInfo.getPassengerIdNo()+",");
            oldPassengerStrBUilder.append("1_");
        });

        String afterPassengerTicketStr = passengerTicketStrBUilder.toString();
        afterPassengerTicketStr = afterPassengerTicketStr.substring(0, afterPassengerTicketStr.lastIndexOf("_"));
        String afterOldPassengerStr = oldPassengerStrBUilder.toString();

        LOGGER.info("======> 乘车人字符串: {}",afterPassengerTicketStr);
        LOGGER.info("======> 原有乘车人字符串: {}",afterOldPassengerStr);

        List<NameValuePair> formPail = new ArrayList<>();
        formPail.add(new BasicNameValuePair("cancel_flag", "2"));
        // 这个参数不知道干什么的:000000000000000000000000000000
        formPail.add(new BasicNameValuePair("bed_level_order_num", "000000000000000000000000000000"));
        formPail.add(new BasicNameValuePair("passengerTicketStr", afterPassengerTicketStr));
        formPail.add(new BasicNameValuePair("oldPassengerStr", afterOldPassengerStr));
        formPail.add(new BasicNameValuePair("tour_flag", "dc"));
        formPail.add(new BasicNameValuePair("randCode", ""));
        formPail.add(new BasicNameValuePair("whatsSelect", "1"));
        formPail.add(new BasicNameValuePair("sessionId", StringUtils.isBlank(sessionId) ? "" : sessionId));
        formPail.add(new BasicNameValuePair("sig", StringUtils.isBlank(sig) ? "" : sig));
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
                        String errMsg=data.get("errMsg",String.class);
                        if (data.get("submitStatus", Boolean.class)) {
                            // 是否需要验证码: Y 需要 / N 不需要
                            String ifShowPassCode = data.get("ifShowPassCode", String.class);
                            // 安全期时间: 单位 -> 毫秒
                            String ifShowPassCodeTime = data.get("ifShowPassCodeTime", String.class);
                            if (StringUtils.equals("Y", ifShowPassCode)) {
                                LOGGER.info("======> 本次订单提交需要验证码...");
                                LOGGER.info("======> 本次订单提交需要等待安全期: {}/ms...", StringUtils.isBlank(ifShowPassCodeTime) ? "empty" : Integer.parseInt(ifShowPassCodeTime));
                                LOGGER.info("======> 订单检查并提交成功...");
                                // 进入安全等待期
                                // Thread.sleep(Long.valueOf(ifShowPassCodeTime));
                                return ISPassCode.YES;
                            } else if (StringUtils.equals("N", ifShowPassCode)) {
                                LOGGER.info("======> 本次订单提交不需要验证码...");
                                LOGGER.info("======> 本次订单提交需要等待安全期: {}/ms...", StringUtils.isBlank(ifShowPassCodeTime) ? "empty" : Integer.parseInt(ifShowPassCodeTime));
                                LOGGER.info("======> 订单检查并提交成功...");
                                // 进入安全等待期
                                // Thread.sleep(Long.valueOf(ifShowPassCodeTime));
                                return ISPassCode.NO;
                            } else if (StringUtils.equals("X", ifShowPassCode)) {
                                LOGGER.info("======> 本次订单提交预定失败，原因: {}...",errMsg);
                                return ISPassCode.ERR;
                            } else {
                                // LOGGER.info("======> 本次订单提交不需要验证码...");
                                LOGGER.info("======> 本次订单提交需要等待安全期: {}/ms...", StringUtils.isBlank(ifShowPassCodeTime) ? "empty" : Integer.parseInt(ifShowPassCodeTime));
                                LOGGER.info("======> 订单检查并提交成功...");
                                // 进入安全等待期
                                // Thread.sleep(Long.valueOf(ifShowPassCodeTime));
                                return ISPassCode.NO;
                            }
                        } else {
                            LOGGER.info("======> 本次订单提交预定失败，原因: {}...",errMsg);
                        }
                    } else {
                        LOGGER.info("======> 本次订单提交预定失败，原因: {}...", message);
                    }
                } else {
                    LOGGER.info("======> 接口返回结果为空: 302...");
                }
            }
        }
        return ISPassCode.ERR;
    }

    // 获取当前车票排队人数和余票信息

    /**
     * 检查当前车票排队人数和余票信息
     *
     * @param ticketInfo
     * @return
     */
    public OrderStatus getQueueCount(TicketInfo ticketInfo, SeatType seatType) throws Exception {
        List<NameValuePair> formPail = new ArrayList<>();
        formPail.add(new BasicNameValuePair("train_date", getGMT(ticketInfo.getStartDate())));
        formPail.add(new BasicNameValuePair("train_no", ticketInfo.getTrainNo()));
        formPail.add(new BasicNameValuePair("stationTrainCode", ticketInfo.getTrainCode()));
        formPail.add(new BasicNameValuePair("seatType", seatType.getValue()));
        formPail.add(new BasicNameValuePair("fromStationTelecode", HttpURL12306.STATION_MAP.get(ticketInfo.getFromStationCode())));
        formPail.add(new BasicNameValuePair("toStationTelecode", HttpURL12306.STATION_MAP.get(ticketInfo.getToStationCode())));
        formPail.add(new BasicNameValuePair("leftTicket", TOKEN_MAP.get("leftTicketStr")));
        formPail.add(new BasicNameValuePair("purpose_codes", "00"));
        formPail.add(new BasicNameValuePair("train_location", ticketInfo.getTrainLocation()));
        formPail.add(new BasicNameValuePair("_json_att", ""));
        formPail.add(new BasicNameValuePair("REPEAT_SUBMIT_TOKEN", TOKEN_MAP.get("submitToken")));
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
                        JSONObject data = json.get("data", JSONObject.class);
                        // 余票
                        String ticket = data.get("ticket", String.class);
                        String count = data.get("countT", String.class);
                        if (StringUtils.isNotBlank(count) && StringUtils.equals("0", count)) {
                            count = data.get("count", String.class);
                        }
                        LOGGER.info("======> 排队成功: 你排在第{}位，目前余票还剩余: {}张...", count, ticket);
                        return OrderStatus.SUCCESS;
                    } else {
                        LOGGER.info("======> 查询当前车票排队和余票信息失败...");
                        return OrderStatus.FAIL;
                    }
                } else {
                    LOGGER.info("======> 接口返回结果为空: 302...");
                }
            }
        }
        return OrderStatus.ERROR;
    }

    // 确认订单提交是否成功

    /**
     * 确认订单提交
     *
     * @param trainLocation  列车标识码
     * @param passengerInfos 乘车人
     */
    public OrderStatus confirmSingleForQueue(String trainLocation, List<PassengerInfo> passengerInfos) throws Exception {
        StringBuilder passengerTicketStrBUilder=new StringBuilder();
        StringBuilder oldPassengerStrBUilder=new StringBuilder();
        passengerInfos.forEach(passengerInfo -> {
            // 乘车人车票字符串
            passengerTicketStrBUilder.append(passengerInfo.getSeatType().getValue()+",");
            passengerTicketStrBUilder.append("0"+",");
            passengerTicketStrBUilder.append("1"+",");
            passengerTicketStrBUilder.append(passengerInfo.getPassengerName()+",");
            passengerTicketStrBUilder.append("1"+",");
            passengerTicketStrBUilder.append(passengerInfo.getPassengerIdNo()+",");
            passengerTicketStrBUilder.append(passengerInfo.getMobileNo()+",");
            passengerTicketStrBUilder.append("N"+",");
            passengerTicketStrBUilder.append(passengerInfo.getAllEncStr()+"_");

            // 原有乘车人字符串
            oldPassengerStrBUilder.append(passengerInfo.getPassengerName()+",");
            oldPassengerStrBUilder.append("1"+",");
            oldPassengerStrBUilder.append(passengerInfo.getPassengerIdNo()+",");
            oldPassengerStrBUilder.append("1_");
        });

        String afterPassengerTicketStr = passengerTicketStrBUilder.toString();
        afterPassengerTicketStr = afterPassengerTicketStr.substring(0, afterPassengerTicketStr.lastIndexOf("_"));
        String afterOldPassengerStr = oldPassengerStrBUilder.toString();

        List<NameValuePair> formPail = new ArrayList<>();
        formPail.add(new BasicNameValuePair("passengerTicketStr", afterPassengerTicketStr));
        formPail.add(new BasicNameValuePair("oldPassengerStr", afterOldPassengerStr));
        formPail.add(new BasicNameValuePair("randCode", ""));
        formPail.add(new BasicNameValuePair("purpose_codes", "00"));
        formPail.add(new BasicNameValuePair("key_check_isChange", TOKEN_MAP.get("keyCheckIsChange")));
        formPail.add(new BasicNameValuePair("leftTicketStr", TOKEN_MAP.get("leftTicketStr")));
        formPail.add(new BasicNameValuePair("train_location", trainLocation));
        formPail.add(new BasicNameValuePair("choose_seats", ""));
        formPail.add(new BasicNameValuePair("seatDetailType", "000"));
        formPail.add(new BasicNameValuePair("whatsSelect", "1"));
        formPail.add(new BasicNameValuePair("roomType", "00"));
        formPail.add(new BasicNameValuePair("dwAll", "N"));
        formPail.add(new BasicNameValuePair("_json_att", ""));
        formPail.add(new BasicNameValuePair("REPEAT_SUBMIT_TOKEN", TOKEN_MAP.get("submitToken")));

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
                            return OrderStatus.SUCCESS;
                        } else {
                            String errMsg = data.get("errMsg", String.class);
                            LOGGER.info("======> 确认订单提交失败，原因: {}...", errMsg);
                            return OrderStatus.FAIL;
                        }
                    } else {
                        LOGGER.info("======> 确认订单提交失败，原因: {}...", message);
                        return OrderStatus.FAIL;
                    }
                } else {
                    LOGGER.info("======> 接口返回结果为空: 302...");
                }
            }
        }
        return OrderStatus.ERROR;
    }

    // 进入订单等待

    /**
     * 进入订单等待处理
     *
     * @return 订单号
     */
    public String orderWait() throws Exception {
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
                                replace("{2}", TOKEN_MAP.get("submitToken")), url12306.getCookieStr(null));
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
                                LOGGER.info("======> 目前排队等待人数: {} -> 排队等待时间预计还剩: {}/ms...", waitCount, waitTime);
                                message = data.get("msg", String.class);
                                if (StringUtils.isNotBlank(message)) {
                                    LOGGER.info("======> 订单异常，处理结果: {}...", message);
                                    break;
                                }
                                // 休眠1秒后继续获取订单等待信息，最高请求次数20次，超过20次视为订单失败
                                sleepCount++;
                                Thread.sleep(1000L);
                            } else {
                                LOGGER.info("======> 订单等待失败，处理结果: {}...", json.get("messages", String.class));
                            }
                        } else {
                            LOGGER.info("======> 接口返回结果为空: 302...");
                        }
                    }
                }
            } else {
                // 订单失败
                LOGGER.info("======> 订单等待时间超时，处理失败...");
                break;
            }
        }
        return sequenceNo;
    }


    /**
     * 获取中国标准时间
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
}