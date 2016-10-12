package com.example.a747.smartlearningmanager;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Switch;
import android.widget.TextView;

public class more_setting extends AppCompatActivity{
    Switch sound;
    Switch vibrate;
    SharedPreferences pref;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        pref = getApplicationContext().getSharedPreferences("Student", 0);
        String std_id = pref.getString("std_id", null);
        if(std_id != null){
            super.onCreate(savedInstanceState);
            setContentView(R.layout.more_setting);
            sound = (Switch)findViewById(R.id.soundSwitch);
            vibrate = (Switch)findViewById(R.id.vibrateSwitch);
            LoadSetting();
        }else{
            Intent intent = new Intent(this, Login.class);
            startActivity(intent);
        }
        TextView tv_ms_std_id = (TextView) findViewById(R.id.tv_ms_std_id);
        tv_ms_std_id.setText(std_id);
    }

    private void LoadSetting(){
            sound.setChecked(pref.getBoolean("sound", false));
            vibrate.setChecked(pref.getBoolean("vibrate", true));
    }

    public void gotoProfile(View v){
        saveSetting();
        Intent intent = new Intent(this, Profile.class);
        startActivity(intent);
    }
    public void gotoTodo(View v){
        saveSetting();
        Intent intent = new Intent(this, Todo_List.class);
        startActivity(intent);
    }

    public void gotoHome(View v){
        saveSetting();
        Intent intent = new Intent(this, Main.class);
        startActivity(intent);
    }

    public void gotoNoti(View v){
        saveSetting();
        Intent intent = new Intent(this, Noti.class);
        startActivity(intent);
    }

    public void gotoElean(View v){
        saveSetting();
        Intent intent = new Intent(this, Elearning.class);
        startActivity(intent);
    }

    public void gotoAbout(View v){
        saveSetting();
        Intent intent = new Intent(this,About.class);
        startActivity(intent);
    }
    public void gotopagenews(View v){
        saveSetting();
        Intent intent = new Intent(this, Page_news.class);
        startActivity(intent);
    }

    private void saveSetting(){
        SharedPreferences pref = getApplicationContext().getSharedPreferences("Student", 0);
        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean("sound",sound.isChecked());
        editor.putBoolean("vibrate",vibrate.isChecked());
        editor.commit();
        finish();
    }

    public void logout(View v){
        SharedPreferences pref = getApplicationContext().getSharedPreferences("Student", 0);
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
            SharedPreferences pref = getApplicationContext().getSharedPreferences("Student", 0);
            SharedPreferences.Editor editor = pref.edit();
            editor.clear();
            editor.commit();

            SharedPreferences pref2 = getApplicationContext().getSharedPreferences("Initial", 0);
            SharedPreferences.Editor editor2 = pref2.edit();
            editor2.clear();
            editor2.commit();

            Intent setIntent = new Intent(Intent.ACTION_MAIN);
            setIntent.addCategory(Intent.CATEGORY_HOME);
            setIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(setIntent);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
