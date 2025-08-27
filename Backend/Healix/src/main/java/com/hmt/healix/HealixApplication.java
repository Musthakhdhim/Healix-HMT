package com.hmt.healix;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class HealixApplication {

    public static void main(String[] args) {
        SpringApplication.run(HealixApplication.class, args);
    }

}
