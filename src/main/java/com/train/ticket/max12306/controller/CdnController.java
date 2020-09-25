package com.train.ticket.max12306.controller;

import com.train.ticket.max12306.request.HttpURL12306;
import com.train.ticket.max12306.common.RestResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

/**
 * @ClassName CdnController
 * @Author duxiaoyu
 * @Date 2020/9/10 11:30
 * @Version 1.0
 */
@RestController
public class CdnController {

    /**
     * 获取可用cdn数量
     *
     * @return
     */
    @GetMapping("/max/getCdnCount")
    public RestResult getCdnCount() {
        Integer cdnCount = HttpURL12306.cdnList.size();
        return RestResult.SUCCESS().data(Objects.isNull(cdnCount) ? 0 : cdnCount).build();
    }
}
