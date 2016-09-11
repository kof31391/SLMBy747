package com.example.a747.smartlearningmanager;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class News extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.news);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String title = extras.getString("title");
            String desc = extras.getString("desc");

            TextView tv_title = (TextView) findViewById(R.id.tv_news_title);
            TextView tv_desc = (TextView) findViewById(R.id.tv_news_desc);

            tv_title.setText(title);
            tv_desc.setText(desc);
        }
    }
        public void gotoHome(View v){
            Intent intent = new Intent(this,Main.class);
            startActivity(intent);
        }
}
