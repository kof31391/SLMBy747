package com.example.a747.smartlearningmanager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by WasanHi on 9/11/2016.
 */
public class Itemelearning {
    public static HashMap<String, List<String>> getData() {
        HashMap<String, List<String>> expandableListDetail = new HashMap<String, List<String>>();

        List<String> thisyear = new ArrayList<String>();
        thisyear.add("-");
        thisyear.add("--");
        thisyear.add("---");
        thisyear.add("----");
        thisyear.add("-----");

        List<String> all = new ArrayList<String>();
        all.add("-");
        all.add("--");
        all.add("---");
        all.add("----");
        all.add("-----");


        expandableListDetail.put("All", all);
        expandableListDetail.put("This Year 1/2556", thisyear);

        return expandableListDetail;
    }
}