package com.example.a747.smartlearningmanager;

import android.app.Dialog;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Elearning extends AppCompatActivity {
    private Boolean isExpand = false;
    private List<String> thisyear = new ArrayList<>();
    private List<String> all = new ArrayList<>();
    private ExpandableListView expandableListView;
    private ExpandableListAdapter expandableListAdapter;
    private List<String> expandableListTitle;
    private HashMap<String, List<String>> expandableListDetail;
    private Map<String,String> mapSubject;
    private Dialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i("INFO", "Loading...");
        dialog = new Dialog(this);
        dialog = getDialogLoading();
        dialog.show();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.elearning);
        if(isNetworkConnected()) {
            getSchedule();

            expandableListView = (ExpandableListView) findViewById(R.id.expandableListView);
            expandableListTitle = new ArrayList<>(expandableListDetail.keySet());
            expandableListAdapter = new CustomExpandableListAdapter(this, expandableListTitle, expandableListDetail);
            expandableListView.setAdapter(expandableListAdapter);
            expandableListView.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {

                @Override
                public void onGroupExpand(int groupPosition) {
                    Toast.makeText(getApplicationContext(),
                            expandableListTitle.get(groupPosition) + " List Expanded.",
                            Toast.LENGTH_SHORT).show();
                }
            });

            expandableListView.setOnGroupCollapseListener(new ExpandableListView.OnGroupCollapseListener() {

                @Override
                public void onGroupCollapse(int groupPosition) {
                    Toast.makeText(getApplicationContext(),
                            expandableListTitle.get(groupPosition) + " List Collapsed.",
                            Toast.LENGTH_SHORT).show();

                }
            });

            expandableListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
                @Override
                public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                    final String subject = (String) expandableListAdapter.getChild(groupPosition, childPosition);
                    switch (subject) {
                        case "B.Sc.IT":
                            gotoAllSubject("B.Sc.IT");
                            break;
                        case "B.Sc.CS":
                            gotoAllSubject("B.Sc.CS");
                            break;
                        case "M.Sc.IT":
                            gotoAllSubject("M.Sc.IT");
                            break;
                        case "M.Sc.EM/M.Sc.BIS":
                            gotoAllSubject("M.Sc.EM/M.Sc.BIS");
                            break;
                        case "M.Sc.SE":
                            gotoAllSubject("M.Sc.SE");
                            break;
                        case "Event":
                            gotoAllSubject("Event");
                            break;
                        default:
                            Intent intent = new Intent(Elearning.this, Subject_elearn.class);
                            intent.putExtra("subject_id", mapSubject.get(subject));
                            intent.putExtra("from", "Elearning");
                            Elearning.this.startActivity(intent);
                            break;
                    }
                    Toast.makeText(
                            getApplicationContext(),
                            expandableListTitle.get(groupPosition)
                                    + " -> "
                                    + expandableListDetail.get(
                                    expandableListTitle.get(groupPosition)).get(
                                    childPosition), Toast.LENGTH_SHORT
                    ).show();
                    return false;
                }
            });
        }else{
            Intent intent = new Intent(this, Main.class);
            startActivity(intent);
        }
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                dialog.cancel();
            }
        }, 1000);
        Log.i("INFO", "Loading complete");
    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null;
    }

    private Dialog getDialogLoading(){
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.loading);
        dialog.setCancelable(true);
        return  dialog;
    }

    public void getSchedule(){
        mapSubject = new HashMap<>();
        SQLiteDatabase Schedule_db = openOrCreateDatabase("Schedule",MODE_PRIVATE,null);
        Cursor resultSet = Schedule_db.rawQuery("SELECT * FROM Subject ORDER BY subject_code;",null);
        resultSet.moveToFirst();
        while(!resultSet.isAfterLast()){
            mapSubject.put(resultSet.getString(resultSet.getColumnIndex("subject_code"))+" - "+resultSet.getString(resultSet.getColumnIndex("subject_name")),resultSet.getString(resultSet.getColumnIndex("subject_id")));
            thisyear.add(resultSet.getString(resultSet.getColumnIndex("subject_code"))+" - "+resultSet.getString(resultSet.getColumnIndex("subject_name")));
            resultSet.moveToNext();
        }
        Schedule_db.close();
        all.add("B.Sc.IT");
        all.add("B.Sc.CS");
        all.add("M.Sc.IT");
        all.add("M.Sc.EM/M.Sc.BIS");
        all.add("M.Sc.SE");
        all.add("Event");
        expandableListDetail  = new HashMap<>();
        expandableListDetail.put("All", all);
        expandableListDetail.put("This semester", thisyear);
    }

    protected void gotoAllSubject(String temp){
        Intent intent = new Intent(Elearning.this, After_allelearn.class);
        intent.putExtra("department",temp);
        intent.putExtra("from","Elearning");
        Elearning.this.startActivity(intent);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (Integer.parseInt(android.os.Build.VERSION.SDK) > 5
                && keyCode == KeyEvent.KEYCODE_BACK
                && event.getRepeatCount() == 0) {
            Log.d("CDA", "onKeyDown Called");
            Intent intent = new Intent(this, Main.class);
            startActivity(intent);
            Log.i("GT", "Go to Main");
        }
        return true;
    }

    public void gotoAbout(View v){
        Intent intent = new Intent(this,About.class);
        startActivity(intent);
    }

    public void gotoTodo(View v){
        Intent intent = new Intent(this, Todo_List.class);
        startActivity(intent);
    }
    public void gotoSetting(View v){
        Intent intent = new Intent(this, more_setting.class);
        startActivity(intent);
    }
    public void gotoHome(View v){
        Intent intent = new Intent(this, Main.class);
        startActivity(intent);
    }
    public void gotoNoti(View v){
        Intent intent = new Intent(this, Noti.class);
        startActivity(intent);
    }

    public void gotopagenews(View v){
        Intent intent = new Intent(this, Page_news.class);
        startActivity(intent);
    }
}
