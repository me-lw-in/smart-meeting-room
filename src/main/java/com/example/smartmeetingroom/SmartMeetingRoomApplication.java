package com.example.smartmeetingroom;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class SmartMeetingRoomApplication {

    public static void main(String[] args) {
        SpringApplication.run(SmartMeetingRoomApplication.class, args);
    }

}
