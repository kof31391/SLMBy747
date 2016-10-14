package com.example.a747.smartlearningmanager;

import android.os.Parcel;
import android.os.Parcelable;


import java.io.Serializable;
import java.util.Arrays;
import java.util.Date;

/**
 * Created by 747 on 27-Aug-16.
 */
public class todoObj implements Parcelable,Serializable,Comparable<todoObj>{
    private String topic;
    private String desc;
    private Date date;
    private String category;
    private boolean isFinish;
    private int notiId;

    public int getNotiId() {
        return notiId;
    }

    public void setNotiId(int notiId) {
        this.notiId = notiId;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public todoObj() {
        isFinish = false;
    }

    public boolean isFinish() {
        return isFinish;
    }

    public void setFinish(boolean finish) {
        isFinish = finish;
    }

    @Override
    public int compareTo(todoObj o) {
        return getDate().compareTo(o.getDate());
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }


    public static final Creator<todoObj> CREATOR = new Creator<todoObj>() {
        @Override
        public todoObj createFromParcel(Parcel in) {
            return new todoObj(in);
        }

        @Override
        public todoObj[] newArray(int size) {
            return new todoObj[size];
        }
    };

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public todoObj(Parcel in) {
        this.topic = in.readString();
        this.desc = in.readString();
        this.category = in.readString();
        this.date = (Date)in.readSerializable();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(topic);
        dest.writeString(desc);
        dest.writeString(category);
        dest.writeSerializable(date);
    }

    public int getPosition(){
        String[] categories = {"Homework","Payment","Meeting","Appointment","Others"};
        int pos =  Arrays.asList(categories).indexOf(this.category);
        return pos;
    }

    @Override
    public String toString() {
        return "todoObj{" +
                "topic='" + topic + '\'' +
                ", desc='" + desc + '\'' +
                ", date=" + date +
                ", category='" + category + '\'' +
                '}';
    }
}
