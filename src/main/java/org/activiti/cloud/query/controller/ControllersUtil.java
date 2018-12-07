package org.activiti.cloud.query.controller;

import java.util.ArrayList;
import java.util.List;

import org.activiti.cloud.query.model.Tweet;
import org.activiti.cloud.services.query.model.ProcessInstanceEntity;
import org.activiti.cloud.services.query.model.ProcessVariableEntity;
import org.springframework.data.domain.Page;

public class ControllersUtil {

    public static List<Tweet> createTweetsFromProcessInstances(Page<ProcessInstanceEntity> matchedProcessInstances) {
        List<Tweet> tweets = new ArrayList<>(matchedProcessInstances.getSize());
        for (ProcessInstanceEntity matchedPI : matchedProcessInstances.getContent()) {

            extractVariables(tweets,
                             matchedPI);
        }
        return tweets;
    }

    public static List<Tweet> createTweetsFromProcessInstances(List<ProcessInstanceEntity> matchedProcessInstances) {
        List<Tweet> tweets = new ArrayList<>(matchedProcessInstances.size());
        for (ProcessInstanceEntity matchedPI : matchedProcessInstances) {

            extractVariables(tweets,
                             matchedPI);
        }
        return tweets;
    }

    private static void extractVariables(List<Tweet> tweets,
                                         ProcessInstanceEntity matchedPI) {
        ProcessVariableEntity text = getVariableByName(matchedPI,
                                          "text");
        ProcessVariableEntity author = getVariableByName(matchedPI,
                                            "author");
        ProcessVariableEntity lang = getVariableByName(matchedPI,
                                          "lang");
        ProcessVariableEntity timestamp = getVariableByName(matchedPI,
                                               "timestamp");
        ProcessVariableEntity attitude = getVariableByName(matchedPI,
                                              "attitude");

        if (text != null && author != null) {
            tweets.add(new Tweet(text.getValue(),
                                 author.getValue(),
                                 (lang != null) ? lang.getValue() : "",
                                 (attitude != null) ? attitude.getValue() : "",
                                 (timestamp != null) ? new Long(timestamp.getValue().toString()) : new Long(0)));
        }
    }

    private static ProcessVariableEntity getVariableByName(ProcessInstanceEntity pi,
                                              String name) {
        for (ProcessVariableEntity v : pi.getVariables()) {
            if (v.getName().equals(name)) {
                return v;
            }
        }
        return null;
    }
}
