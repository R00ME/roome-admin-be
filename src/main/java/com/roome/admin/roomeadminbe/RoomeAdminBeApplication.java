package com.roome.admin.roomeadminbe;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class RoomeAdminBeApplication {

    public static void main(String[] args) {
        SpringApplication.run(RoomeAdminBeApplication.class, args);
    }

}
