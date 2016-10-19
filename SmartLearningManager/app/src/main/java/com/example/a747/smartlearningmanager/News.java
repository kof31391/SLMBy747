package com.example.a747.smartlearningmanager;

import android.content.Intent;
import android.net.ConnectivityManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class News extends AppCompatActivity {

    String from;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.news);
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            from = extras.getString("from");
            String title = extras.getString("title");
            String desc = extras.getString("desc");
            String cutAttachment = desc;
            String temp;
            TextView tv_title = (TextView) findViewById(R.id.tv_news_title);
            TextView tv_desc = (TextView) findViewById(R.id.tv_news_desc);
            TextView url = ((TextView)findViewById(R.id.tv_news_url));
            tv_title.setText(title);
            if(desc.contains("Attachment")) {
                while (cutAttachment.contains("Attachment")) {
                    cutAttachment = desc.substring(0, desc.indexOf("Attachment"));
                    temp = desc.substring(desc.indexOf("KB") + 2, desc.length());
                    while (temp.contains("KB")) {
                        temp = temp.substring(temp.indexOf("KB") + 2, temp.length());
                    }
                    if(temp.contains("pdf")){
                        temp = temp.replace("pdf","pdf\n");
                    }
                    tv_desc.setText(cutAttachment);
                    url.setText(Html.fromHtml(temp));
                    url.setClickable(true);
                    url.setMovementMethod(LinkMovementMethod.getInstance());
                }
            }else{
                tv_desc.setText(desc);
            }
        }
    }

    public void gotoHome(View v){
        Intent intent;
        if(from.equalsIgnoreCase("Main")) {
            intent = new Intent(this, Main.class);
        }else{
            intent = new Intent(this, Page_news.class);
        }
        startActivity(intent);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)  {
        if (Integer.parseInt(android.os.Build.VERSION.SDK) > 5
                && keyCode == KeyEvent.KEYCODE_BACK
                && event.getRepeatCount() == 0) {
            Log.d("CDA", "onKeyDown Called");
            Intent intent;
            if(from.equalsIgnoreCase("Main")) {
                intent = new Intent(this, Main.class);
            }else{
                intent = new Intent(this, Page_news.class);
            }
            startActivity(intent);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
