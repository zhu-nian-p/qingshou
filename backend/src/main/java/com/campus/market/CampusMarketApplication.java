package com.campus.market;

import com.campus.market.security.JwtProperties;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@MapperScan({
        "com.campus.market.user.mapper",
        "com.campus.market.item.mapper",
        "com.campus.market.order.mapper",
        "com.campus.market.review.mapper",
        "com.campus.market.topup.mapper"
})
@EnableConfigurationProperties(JwtProperties.class)
public class CampusMarketApplication {

    public static void main(String[] args) {
        SpringApplication.run(CampusMarketApplication.class, args);
    }
}
