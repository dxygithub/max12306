package com.train.ticket.max12306;

import com.train.ticket.max12306.request.HttpURL12306;
import com.train.ticket.max12306.constant.HttpURLConstant12306;
import com.train.ticket.max12306.requestvo.QueryTicketPriceRequest;
import com.train.ticket.max12306.requestvo.QueryTicketRequest;
import com.train.ticket.max12306.entity.StationInfo;
import com.train.ticket.max12306.entity.TicketInfo;
import com.train.ticket.max12306.entity.TicketPrice;
import com.train.ticket.max12306.enumeration.HttpHeaderParamter;
import com.train.ticket.max12306.enumeration.TicketType;
import com.train.ticket.max12306.mapper.StationInfoMapper;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.CookieStore;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.util.EntityUtils;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.net.ssl.SSLContext;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.stream.Collectors;

@SpringBootTest
class Max12306ApplicationTests {

    private static final Logger LOGGER = LoggerFactory.getLogger(Max12306ApplicationTests.class);

    private static CookieStore cookieStore = null;

    private static HttpClientContext context = null;

    @Autowired
    private StationInfoMapper stationInfoMapper;

    @Autowired
    private HttpURL12306 url12306;

    @Test
    void testStationInfo() {
        try {
            List<StationInfo> list = url12306.parseStationInfo();
            for (StationInfo item : list) {
                LOGGER.info("车站名称:{}，编码:{}，全拼:{}，简拼:{}，车站序号:{}", item.getStationName(), item.getStationCode(), item.getStationSpell(), item.getStationLogogram(), item.getStationSort());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    void testTicketQuery() {
        try {
            List<StationInfo> list = url12306.parseStationInfo();
            for (StationInfo item : list) {
                LOGGER.info("车站名称:{}，编码:{}，全拼:{}，简拼:{}，车站序号:{}", item.getStationName(), item.getStationCode(), item.getStationSpell(), item.getStationLogogram(), item.getStationSort());
            }
            LOGGER.info("\n\n==========================================================================================================================================================================\n\n");
            QueryTicketRequest request = new QueryTicketRequest();
            request.setFromDate("2020-08-13");
            request.setFromStationCode("SHH");
            request.setToStationCode("TYV");
            request.setTicketType(TicketType.TICKETS);
            List<TicketInfo> ticketInfos = url12306.parseTicketInfo(request);
            List<TicketInfo> offStreamTrain = ticketInfos.stream().filter(x -> x.getRemark().equals("列车停运")).collect(Collectors.toList());
            ticketInfos.removeAll(offStreamTrain);
            ticketInfos.addAll(offStreamTrain);
            ticketInfos.forEach(x -> {
                QueryTicketPriceRequest priceRequest = new QueryTicketPriceRequest();
                priceRequest.setTrainCode(x.getTrainCode());
                priceRequest.setTrainNo(x.getTrainNo());
                priceRequest.setTrainDate("2020-08-13");
                priceRequest.setFromStationNo(x.getFromStationNo());
                priceRequest.setToStationNo(x.getToStationNo());
                priceRequest.setSeatTypes(x.getSeatType());
                TicketPrice ticketPrice = null;
                try {
                    ticketPrice = url12306.parseTicketPrice(priceRequest);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                LOGGER.info("\n车次:{}，出发站:{}，到达站:{}，出发时间:{}，到达时间:{}，历时:{}/hh:mm，\n" +
                                "商务座/特等座:{}，一等座:{}，二等座:{}，高级软卧:{}，软卧:{}，动卧:{}，硬卧:{}，软座:{}，硬座:{}，无座:{}，其他:{}，备注:{}，座位类型:{}\n" +
                                "{}，{}，{}，{}，{}，{}，{}，{}，{}，{}，{}\n",
                        x.getTrainCode(), x.getFromStationName(), x.getToStationName(), x.getFromTime(), x.getToTime(), x.getLastTime(),
                        StringUtils.isBlank(x.getBusinessSeatCount()) ? "--" : x.getBusinessSeatCount(),
                        StringUtils.isBlank(x.getFirstSeatCount()) ? "--" : x.getFirstSeatCount(),
                        StringUtils.isBlank(x.getSecondSeatCount()) ? "--" : x.getSecondSeatCount(),
                        StringUtils.isBlank(x.getHighSoftSleepCount()) ? "--" : x.getHighSoftSleepCount(),
                        StringUtils.isBlank(x.getSoftSleepCount()) ? "--" : x.getSoftSleepCount(),
                        StringUtils.isBlank(x.getMotorSleepCount()) ? "--" : x.getMotorSleepCount(),
                        StringUtils.isBlank(x.getHardSleepCount()) ? "--" : x.getHardSleepCount(),
                        StringUtils.isBlank(x.getSoftSeatCount()) ? "--" : x.getSoftSeatCount(),
                        StringUtils.isBlank(x.getHardSeatCount()) ? "--" : x.getHardSeatCount(),
                        StringUtils.isBlank(x.getNoneSeatCount()) ? "--" : x.getNoneSeatCount(),
                        StringUtils.isBlank(x.getOther()) ? "--" : x.getOther(), x.getRemark(),
                        StringUtils.isBlank(x.getSeatType()) ? "--" : x.getSeatType(),
                        StringUtils.isBlank(ticketPrice.getBusinessSeatPrice()) ? "[--]" : String.format("[%s]", ticketPrice.getBusinessSeatPrice()),
                        StringUtils.isBlank(ticketPrice.getFirstSeatPrice()) ? "[--]" : String.format("[%s]", ticketPrice.getFirstSeatPrice()),
                        StringUtils.isBlank(ticketPrice.getSecondSeatPrice()) ? "[--]" : String.format("[%s]", ticketPrice.getSecondSeatPrice()),
                        StringUtils.isBlank(ticketPrice.getHighSoftSleepPrice()) ? "[--]" : String.format("[%s]", ticketPrice.getHighSoftSleepPrice()),
                        StringUtils.isBlank(ticketPrice.getSoftSleepPrice()) ? "[--]" : String.format("[%s]", ticketPrice.getSoftSleepPrice()),
                        StringUtils.isBlank(ticketPrice.getMotorSleepPrice()) ? "[--]" : String.format("[%s]", ticketPrice.getMotorSleepPrice()),
                        StringUtils.isBlank(ticketPrice.getHardSleepPrice()) ? "[--]" : String.format("[%s]", ticketPrice.getHardSleepPrice()),
                        StringUtils.isBlank(ticketPrice.getSoftSeatPrice()) ? "[--]" : String.format("[%s]", ticketPrice.getSoftSeatPrice()),
                        StringUtils.isBlank(ticketPrice.getHardSeatPrice()) ? "[--]" : String.format("[%s]", ticketPrice.getHardSeatPrice()),
                        StringUtils.isBlank(ticketPrice.getNoneSeatPrice()) ? "[--]" : String.format("[%s]", ticketPrice.getNoneSeatPrice()),
                        "[--]");
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void main(String[] args) throws Exception {
        String url = "https://kyfw.12306.cn/otn/leftTicket/query?leftTicketDTO.train_date=2020-07-31&leftTicketDTO.from_station=SHH&leftTicketDTO.to_station=TJP&purpose_codes=ADULT";
        try (CloseableHttpClient httpClient = createHttpClient()) {
            QueryTicketRequest request = new QueryTicketRequest();
            request.setFromDate("2020-07-31");
            request.setFromStationCode("SHH");
            request.setToStationCode("BJP");
            request.setTicketType(TicketType.TICKETS);
            HttpGet httpGet = new HttpGet(HttpURLConstant12306.TICKET_QUERY_URL.
                    replace("{1}", request.getFromDate()).
                    replace("{2}", request.getFromStationCode()).
                    replace("{3}", request.getToStationCode()).
                    replace("{4}", request.getTicketType().getValue()));
            httpGet.addHeader(HttpHeaderParamter.ACCEPT.getKey(), HttpHeaderParamter.ACCEPT.getValue());
            httpGet.addHeader(HttpHeaderParamter.ACCEPT_ENCODING.getKey(), HttpHeaderParamter.ACCEPT_ENCODING.getValue());
            httpGet.addHeader(HttpHeaderParamter.ACCEPT_LANGUAGE.getKey(), HttpHeaderParamter.ACCEPT_LANGUAGE.getValue());
            httpGet.addHeader(HttpHeaderParamter.USER_AGENT.getKey(), HttpHeaderParamter.USER_AGENT.getValue());
            httpGet.addHeader(HttpHeaderParamter.X_REQUESTED_WITH.getKey(), HttpHeaderParamter.X_REQUESTED_WITH.getValue());
            httpGet.addHeader(HttpHeaderParamter.COOKIE.getKey(), HttpHeaderParamter.COOKIE.getValue().
                    replace("{1}", "E3DCB0F62732801E984A6DBB00477D4E").
                    replace("{2}", "1596163502806").
                    replace("{3}", "nBVP2rmFnuqc8JeR6Pu4GHIBLxXaGlDg2jP2mTLakZJ-otNv4aV-albpv3H-uFZznksWqnbPgliWn_Cu6sSdm1qOtaPoUi3VCMHaSDqb8nS-ZkDBmwwPiT4jOt9LlMQC-NvnjzHymbsq2cKKqt4nyhqp-2Ac9M5h"));
            try (CloseableHttpResponse httpResponse = httpClient.execute(httpGet, context)) {
                HttpEntity entity = httpResponse.getEntity();
                String result = EntityUtils.toString(entity);
                EntityUtils.consume(entity);
                System.out.println("===>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
                System.out.println(result);
                System.out.println("===>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>: " + cookieStore.getCookies().size());
                for (Cookie item : cookieStore.getCookies()) {
                    System.out.println(item.getName() + ":" + item.getValue());
                }
            }
        }
    }

    private static CloseableHttpClient createHttpClient() throws KeyStoreException, NoSuchAlgorithmException, KeyManagementException {
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


}
