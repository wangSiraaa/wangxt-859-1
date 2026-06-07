package com.grain.fumigation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "com.grain.fumigation")
public class FumigationApprovalApplication {

    public static void main(String[] args) {
        SpringApplication.run(FumigationApprovalApplication.class, args);
    }
}
