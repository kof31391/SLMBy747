package com.example.a747.smartlearningmanager;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class Elearning extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.elearning);
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
