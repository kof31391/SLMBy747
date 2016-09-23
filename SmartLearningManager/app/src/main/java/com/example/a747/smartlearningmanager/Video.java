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
import android.view.Display;
import android.view.Gravity;
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
    private String e_link;
    private ProgressDialog preload_dialog;
    private SeekBar seekBar;
    private VideoView video_view;
    private TextView timing;
    private Boolean isFullscreen = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.video);

        SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", 0);
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
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams((width*55)/100,LinearLayout.LayoutParams.MATCH_PARENT);
        seekBar.setLayoutParams(lp);
        try {
            Video_object vdo = Read();
            System.out.println(vdo.toString());
            if (vdo.getE_code().equals("I")) {
                System.out.println("NOOO");
                video_view.seekTo(Read().getLastMinute());
            }
        }catch(Exception e){}
    }


    protected void setVideoDetail(){
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            subject_id = extras.getString("id");
            e_code = extras.getString("code");
            e_name = extras.getString("name");
            e_room = extras.getString("room");
            e_date = extras.getString("date");
            e_time = extras.getString("time");
            e_link = extras.getString("link");
        }
        TextView tv_subject = (TextView) findViewById(R.id.video_detail_subcode);
        tv_subject.setText(e_code+" - "+e_name);
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
        if(isFullscreen == false) {
            Video_object video = new Video_object();
            video.setE_code("I");
            video.setLastMinute(seekBar.getProgress());
            Write(video);
            video_view.pause();
            setRequestedOrientation(SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
            isFullscreen = true;
        }else{
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
            isFullscreen = false;
        }
    }

    private void Write(Video_object v){
        File filesDir = getFilesDir();
        File todoFile = new File(filesDir, "progress.txt");
        try{
            ObjectOutputStream ois = new ObjectOutputStream(new FileOutputStream(todoFile));
            ois.writeObject(v);
            System.out.println("Write");
        }catch(Exception e){
            System.out.println("Err: "+e);
        }
    }

    private Video_object Read() {
        Video_object obj = new Video_object();
        File filesDir = getFilesDir();
        File todoFile = new File(filesDir, "progress.txt");
        try {
            FileInputStream fis = new FileInputStream(todoFile);
            ObjectInputStream ois = new ObjectInputStream(fis);
            obj = (Video_object) ois.readObject();
        } catch (Exception e) {
        }
        return obj;
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
                setVideoDetail();
                getPreload();
                timing = (TextView) findViewById(R.id.timing);
                video_view = (VideoView) findViewById(R.id.video_view);
                video_view.setVideoURI(Uri.parse("http://54.169.58.93:80/video_elearning/" + e_link));
                video_view.requestFocus();
                video_view.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mp) {
                        preload_dialog.dismiss();
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

                setVideoDetail();
                getPreload();
                timing = (TextView) findViewById(R.id.timing);
                video_view = (VideoView) findViewById(R.id.video_view);
                video_view.setVideoURI(Uri.parse("http://54.169.58.93:80/video_elearning/" + e_link));
                video_view.requestFocus();
                video_view.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mp) {
                        preload_dialog.dismiss();
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
        Intent intent = new Intent(this, Subject_elearn.class);
        intent.putExtra("subject",e_code);
        Intent temp = getIntent();
        String from = temp.getExtras().getString("from");
        intent.putExtra("from",from);
        startActivity(intent);
    }
}
