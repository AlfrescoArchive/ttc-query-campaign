package org.activiti.cloud.query.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.activiti.cloud.query.QueryApplication;
import org.activiti.cloud.query.model.Tweet;
import org.activiti.cloud.query.repository.ExtendedProcessInstanceRepository;
import org.activiti.cloud.query.repository.ExtendedVariableRepository;
import org.activiti.cloud.services.query.model.ProcessInstanceEntity;
import org.activiti.cloud.services.query.model.VariableEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.PagedResources;
import org.springframework.hateoas.Resource;
import org.springframework.web.bind.annotation.*;

import static org.activiti.cloud.query.controller.ControllersUtil.createTweetsFromProcessInstances;

@RequestMapping(path = "/v1")
@RestController
@RefreshScope
public class ProcessedFeedController {

    @Autowired
    private ExtendedProcessInstanceRepository repository;

    @Autowired
    private ExtendedVariableRepository variableRepository;

    private Logger logger = LoggerFactory.getLogger(QueryApplication.class);

    @Autowired
    private PagedResourcesAssembler<Tweet> pagedResourcesAssembler;


    @RequestMapping(path = "/")
    public String helloFromQuery() {
        return "{ \"welcome\":\"Hello from the Trending Topic Campaigns Query Service\" } ";
    }

    @RequestMapping(path = "/processed/{campaign}")
    public PagedResources<Resource<Tweet>> getProcessedTweets(
            @PathVariable("campaign") String campaign,
            Pageable pageable) {

        List<VariableEntity> matchedVariables = variableRepository.findAllCompletedAndMatched(campaign);


        List<ProcessInstanceEntity> matchedProcessInstancesList = new ArrayList<>();

        for(VariableEntity variableEntity:matchedVariables){

            if(variableEntity.getName().equalsIgnoreCase("matched") && variableEntity.getType().equalsIgnoreCase("string")){
                if(variableEntity.getValue()!=null &&variableEntity.getValue() instanceof String && ((String) variableEntity.getValue()).equalsIgnoreCase("true")){
                    matchedProcessInstancesList.add(variableEntity.getProcessInstance());
                }
            }

        }

        Page<ProcessInstanceEntity> matchedProcessInstances = new PageImpl<>(matchedProcessInstancesList);

        List<Tweet> tweets = createTweetsFromProcessInstances(matchedProcessInstances);

        return pagedResourcesAssembler.toResource(new PageImpl<>(tweets,
                pageable,
                matchedProcessInstances.getTotalElements()));
    }

    @RequestMapping(path = "/inflight/{campaign}")
    public PagedResources<Resource<Tweet>> getInFlightTweets(
            @PathVariable("campaign") String campaign,
            Pageable pageable) {

        Page<ProcessInstanceEntity> matchedProcessInstances = repository.findAllInFlight(campaign,
                                                                                   pageable);
        List<Tweet> tweets = createTweetsFromProcessInstances(matchedProcessInstances);

        return pagedResourcesAssembler.toResource(new PageImpl<>(tweets,
                                                                      pageable,
                                                                      matchedProcessInstances.getTotalElements()));
    }

    @RequestMapping(path = "/discarded/{campaign}")
    public PagedResources<Resource<Tweet>> getDiscardedTweets(
            @PathVariable("campaign") String campaign,
            Pageable pageable) {

        Page<ProcessInstanceEntity> matchedProcessInstances = repository.findAllCompletedAndDiscarded(campaign,
                                                                                                pageable);
        List<Tweet> tweets = createTweetsFromProcessInstances(matchedProcessInstances);

        return pagedResourcesAssembler.toResource(new PageImpl<>(tweets,
                                                                      pageable,
                                                                      matchedProcessInstances.getTotalElements()));
    }


    @DeleteMapping(path = "/")
    public void cleanTweets(){
        repository.deleteAll();
    }

}
