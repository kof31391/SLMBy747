package com.example.a747.smartlearningmanager;

import android.app.ProgressDialog;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.VideoView;

import java.text.SimpleDateFormat;
import java.util.Date;


public class Video_elearning extends AppCompatActivity {
    ProgressDialog preload_dialog;
    VideoView video_display;
    SeekBar seekBar;
    TextView timing;
    int total_time;
    boolean isFullScreen = false;
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
            public void onProgressChanged(SeekBar seekBar, int progress,
                                          boolean fromUser) {


                SimpleDateFormat sdf = new SimpleDateFormat("mm:ss");
                Date time = new Date();
                int stepSize = 1000;
                int temp;
                if(fromUser) {
                    // this is when actually seekbar has been seeked to a new position
                    video_display.seekTo(progress);
                }
                progress = ((int)Math.round(progress/stepSize))*stepSize;
                seekBar.setProgress(progress);
                temp = progress;
                time.setSeconds(temp);
                timing.setText(sdf.format(time));
            }
        });



        /*Setup Timing*/
        timing = (TextView) findViewById(R.id.timing);

        /*Setup video*/
        video_display = (VideoView) findViewById(R.id.video_display);
        video_display.setVideoURI(Uri.parse("http://techslides.com/demos/sample-videos/small.mp4"));

        /*Setup layout video*/
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT) ;
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
    public void videoFullscreen(View v){
        if(isFullScreen==false) {
            DisplayMetrics metrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(metrics);
            android.widget.FrameLayout.LayoutParams params = (android.widget.FrameLayout.LayoutParams) video_display.getLayoutParams();
            params.width = metrics.widthPixels;
            params.height = metrics.heightPixels;
            params.leftMargin = 0;
            video_display.setLayoutParams(params);
        }else{
            DisplayMetrics metrics = new DisplayMetrics(); getWindowManager().getDefaultDisplay().getMetrics(metrics);
            android.widget.FrameLayout.LayoutParams params = (android.widget.FrameLayout.LayoutParams) video_display.getLayoutParams();
            params.width =  (int) (300*metrics.density);
            params.height = (int) (250*metrics.density);
            params.leftMargin = 30;
            video_display.setLayoutParams(params);
        }
    }
    public void gotoElearning(View v) {
        Intent intent = new Intent(this, Elearning.class);
        startActivity(intent);
    }
}
