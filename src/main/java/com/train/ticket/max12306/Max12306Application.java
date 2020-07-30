package com.train.ticket.max12306;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Max12306
 * @date 2020-07-27 15:59
 * @author duxiaoyu
 */
@MapperScan("com.train.ticket.max12306.mapper")
@SpringBootApplication
public class Max12306Application {

    public static void main(String[] args) {
        SpringApplication.run(Max12306Application.class, args);
    }
}
