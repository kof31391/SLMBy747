package com.example.a747.smartlearningmanager;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", 0);
        SharedPreferences.Editor editor = pref.edit();
        std_id = pref.getString("std_id", null);

        getSchedule(std_id);

        news0 = (TextView) findViewById(R.id.tv_news_1);
        news1 = (TextView) findViewById(R.id.tv_news_1);
        news2 = (TextView) findViewById(R.id.tv_news_2);
        news3 = (TextView) findViewById(R.id.tv_news_3);
        news4 = (TextView) findViewById(R.id.tv_news_4);
        news5 = (TextView) findViewById(R.id.tv_news_5);
        news6 = (TextView) findViewById(R.id.tv_news_6);
        news7 = (TextView) findViewById(R.id.tv_news_7);
        news8 = (TextView) findViewById(R.id.tv_news_8);
        news9 = (TextView) findViewById(R.id.tv_news_9);

        ArrayList al_news = new ArrayList();
        al_news.add(news0);
        al_news.add(news1);
        al_news.add(news2);
        al_news.add(news3);
        al_news.add(news4);
        al_news.add(news5);
        al_news.add(news6);
        al_news.add(news7);
        al_news.add(news8);
        al_news.add(news9);


        obj = new HandleXML(finalUrl);
        obj.fetchXML(al_news);
        al_title = obj.getAl_title();
        al_desc = obj.getAl_desc();

        while(obj.parsingComplete);
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
                            String line = "";
                            while ((line = bufferedReader.readLine()) != null)
                                strJSON = line;
                        }
                        in.close();
                    }
                    System.out.println(strJSON);
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
        GetDataJSON g = (GetDataJSON) new GetDataJSON().execute(std_id);
    }
    public void refreshSchedule(View v){
        getSchedule(std_id);
    }
    public void onClickNews(View v){
        String idn = v.getResources().getResourceName(v.getId());
        int id = Integer.valueOf(idn.substring(idn.length() - 1));
        String title = al_title.get(id).toString();
        String desc = android.text.Html.fromHtml(al_desc.get(id).toString()).toString();
        System.out.println(title);
        System.out.println(desc);
        Intent intent = new Intent(Main.this, News.class);
        intent.putExtra("title",title);
        intent.putExtra("desc", desc);
        startActivity(intent);
    }
    public void gotoMoreNews(View v){
        Intent intent = new Intent(this, More_News.class);
        startActivity(intent);
    }
    public void gotoTodo(View v){
        Intent intent = new Intent(this, Todo_List.class);
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

    public void parseXMLAndStoreIt(XmlPullParser myParser, ArrayList al_news) {

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
                                TextView tf = (TextView) al_news.get(count);
                                tf.setText(text);
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
    public void fetchXML(ArrayList al_news){
        final ArrayList f_al_news = al_news;
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

                    parseXMLAndStoreIt(myparser,f_al_news);
                    stream.close();
                }
                catch (Exception e) {
                }
            }
        });
        thread.start();
    }

}