package com.train.ticket.max12306.common;


/**
 * @ClassName HttpURLConstant12306
 * @Author duxiaoyu
 * @Date 2020/7/28 18:32
 * @Version 1.0
 */
public interface HttpURLConstant12306 {

    /**
     * 车站信息url:get(无需参数)
     */
    String STATION_INFO_URL = "https://kyfw.12306.cn/otn/resources/js/framework/station_name.js?station_version=1.9151";

    /**
     * 获取algID和hashCode参数：get(无需参数)
     */
    String GET_JS_URL = "https://kyfw.12306.cn/otn/HttpZF/GetJS";

    /**
     * 登录初始化: get(无需参数)
     * 用于获取后面请求中的一些cookie信息: 返回内容: JSESSIONID、BIGipServerotn、route
     */
    String LOGIN_INIT = "https://kyfw.12306.cn/otn/login/init";

    /**
     * 获取cookie: route: get
     */
    String LOGIN_BANNER="https://kyfw.12306.cn/otn/index12306/getLoginBanner";

    /**
     * 获取JSESESSIONID:get
     */
    String JSESESSIONID="https://kyfw.12306.cn/otn/passport?redirect=/otn/login/userLogin";

    /**
     * 获取cookie: uKey(cookie中携带tk获取): get
     */
    String USER_LOGIN="https://kyfw.12306.cn/otn/login/userLogin";

    /**
     * 车票查询url: get(出发日期:{1}、始发站:{2}、终点站:{3}、车票类型:{4})
     */
    String TICKET_QUERY_URL = "https://kyfw.12306.cn/otn/leftTicket/query?leftTicketDTO.train_date={1}&leftTicketDTO.from_station={2}&leftTicketDTO.to_station={3}&purpose_codes={4}";

    /**
     * 车票价格查询url: get(列车号:{1}、出发站站序:{2}、到达站站序{3}、座位类型{4}、出发日期:{5})
     */
    String TICKET_PRICE_QUERY_URL="https://kyfw.12306.cn/otn/leftTicket/queryTicketPrice?train_no={1}&from_station_no={2}&to_station_no={3}&seat_types={4}&train_date={5}";

    /**
     * 获取登录验证码: get(当前时间毫秒数:{1},{2},{3})
     */
    String GET_CAPTCHA="https://kyfw.12306.cn/passport/captcha/captcha-image64?login_site=E&module=login&rand=sjrand&{1}&callback=jQuery191013540634449692446_{2}&_={3}";

    /**
     * 校验验证码: get(验证码坐标:{xyz},{1}{2}:回调函数毫秒数)
     */
    String CHECK_CAPTCHA="https://kyfw.12306.cn/passport/captcha/captcha-check?callback=jQuery191013540634449692446_{1}&answer={xyz}&rand=sjrand&login_site=E&_={2}";

    /**
     * 图片验证码OCR校验: post(img: img-baseCode64)
     * 校验响应速度较慢
     */
    String OCR_AUTO_CHECK="https://12306-ocr.pjialin.com/check/";

    /**
     * 初始化滑块验证: post(appid/username/slideMode)
     */
    String INIT_SLIDE_PASSPORT_URL="https://kyfw.12306.cn/passport/web/slide-passcode";

    /**
     * 登录请求: post 参数->{“username:用户名”:“xxx”, “password:密码”:“xxx”, “appid”:“otn”, “answer:图形验证码坐标”:“xxx”}
     */
    String LOGIN_URL="https://kyfw.12306.cn/passport/web/login";

    /**
     * 用户认证: post(appid)
     */
    String PASSPORT_UAMTK_URL="https://kyfw.12306.cn/passport/web/auth/uamtk";

    /**
     * 用户认证：get(appid)
     */
    String PASSPORT_UAMTK_STATIC_URL="https://kyfw.12306.cn/passport/web/auth/uamtk-static";

    /**
     * 登录页面: get
     */
    String LOGIN_INIT_CDN1="https://kyfw.12306.cn/otn/resources/login.html";

    /**
     * 用户退出: get
     */
    String LOGIN_OUT="https://kyfw.12306.cn/otn/login/loginOut";

    /**
     * 查询乘车人信息: post
     */
    String PASSENGERS_QUERY="https://kyfw.12306.cn/otn/passengers/query";

    /**
     * 获取用户信息: post(tk)
     */
    String API_AUTH_UAMAUTHCLIENT="https://kyfw.12306.cn/otn/uamauthclient";

    /**
     * 删除乘车人: post
     */
    String DEL_PASSENGERS="https://kyfw.12306.cn/otn/passengers/delete";

    /**
     * 新增乘车人: post
     */
    String ADD_PASSENGERS="https://kyfw.12306.cn/otn/passengers/add";

    /**
     * 查询我的订单: post
     */
    String QUERY_MY_ORDER="https://kyfw.12306.cn/otn/queryOrder/queryMyOrder";

    /**
     * 查询未完成订单: post
     */
    String QUERY_MY_ORDER_NO_COMPLETE="https://kyfw.12306.cn/otn/queryOrder/queryMyOrderNoComplete";

    /**
     * 检查用户是否已登录: post: _json_att:
     */
    String CHECK_USER="https://kyfw.12306.cn/otn/login/checkUser";

    /**
     * 提交坐席订单: post
     */
    String SUBMIT_ORDER_REQUEST="https://kyfw.12306.cn/otn/leftTicket/submitOrderRequest";

    /**
     * 初始化订单token: post
     */
    String INIT_DC="https://kyfw.12306.cn/otn/confirmPassenger/initDc";

    /**
     * 检查订单信息: post
     */
    String CHECK_ORDER_INFO="https://kyfw.12306.cn/otn/confirmPassenger/checkOrderInfo";

    /**
     * 检查排队人数和余票: post
     */
    String GET_QUEUE_COUNT="https://kyfw.12306.cn/otn/confirmPassenger/getQueueCount";

    /**
     * 确认订单提交: post
     */
    String CONFIRM_SINGLE_FOR_QUEUE="https://kyfw.12306.cn/otn/confirmPassenger/confirmSingleForQueue";

    /**
     * 订单等待处理: get
     */
    String ORDER_WAIT="https://kyfw.12306.cn/otn/confirmPassenger/queryOrderWaitTime?random={1}&tourFlag=dc&_json_att=&REPEAT_SUBMIT_TOKEN={2}";

    /**
     * 提交订单图片验证码校验: post
     */
    String ORDER_IMG_CAPTCHA_CHECK="https://kyfw.12306.cn/otn/passcodeNew/checkRandCodeAnsyn";

    /**
     * 获取浏览器标识参数: 云平台获取
     */
    String API_GET_BROWSER_DEVICE_ID="https://12306-rail-id-v2.pjialin.com/";

    /**
     * 获取RAIL_EXPIRATION和RAIL_DEVICEID参数：get(algID:{1}、hashCode:{2}、timestamp:{3}当前时间毫秒数)
     */
    String GET_RAIL_URL =   "https://kyfw.12306.cn/otn/HttpZF/logdevice?" +
            "algID={1}&" +
            "hashCode={2}&" +
            "FMQw=0&" +
            "q4f3=zh-CN&" +
            "VPIf=1&" +
            "custID=133&" +
            "VEek=unknown&" +
            "dzuS=0&" +
            "yD16=0&" +
            "EOQP=89f60554e6cb588cf7dcc391a91488a1&" +
            "lEnu=176525634&" +
            "jp76=52d67b2a5aa5e031084733d5006cc664&" +
            "hAqN=Win32&" +
            "platform=WEB&" +
            "ks0Q=d22ca0b81584fbea62237b14bd04c866&" +
            "TeRS=1010x1680&" +
            "tOHY=24xx1050x1680&" +
            "Fvje=i1l1o1s1&" +
            "q5aJ=-8&" +
            "wNLf=99115dfb07133750ba677d055874de87&" +
            "0aew=Mozilla/5.0%20(Windows%20NT%2010.0;%20Win64;%20x64)%20AppleWebKit/537.36%20(KHTML,%20like%20Gecko)%20Chrome/76.0.3809.100%20Safari/537.36&" +
            "E3gR=c0e12f6dc8fe327988a902df0d354cec&" +
            "timestamp={3}";
}
