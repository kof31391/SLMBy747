package com.example.a747.smartlearningmanager;

import android.app.ProgressDialog;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.MediaController;
import android.widget.VideoView;

public class Video_elearning extends AppCompatActivity {
    ProgressDialog pDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.video_elearning);

        final VideoView vv = (VideoView) findViewById(R.id.vv_display);
        try{
            pDialog = new ProgressDialog(this);
            pDialog.setTitle("Prepare Video Streaming");
            pDialog.setMessage("Buffering...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();

            vv.setMediaController(new MediaController(this));
            vv.setVideoURI(Uri.parse("http://techslides.com/demos/sample-videos/small.mp4"));
        }catch(Exception e){
            e.printStackTrace();
        }
        vv.requestFocus();
        vv.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            public void onPrepared(MediaPlayer mp) {
                pDialog.dismiss();
                vv.start();
            }
        });
    }
    public void gotoElearning(View v){
        Intent intent = new Intent(this, Elearning.class);
        startActivity(intent);
    }
}
