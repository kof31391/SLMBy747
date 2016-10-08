package com.example.a747.smartlearningmanager;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Point;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import static android.content.pm.ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE;

public class Video extends AppCompatActivity {
    private String std_id;
    private String subject_id;
    private String e_code;
    private String e_name;
    private String e_room;
    private String e_date;
    private String e_time;
    private String e_count;
    private String e_link;
    private String lecturer;
    private int lastMinute = 0;
    private ProgressDialog preload_dialog;
    private SeekBar seekBar;
    private VideoView video_view;
    private TextView timing;
    private Video_object video_object;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.video);

        SharedPreferences pref = getApplicationContext().getSharedPreferences("Student", 0);
        std_id = pref.getString("std_id", null);

        setVideo(getResources().getConfiguration());

        /*SeekBar*/
        seekBar = (SeekBar) findViewById(R.id.video_seekBar);
        seekBar.setProgress(0);
        seekBar.setMax(100);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,boolean fromUser) {
                int hour = 0;
                int timecur;
                int stepSize = 1000;
                if(fromUser) {
                    video_view.seekTo(progress);
                }
                progress = (Math.round(progress/stepSize))*stepSize;
                seekBar.setProgress(progress);
                timecur = video_view.getCurrentPosition();
                SimpleDateFormat sdf =  new SimpleDateFormat("mm:ss");
                if(video_view.getCurrentPosition()>=3600000){
                    hour ++;
                    sdf = new SimpleDateFormat(hour+":"+"mm:ss");
                }
                timing.setText(sdf.format(timecur));
            }
        });
        /*Set seekbar layout*/
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams((width*50)/100,LinearLayout.LayoutParams.MATCH_PARENT);
        seekBar.setLayoutParams(lp);

        /*Play continues*/
        video_view.seekTo(video_object.getLastMinute());
    }

    protected void setVideoDetail(){
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            subject_id = extras.getString("id");
            e_code = extras.getString("code");
            lecturer = extras.getString("lecturer");
            e_name = extras.getString("name");
            e_room = extras.getString("room");
            e_date = extras.getString("date");
            e_time = extras.getString("time");
            e_count = extras.getString("count");
            e_link = extras.getString("link");

            video_object = new Video_object(this);
            video_object.setE_code(e_code);
            video_object.setE_name(e_name);
            video_object.setE_room(e_room);
            video_object.setE_date(e_date);
            video_object.setE_time(e_time);
            video_object.setE_link(e_link);

            if(video_object.readInstace(e_code,e_date,e_time)){
                e_code = video_object.getE_code();
                e_name = video_object.getE_name();
                e_room = video_object.getE_room();
                e_date = video_object.getE_date();
                e_time = video_object.getE_time();
                e_link = video_object.getE_link();
                lastMinute = video_object.getLastMinute();
            }else{
                video_object.saveInstace();
            }
        }
        TextView tv_subject = (TextView) findViewById(R.id.video_detail_subcode);
        tv_subject.setText(e_code+" - "+e_name);
        TextView tv_lecturer = (TextView) findViewById(R.id.video_detail_lecturer);
        tv_lecturer.setText(lecturer);
        TextView tv_room = (TextView) findViewById(R.id.video_detail_room);
        tv_room.setText(e_room);
        TextView tv_date = (TextView) findViewById(R.id.video_detail_date);
        String date_temp = e_date;
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        try {
            java.util.Date date = df.parse(date_temp);
            df = new SimpleDateFormat("dd/MM/yyyy");
            date_temp = df.format(date);
        }catch (ParseException e){
            e.printStackTrace();
        }
        tv_date.setText(date_temp);
        TextView tv_time = (TextView) findViewById(R.id.video_detail_time);
        tv_time.setText(e_time.substring(0,5));
        TextView tv_count = (TextView) findViewById(R.id.video_detail_count);
        long temp = Long.valueOf(e_count);
        temp++;
        tv_count.setText(String.valueOf(temp));
    }

    protected void getPreload() {
        preload_dialog = new ProgressDialog(this);
        preload_dialog.setTitle("Prepare Video Streaming");
        preload_dialog.setMessage("Buffering...");
        preload_dialog.setIndeterminate(false);
        preload_dialog.setCancelable(false);
        preload_dialog.show();
    }

    protected void autoHideControl(){
        Handler handler=new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if(video_view.isPlaying()){
                    LinearLayout video_controller = (LinearLayout)findViewById(R.id.video_controller);
                    video_controller.setVisibility(View.INVISIBLE);
                }
            }
        },5000);
    }

    protected void autoShowControl(View v){
        LinearLayout video_controller = (LinearLayout)findViewById(R.id.video_controller);
        video_controller.setVisibility(View.VISIBLE);
        autoHideControl();
    }

    protected void pause(View v){
        if(video_view.isPlaying()){
            video_view.pause();
            ImageButton btn_play = (ImageButton) findViewById(R.id.btnPlay);
            btn_play.setImageResource(R.drawable.play);
        }else{
            video_view.start();
            ImageButton btn_play = (ImageButton) findViewById(R.id.btnPlay);
            btn_play.setImageResource(R.drawable.pause);
        }
    }

    protected void fullscreen(View v){
        Configuration configuration = getResources().getConfiguration();
        super.onConfigurationChanged(configuration);
        if(configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
            video_object.setLastMinute(seekBar.getProgress());
            video_object.saveInstace();
            video_view.pause();
            setRequestedOrientation(SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
        }else{
            video_object.setLastMinute(seekBar.getProgress());
            video_object.saveInstace();
            video_view.pause();
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
        }
    }

    private Runnable onEverySecond = new Runnable() {
        @Override
        public void run() {
            if(seekBar != null) {
                seekBar.setProgress(video_view.getCurrentPosition());
            }
            if(video_view.isPlaying()) {
                seekBar.postDelayed(onEverySecond, 1000);
            }
        }
    };

    private void setVideo(Configuration configuration) {
        try {
            super.onConfigurationChanged(configuration);
            if(configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
                ImageButton btn_fullscreen = (ImageButton) findViewById(R.id.btnFullscreen);
                btn_fullscreen.setImageResource(R.drawable.fullscreen);
                setVideoDetail();
                //getPreload();
                timing = (TextView) findViewById(R.id.timing);
                video_view = (VideoView) findViewById(R.id.video_view);
                video_view.setVideoURI(Uri.parse("http://54.169.58.93:80/video_elearning/" + e_link));
                video_view.requestFocus();
                video_view.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mp) {
                        //preload_dialog.dismiss();
                        seekBar.setMax(video_view.getDuration());
                        seekBar.postDelayed(onEverySecond, 1000);
                        video_view.getCurrentPosition();
                        video_view.start();
                        autoHideControl();
                    }
                });
            }else{
                /*Set layout fullscreen*/
                RelativeLayout rl_video_titlebar = (RelativeLayout) findViewById(R.id.video_titlebar);
                rl_video_titlebar.setVisibility(View.INVISIBLE);
                RelativeLayout rl_video_detail = (RelativeLayout) findViewById(R.id.video_detail);
                rl_video_detail.setVisibility(View.INVISIBLE);
                RelativeLayout rl_video_display = (RelativeLayout) findViewById(R.id.video_display);
                rl_video_display.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT));
                /*End set layout fullscreen*/
                ImageButton btn_fullscreen = (ImageButton) findViewById(R.id.btnFullscreen);
                btn_fullscreen.setImageResource(R.drawable.fullscreen_exit);

                setVideoDetail();
                //getPreload();
                timing = (TextView) findViewById(R.id.timing);
                video_view = (VideoView) findViewById(R.id.video_view);
                video_view.setVideoURI(Uri.parse("http://54.169.58.93:80/video_elearning/" + e_link));
                video_view.requestFocus();
                video_view.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mp) {
                        //preload_dialog.dismiss();
                        seekBar.setMax(video_view.getDuration());
                        seekBar.postDelayed(onEverySecond, 1000);
                        video_view.getCurrentPosition();
                        video_view.start();
                        autoHideControl();
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void gotoSubjectElearn(View v) {
        video_object.setLastMinute(seekBar.getProgress());
        video_object.saveInstace();
        Intent intent = new Intent(this, Subject_elearn.class);
        intent.putExtra("subject",e_code);
        Intent temp = getIntent();
        String from = temp.getExtras().getString("from");
        intent.putExtra("from",from);
        startActivity(intent);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)  {
        if (Integer.parseInt(android.os.Build.VERSION.SDK) > 5
                && keyCode == KeyEvent.KEYCODE_BACK
                && event.getRepeatCount() == 0) {
            Log.d("CDA", "onKeyDown Called");
            video_object.setLastMinute(seekBar.getProgress());
            video_object.saveInstace();
            Intent intent = new Intent(this, Subject_elearn.class);
            intent.putExtra("subject",e_code);
            Intent temp = getIntent();
            String from = temp.getExtras().getString("from");
            intent.putExtra("from",from);
            startActivity(intent);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
