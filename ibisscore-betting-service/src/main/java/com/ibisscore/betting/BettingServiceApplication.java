package com.ibisscore.betting;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class BettingServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(BettingServiceApplication.class, args);
    }
}
