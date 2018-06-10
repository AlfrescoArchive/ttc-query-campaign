package org.activiti.cloud.query.controller;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.transaction.Transactional;

import org.activiti.cloud.query.QueryApplication;
import org.activiti.cloud.query.configuration.QueryConfiguration;
import org.activiti.cloud.query.model.Tweet;
import org.activiti.cloud.query.repository.ExtendedProcessInstanceRepository;
import org.activiti.cloud.services.query.model.ProcessInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Scheduler;

import static org.activiti.cloud.query.controller.ControllersUtil.createTweetsFromProcessInstances;

@RestController
@RefreshScope
public class ReactiveProcessedFeedController {

    @Autowired
    private ExtendedProcessInstanceRepository repository;

    private Logger logger = LoggerFactory.getLogger(QueryApplication.class);

    private Map<String, List<Tweet>> cacheTweetsForFlux = new HashMap<>();

    private final Scheduler scheduler;

    @Autowired
    private QueryConfiguration queryConfiguration;

    public ReactiveProcessedFeedController(@Qualifier("myScheduler") Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    @RequestMapping(path = "/reactive/processed/{campaign}")
    public Flux<Tweet> getProcessedTweets(@PathVariable("campaign") String campaign,
                                          Pageable pageable) {
        logger.info(">>> Request campaign feed check for: " + campaign);
        if (cacheTweetsForFlux.get(campaign) == null) {
            cacheTweetsForFlux.put(campaign,
                                   new ArrayList<>());
        }
        return Flux.fromIterable(cacheTweetsForFlux.get(campaign)).subscribeOn(scheduler);
    }

    @Scheduled(fixedRateString = "${query.refresh}")
    @Transactional
    public void checkForNewTweetsForAllCampaigns() {
        logger.info(">>> Triggering campaign feed check for: " + cacheTweetsForFlux.keySet() + " every: " + queryConfiguration.getRefresh());
        for (String campaign : cacheTweetsForFlux.keySet()) {
            List<ProcessInstance> matchedProcessInstances = repository.findAllCompletedAndMatchedSince(campaign,
                                                                                                       new Date(System.currentTimeMillis() - queryConfiguration.getRefresh()));
            List<Tweet> tweetsFromProcessInstances = createTweetsFromProcessInstances(matchedProcessInstances);
            cacheTweetsForFlux.get(campaign).addAll(tweetsFromProcessInstances);
            cacheTweetsForFlux.get(campaign).sort((o1, o2) -> (o1.getTimestamp() > o2.getTimestamp()) ? -1 : 1);
        }
    }
}
