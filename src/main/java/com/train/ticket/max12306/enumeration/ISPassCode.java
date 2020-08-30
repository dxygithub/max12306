package com.train.ticket.max12306.enumeration;

import lombok.Getter;

/**
 * @ClassName ISPassCode
 * @Author duxiaoyu
 * @Date 2020/8/28 10:21
 * @Version 1.0
 */
@Getter
public enum ISPassCode {

    YES(1),
    ERR(2),
    /**
     * 滑块验证
     */
    IS_SLIDE(3),
    NO(0);

    private int value;

    ISPassCode(int value){
        this.value=value;
    }
}
