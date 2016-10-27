package com.example.a747.smartlearningmanager;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class News extends AppCompatActivity {
    private String host = "http://54.169.58.93/";
    private String from;
    private Boolean updateStatus = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.news);
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            from = extras.getString("from");
            String title = extras.getString("title");
            RSS_UpdateCount(title);
            String desc = extras.getString("desc");
            String cutAttachment = desc;
            String pubDate = extras.getString("pubDate");
            DateFormat inputPattern = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            DateFormat outputPattern = new SimpleDateFormat("dd/MM/yyyy HH:mm");
            Date date = null;
            String sDate = null;
            try{
                date = inputPattern.parse(pubDate);
                sDate = outputPattern.format(date);
            }catch (Exception e){
                e.printStackTrace();
            }
            String temp;
            TextView tv_title = (TextView) findViewById(R.id.tv_news_title);
            TextView tv_desc = (TextView) findViewById(R.id.tv_news_desc);
            TextView url = ((TextView)findViewById(R.id.tv_news_url));
            TextView tv_pubDate = (TextView) findViewById(R.id.tv_news_pubDate);
            tv_title.setText(title);
            tv_pubDate.setText(sDate);
            GradientDrawable gd = new GradientDrawable();
            gd.setColor(Color.parseColor("#BEBEBE"));
            gd.setCornerRadius(8);
            gd.setStroke(1, 0xFF000000);
            tv_title.setBackgroundDrawable(gd);
            if(desc.contains("Attachment")) {
                while (cutAttachment.contains("Attachment")) {
                    cutAttachment = desc.substring(0, desc.indexOf("Attachment"));
                    temp = desc.substring(desc.indexOf("KB") + 2, desc.length());
                    while (temp.contains("KB")) {
                        temp = temp.substring(temp.indexOf("KB") + 2, temp.length());
                    }
                    tv_desc.setText(cutAttachment);
                    url.setText(Html.fromHtml(temp));
                    url.setClickable(true);
                    url.setMovementMethod(LinkMovementMethod.getInstance());
                }

            }else{
                tv_desc.setText(Html.fromHtml(desc));
                tv_desc.setClickable(true);
                tv_desc.setMovementMethod(LinkMovementMethod.getInstance());
            }
            if(tv_desc.getText().length() == 1){
                tv_desc.setVisibility(View.GONE);
            }
        }
    }

    private void RSS_UpdateCount(String title){
        class GetDataJSON extends AsyncTask<String,Void,String> {
            HttpURLConnection urlConnection = null;
            public String strJSON;
            protected String doInBackground(String... params) {
                try {
                    URL url = new URL(host+"RSS_UpdateCount.php?title="+params[0]);
                    urlConnection = (HttpURLConnection) url.openConnection();
                    int code = urlConnection.getResponseCode();
                    if(code==200){
                        updateStatus = true;
                    }else{
                        updateStatus = false;
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }finally {
                    urlConnection.disconnect();
                }
                return strJSON;
            }
            protected void onPostExecute(String strJSON) {
                try{
                    if(updateStatus == true){
                        Toast.makeText(getApplicationContext(),"News updated", Toast.LENGTH_SHORT).show();
                    }else{
                        Toast.makeText(getApplicationContext(),"News not updated", Toast.LENGTH_SHORT).show();
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
        new GetDataJSON().execute(title.replaceAll(" ","%20"));
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
