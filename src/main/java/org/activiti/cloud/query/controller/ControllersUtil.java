package org.activiti.cloud.query.controller;

import java.util.ArrayList;
import java.util.List;

import org.activiti.cloud.query.model.Tweet;
import org.activiti.cloud.services.query.model.ProcessInstance;
import org.activiti.cloud.services.query.model.Variable;
import org.springframework.data.domain.Page;

public class ControllersUtil {

    public static List<Tweet> createTweetsFromProcessInstances(Page<ProcessInstance> matchedProcessInstances) {
        List<Tweet> tweets = new ArrayList<>(matchedProcessInstances.getSize());
        for (ProcessInstance matchedPI : matchedProcessInstances.getContent()) {

            extractVariables(tweets,
                             matchedPI);
        }
        return tweets;
    }

    public static List<Tweet> createTweetsFromProcessInstances(List<ProcessInstance> matchedProcessInstances) {
        List<Tweet> tweets = new ArrayList<>(matchedProcessInstances.size());
        for (ProcessInstance matchedPI : matchedProcessInstances) {

            extractVariables(tweets,
                             matchedPI);
        }
        return tweets;
    }

    private static void extractVariables(List<Tweet> tweets,
                                         ProcessInstance matchedPI) {
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

    private static Variable getVariableByName(ProcessInstance pi,
                                              String name) {
        for (Variable v : pi.getVariables()) {
            if (v.getName().equals(name)) {
                return v;
            }
        }
        return null;
    }
}
