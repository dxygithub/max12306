package com.train.ticket.max12306.service.impl;

import com.train.ticket.max12306.common.HttpURL12306;
import com.train.ticket.max12306.common.RestResult;
import com.train.ticket.max12306.service.UserLoginService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

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

    /**验证码坐标中心位置**/
    private String yzm1Point = "35,72";
    private String yzm2Point = "111,72";
    private String yzm3Point = "181,72";
    private String yzm4Point = "250,72";
    private String yzm5Point = "35,148";
    private String yzm6Point = "111,148";
    private String yzm7Point = "181,148";
    private String yzm8Point = "250,148";
    /**验证码坐标中心位置**/

    /**
     * 获取图片验证码
     *
     * @return
     */
    @Override
    public RestResult getImgCaptcha() {
        String[] resultArr=null;
        try {
            String result = HttpURL12306.getImgCaptcha();
            resultArr=result.split("--");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return RestResult.SUCCESS().data(resultArr).build();
    }

    /**
     * 图片验证码校验
     * 目前自动验证存在问题，只支持手动验证
     *
     * @param imgIndex
     * @param autoCheck
     * @return
     */
    @Override
    public RestResult checkImgCaptcha(String imgIndex, boolean autoCheck) {
        if (StringUtils.isNotBlank(imgIndex)) {
            try {
                if (!autoCheck) {
                    StringJoiner answer=new StringJoiner(",");
                    String[] imgIndexArr=imgIndex.split(",");
                    for (String index:imgIndexArr){
                        switch (index){
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
                    String resultCode = HttpURL12306.checkImgCapthcha(answer.toString());
                    if (resultCode.equals("4")) {
                        return RestResult.SUCCESS().message("验证码校验成功").build();
                    } else {
                        return RestResult.SUCCESS().message("验证码校验失败").build();
                    }
                } else {
                    // 自动验证
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return RestResult.ERROR_PARAMS().message("验证码坐标为空").build();
    }
}
