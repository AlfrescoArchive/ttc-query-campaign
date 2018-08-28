package org.activiti.cloud.query.controller;

import java.util.ArrayList;
import java.util.List;

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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    public List<Tweet> getProcessedTweets(
            @PathVariable("campaign") String campaign) {

        List<VariableEntity> matchedVariables = variableRepository.findAllCompletedAndMatched(campaign);
        List<ProcessInstanceEntity> matchedProcessInstancesList = new ArrayList<ProcessInstanceEntity>();

        for(VariableEntity variableEntity:matchedVariables){

            if(variableEntity.getName().equalsIgnoreCase("matched") && variableEntity.getType().equalsIgnoreCase("string")){
                if(variableEntity.getValue()!=null &&variableEntity.getValue() instanceof String && ((String) variableEntity.getValue()).equalsIgnoreCase("true")){
                    matchedProcessInstancesList.add(variableEntity.getProcessInstance());
                }
            }

        }

        List<ProcessInstanceEntity> matchedProcessInstances = new ArrayList<>(matchedProcessInstancesList);

        List<Tweet> tweets = createTweetsFromProcessInstances(matchedProcessInstances);
        return tweets;
    }

    @RequestMapping(path = "/inflight/{campaign}")
    public PagedResources<Resource<Tweet>> getInFlightTweets(
            @PathVariable("campaign") String campaign,
            Pageable pageable) {

        Page<ProcessInstanceEntity> matchedProcessInstances = repository.findAllInFlight(campaign,
                                                                                   pageable);
        List<Tweet> tweets = createTweetsFromProcessInstances(matchedProcessInstances);

        return pagedResourcesAssembler.toResource(new PageImpl<Tweet>(tweets,
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

        return pagedResourcesAssembler.toResource(new PageImpl<Tweet>(tweets,
                                                                      pageable,
                                                                      matchedProcessInstances.getTotalElements()));
    }
}
