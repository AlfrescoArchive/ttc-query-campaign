package org.activiti.cloud.query.configuration;

import java.util.concurrent.Executors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

@Configuration
public class SchedulerConfiguration {

    private final Integer poolSize  = 1;

    @Bean
    public Scheduler myScheduler() {
        return Schedulers.fromExecutor(Executors.newFixedThreadPool(poolSize));
    }
}
