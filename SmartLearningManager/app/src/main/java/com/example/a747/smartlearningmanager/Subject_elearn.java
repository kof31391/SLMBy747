package com.example.a747.smartlearningmanager;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class Subject_elearn extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.subject_elearn);
    }

    public void gotoVideo(View v){
        Intent intent = getIntent();
        String subject = intent.getExtras().getString("subject");
        TextView tv = (TextView) v;
        String day = tv.getText().toString();
        String date = day.substring(0,10);
        String time = day.substring(11,13);
        time += "-"+day.substring(14,16);
        intent = new Intent(this,Video_elearning.class);
        intent.putExtra("subject",subject);
        intent.putExtra("room","CB2312");
        intent.putExtra("date",date);
        intent.putExtra("time",time+"-00");
        startActivity(intent);
    }
}
