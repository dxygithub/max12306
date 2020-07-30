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
     * 车票查询url: get(出发日期:{1}、始发站:{2}、终点站:{3}、车票类型:{4})
     */
    String TICKET_QUERY_URL = "https://kyfw.12306.cn/otn/leftTicket/query?leftTicketDTO.train_date={1}&leftTicketDTO.from_station={2}&leftTicketDTO.to_station={3}&purpose_codes={4}";

    /**
     * 车票价格查询url: get(列车号:{1}、出发站站序:{2}、到达站站序{3}、座位类型{4}、出发日期:{5})
     */
    String TICKET_PRICE_QUERY_URL="https://kyfw.12306.cn/otn/leftTicket/queryTicketPrice?train_no={1}&from_station_no={2}&to_station_no={3}&seat_types={4}&train_date={5}";

    /**
     * 获取登录验证码: get(当前时间毫秒数:{1})
     */
    String GET_CAPTCHA="https://kyfw.12306.cn/passport/captcha/captcha-image64?login_site=E&module=login&rand=sjrand&{1}&callback=jQuery19105718945282709293_1595988456279&_=1595988456286";

    /**
     * 校验验证码: get(验证码坐标:{xyz})
     */
    String CHECK_CAPTCHA="https://kyfw.12306.cn/passport/captcha/captcha-check?callback=jQuery19105718945282709293_1595988456279&answer={xyz}&rand=sjrand&login_site=E&_=1595988456290";

    /**
     * 登录请求: post 参数->{“username:用户名”:“xxx”, “password:密码”:“xxx”, “appid”:“otn”, “answer:图形验证码坐标”:“xxx”}
     */
    String LOGIN_URL="https://kyfw.12306.cn/passport/web/login";

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
