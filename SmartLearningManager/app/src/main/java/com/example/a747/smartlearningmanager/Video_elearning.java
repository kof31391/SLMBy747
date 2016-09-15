package com.example.a747.smartlearningmanager;

import android.app.MediaRouteButton;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.VideoView;

import org.w3c.dom.Text;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;


public class Video_elearning extends AppCompatActivity {
    ProgressDialog preload_dialog;
    VideoView video_display;
    SeekBar seekBar;
    TextView timing;
    int total_time;
    boolean isFullScreen = false;
    String elearning_code = "";
    String elearning_name = "";
    String elearning_room = "";
    String elearning_date = "";
    String elearning_time = "";
    String elearning_link = "";

    private Runnable onEverySecond=new Runnable() {

        @Override
        public void run() {

            if(seekBar != null) {
                seekBar.setProgress(video_display.getCurrentPosition());
            }

            if(video_display.isPlaying()) {
                seekBar.postDelayed(onEverySecond, 1000);
            }

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.video_elearning);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            elearning_code = extras.getString("code");
            elearning_name = extras.getString("name");
            elearning_room = extras.getString("room");
            elearning_date = extras.getString("date");
            elearning_time = extras.getString("time");
            elearning_link = extras.getString("link");
        }

        /*Start preload*/
        preload_dialog = new ProgressDialog(this);
        preload_dialog.setTitle("Prepare Video Streaming");
        preload_dialog.setMessage("Buffering...");
        preload_dialog.setIndeterminate(false);
        preload_dialog.setCancelable(false);
        preload_dialog.show();
        /*End preload*/

        /*Setup progressbar*/
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
                    // this is when actually seekbar has been seeked to a new position
                    video_display.seekTo(progress);
                }
                progress = (Math.round(progress/stepSize))*stepSize;
                seekBar.setProgress(progress);
                timecur = video_display.getCurrentPosition();
                SimpleDateFormat sdf =  new SimpleDateFormat("mm:ss");
                if(video_display.getCurrentPosition()>=3600000){
                    hour ++;
                    sdf = new SimpleDateFormat(hour+":"+"mm:ss");
                }
                timing.setText(sdf.format(timecur));
            }
        });
        /*Setup detail*/
        TextView e_subject = (TextView) findViewById(R.id.video_subcode);
        e_subject.setText(elearning_code+" - "+elearning_name);
        TextView e_room = (TextView) findViewById(R.id.video_room);
        e_room.setText(elearning_room);
        TextView e_date = (TextView) findViewById(R.id.video_date);
        e_date.setText(elearning_date);
        TextView e_time = (TextView) findViewById(R.id.video_time);
        e_time.setText(elearning_time);

        /*Setup Timing*/
        timing = (TextView) findViewById(R.id.timing);

        /*Setup video*/
        video_display = (VideoView) findViewById(R.id.video_display);
        video_display.setVideoURI(Uri.parse("http://54.169.58.93:80/video_elearning/"+elearning_link));

        /*Setup layout video*/
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT) ;
        video_display.setLayoutParams(params);
        video_display.requestFocus();
        video_display.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {

                seekBar.setMax(video_display.getDuration());
                seekBar.postDelayed(onEverySecond, 1000);
                preload_dialog.dismiss();
                video_display.start();
                total_time = video_display.getDuration();
                video_display.getCurrentPosition();

            }
        });
    }

    public void pause(View v){
        if(video_display.isPlaying()){
            video_display.pause();
            ImageButton btn_play = (ImageButton) findViewById(R.id.btnPlay);
            btn_play.setImageResource(R.drawable.play);
        }else{
            video_display.start();
            ImageButton btn_play = (ImageButton) findViewById(R.id.btnPlay);
            btn_play.setImageResource(R.drawable.pause);
        }
    }

    public void videoFullscreen(View v){
        /*LinearLayout f_layout = (LinearLayout) findViewById(R.id.VideoSurfaceView);
        android.widget.LinearLayout.LayoutParams params = (android.widget.LinearLayout.LayoutParams) f_layout.getLayoutParams();*/
        if(isFullScreen == false) {
          /*  DisplayMetrics metrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(metrics);
            params.width = metrics.widthPixels;
            params.height = metrics.heightPixels;
            params.leftMargin = 0;
            video_display.setLayoutParams(params);*/
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            isFullScreen = true;
        }else{
          /*  DisplayMetrics metrics = new DisplayMetrics(); getWindowManager().getDefaultDisplay().getMetrics(metrics);
            params.width =  (int) (300*metrics.density);
            params.height = (int) (250*metrics.density);
            params.leftMargin = 30;
            video_display.setLayoutParams(params);*/
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            isFullScreen = false;
        }
    }

    public void gotoSubject_elearn(View v) {
        Intent intent = new Intent(this, Subject_elearn.class);
        intent.putExtra("subject",elearning_code);
        Intent temp = getIntent();
        String from = temp.getExtras().getString("from");
        intent.putExtra("from",from);
        startActivity(intent);
    }
}
