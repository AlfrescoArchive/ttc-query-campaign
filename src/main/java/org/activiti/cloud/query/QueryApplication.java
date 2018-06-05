/*
 * Copyright 2018 Alfresco, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.activiti.cloud.query;

import java.util.ArrayList;
import java.util.List;

import org.activiti.cloud.query.model.Tweet;
import org.activiti.cloud.services.query.app.repository.ProcessInstanceRepository;
import org.activiti.cloud.services.query.model.ProcessInstance;
import org.activiti.cloud.services.query.model.Variable;
import org.activiti.cloud.starter.query.configuration.EnableActivitiQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.PagedResources;
import org.springframework.hateoas.Resource;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@EnableActivitiQuery
//@EnableActivitiNotificationsGateway
//@EnableActivitiGraphQLQueryService
@RestController
@EnableJpaRepositories(basePackageClasses = {ExtendedProcessInstanceRepository.class, ProcessInstanceRepository.class})
public class QueryApplication {

    @Autowired
    private ExtendedProcessInstanceRepository repository;

    private Logger logger = LoggerFactory.getLogger(QueryApplication.class);

    @Autowired
    private PagedResourcesAssembler<Tweet> pagedResourcesAssembler;

    public static void main(String[] args) {
        SpringApplication.run(QueryApplication.class,
                              args);
    }

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
        return pagedResourcesAssembler.toResource(new PageImpl<Tweet>(tweets,
                                                                      pageable,
                                                                      matchedProcessInstances.getTotalElements()));
    }

    public Variable getVariableByName(ProcessInstance pi,
                                      String name) {
        for (Variable v : pi.getVariables()) {
            if (v.getName().equals(name)) {
                return v;
            }
        }
        return null;
    }
}