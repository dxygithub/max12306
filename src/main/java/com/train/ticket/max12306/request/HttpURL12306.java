package com.train.ticket.max12306.request;

import cn.hutool.core.codec.Base64;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.train.ticket.max12306.common.ConfigFileUtil;
import com.train.ticket.max12306.constant.HttpURLConstant12306;
import com.train.ticket.max12306.entity.*;
import com.train.ticket.max12306.enumeration.HttpHeaderParamter;
import com.train.ticket.max12306.enumeration.UseCdnProxy;
import com.train.ticket.max12306.exception.Max12306Exception;
import com.train.ticket.max12306.requestvo.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.net.ssl.SSLContext;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * @ClassName HttpURL12306
 * @Author duxiaoyu
 * @Date 2020/7/28 18:14
 * @Version 1.0
 */
@Component
public class HttpURL12306 {


    private static final Logger LOGGER = LoggerFactory.getLogger(HttpURL12306.class);

    private static final int SUCCESS = 200;

    private static final String BIGIPSERVERPASSPORT = "820510986.50215.0000";

    public static final String host = "kyfw.12306.cn";

    @Autowired
    private ConfigFileUtil config;

    /**
     * 车站信息Map
     */
    public static final Map<String, String> STATION_MAP = new HashMap<>(16);

    /**
     * 图片验证码Map
     */
    public static final Map<String, String> IMG_CAPTHCHA_MAP = new HashMap<>(16);

    /**
     * 12306 - Cookie缓存
     */
    public static final Map<String, String> COOKIE_CACHE_MAP = new HashMap<>(16);

    /**
     * 可用cdn
     */
    public static List<String> cdnList = new ArrayList<>();

    /**
     * 本地cookie实例
     */
    public static CookieStore cookieStore;

    /**
     * HttpClientContext上下文
     */
    public static HttpClientContext context;

    /**
     * 解析车站信息
     *
     * @return
     * @throws Exception
     */
    public List<StationInfo> parseStationInfo() throws Exception {
        try (CloseableHttpClient httpClient = httpClientBuild()) {
            HttpGet httpGet = httpGetBuild(HttpURLConstant12306.STATION_INFO_URL, getCookieStr(null), UseCdnProxy.YES);
            try (CloseableHttpResponse httpResponse = httpClient.execute(httpGet, context)) {
                HttpEntity httpEntity = httpResponse.getEntity();
                String result = EntityUtils.toString(httpEntity);
                // 释放资源
                EntityUtils.consume(httpEntity);
                List<StationInfo> list = new ArrayList<>();
                if (StringUtils.isNotBlank(result)) {
                    String stationContent = StringUtils.substringBetween(result, "'", "'");
                    String[] stationArray = stationContent.split("@");
                    for (String item : stationArray) {
                        String[] stationDes = item.split("\\|");
                        if (stationDes.length > 1) {
                            StationInfo stationInfo = new StationInfo();
                            stationInfo.setStationName(stationDes[1]);
                            stationInfo.setStationCode(stationDes[2]);
                            stationInfo.setStationSpell(stationDes[3]);
                            stationInfo.setStationLogogram(stationDes[4]);
                            stationInfo.setStationSort(Integer.valueOf(stationDes[5]));
                            STATION_MAP.put(stationInfo.getStationCode(), stationInfo.getStationName());
                            list.add(stationInfo);
                        }
                    }
                    if (!CollectionUtils.isEmpty(list)) {
                        cacheCookie(cookieStore.getCookies());
                        LOGGER.info("======> 解析车站信息成功...");
                        return list;
                    }
                }
                LOGGER.info("======> 解析车站信息失败...");
            }
        }
        return null;
    }


    /**
     * 解析车票信息
     *
     * @param ticketRequest 查询参数
     * @return
     * @throws Exception
     */
    public List<TicketInfo> parseTicketInfo(QueryTicketRequest ticketRequest) throws Exception {
        if (Objects.nonNull(ticketRequest)) {
            try (CloseableHttpClient httpClient = httpClientBuild()) {
                HttpGet httpGet = httpGetBuild(HttpURLConstant12306.TICKET_QUERY_URL.
                        replace("{1}", ticketRequest.getFromDate()).
                        replace("{2}", ticketRequest.getFromStationCode()).
                        replace("{3}", ticketRequest.getToStationCode()).
                        replace("{4}", ticketRequest.getTicketType().getValue()), getCookieStr(null), UseCdnProxy.NO);
                RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(3000)
                        .setConnectionRequestTimeout(1000).setSocketTimeout(3000).build();
                // 设置请求等待超时
                httpGet.setConfig(requestConfig);
                try (CloseableHttpResponse httpResponse = httpClient.execute(httpGet, context)) {
                    HttpEntity httpEntity = httpResponse.getEntity();
                    String result = EntityUtils.toString(httpEntity);
                    // 释放资源
                    EntityUtils.consume(httpEntity);
                    List<TicketInfo> list = new ArrayList<>();
                    if (StringUtils.isNotBlank(result) && httpResponse.getStatusLine().getStatusCode() == SUCCESS) {
                        JSONObject jsonObject = JSONUtil.parseObj(result);
                        int status = (int) jsonObject.get("httpstatus");
                        if (status == SUCCESS) {
                            JSONObject data = (JSONObject) jsonObject.get("data");
                            JSONArray ticketArray = (JSONArray) data.get("result");
                            if (ticketArray.size() == 0) {
                                LOGGER.info("======> {} {} - {} 暂无出发车次",
                                        ticketRequest.getFromDate(),
                                        STATION_MAP.get(ticketRequest.getFromStationCode()),
                                        STATION_MAP.get(ticketRequest.getToStationCode()));
                                return null;
                            }
                            // 车票信息
                            String[] ticketStrArray = ticketArray.toArray(new String[0]);
                            String map = data.get("map").toString();
                            // 设置车票信息
                            list = settingTicketInfo(ticketStrArray);
                            if (!CollectionUtils.isEmpty(list)) {
                                cacheCookie(cookieStore.getCookies());
                                LOGGER.info("======> 解析车票信息成功...");
                                LOGGER.info("======> {} {} - {} 车次总趟数:{}",
                                        ticketRequest.getFromDate(),
                                        STATION_MAP.get(ticketRequest.getFromStationCode()),
                                        STATION_MAP.get(ticketRequest.getToStationCode()), list.size());
                                return list;
                            }
                        }
                    }
                    LOGGER.info("======> 解析车票信息失败，状态：{}...", httpResponse.getStatusLine().getStatusCode());
                }
            }
        }
        return null;
    }


    /**
     * 解析车票价格信息
     *
     * @param ticketPriceRequest
     * @return
     */
    public TicketPrice parseTicketPrice(QueryTicketPriceRequest ticketPriceRequest) throws Exception {
        if (Objects.nonNull(ticketPriceRequest)) {
            try (CloseableHttpClient httpClient = httpClientBuild()) {
                HttpGet httpGet = httpGetBuild(HttpURLConstant12306.TICKET_PRICE_QUERY_URL.
                        replace("{1}", ticketPriceRequest.getTrainNo()).
                        replace("{2}", ticketPriceRequest.getFromStationNo()).
                        replace("{3}", ticketPriceRequest.getToStationNo()).
                        replace("{4}", ticketPriceRequest.getSeatTypes()).
                        replace("{5}", ticketPriceRequest.getTrainDate()), getCookieStr(null), UseCdnProxy.YES);
                try (CloseableHttpResponse httpResponse = httpClient.execute(httpGet, context)) {
                    HttpEntity httpEntity = httpResponse.getEntity();
                    String result = EntityUtils.toString(httpEntity);
                    // 释放资源
                    EntityUtils.consume(httpEntity);
                    TicketPrice ticketPrice = new TicketPrice();
                    ticketPrice.setTrainCode(ticketPriceRequest.getTrainCode());
                    ticketPrice.setTrainNo(ticketPriceRequest.getTrainNo());
                    if (StringUtils.isNotBlank(result)) {
                        JSONObject jsonObject = JSONUtil.parseObj(result);
                        int status = (int) jsonObject.get("httpstatus");
                        if (status == SUCCESS) {
                            JSONObject data = (JSONObject) jsonObject.get("data");
                            // 设置车票价格信息
                            settingTicketPrice(data, ticketPriceRequest, ticketPrice);
                            cacheCookie(cookieStore.getCookies());
                            LOGGER.info("======> 解析车票价格成功...");
                            return ticketPrice;
                        }
                    }
                    LOGGER.info("======> 解析车票价格失败...");
                }
            }
        }
        return null;
    }


    /**
     * 获取登录图片验证码
     *
     * @return
     */
    public String getImgCaptcha() throws Exception {
        try (CloseableHttpClient httpClient = httpClientBuild()) {
            String currentMills = String.valueOf(System.currentTimeMillis());
            HttpGet httpGet = httpGetBuild(HttpURLConstant12306.GET_CAPTCHA.
                    replace("{1}", currentMills).
                    replace("{2}", currentMills).
                    replace("{3}", currentMills), getCookieStr(null), UseCdnProxy.YES);
            try (CloseableHttpResponse httpResponse = httpClient.execute(httpGet, context)) {
                HttpEntity entity = httpResponse.getEntity();
                String result = EntityUtils.toString(entity);
                // 释放资源
                EntityUtils.consume(entity);
                if (StringUtils.isNotBlank(result)) {
                    // 验证码json
                    String jsonResult = StringUtils.substringBetween(result, "(", ")");
                    // 验证码回调函数
                    String jqueryCallBack = StringUtils.substringBetween(result, "/**/", "(");
                    JSONObject json = JSONUtil.parseObj(jsonResult);
                    if (json.get("result_code", String.class).equals("0")) {
                        String img = json.get("image", String.class);
                        IMG_CAPTHCHA_MAP.put(currentMills, img);
                        cacheCookie(cookieStore.getCookies());
                        LOGGER.info("======> 图片验证码获取成功...");
                        return jqueryCallBack + "--" + img;
                    } else {
                        LOGGER.info("======> 图片验证码获取失败...");
                    }
                }
            }
        }
        return null;
    }


    /**
     * 图片验证码校验
     *
     * @param answer
     * @return
     */
    public String checkImgCapthcha(String answer, String timer) throws Exception {
        try (CloseableHttpClient httpClient = httpClientBuild()) {
            HttpGet httpGet = httpGetBuild(HttpURLConstant12306.CHECK_CAPTCHA.
                    replace("{xyz}", answer).
                    replace("{1}", timer).
                    replace("{2}", timer), getCookieStr(null), UseCdnProxy.YES);
            try (CloseableHttpResponse httpResponse = httpClient.execute(httpGet, context)) {
                HttpEntity entity = httpResponse.getEntity();
                String result = EntityUtils.toString(entity);
                // 释放资源
                EntityUtils.consume(entity);
                if (StringUtils.isNotBlank(result)) {
                    String jsonResult = StringUtils.substringBetween(result, "(", ")");
                    JSONObject json = JSONUtil.parseObj(jsonResult);
                    if (json.get("result_code", String.class).equals("4")) {
                        String resultCode = "4";
                        cacheCookie(cookieStore.getCookies());
                        LOGGER.info("======> 图片验证码校验成功...");
                        return resultCode;
                    } else {
                        LOGGER.info("======> 图片验证码校验失败...");
                    }
                }
            }
        }
        return "5";
    }

    /**
     * 初始化滑块验证
     *
     * @param passPort
     * @return
     * @throws Exception
     */
    public String initSlidePassPort(InitSlidePassPort passPort) throws Exception {
        try (CloseableHttpClient client = httpClientBuild()) {
            List<NameValuePair> formPail = new ArrayList<>();
            formPail.add(new BasicNameValuePair("appid", passPort.getAppid()));
            formPail.add(new BasicNameValuePair("username", passPort.getUsername()));
            formPail.add(new BasicNameValuePair("slideMode", passPort.getSlideMode()));
            HttpPost post = httpPostBuild(HttpURLConstant12306.INIT_SLIDE_PASSPORT_URL, formPail, getCookieStr(null));
            try (CloseableHttpResponse response = client.execute(post, context)) {
                HttpEntity entity = response.getEntity();
                String result = EntityUtils.toString(entity);
                // 释放资源
                EntityUtils.consume(entity);
                if (StringUtils.isNotBlank(result)) {
                    JSONObject json = JSONUtil.parseObj(result);
                    if (json.get("result_code", String.class).equals("0")) {
                        String ifCheckSlidePasscodeToken = json.get("if_check_slide_passcode_token", String.class);
                        cacheCookie(cookieStore.getCookies());
                        return ifCheckSlidePasscodeToken + "--" + json.get("result_code", String.class);
                    } else {
                        return json.get("result_code", String.class);
                    }
                }
            }
        }
        return "5";
    }

    /**
     * 请求登录
     *
     * @param loginRequest
     * @return
     */
    public String loginRequest(UserLoginRequest loginRequest) throws Exception {
        try (CloseableHttpClient client = httpClientBuild()) {
            /*HttpGet loginGet = httpGetBuild(HttpURLConstant12306.LOGIN_INIT, getCookieStr(null));
            try (CloseableHttpResponse loginResponse = client.execute(loginGet, context)) {
                // 此处只为获取初始化登录前的cookie
                cacheCookie(cookieStore.getCookies());
            }
            loginGet = httpGetBuild(HttpURLConstant12306.LOGIN_BANNER, getCookieStr(null));
            try (CloseableHttpResponse loginResponse = client.execute(loginGet, context)) {
                // 此处只为获取初始化登录前的cookie
                cacheCookie(cookieStore.getCookies());
            }*/

            List<NameValuePair> formPail = new ArrayList<>();
            formPail.add(new BasicNameValuePair("sessionId", loginRequest.getSessionId()));
            formPail.add(new BasicNameValuePair("sig", loginRequest.getSig()));
            formPail.add(new BasicNameValuePair("if_check_slide_passcode_token", loginRequest.getIfCheckSlidePasscodeToken()));
            formPail.add(new BasicNameValuePair("scene", loginRequest.getScene()));
            formPail.add(new BasicNameValuePair("tk", loginRequest.getTk()));
            formPail.add(new BasicNameValuePair("username", loginRequest.getUsername()));
            formPail.add(new BasicNameValuePair("password", loginRequest.getPassword()));
            formPail.add(new BasicNameValuePair("appid", loginRequest.getAppid()));
            HttpPost post = httpPostBuild(HttpURLConstant12306.LOGIN_URL, formPail, getCookieStr(null));
            post.addHeader("Referer", "https://kyfw.12306.cn/otn/resources/login.html");
            post.addHeader("Origin", "https://kyfw.12306.cn");
            try (CloseableHttpResponse response = client.execute(post, context)) {
                HttpEntity entity = response.getEntity();
                String result = EntityUtils.toString(entity);
                // 释放资源
                EntityUtils.consume(entity);
                if (StringUtils.isNotBlank(result)) {
                    JSONObject json = JSONUtil.parseObj(result);
                    if ("0".equals(json.get("result_code", String.class))) {
                        String uamtk = json.get("uamtk", String.class);
                        cacheCookie(cookieStore.getCookies());
                        LOGGER.info("======> username: {}，uamtk: {} -> 登录成功...", loginRequest.getUsername(), uamtk);
                        return uamtk;
                    } else {
                        LOGGER.info("======> username: {} -> 登录失败，原因: {}...", loginRequest.getUsername(), json.get("result_message", String.class));
                        return "5";
                    }
                }
            }
        }
        return "5";
    }

    /**
     * 登录成功认证回调
     *
     * @param appId
     * @return
     */
    public String loginSuccessPassportUamtk(String appId, String uamtk) throws Exception {
        try (CloseableHttpClient client = httpClientBuild()) {
            /*HttpGet loginGet = httpGetBuild(HttpURLConstant12306.LOGIN_INIT_CDN1, getCookieStr(null));
            try (CloseableHttpResponse loginResponse = client.execute(loginGet, context)) {
                // 此处只为获取认证前的cookie
                cacheCookie(cookieStore.getCookies());
            }*/
            List<NameValuePair> formPail = new ArrayList<>();
            formPail.add(new BasicNameValuePair("appid", appId));
            HttpPost post = httpPostBuild(HttpURLConstant12306.PASSPORT_UAMTK_STATIC_URL, formPail, getCookieStr(new String[]{"_passport_ct", "JSESSIONID"}));
            // 认证必须携带以下请求头内容，否则返回302，内容为空
            post.addHeader("Referer", "https://kyfw.12306.cn/otn/passport?redirect=/otn/login/userLogin");
            post.addHeader("Origin", "https://kyfw.12306.cn");
            try (CloseableHttpResponse response = client.execute(post, context)) {
                HttpEntity entity = response.getEntity();
                String result = EntityUtils.toString(entity);
                // 释放资源
                EntityUtils.consume(entity);
                if (StringUtils.isNotBlank(result)) {
                    JSONObject json = JSONUtil.parseObj(result);
                    if ("0".equals(json.get("result_code", String.class))) {
                        String apptk = json.get("newapptk", String.class);
                        if (StringUtils.isBlank(apptk)) {
                            apptk = json.get("apptk", String.class);
                        }
                        cacheCookie(cookieStore.getCookies());
                        LOGGER.info("======> uamtk: {}，apptk: {} -> 认证成功...", uamtk, apptk);
                        return apptk;
                    } else {
                        LOGGER.info("======> 认证失败，原因: {}...", json.get("result_message", String.class));
                    }
                }
            }
        }
        return "";
    }

    /**
     * 获取用户名
     *
     * @param tk 会有过期时间，过期后需要重新登录认证获取
     * @return
     * @throws Exception
     */
    public String getUserName(String tk) throws Exception {
        try (CloseableHttpClient client = httpClientBuild()) {
            List<NameValuePair> formPail = new ArrayList<>();
            formPail.add(new BasicNameValuePair("tk", tk));
            HttpPost post = httpPostBuild(HttpURLConstant12306.API_AUTH_UAMAUTHCLIENT, formPail, getCookieStr(null));
            post.addHeader("Referer", "https://kyfw.12306.cn/otn/passport?redirect=/otn/login/userLogin");
            post.addHeader("Origin", "https://kyfw.12306.cn");
            try (CloseableHttpResponse response = client.execute(post, context)) {
                HttpEntity entity = response.getEntity();
                String result = EntityUtils.toString(entity);
                // 释放资源
                EntityUtils.consume(entity);
                if (StringUtils.isNotBlank(result)) {
                    JSONObject json = JSONUtil.parseObj(result);
                    if ("0".equals(json.get("result_code", String.class))) {
                        String userName = json.get("username", String.class);
                        cacheCookie(cookieStore.getCookies());
                        LOGGER.info("======> 获取用户名成功 -> {}", userName);
                        LOGGER.info("======> Cookies: {}", getCookieStr(null));
                        return userName;
                    } else {
                        LOGGER.info("======> 获取用户名失败，原因: {}...", json.get("result_message", String.class));
                        return "5";
                    }
                } else {
                    LOGGER.info("======> 获取用户名失败...");
                    return "5";
                }
            }
        }
    }

    /**
     * 检查用户是否已登录
     *
     * @return Y:已登录 N: 未登录
     */
    public String checkUser() throws Exception {
        try (CloseableHttpClient client = httpClientBuild()) {
            List<NameValuePair> formPail = new ArrayList<>();
            formPail.add(new BasicNameValuePair("_json_att", ""));
            HttpPost post = httpPostBuild(HttpURLConstant12306.CHECK_USER, formPail, getCookieStr(null));
            try (CloseableHttpResponse response = client.execute(post, context)) {
                HttpEntity entity = response.getEntity();
                String result = EntityUtils.toString(entity);
                // 释放资源
                EntityUtils.consume(entity);
                if (StringUtils.isNotBlank(result)) {
                    JSONObject json = JSONUtil.parseObj(result);
                    if (SUCCESS == json.get("httpstatus", Integer.class) && json.get("status", Boolean.class)) {
                        JSONObject data = json.get("data", JSONObject.class);
                        if (data.get("flag", Boolean.class)) {
                            return "Y";
                        }
                        return "N";
                    }
                } else {
                    LOGGER.info("======> 接口返回结果为空: 302...");
                }
            }
        }
        return "N";
    }


    /**
     * 获取乘车人信息
     */
    public List<PassengerInfo> getPassengersInfo() throws Exception {
        try (CloseableHttpClient client = httpClientBuild()) {
            List<NameValuePair> formPail = new ArrayList<>();
            formPail.add(new BasicNameValuePair("pageIndex", "1"));
            formPail.add(new BasicNameValuePair("pageSize", "10"));
            HttpPost post = httpPostBuild(HttpURLConstant12306.PASSENGERS_QUERY, formPail, getCookieStr(null));
            try (CloseableHttpResponse response = client.execute(post, context)) {
                HttpEntity entity = response.getEntity();
                String result = EntityUtils.toString(entity);
                // 释放资源
                EntityUtils.consume(entity);
                if (StringUtils.isNotBlank(result)) {
                    JSONObject json = JSONUtil.parseObj(result);
                    if (SUCCESS == json.get("httpstatus", Integer.class)) {
                        LOGGER.info("======> 获取乘车人成功...");
                        return settingPassengerInfo(json);
                    } else {
                        LOGGER.info("======> 获取乘车人失败...");
                    }
                } else {
                    LOGGER.info("======> 获取乘车人失败...");
                }
            }
        }
        return Collections.emptyList();
    }

    /**
     * 获取订单信息: 未出行订单query_where(G)、历史订单query_where(H)
     *
     * @return
     * @throws Exception
     */
    public List<MyOrder> getOrderInfo(String queryStartDate, String queryEndDate, String queryWhere) throws Exception {
        try (CloseableHttpClient client = httpClientBuild()) {
            List<NameValuePair> formPail = new ArrayList<>();
            formPail.add(new BasicNameValuePair("pageIndex", "0"));
            formPail.add(new BasicNameValuePair("pageSize", "10"));
            formPail.add(new BasicNameValuePair("come_from_flag", "my_order"));
            formPail.add(new BasicNameValuePair("query_where", queryWhere));
            formPail.add(new BasicNameValuePair("queryStartDate", queryStartDate));
            formPail.add(new BasicNameValuePair("queryEndDate", queryEndDate));
            formPail.add(new BasicNameValuePair("sequeue_train_name", ""));
            formPail.add(new BasicNameValuePair("queryType", "1"));
            HttpPost post = httpPostBuild(HttpURLConstant12306.QUERY_MY_ORDER, formPail, getCookieStr(null));
            try (CloseableHttpResponse response = client.execute(post, context)) {
                HttpEntity entity = response.getEntity();
                String result = EntityUtils.toString(entity);
                // 释放资源
                EntityUtils.consume(entity);
                if (StringUtils.isNotBlank(result)) {
                    JSONObject json = JSONUtil.parseObj(result);
                    if (SUCCESS == json.get("httpstatus", Integer.class)) {
                        LOGGER.info("======> 获取订单信息成功...");
                        return settingOrderInfo(json.get("data", JSONObject.class));
                    } else {
                        LOGGER.info("======> 获取订单信息失败...");
                    }
                } else {
                    LOGGER.info("======> 获取订单信息失败...");
                }
            }
        }
        return Collections.emptyList();
    }

    /**
     * 获取未完成订单: 待支付
     *
     * @return
     * @throws Exception
     */
    public List<MyOrder> getOrderNoComplete() throws Exception {
        try (CloseableHttpClient client = httpClientBuild()) {
            List<NameValuePair> formPail = new ArrayList<>();
            formPail.add(new BasicNameValuePair("_json_att", ""));
            HttpPost post = httpPostBuild(HttpURLConstant12306.QUERY_MY_ORDER_NO_COMPLETE, formPail, getCookieStr(null));
            try (CloseableHttpResponse response = client.execute(post, context)) {
                HttpEntity entity = response.getEntity();
                String result = EntityUtils.toString(entity);
                // 释放资源
                EntityUtils.consume(entity);
                if (StringUtils.isNotBlank(result)) {
                    JSONObject json = JSONUtil.parseObj(result);
                    if (SUCCESS == json.get("httpstatus", Integer.class)) {
                        LOGGER.info("======> 获取未完成订单信息成功...");
                        return settingNoCompleteOrder(json.get("data", JSONObject.class));
                    } else {
                        LOGGER.info("======> 获取未完成订单信息失败...");
                    }
                } else {
                    LOGGER.info("======> 获取未完成订单信息失败...");
                }
            }
        }
        return Collections.emptyList();
    }


    /**
     * 删除乘车人信息
     *
     * @param passengersVo
     * @return
     */
    public String delPassenger(PassengersVo passengersVo) throws Exception {
        try (CloseableHttpClient client = httpClientBuild()) {
            List<NameValuePair> formPail = new ArrayList<>();
            formPail.add(new BasicNameValuePair("passenger_name", passengersVo.getPassengerName()));
            formPail.add(new BasicNameValuePair("passenger_id_type_code", passengersVo.getPassengerIdTypeCode()));
            formPail.add(new BasicNameValuePair("passenger_id_no", passengersVo.getPassengerIdNo()));
            formPail.add(new BasicNameValuePair("isUserSelf", passengersVo.getIsUserSelf()));
            formPail.add(new BasicNameValuePair("allEncStr", passengersVo.getAllEncStr()));
            HttpPost post = httpPostBuild(HttpURLConstant12306.DEL_PASSENGERS, formPail, getCookieStr(null));
            try (CloseableHttpResponse response = client.execute(post, context)) {
                HttpEntity entity = response.getEntity();
                String result = EntityUtils.toString(entity);
                // 释放资源
                EntityUtils.consume(entity);
                if (StringUtils.isNotBlank(result)) {
                    JSONObject json = JSONUtil.parseObj(result);
                    if (SUCCESS == json.get("httpstatus", Integer.class)) {
                        String message = json.get("data", JSONObject.class).get("message", String.class);
                        boolean flag = json.get("data", JSONObject.class).get("flag", boolean.class);
                        LOGGER.info("======> 删除乘车人: {}，结果: {}...", passengersVo.getPassengerName(), flag);
                        if (flag) {
                            return "删除成功-0";
                        } else {
                            return message + "-1";
                        }
                    }
                }
            }
        }
        return "删除失败";
    }

    /**
     * 新增乘车人
     *
     * @param passengersVo
     * @return
     * @throws Exception
     */
    public String addPassenger(PassengersVo passengersVo) throws Exception {
        try (CloseableHttpClient client = httpClientBuild()) {
            List<NameValuePair> formPail = new ArrayList<>();
            formPail.add(new BasicNameValuePair("passenger_name", passengersVo.getPassengerName()));
            formPail.add(new BasicNameValuePair("sex_code", passengersVo.getSexCode()));
            formPail.add(new BasicNameValuePair("passenger_id_no", passengersVo.getPassengerIdNo()));
            formPail.add(new BasicNameValuePair("mobile_no", passengersVo.getMobileNo()));
            formPail.add(new BasicNameValuePair("country_code", "CN"));
            formPail.add(new BasicNameValuePair("passenger_id_type_code", "1"));
            formPail.add(new BasicNameValuePair("passenger_type", "1"));
            HttpPost post = httpPostBuild(HttpURLConstant12306.ADD_PASSENGERS, formPail, getCookieStr(null));
            try (CloseableHttpResponse response = client.execute(post, context)) {
                HttpEntity entity = response.getEntity();
                String result = EntityUtils.toString(entity);
                // 释放资源
                EntityUtils.consume(entity);
                if (StringUtils.isNotBlank(result)) {
                    JSONObject json = JSONUtil.parseObj(result);
                    if (SUCCESS == json.get("httpstatus", Integer.class)) {
                        String message = json.get("data", JSONObject.class).get("message", String.class);
                        boolean flag = json.get("data", JSONObject.class).get("flag", boolean.class);
                        LOGGER.info("======> 新增乘车人: {}，结果: {}...", passengersVo.getPassengerName(), flag);
                        if (flag) {
                            return "新增成功-0";
                        } else {
                            return message + "-1";
                        }
                    }
                }
            }
        }
        return "新增失败";
    }

    /**
     * 获取浏览器标识请求参数
     */
    public static void getDeviceIdParams() throws Exception {
        String deviceIDUrl = getDeviceIdRequestUrl();
        String params = deviceIDUrl.substring(deviceIDUrl.indexOf("?") + 1);
        String[] paramsArr = params.split("&");
        Map<String, String> paramsMap = new HashMap<>(16);
        for (String item : paramsArr) {
            String[] param = item.split("=");
            if (param != null) {
                paramsMap.put(param[0], param[1]);
            }
        }
        StringJoiner paramsSJ = new StringJoiner("&");
        for (String key : paramsMap.keySet()) {
            paramsSJ.add(key + "=" + URLEncoder.encode(paramsMap.get(key), "UTF-8"));
        }

        String newUrl = paramsSJ.toString();
        HttpGet httpGet = new HttpGet(deviceIDUrl.substring(0, deviceIDUrl.indexOf("?") + 1) + newUrl);
        httpGet.addHeader(HttpHeaderParamter.ACCEPT.getKey(), HttpHeaderParamter.ACCEPT.getValue());
        httpGet.addHeader(HttpHeaderParamter.ACCEPT_ENCODING.getKey(), HttpHeaderParamter.ACCEPT_ENCODING.getValue());
        httpGet.addHeader(HttpHeaderParamter.ACCEPT_LANGUAGE.getKey(), HttpHeaderParamter.ACCEPT_LANGUAGE.getValue());
        httpGet.addHeader(HttpHeaderParamter.USER_AGENT.getKey(), HttpHeaderParamter.USER_AGENT.getValue());
        httpGet.addHeader(HttpHeaderParamter.X_REQUESTED_WITH.getKey(), HttpHeaderParamter.X_REQUESTED_WITH.getValue());
        try (CloseableHttpClient client = httpClientBuild()) {
            try (CloseableHttpResponse response = client.execute(httpGet)) {
                HttpEntity entity = response.getEntity();
                String result = EntityUtils.toString(entity);
                // 释放资源
                EntityUtils.consume(entity);
                if (StringUtils.isNotBlank(result)) {
                    String jsonResult = StringUtils.substringBetween(result, "('", "')");
                    JSONObject json = JSONUtil.parseObj(jsonResult);
                    if (Objects.nonNull(json)) {
                        String RAIL_EXPIRATION = json.get("exp", String.class);
                        String RAIL_DEVICEID = json.get("dfp", String.class);
                        COOKIE_CACHE_MAP.put("RAIL_EXPIRATION", RAIL_EXPIRATION);
                        COOKIE_CACHE_MAP.put("RAIL_DEVICEID", RAIL_DEVICEID);
                        LOGGER.info("======> 获取浏览器标识请求参数成功...");
                        return;
                    }
                }
            }
        }
        LOGGER.info("======> 获取浏览器标识请求参数失败...");
        throw new Max12306Exception("获取浏览器标识请求参数失败...");
    }

    /**
     * 获取浏览器标识请求参数url完整地址
     *
     * @return
     */
    public static String getDeviceIdRequestUrl() throws Exception {
        HttpGet httpGet = new HttpGet(HttpURLConstant12306.API_GET_BROWSER_DEVICE_ID);
        httpGet.addHeader(HttpHeaderParamter.ACCEPT.getKey(), HttpHeaderParamter.ACCEPT.getValue());
        httpGet.addHeader(HttpHeaderParamter.ACCEPT_ENCODING.getKey(), HttpHeaderParamter.ACCEPT_ENCODING.getValue());
        httpGet.addHeader(HttpHeaderParamter.ACCEPT_LANGUAGE.getKey(), HttpHeaderParamter.ACCEPT_LANGUAGE.getValue());
        httpGet.addHeader(HttpHeaderParamter.USER_AGENT.getKey(), HttpHeaderParamter.USER_AGENT.getValue());
        httpGet.addHeader(HttpHeaderParamter.X_REQUESTED_WITH.getKey(), HttpHeaderParamter.X_REQUESTED_WITH.getValue());
        try (CloseableHttpClient client = httpClientBuild()) {
            try (CloseableHttpResponse response = client.execute(httpGet)) {
                HttpEntity entity = response.getEntity();
                String result = EntityUtils.toString(entity);
                // 释放资源
                EntityUtils.consume(entity);
                if (StringUtils.isNotBlank(result)) {
                    JSONObject json = JSONUtil.parseObj(result);
                    String deviceIDUrl = Base64.decodeStr(json.get("id", String.class), "UTF-8");
                    if (StringUtils.isNotBlank(deviceIDUrl)) {
                        return deviceIDUrl;
                    }
                }
            }
        }
        return null;
    }

    /**
     * 获取登录时初始化校验的appkey
     *
     * @return
     */
    public static String getLoginInitAppKey() throws Exception {
        HttpGet httpGet = new HttpGet(HttpURLConstant12306.LOGIN_INIT_CDN1);
        httpGet.addHeader(HttpHeaderParamter.ACCEPT.getKey(), HttpHeaderParamter.ACCEPT.getValue());
        httpGet.addHeader(HttpHeaderParamter.ACCEPT_ENCODING.getKey(), HttpHeaderParamter.ACCEPT_ENCODING.getValue());
        httpGet.addHeader(HttpHeaderParamter.ACCEPT_LANGUAGE.getKey(), HttpHeaderParamter.ACCEPT_LANGUAGE.getValue());
        httpGet.addHeader(HttpHeaderParamter.USER_AGENT.getKey(), HttpHeaderParamter.USER_AGENT.getValue());
        httpGet.addHeader(HttpHeaderParamter.X_REQUESTED_WITH.getKey(), HttpHeaderParamter.X_REQUESTED_WITH.getValue());
        try (CloseableHttpClient client = httpClientBuild()) {
            try (CloseableHttpResponse response = client.execute(httpGet)) {
                HttpEntity entity = response.getEntity();
                String result = EntityUtils.toString(entity);
                // 释放资源
                EntityUtils.consume(entity);
                if (StringUtils.isNotBlank(result)) {
                    return result;
                }
            }
        }
        return null;
    }

    /**
     * 获取最新的登录脚本
     *
     * @return
     */
    public static String getLoginNewScript(String url) throws Exception {
        HttpGet httpGet = new HttpGet(url);
        httpGet.addHeader(HttpHeaderParamter.ACCEPT.getKey(), HttpHeaderParamter.ACCEPT.getValue());
        httpGet.addHeader(HttpHeaderParamter.ACCEPT_ENCODING.getKey(), HttpHeaderParamter.ACCEPT_ENCODING.getValue());
        httpGet.addHeader(HttpHeaderParamter.ACCEPT_LANGUAGE.getKey(), HttpHeaderParamter.ACCEPT_LANGUAGE.getValue());
        httpGet.addHeader(HttpHeaderParamter.USER_AGENT.getKey(), HttpHeaderParamter.USER_AGENT.getValue());
        httpGet.addHeader(HttpHeaderParamter.X_REQUESTED_WITH.getKey(), HttpHeaderParamter.X_REQUESTED_WITH.getValue());
        try (CloseableHttpClient client = httpClientBuild()) {
            try (CloseableHttpResponse response = client.execute(httpGet)) {
                HttpEntity entity = response.getEntity();
                String result = EntityUtils.toString(entity);
                // 释放资源
                EntityUtils.consume(entity);
                if (StringUtils.isNotBlank(result)) {
                    return result;
                }
            }
        }
        return null;
    }

    /**
     * 校验登录滑块验证是否开启
     *
     * @param formPail
     * @return
     */
    public static String slidePassportValidate(List<NameValuePair> formPail) throws Exception {
        HttpPost httpPost = new HttpPost(HttpURLConstant12306.SLIDE_PASSPORT_URL);
        httpPost.addHeader(HttpHeaderParamter.ACCEPT.getKey(), HttpHeaderParamter.ACCEPT.getValue());
        httpPost.addHeader(HttpHeaderParamter.ACCEPT_ENCODING.getKey(), HttpHeaderParamter.ACCEPT_ENCODING.getValue());
        httpPost.addHeader(HttpHeaderParamter.ACCEPT_LANGUAGE.getKey(), HttpHeaderParamter.ACCEPT_LANGUAGE.getValue());
        httpPost.addHeader(HttpHeaderParamter.USER_AGENT.getKey(), HttpHeaderParamter.USER_AGENT.getValue());
        httpPost.addHeader(HttpHeaderParamter.X_REQUESTED_WITH.getKey(), HttpHeaderParamter.X_REQUESTED_WITH.getValue());
        UrlEncodedFormEntity formEntity = new UrlEncodedFormEntity(formPail, "UTF-8");
        httpPost.setEntity(formEntity);
        try (CloseableHttpClient client = httpClientBuild()) {
            try (CloseableHttpResponse response = client.execute(httpPost)) {
                HttpEntity entity = response.getEntity();
                String result = EntityUtils.toString(entity);
                // 释放资源
                EntityUtils.consume(entity);
                if (StringUtils.isNotBlank(result)) {
                    return result;
                }
            }
        }
        return null;
    }

    /**
     * 用户退出登录
     *
     * @throws Exception
     */
    public void loginOut() throws Exception {
        try (CloseableHttpClient client = httpClientBuild()) {
            HttpGet get = httpGetBuild(HttpURLConstant12306.LOGIN_OUT, getCookieStr(null), UseCdnProxy.YES);
            // 执行退出，正常情况12306会重定向到登录页面
            client.execute(get);
            LOGGER.info("======> 退出成功...");
        }
    }


    /**
     * 设置车票信息
     *
     * @param ticketStrArray
     * @return
     */
    private static List<TicketInfo> settingTicketInfo(String[] ticketStrArray) {
        List<TicketInfo> list = new ArrayList<>();
        for (String ticket : ticketStrArray) {
            String[] ticketDes = ticket.split("\\|");
            TicketInfo ticketInfo = new TicketInfo();
            ticketInfo.setTicketSecretKey(ticketDes[0]);
            ticketInfo.setRemark(ticketDes[1]);
            ticketInfo.setTrainNo(ticketDes[2]);
            ticketInfo.setTrainCode(ticketDes[3]);
            ticketInfo.setStartStationCode(ticketDes[4]);
            ticketInfo.setEndStationCode(ticketDes[5]);
            ticketInfo.setFromStationCode(ticketDes[6]);
            ticketInfo.setFromStationName(STATION_MAP.get(ticketDes[6]));
            ticketInfo.setToStationCode(ticketDes[7]);
            ticketInfo.setToStationName(STATION_MAP.get(ticketDes[7]));
            ticketInfo.setFromTime(ticketDes[8]);
            ticketInfo.setToTime(ticketDes[9]);
            ticketInfo.setLastTime(ticketDes[10]);
            ticketInfo.setCanBuy(ticketDes[11]);
            ticketInfo.setStartDate(ticketDes[13]);
            ticketInfo.setTrainLocation(ticketDes[15]);
            ticketInfo.setFromStationNo(ticketDes[16]);
            ticketInfo.setToStationNo(ticketDes[17]);
            ticketInfo.setIsSupportCard(ticketDes[18]);
            ticketInfo.setHighSoftSleepCount(ticketDes[21]);
            ticketInfo.setOther(ticketDes[22]);
            ticketInfo.setSoftSleepCount(ticketDes[23]);
            ticketInfo.setSoftSeatCount(ticketDes[24]);
            ticketInfo.setSpecialSeatCount(ticketDes[25]);
            ticketInfo.setNoneSeatCount(ticketDes[26]);
            ticketInfo.setYbCount(ticketDes[27]);
            ticketInfo.setHardSleepCount(ticketDes[28]);
            ticketInfo.setHardSeatCount(ticketDes[29]);
            ticketInfo.setSecondSeatCount(ticketDes[30]);
            ticketInfo.setFirstSeatCount(ticketDes[31]);
            ticketInfo.setBusinessSeatCount(ticketDes[32]);
            ticketInfo.setMotorSleepCount(ticketDes[33]);
            ticketInfo.setSeatType(ticketDes[35]);
            ticketInfo.setCanBackup(ticketDes[37]);
            list.add(ticketInfo);
        }
        return list;
    }

    /**
     * 设置乘车人信息
     *
     * @return
     */
    public static List<PassengerInfo> settingPassengerInfo(JSONObject json) {
        List<JSONObject> array = JSONUtil.toList(json.get("data", JSONObject.class).getJSONArray("datas"), JSONObject.class);
        List<PassengerInfo> passengerInfos = new ArrayList<>();
        array.forEach(x -> {
            PassengerInfo passengerInfo = new PassengerInfo();
            passengerInfo.setAddress(x.get("address", String.class));
            passengerInfo.setAllEncStr(x.get("allEncStr", String.class));
            passengerInfo.setBornDate(x.get("born_date", String.class));
            passengerInfo.setFirstLetter(x.get("first_letter", String.class));
            passengerInfo.setMobileNo(x.get("mobile_no", String.class));
            passengerInfo.setPassengerIdNo(x.get("passenger_id_no", String.class));
            passengerInfo.setPassengerIdTypeCode(x.get("passenger_id_type_code", String.class));
            passengerInfo.setPassengeridTypeName(x.get("passenger_id_type_name", String.class));
            passengerInfo.setPassengerName(x.get("passenger_name", String.class));
            passengerInfo.setPassengerType(x.get("passenger_type", String.class));
            passengerInfo.setPassengerTypeName(x.get("passenger_type_name", String.class));
            passengerInfo.setPassengerUUid(x.get("passenger_uuid", String.class));
            passengerInfo.setSexCode(x.get("sex_code", String.class));
            passengerInfo.setSexName(x.get("sex_name", String.class));
            passengerInfo.setIsUserSelf(x.get("isUserSelf", String.class));
            passengerInfos.add(passengerInfo);
        });
        return passengerInfos;
    }

    /**
     * 设置订单信息
     *
     * @param json
     * @return
     */
    public static List<MyOrder> settingOrderInfo(JSONObject json) {
        List<MyOrder> orderList = new ArrayList<>();
        if (Objects.nonNull(json)) {
            List<JSONObject> array = JSONUtil.toList(json.getJSONArray("OrderDTODataList"), JSONObject.class);
            array.forEach(x -> {
                List<JSONObject> tickets = JSONUtil.toList(x.getJSONArray("tickets"), JSONObject.class);
                tickets.forEach(t -> {
                    MyOrder order = new MyOrder();
                    JSONObject passenger = t.get("passengerDTO", JSONObject.class);
                    JSONObject stationTrain = t.get("stationTrainDTO", JSONObject.class);
                    // 开始设置订单信息
                    order.setArrayPassserNamePage(passenger.get("passenger_name", String.class));
                    order.setFromStationNamePage(stationTrain.get("from_station_name", String.class));
                    order.setToStationNamePage(stationTrain.get("to_station_name", String.class));
                    order.setOrderDate(x.get("order_date", String.class));
                    order.setSequenceNo(x.get("sequence_no", String.class));
                    order.setStartTrainDatePage(x.get("start_train_date_page", String.class));
                    order.setTrainCodePage(x.get("train_code_page", String.class));
                    order.setTicketTotalPricePage(x.get("ticket_total_price_page", String.class));
                    order.setTicketTotalnum(x.get("ticket_totalnum", String.class));
                    order.setCoachNo(t.get("coach_no", String.class));
                    order.setSeatName(t.get("seat_name", String.class));
                    order.setSeatTypeName(t.get("seat_type_name", String.class));
                    order.setStrTicketPricePage(t.get("str_ticket_price_page", String.class));
                    order.setTicketStatusName(t.get("ticket_status_name", String.class));
                    order.setTicketTypeName(t.get("ticket_type_name", String.class));
                    orderList.add(order);
                });
            });
        }
        return orderList;
    }

    /**
     * 设置未完成的订单信息
     *
     * @param json
     * @return
     */
    public static List<MyOrder> settingNoCompleteOrder(JSONObject json) {
        List<MyOrder> orderList = new ArrayList<>();
        if (Objects.nonNull(json)) {
            List<JSONObject> array = JSONUtil.toList(json.getJSONArray("orderDBList"), JSONObject.class);
            array.forEach(x -> {
                List<JSONObject> tickets = JSONUtil.toList(x.getJSONArray("tickets"), JSONObject.class);
                tickets.forEach(t -> {
                    MyOrder order = new MyOrder();
                    JSONObject passenger = t.get("passengerDTO", JSONObject.class);
                    JSONObject stationTrain = t.get("stationTrainDTO", JSONObject.class);
                    // 开始设置订单信息
                    order.setArrayPassserNamePage(passenger.get("passenger_name", String.class));
                    order.setFromStationNamePage(stationTrain.get("from_station_name", String.class));
                    order.setToStationNamePage(stationTrain.get("to_station_name", String.class));
                    order.setOrderDate(x.get("order_date", String.class));
                    order.setSequenceNo(x.get("sequence_no", String.class));
                    order.setStartTrainDatePage(x.get("start_train_date_page", String.class));
                    order.setTrainCodePage(x.get("train_code_page", String.class));
                    order.setTicketTotalPricePage(x.get("ticket_total_price_page", String.class));
                    order.setTicketTotalnum(x.get("ticket_totalnum", String.class));
                    order.setCoachNo(t.get("coach_no", String.class));
                    order.setSeatName(t.get("seat_name", String.class));
                    order.setSeatTypeName(t.get("seat_type_name", String.class));
                    order.setStrTicketPricePage(t.get("str_ticket_price_page", String.class));
                    order.setTicketStatusName(t.get("ticket_status_name", String.class));
                    order.setTicketTypeName(t.get("ticket_type_name", String.class));
                    order.setStartTimePage(x.get("start_time_page", String.class));
                    order.setArriveTimePage(x.get("arrive_time_page", String.class));
                    orderList.add(order);
                });
            });
        }
        return orderList;
    }


    /**
     * 设置车票价格信息
     *
     * @param data
     * @param ticketPriceRequest
     * @return
     */
    public static void settingTicketPrice(JSONObject data, QueryTicketPriceRequest ticketPriceRequest, TicketPrice ticketPrice) {
        switch (ticketPriceRequest.getTrainCode().toCharArray()[0]) {
            case 'G':
                ticketPrice.setA9(data.get("A9", String.class));
                ticketPrice.setM(data.get("M", String.class));
                ticketPrice.setWZ(data.get("WZ", String.class));
                ticketPrice.setO(data.get("O", String.class));

                ticketPrice.setBusinessSeatPrice(ticketPrice.getA9());
                ticketPrice.setFirstSeatPrice(ticketPrice.getM());
                ticketPrice.setSecondSeatPrice(ticketPrice.getWZ());
                ticketPrice.setNoneSeatPrice(ticketPrice.getO());
                break;
            case 'D':
                ticketPrice.setM(data.get("M", String.class));
                ticketPrice.setWZ(data.get("WZ", String.class));
                ticketPrice.setAI(data.get("AI", String.class));
                ticketPrice.setAJ(data.get("AJ", String.class));
                ticketPrice.setO(data.get("O", String.class));

                ticketPrice.setFirstSeatPrice(ticketPrice.getM());
                ticketPrice.setSecondSeatPrice(ticketPrice.getWZ());
                ticketPrice.setSoftSleepPrice(ticketPrice.getAI());
                ticketPrice.setHardSleepPrice(ticketPrice.getAJ());
                ticketPrice.setNoneSeatPrice(ticketPrice.getO());
                break;
            case 'Z':
                ticketPrice.setA6(data.get("A6", String.class));
                ticketPrice.setA4(data.get("A4", String.class));
                ticketPrice.setA3(data.get("A3", String.class));
                ticketPrice.setA1(data.get("A1", String.class));
                ticketPrice.setWZ(data.get("WZ", String.class));

                ticketPrice.setHighSoftSleepPrice(ticketPrice.getA6());
                ticketPrice.setSoftSleepPrice(ticketPrice.getA4());
                ticketPrice.setHardSleepPrice(ticketPrice.getA3());
                ticketPrice.setHardSeatPrice(ticketPrice.getA1());
                ticketPrice.setNoneSeatPrice(ticketPrice.getWZ());
                break;
            case 'T':
                ticketPrice.setA4(data.get("A4", String.class));
                ticketPrice.setA3(data.get("A3", String.class));
                ticketPrice.setA1(data.get("A1", String.class));
                ticketPrice.setWZ(data.get("WZ", String.class));

                ticketPrice.setSoftSleepPrice(ticketPrice.getA4());
                ticketPrice.setHardSleepPrice(ticketPrice.getA3());
                ticketPrice.setHardSeatPrice(ticketPrice.getA1());
                ticketPrice.setNoneSeatPrice(ticketPrice.getWZ());
                break;
            case 'K':
                ticketPrice.setA4(data.get("A4", String.class));
                ticketPrice.setA3(data.get("A3", String.class));
                ticketPrice.setA1(data.get("A1", String.class));
                ticketPrice.setWZ(data.get("WZ", String.class));

                ticketPrice.setSoftSleepPrice(ticketPrice.getA4());
                ticketPrice.setHardSleepPrice(ticketPrice.getA3());
                ticketPrice.setHardSeatPrice(ticketPrice.getA1());
                ticketPrice.setNoneSeatPrice(ticketPrice.getWZ());
                break;
            default:
                ticketPrice.setA4(data.get("A4", String.class));
                ticketPrice.setA3(data.get("A3", String.class));
                ticketPrice.setA1(data.get("A1", String.class));
                ticketPrice.setWZ(data.get("WZ", String.class));

                ticketPrice.setSoftSleepPrice(ticketPrice.getA4());
                ticketPrice.setHardSleepPrice(ticketPrice.getA3());
                ticketPrice.setHardSeatPrice(ticketPrice.getA1());
                ticketPrice.setNoneSeatPrice(ticketPrice.getWZ());
                break;
        }
    }


    /**
     * 创建HttpClient
     *
     * @return
     * @throws KeyStoreException
     * @throws NoSuchAlgorithmException
     * @throws KeyManagementException
     */
    public static CloseableHttpClient httpClientBuild() throws KeyStoreException, NoSuchAlgorithmException, KeyManagementException {
        // 创建SSL安全认证
        SSLContext sslcontext = SSLContexts.custom().loadTrustMaterial(null, (chain, authType) -> true).build();
        SSLConnectionSocketFactory sslSf = new SSLConnectionSocketFactory(sslcontext, null, null, new NoopHostnameVerifier());
        // 创建cookieStore本地实例
        RequestConfig globalConfig = RequestConfig.custom().setCookieSpec(CookieSpecs.STANDARD).build();
        cookieStore = new BasicCookieStore();
        context = HttpClientContext.create();
        context.setCookieStore(cookieStore);
        return HttpClients.custom().setSSLSocketFactory(sslSf).setDefaultRequestConfig(globalConfig).setDefaultCookieStore(cookieStore).build();
    }

    /**
     * 创建get请求
     *
     * @param url
     * @param cookie
     * @return
     */
    public static HttpGet httpGetBuild(String url, String cookie, UseCdnProxy cdnProxy) {
        String cdn = "127.0.0.1";

        HttpGet httpGet = new HttpGet(url);

        if (UseCdnProxy.YES == cdnProxy) {
            int index = getRandomIndex();
            cdn = cdnList.get(index);
            url.replace("kyfw.12306.cn", cdn);
            httpGet.addHeader("Host", host);
        }

        httpGet.addHeader(HttpHeaderParamter.ACCEPT.getKey(), HttpHeaderParamter.ACCEPT.getValue());
        httpGet.addHeader(HttpHeaderParamter.ACCEPT_ENCODING.getKey(), HttpHeaderParamter.ACCEPT_ENCODING.getValue());
        httpGet.addHeader(HttpHeaderParamter.ACCEPT_LANGUAGE.getKey(), HttpHeaderParamter.ACCEPT_LANGUAGE.getValue());
        httpGet.addHeader(HttpHeaderParamter.USER_AGENT.getKey(), HttpHeaderParamter.USER_AGENT.getValue());
        httpGet.addHeader(HttpHeaderParamter.X_REQUESTED_WITH.getKey(), HttpHeaderParamter.X_REQUESTED_WITH.getValue());
        httpGet.addHeader(HttpHeaderParamter.COOKIE.getKey(), cookie);

        LOGGER.info("======> Get: 服务器地址: {} <======", cdn);
        return httpGet;
    }

    /**
     * 创建post请求
     * 所有post请求暂时不使用cdn，容易被封IP
     *
     * @param url
     * @param formPail 请求参数
     * @param cookie
     * @return
     */
    public static HttpPost httpPostBuild(String url, List<NameValuePair> formPail, String cookie) throws Exception {
        /*int index = getRandomIndex();
        String cdn = cdnList.get(index);
        LOGGER.info("======> Post: 服务器地址: {} <======", cdn);*/

        HttpPost httpPost = new HttpPost(url);
        // httpPost.addHeader("Host", host);
        httpPost.addHeader(HttpHeaderParamter.ACCEPT.getKey(), HttpHeaderParamter.ACCEPT.getValue());
        httpPost.addHeader(HttpHeaderParamter.ACCEPT_ENCODING.getKey(), HttpHeaderParamter.ACCEPT_ENCODING.getValue());
        httpPost.addHeader(HttpHeaderParamter.ACCEPT_LANGUAGE.getKey(), HttpHeaderParamter.ACCEPT_LANGUAGE.getValue());
        httpPost.addHeader(HttpHeaderParamter.USER_AGENT.getKey(), HttpHeaderParamter.USER_AGENT.getValue());
        httpPost.addHeader(HttpHeaderParamter.X_REQUESTED_WITH.getKey(), HttpHeaderParamter.X_REQUESTED_WITH.getValue());
        httpPost.addHeader(HttpHeaderParamter.COOKIE.getKey(), cookie);
        UrlEncodedFormEntity formEntity = new UrlEncodedFormEntity(formPail, "UTF-8");
        httpPost.setEntity(formEntity);
        return httpPost;
    }


    /**
     * 缓存cookie
     *
     * @param cookies
     */
    public static void cacheCookie(List<Cookie> cookies) {
        if (!CollectionUtils.isEmpty(cookies)) {
            cookies.forEach(cookie -> {
                COOKIE_CACHE_MAP.put(cookie.getName(), cookie.getValue());
            });
        }
    }

    /**
     * 获取cookie字符串
     *
     * @param reCookieKeys 不需要携带的cookie-key
     */
    public String getCookieStr(String[] reCookieKeys) {
        StringJoiner sj = new StringJoiner(";");
        for (Map.Entry<String, String> cookie : COOKIE_CACHE_MAP.entrySet()) {
            boolean flag = false;
            if (reCookieKeys != null && reCookieKeys.length > 0) {
                for (String reCookieKey : reCookieKeys) {
                    if (cookie.getKey().equals(reCookieKey)) {
                        flag = true;
                    }
                }
            }
            if (!flag) {
                sj.add(String.format("%s=%s", cookie.getKey(), cookie.getValue()));
            }
        }
        // 以下两个cookie参数为必带参数
        /*sj.add(String.format("%s=%s", "RAIL_EXPIRATION", config.getRAIL_EXPIRATION()));
        sj.add(String.format("%s=%s", "RAIL_DEVICEID", config.getRAIL_DEVICEID()));*/
        // 暂时携带该cookie,后面换成动态获取
        sj.add(String.format("%s=%s", "BIGipServerpassport", BIGIPSERVERPASSPORT));
        return sj.toString();
    }

    /**
     * 获取随机cdn
     *
     * @return
     */
    public static int getRandomIndex() {
        int max, min, index;
        min = 0;
        index = 0;
        max = cdnList.size() - 1;
        index = (int) (Math.random() * (max - min) + min);
        /*for (int x = 0; x < cdnList.size(); x++) {
            index = (int) (Math.random() * (max - min) + min);

            LOGGER.info("======> 下标:{}，本次服务cdn地址: {}", index, cdnList.get(index));
        }*/
        return index;
    }

    /**
     * 去重-stream
     *
     * @param keyExtractor
     * @param <T>
     * @return
     */
    public static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
        Map<Object, Boolean> seen = new ConcurrentHashMap<>();
        return t -> seen.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
    }
}
