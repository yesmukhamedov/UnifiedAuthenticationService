package com.project.unifiedauthenticationservice.Schedulers;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class TestSchedule {

    @Scheduled(fixedRate = 5000)
    public void runTask() {
        System.out.println("Scheduled task is running...");
    }
}
