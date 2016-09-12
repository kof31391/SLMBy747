package com.example.a747.smartlearningmanager;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Elearning extends AppCompatActivity {
    List<String> thisyear = new ArrayList<>();
    List<String> all = new ArrayList<>();

    ExpandableListView expandableListView;
    ExpandableListAdapter expandableListAdapter;
    List<String> expandableListTitle;
    HashMap<String, List<String>> expandableListDetail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.elearning);

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

                Intent intent = new Intent(Elearning.this, Subject_elearn.class);
                intent.putExtra("subject",subject);
                Elearning.this.startActivity(intent);

                /*Intent intent = new Intent(Elearning.this, Video_elearning.class);
                intent.putExtra("subject",subject);
                intent.putExtra("room","CB2312");
                intent.putExtra("date","2014-08-05");
                intent.putExtra("time","10-30-00");
                Elearning.this.startActivity(intent);*/

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
    }

    public void getSchedule(){
        SQLiteDatabase mydatabase = openOrCreateDatabase("Schedule",MODE_PRIVATE,null);
        Cursor resultSet = mydatabase.rawQuery("SELECT * FROM Schedule;",null);
        resultSet.moveToFirst();
        while(!resultSet.isAfterLast()){
            thisyear.add(resultSet.getString(resultSet.getColumnIndex("subject_code")));
            resultSet.moveToNext();
        }
        mydatabase.close();
        expandableListDetail  = new HashMap<>();
        expandableListDetail.put("All", all);
        expandableListDetail.put("This Year", thisyear);
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
    public void gotoElean(View v){
        Intent intent = new Intent(this, Elearning.class);
        startActivity(intent);
    }
}
