package com.example.a747.smartlearningmanager;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.GridLayout;
import android.widget.TextView;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class More_News extends AppCompatActivity {
    private String finalUrl="http://www4.sit.kmutt.ac.th/student/bsc_it_feed";
    private HandleXML2 obj;
    private ArrayList al_title;
    private ArrayList al_desc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.more__news);

        obj = new HandleXML2(finalUrl);
        obj.fetchXML();

        al_title = obj.getAl_title();
        al_desc = obj.getAl_desc();

        while (obj.parsingComplete) ;
        System.out.println(al_title.size());

        GridLayout gl_list_news = (GridLayout) findViewById(R.id.gl_list_news);
        int y = 0;
        for (int i = al_title.size(); i > al_title.size(); i--) {
            String title = al_title.get(i).toString();
            TextView dynamicTextView = new TextView(this);
            dynamicTextView.setText(" Hello World ");
            gl_list_news.addView(dynamicTextView);
        }
    }
}
//สำรหบัดึงหน้าเว็บ RSS ที่เป็นแบบ XML
class HandleXML2 {
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

    public HandleXML2(String url){
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
