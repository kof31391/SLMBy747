package com.example.a747.smartlearningmanager;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Itemlist {
    public static HashMap<String, List<String>> getData() {
        HashMap<String, List<String>> expandableListDetail = new HashMap<String, List<String>>();

        List<String> monday = new ArrayList<String>();
        monday.add("-");
        monday.add("--");
        monday.add("---");
        monday.add("----");
        monday.add("-----");

        List<String> tuesday = new ArrayList<String>();
        tuesday.add("-");
        tuesday.add("--");
        tuesday.add("---");
        tuesday.add("----");
        tuesday.add("-----");

        List<String> wednesday = new ArrayList<String>();
        wednesday.add("-");
        wednesday.add("--");
        wednesday.add("---");
        wednesday.add("----");
        wednesday.add("-----");

        List<String> thursday = new ArrayList<String>();
        thursday.add("-");
        thursday.add("--");
        thursday.add("---");
        thursday.add("----");
        thursday.add("-----");



        List<String> friday = new ArrayList<String>();
        friday.add("-");
        friday.add("--");
        friday.add("---");
        friday.add("----");
        friday.add("-----");

        List<String> saturday = new ArrayList<String>();
        saturday.add("-");
        saturday.add("--");
        saturday.add("---");
        saturday.add("----");
        saturday.add("-----");

        List<String> sunday = new ArrayList<String>();
        sunday.add("-");
        sunday.add("--");
        sunday.add("---");
        sunday.add("----");
        sunday.add("-----");

        expandableListDetail.put("Sunday", sunday);
        expandableListDetail.put("Saturday", saturday);
        expandableListDetail.put("Friday", friday);
        expandableListDetail.put("Thursday", thursday);
        expandableListDetail.put("Wednesday", wednesday);
        expandableListDetail.put("Tuesday", tuesday);
        expandableListDetail.put("Monday", monday);

        return expandableListDetail;
    }
}