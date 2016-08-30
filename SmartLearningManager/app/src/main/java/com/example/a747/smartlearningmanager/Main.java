package com.example.a747.smartlearningmanager;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutCompat;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Text;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class Main extends AppCompatActivity {

    TextView news0,news1,news2,news3,news4,news5,news6,news7,news8,news9;
    ArrayList al_desc;
    ArrayList al_title;

    private String finalUrl="http://www4.sit.kmutt.ac.th/student/bsc_it_feed";
    private HandleXML obj;
    private String std_id;

    private int lastest_news = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", 0);
        std_id = pref.getString("std_id", null);
        if(std_id != null){
            super.onCreate(savedInstanceState);
            setContentView(R.layout.main);
        }else{
            Intent intent = new Intent(this, Login.class);
            startActivity(intent);
        }

        getNews();
        getSchedule(std_id);
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)  {
        if (Integer.parseInt(android.os.Build.VERSION.SDK) > 5
                && keyCode == KeyEvent.KEYCODE_BACK
                && event.getRepeatCount() == 0) {
            Log.d("CDA", "onKeyDown Called");
            Intent setIntent = new Intent(Intent.ACTION_MAIN);
            setIntent.addCategory(Intent.CATEGORY_HOME);
            setIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(setIntent);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    public void getNews(){
        obj = new HandleXML(finalUrl);
        obj.fetchXML();
        al_title = obj.getAl_title();
        al_desc = obj.getAl_desc();
        while(obj.parsingComplete);

        TableLayout tl_news = (TableLayout) findViewById(R.id.tl_news);
        TableRow.LayoutParams params1 = new TableRow.LayoutParams(TableRow.LayoutParams.FILL_PARENT, TableRow.LayoutParams.WRAP_CONTENT);
        TableRow.LayoutParams params2=new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT);
        int count = 7;
        for(int i=al_title.size()-1;i>=0;i--){
            TableRow row = new TableRow(this);
            TextView title = new TextView(this);
            if(count > 0) {
                title.setId(i);
                title.setClickable(true);
                title.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onClickNews(v);
                    }
                });
                title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 23);
                title.setPadding(20, 20, 0, 20);
                if ((i % 2) == 0) {
                    title.setBackgroundColor(Color.parseColor("#E6E6E6"));
                }
                title.setText(al_title.get(i).toString());
                title.setLayoutParams(params1);
                row.addView(title);
                row.setLayoutParams(params2);
                tl_news.addView(row);
                count--;
            }else{
                //More News
                title.setId(i);
                title.setClickable(true);
                lastest_news = count+1;
                title.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onClickMoreNews(v);
                    }
                });
                title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 23);
                title.setPadding(500, 20, 0, 50);
                if ((i % 2) == 0) {
                    title.setBackgroundColor(Color.parseColor("#E6E6E6"));
                }
                title.setText("More");
                title.setLayoutParams(params1);
                row.addView(title);
                row.setLayoutParams(params2);
                tl_news.addView(row);
                break;
            }
        }
    }
    public void getSchedule(String std_id){
        class GetDataJSON extends AsyncTask<String,Void,String> {
            HttpURLConnection urlConnection = null;
            public String strJSON;
            protected String doInBackground(String... params) {
                try {
                    URL url = new URL("http://54.169.58.93/Schedule.php?std_id="+params[0]);
                    urlConnection = (HttpURLConnection) url.openConnection();
                    int code = urlConnection.getResponseCode();
                    if(code==200){
                        InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                        if (in != null) {
                            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in));
                            String line;
                            while ((line = bufferedReader.readLine()) != null)
                                strJSON = line;
                        }
                        in.close();
                    }
                    return strJSON;
                }catch (Exception e){
                    e.printStackTrace();
                }finally {
                    urlConnection.disconnect();
                }
                return strJSON;
            }
            protected void onPostExecute(String strJSON) {
                TextView tv_scheduleToday;
                try{
                    JSONArray data = new JSONArray(strJSON);
                    for(int i = 0; i < data.length(); i++) {
                        JSONObject c = data.getJSONObject(i);
                        int findViewId = i+1;
                        TextView tv_scheduleToday_code2 = (TextView) findViewById(R.id.tv_scheduleToday_code2);
                        TextView tv_scheduleToday_name2 = (TextView) findViewById(R.id.tv_scheduleToday_name2);
                        TextView tv_scheduleToday_room2 = (TextView) findViewById(R.id.tv_scheduleToday_room2);
                        TextView tv_scheduleToday_ts2 = (TextView) findViewById(R.id.tv_scheduleToday_ts2);
                        TextView tv_scheduleToday_te2 = (TextView) findViewById(R.id.tv_scheduleToday_te2);
                        if(findViewId == 1){
                            tv_scheduleToday_code2.setVisibility(View.GONE);
                            tv_scheduleToday_name2.setVisibility(View.GONE);
                            tv_scheduleToday_room2.setVisibility(View.GONE);
                            tv_scheduleToday_ts2.setVisibility(View.GONE);
                            tv_scheduleToday_te2.setVisibility(View.GONE);
                        }else{
                            tv_scheduleToday_code2.setVisibility(View.VISIBLE);
                            tv_scheduleToday_name2.setVisibility(View.VISIBLE);
                            tv_scheduleToday_room2.setVisibility(View.VISIBLE);
                            tv_scheduleToday_ts2.setVisibility(View.VISIBLE);
                            tv_scheduleToday_te2.setVisibility(View.VISIBLE);
                        }
                        String name = "tv_scheduleToday_code"+findViewId;
                        int id = getResources().getIdentifier(name, "id", getPackageName());
                        if (id != 0) {
                            tv_scheduleToday = (TextView) findViewById(id);
                            tv_scheduleToday.setText(c.getString("subject_code"));
                        }
                        name = "tv_scheduleToday_name"+findViewId;
                        id = getResources().getIdentifier(name, "id", getPackageName());
                        if(id != 0){
                            tv_scheduleToday = (TextView) findViewById(id);
                            tv_scheduleToday.setText(c.getString("subject_name"));
                        }
                        name = "tv_scheduleToday_room"+findViewId;
                        id = getResources().getIdentifier(name, "id", getPackageName());
                        if(id != 0){
                            tv_scheduleToday = (TextView) findViewById(id);
                            tv_scheduleToday.setText(c.getString("subject_room"));
                        }
                        name = "tv_scheduleToday_ts"+findViewId;
                        id = getResources().getIdentifier(name, "id", getPackageName());
                        if(id != 0){
                            tv_scheduleToday = (TextView) findViewById(id);
                            tv_scheduleToday.setText(c.getString("subject_time_start"));
                        }
                        name = "tv_scheduleToday_te"+findViewId;
                        id = getResources().getIdentifier(name, "id", getPackageName());
                        if(id != 0){
                            tv_scheduleToday = (TextView) findViewById(id);
                            tv_scheduleToday.setText(c.getString("subject_time_ended"));
                        }
                    }
                }catch(Exception e){
                    e.printStackTrace();
                }
            }
        }
        new GetDataJSON().execute(std_id);
    }
    public void refreshSchedule(View v){
        getSchedule(std_id);
    }

    public void onClickNews(View v){
        int idv = v.getId();
        String title = al_title.get(idv).toString();
        String desc = android.text.Html.fromHtml(al_desc.get(idv).toString()).toString();
        Intent intent = new Intent(Main.this, News.class);
        intent.putExtra("title",title);
        intent.putExtra("desc", desc);
        startActivity(intent);
    }
    public void onClickMoreNews(View v){
        if(lastest_news >= 0) {
            for (int i = 0; i < 4; i++) {
                TableLayout tl_news = (TableLayout) findViewById(R.id.tl_news);
                TextView remove_more_news = (TextView) findViewById(v.getId());
                remove_more_news.setVisibility(v.GONE);
                TableRow row = new TableRow(this);
                TextView title = new TextView(this);
                if (lastest_news >= 0) {
                    title.setId(lastest_news);
                    title.setClickable(true);
                    title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 23);
                    title.setPadding(20, 20, 0, 20);
                    if ((lastest_news % 2) == 1) {
                        title.setBackgroundColor(Color.parseColor("#E6E6E6"));
                    }
                    title.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onClickNews(v);
                        }
                    });
                    title.setText(al_title.get(lastest_news).toString());
                    row.addView(title);
                    tl_news.addView(row);
                    lastest_news--;
                } else {
                    if(lastest_news > 0) {
                        //More News
                        title.setId(i);
                        title.setClickable(true);
                        title.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                onClickMoreNews(v);
                            }
                        });
                        title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 23);
                        title.setPadding(500, 20, 0, 50);
                        if ((i % 2) == 0) {
                            title.setBackgroundColor(Color.parseColor("#E6E6E6"));
                        }
                        title.setText("More");
                        row.addView(title);
                        tl_news.addView(row);
                        break;
                    }
                }
            }
        }
    }
    public void gotoTodo(View v){
        Intent intent = new Intent(this, Todo.class);
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

}
//สำรหบัดึงหน้าเว็บ RSS ที่เป็นแบบ XML
class HandleXML {
    private String urlString = null;
    private XmlPullParserFactory xmlFactoryObject;
    public volatile boolean parsingComplete = true;

    ArrayList al_title = new ArrayList();
    ArrayList al_desc = new ArrayList();
    int count = 0;

    public ArrayList getAl_desc(){
        return al_desc;
    }
    public ArrayList getAl_title(){ return al_title; }

    public void parseXMLAndStoreIt(XmlPullParser myParser) {

        int event;
        String text = null;

        try {
            event = myParser.getEventType();
            while (event != XmlPullParser.END_DOCUMENT) {
                String name=myParser.getName();
                switch (event){
                    case XmlPullParser.START_TAG:
                        break;
                    case XmlPullParser.TEXT:
                        text = myParser.getText();
                        break;
                    case XmlPullParser.END_TAG:
                        if(name.equals("title")){
                            if(count < 10) {
                                al_title.add(text);
                                count++;
                            }else{
                                break;
                            }
                        }else if(name.equals("description")){
                            al_desc.add(text);
                        }
                        else{
                        }
                        break;
                }
                event = myParser.next();
            }
            parsingComplete = false;
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public HandleXML(String url){
        this.urlString = url;
    }
    public void fetchXML(){
        Thread thread = new Thread(new Runnable(){
            @Override
            public void run() {

                try {
                    URL url = new URL(urlString);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                    conn.setReadTimeout(10000 /* milliseconds */);
                    conn.setConnectTimeout(15000 /* milliseconds */);
                    conn.setRequestMethod("GET");
                    conn.setDoInput(true);

                    // Starts the query
                    conn.connect();
                    InputStream stream = conn.getInputStream();

                    xmlFactoryObject = XmlPullParserFactory.newInstance();
                    XmlPullParser myparser = xmlFactoryObject.newPullParser();

                    myparser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
                    myparser.setInput(stream, null);

                    parseXMLAndStoreIt(myparser);
                    stream.close();
                }
                catch (Exception e) {
                }
            }
        });
        thread.start();
    }

}