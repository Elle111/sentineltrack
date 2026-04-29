package com.sentineltrack.risk;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;

@SpringBootApplication
@EnableKafka
public class RiskEvaluationApplication {
    public static void main(String[] args) {
        SpringApplication.run(RiskEvaluationApplication.class, args);
    }
}
