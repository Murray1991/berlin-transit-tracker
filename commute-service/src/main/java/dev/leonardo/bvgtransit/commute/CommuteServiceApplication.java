package dev.leonardo.bvgtransit.commute;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CommuteServiceApplication {

    static void main(String[] args) {
        SpringApplication.run(CommuteServiceApplication.class, args);
    }
}
