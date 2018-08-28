package org.activiti.cloud.query.controller;

import java.util.ArrayList;
import java.util.List;

import org.activiti.cloud.query.model.Tweet;
import org.activiti.cloud.services.query.model.ProcessInstanceEntity;
import org.activiti.cloud.services.query.model.VariableEntity;
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
        VariableEntity text = getVariableByName(matchedPI,
                                          "text");
        VariableEntity author = getVariableByName(matchedPI,
                                            "author");
        VariableEntity lang = getVariableByName(matchedPI,
                                          "lang");
        VariableEntity timestamp = getVariableByName(matchedPI,
                                               "timestamp");
        VariableEntity attitude = getVariableByName(matchedPI,
                                              "attitude");
        if (text != null && author != null) {
            tweets.add(new Tweet(text.getValue(),
                                 author.getValue(),
                                 (lang != null) ? lang.getValue() : "",
                                 (attitude != null) ? attitude.getValue() : "",
                                 (timestamp != null) ? new Long(timestamp.getValue().toString()) : new Long(0)));
        }
    }

    private static VariableEntity getVariableByName(ProcessInstanceEntity pi,
                                              String name) {
        for (VariableEntity v : pi.getVariables()) {
            if (v.getName().equals(name)) {
                return v;
            }
        }
        return null;
    }
}
