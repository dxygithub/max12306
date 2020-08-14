package com.train.ticket.max12306.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

/**
 * @ClassName RestResult
 * @Author duxiaoyu
 * @Date 2020/8/14 10:42
 * @Version 1.0
 */
@Builder
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RestResult<T> {

    /**
     * 状态码
     */
    private int code;

    /**
     * 响应提示
     */
    private String message;

    /**
     * 数据
     */
    private T data;

    /**
     * 请求成功
     * @return
     */
    public static RestResultBuilder SUCCESS(){
        return RestResult.builder().code(200).message("请求成功");
    }

    /**
     * 参数错误
     * @return
     */
    public static RestResultBuilder ERROR_PARAMS(){
        return RestResult.builder().code(400).message("请求参数错误，请重新尝试");
    }

    /**
     * 服务器异常
     * @return
     */
    public static RestResultBuilder SERVER_ERROR(){
        return RestResult.builder().code(500).message("服务器异常，请稍后再试");
    }
}
