package org.activiti.cloud.query.controller;

import java.util.ArrayList;
import java.util.List;

import org.activiti.cloud.query.QueryApplication;
import org.activiti.cloud.query.model.Tweet;
import org.activiti.cloud.query.repository.ExtendedProcessInstanceRepository;
import org.activiti.cloud.services.query.model.ProcessInstance;
import org.activiti.cloud.services.query.model.Variable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.PagedResources;
import org.springframework.hateoas.Resource;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ProcessedFeedController {

    @Autowired
    private ExtendedProcessInstanceRepository repository;

    private Logger logger = LoggerFactory.getLogger(QueryApplication.class);

    @Autowired
    private PagedResourcesAssembler<Tweet> pagedResourcesAssembler;

    @RequestMapping(path = "/")
    public String helloFromQuery() {
        return "Hello from the Trending Topic Campaigns Query Service";
    }

    @RequestMapping(path = "/processed/{campaign}")
    public PagedResources<Resource<Tweet>> getProcessedTweets(
            @PathVariable("campaign") String campaign,
            Pageable pageable) {

        Page<ProcessInstance> matchedProcessInstances = repository.findAllCompletedAndMatched(campaign,
                                                                                              pageable);
        List<Tweet> tweets = new ArrayList<>();
        createTweetsFromProcessInstances(matchedProcessInstances,
                                         tweets);
        return pagedResourcesAssembler.toResource(new PageImpl<Tweet>(tweets,
                                                                      pageable,
                                                                      matchedProcessInstances.getTotalElements()));
    }

    @RequestMapping(path = "/inflight/{campaign}")
    public PagedResources<Resource<Tweet>> getInFlightTweets(
            @PathVariable("campaign") String campaign,
            Pageable pageable) {

        Page<ProcessInstance> matchedProcessInstances = repository.findAllInFlight(campaign,
                                                                                   pageable);
        List<Tweet> tweets = new ArrayList<>();
        createTweetsFromProcessInstances(matchedProcessInstances,
                                         tweets);
        return pagedResourcesAssembler.toResource(new PageImpl<Tweet>(tweets,
                                                                      pageable,
                                                                      matchedProcessInstances.getTotalElements()));
    }

    @RequestMapping(path = "/discarded/{campaign}")
    public PagedResources<Resource<Tweet>> getDiscardedTweets(
            @PathVariable("campaign") String campaign,
            Pageable pageable) {

        Page<ProcessInstance> matchedProcessInstances = repository.findAllCompletedAndDiscarded(campaign,
                                                                                                pageable);
        List<Tweet> tweets = new ArrayList<>();
        createTweetsFromProcessInstances(matchedProcessInstances,
                                         tweets);
        return pagedResourcesAssembler.toResource(new PageImpl<Tweet>(tweets,
                                                                      pageable,
                                                                      matchedProcessInstances.getTotalElements()));
    }

    private void createTweetsFromProcessInstances(Page<ProcessInstance> matchedProcessInstances,
                                                  List<Tweet> tweets) {
        for (ProcessInstance matchedPI : matchedProcessInstances.getContent()) {

            Variable text = getVariableByName(matchedPI,
                                              "text");
            Variable author = getVariableByName(matchedPI,
                                                "author");
            Variable lang = getVariableByName(matchedPI,
                                              "lang");
            Variable timestamp = getVariableByName(matchedPI,
                                                   "timestamp");
            Variable attitude = getVariableByName(matchedPI,
                                                  "attitude");
            if (text != null && author != null) {
                tweets.add(new Tweet(text.getValue(),
                                     author.getValue(),
                                     (lang != null) ? lang.getValue() : "",
                                     (attitude != null) ? attitude.getValue() : "",
                                     (timestamp != null) ? new Long(timestamp.getValue()) : new Long(0)));
            }
        }
    }

    private Variable getVariableByName(ProcessInstance pi,
                                       String name) {
        for (Variable v : pi.getVariables()) {
            if (v.getName().equals(name)) {
                return v;
            }
        }
        return null;
    }
}
