package com.example.a747.smartlearningmanager;

import android.app.Notification;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInstaller;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;

public class more_setting extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", 0);
        String std_id = pref.getString("std_id", null);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        if(std_id != null){
            super.onCreate(savedInstanceState);
            setContentView(R.layout.more_setting);
        }else{
            Intent intent = new Intent(this, Login.class);
            startActivity(intent);
        }
        TextView tv_ms_std_id = (TextView) findViewById(R.id.tv_ms_std_id);
        tv_ms_std_id.setText(std_id);
    }
    protected void setMute(){
        Notification notification = new Notification();
        notification.defaults = 0;
        notification.defaults |= Notification.DEFAULT_VIBRATE;
    }

    public void gotoProfile(View v){
        Intent intent = new Intent(this, Profile.class);
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

    public void gotoElean(View v){
        Intent intent = new Intent(this, Elearning.class);
        startActivity(intent);
    }

    public void gotoAbout(View v){
        Intent intent = new Intent(this,About.class);
        startActivity(intent);
    }
    public void gotopagenews(View v){
        Intent intent = new Intent(this, Page_news.class);
        startActivity(intent);
    }

    public void logout(View v){
        SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", 0);
        SharedPreferences.Editor editor = pref.edit();
        editor.clear();
        editor.commit();

        SharedPreferences pref2 = getApplicationContext().getSharedPreferences("Initial", 0);
        SharedPreferences.Editor editor2 = pref2.edit();
        editor2.clear();
        editor2.commit();

        Intent intent = new Intent(this, Login.class);
        startActivity(intent);
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)  {

        if (Integer.parseInt(android.os.Build.VERSION.SDK) > 5
                && keyCode == KeyEvent.KEYCODE_BACK
                && event.getRepeatCount() == 0) {
            Log.d("CDA", "onKeyDown Called");
            SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", 0);
            SharedPreferences.Editor editor = pref.edit();
            editor.clear();
            editor.commit();
            Intent setIntent = new Intent(Intent.ACTION_MAIN);
            setIntent.addCategory(Intent.CATEGORY_HOME);
            setIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(setIntent);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
