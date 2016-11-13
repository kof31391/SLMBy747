package com.example.a747.smartlearningmanager;

import java.io.Serializable;
import java.util.Date;


public class NotificationObj implements Comparable<NotificationObj>,Serializable{
    private String topic;
    private String desc;
    private Date date;
    private boolean isFinish;
    private String category;

    public boolean isFinish() {
        return isFinish;
    }


    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getDesc() {
        return desc;
    }


    public String getTopic() {
        return topic;
    }


    public NotificationObj(todoObj obj) {
        this.topic = obj.getTopic();
        this.desc = obj.getDesc();
        this.date = obj.getDate();
        this.isFinish = obj.isFinish();
        this.category = obj.getCategory();
    }


    @Override
    public int compareTo(NotificationObj another) {
            return getDate().compareTo(another.getDate());
    }

    @Override
    public String toString() {
        return "NotificationObj{" +
                "topic='" + topic + '\'' +
                ", desc='" + desc + '\'' +
                ", date=" + date +
                '}';
    }
}

