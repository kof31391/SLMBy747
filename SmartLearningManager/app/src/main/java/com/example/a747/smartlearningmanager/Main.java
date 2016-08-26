package com.example.a747.smartlearningmanager;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import org.w3c.dom.Text;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class Main extends AppCompatActivity {

    TextView news0,news1,news2,news3,news4,news5,news6,news7,news8,news9;
    ArrayList al_desc;
    ArrayList al_title;

    private String finalUrl="http://www4.sit.kmutt.ac.th/student/bsc_it_feed";
    private HandleXML obj;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

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
    public void gotoTodo(View v){
        Intent intent = new Intent(Main.this, Todo.class);
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
