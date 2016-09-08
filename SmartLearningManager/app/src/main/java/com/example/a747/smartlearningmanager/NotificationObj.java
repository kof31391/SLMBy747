package com.example.a747.smartlearningmanager;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by 747 on 07-Sep-16.
 */
public class NotificationObj implements Comparable<NotificationObj>,Serializable{
    private String topic;
    private String desc;
    private Date date;

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public NotificationObj(todoObj obj) {
        this.topic = obj.getTopic();
        this.desc = obj.getDesc();
        this.date = obj.getDate();
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
