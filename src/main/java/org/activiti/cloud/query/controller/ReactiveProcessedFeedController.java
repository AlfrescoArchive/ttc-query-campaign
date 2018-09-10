package org.activiti.cloud.query.controller;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.transaction.Transactional;

import org.activiti.cloud.query.QueryApplication;
import org.activiti.cloud.query.configuration.QueryConfiguration;
import org.activiti.cloud.query.model.Tweet;
import org.activiti.cloud.query.repository.ExtendedProcessInstanceRepository;
import org.activiti.cloud.query.repository.ExtendedVariableRepository;
import org.activiti.cloud.services.query.model.ProcessInstanceEntity;
import org.activiti.cloud.services.query.model.VariableEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import static org.activiti.cloud.query.controller.ControllersUtil.createTweetsFromProcessInstances;

@RestController
@RefreshScope
public class ReactiveProcessedFeedController {

    @Autowired
    private ExtendedProcessInstanceRepository repository;
    @Autowired
    private ExtendedVariableRepository variableRepository;

    private Logger logger = LoggerFactory.getLogger(QueryApplication.class);

    private Map<String, List<Tweet>> cacheProcessedTweetsForFlux = new HashMap<>();

    private Map<String, List<Tweet>> cacheDiscardedTweetsForFlux = new HashMap<>();

    @Autowired
    private QueryConfiguration queryConfiguration;

    @RequestMapping(path = "/reactive/processed/{campaign}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<Tweet> getProcessedTweets(@PathVariable("campaign") String campaign,
                                          Pageable pageable) {

        if (cacheProcessedTweetsForFlux.get(campaign) == null) {
            cacheProcessedTweetsForFlux.put(campaign,
                                            new CopyOnWriteArrayList<>());
        }

        return Flux.interval(Duration.ofSeconds(5)).flatMapIterable(list -> cacheProcessedTweetsForFlux.get(campaign));
    }

    @RequestMapping(path = "/reactive/discarded/{campaign}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<Tweet> getDiscardedTweets(@PathVariable("campaign") String campaign,
                                          Pageable pageable) {

        if (cacheDiscardedTweetsForFlux.get(campaign) == null) {
            cacheDiscardedTweetsForFlux.put(campaign,
                                            new CopyOnWriteArrayList<>());
        }

        return Flux.interval(Duration.ofSeconds(5)).flatMapIterable(list -> cacheDiscardedTweetsForFlux.get(campaign));
    }

    @Scheduled(fixedRateString = "${query.refresh}")
    @Transactional
    public void refreshCampaignFeed() {
        for (String campaign : cacheProcessedTweetsForFlux.keySet()) {
            List<VariableEntity> matchedVariables = variableRepository.findAllCompletedAndMatchedSince(campaign,
                    new Date(System.currentTimeMillis() - queryConfiguration.getRefresh()));
            List<ProcessInstanceEntity> matchedProcessInstancesList = new ArrayList<ProcessInstanceEntity>();

            for(VariableEntity variableEntity:matchedVariables){

                if(variableEntity.getName().equalsIgnoreCase("matched") && variableEntity.getType().equalsIgnoreCase("string")){
                    if(variableEntity.getValue()!=null &&variableEntity.getValue() instanceof String && ((String) variableEntity.getValue()).equalsIgnoreCase("true")){
                        matchedProcessInstancesList.add(variableEntity.getProcessInstance());
                    }
                }

            }

            List<ProcessInstanceEntity> matchedProcessInstances = new ArrayList<>(matchedProcessInstancesList);


            List<Tweet> tweetsFromProcessInstances = createTweetsFromProcessInstances(matchedProcessInstances);

            cacheProcessedTweetsForFlux.get(campaign).addAll(tweetsFromProcessInstances);

            List<ProcessInstanceEntity> discardedProcessInstances = repository.findAllCompletedAndDiscardedSince(campaign,
                                                                                                         new Date(System.currentTimeMillis() - queryConfiguration.getRefresh()));
            List<Tweet> discardedTweetsFromProcessInstances = createTweetsFromProcessInstances(discardedProcessInstances);
            cacheDiscardedTweetsForFlux.get(campaign).addAll(discardedTweetsFromProcessInstances);
        }
    }
}
