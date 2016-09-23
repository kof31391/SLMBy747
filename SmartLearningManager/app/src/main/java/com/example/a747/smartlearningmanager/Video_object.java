package com.example.a747.smartlearningmanager;

import android.database.sqlite.SQLiteDatabase;

/**
 * Created by lenovo on 22/9/2559.
 */

public class Video_object {
    private String e_code;
    private String e_name;
    private String e_room;
    private String e_date;
    private String e_time;
    private String e_link;
    private String lastMinute;

    public String getE_code(){
        return e_code;
    }

    public String getE_name() {
        return e_name;
    }

    public String getE_room() {
        return e_room;
    }

    public String getE_date() {
        return e_date;
    }

    public String getE_time() {
        return e_time;
    }

    public String getE_link() {
        return e_link;
    }

    public String getLastMinute() {
        return lastMinute;
    }

    public void setE_code(String e_code) {
        this.e_code = e_code;
    }

    public void setE_name(String e_name) {
        this.e_name = e_name;
    }

    public void setE_room(String e_room) {
        this.e_room = e_room;
    }

    public void setE_date(String e_date) {
        this.e_date = e_date;
    }

    public void setE_time(String e_time) {
        this.e_time = e_time;
    }

    public void setE_link(String e_link) {
        this.e_link = e_link;
    }

    public void setLastMinute(String lastMinute) {
        this.lastMinute = lastMinute;
    }

    public void saveInstace(){
        SQLiteDatabase video_db = SQLiteDatabase.openOrCreateDatabase("Video",null);
    }
}