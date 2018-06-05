package org.activiti.cloud.query.model;

import java.io.Serializable;

public class Tweet implements Serializable {

    private String text;
    private String author;
    private String lang;
    private String attitude;
    private long timestamp;

    public Tweet() {
    }

    public Tweet(String text,
                 String author,
                 String lang,
                 String attitude,
                 long timestamp) {
        this.text = text;
        this.author = author;
        this.lang = lang;
        this.attitude = attitude;
        this.timestamp = timestamp;
    }

    public String getText() {
        return text;
    }

    public String getAuthor() {
        return author;
    }

    public String getLang() {
        return lang;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getAttitude() {
        return attitude;
    }
}
