package com.example.a747.smartlearningmanager;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class Page_news extends AppCompatActivity {

    private ArrayList<String> al_desc;
    private ArrayList<String> al_title;
    private Dialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i("INFO", "Loading...");
        dialog = new Dialog(this);
        dialog = getDialogLoading();
        dialog.show();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pagenews);
        if(isNetworkConnected()) {
            getRSS();
        }else{
            Intent intent = new Intent(this, Main.class);
            startActivity(intent);
        }
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                dialog.cancel();
            }
        }, 1000);
        Log.i("INFO", "Loading complete");
    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null;
    }

    private Dialog getDialogLoading(){
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.loading);
        dialog.setCancelable(true);
        return  dialog;
    }

    private void getRSS(){
        Log.i("Initial","Initial get RSS...");
        SQLiteDatabase RSS_db = openOrCreateDatabase("RSS",MODE_PRIVATE,null);
        Cursor resultSet = RSS_db.rawQuery("SELECT title, description FROM RSS;",null);
        resultSet.moveToFirst();
        al_title = new ArrayList();
        al_desc = new ArrayList();
        while(!resultSet.isAfterLast()){
            al_title.add(resultSet.getString(resultSet.getColumnIndex("title")));
            al_desc.add(resultSet.getString(resultSet.getColumnIndex("description")));
            resultSet.moveToNext();
        }
        RSS_db.close();

        LinearLayout ll_news = (LinearLayout) findViewById(R.id.ll_news);
        ViewGroup.LayoutParams vlp = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        Display display = getWindowManager().getDefaultDisplay();
        int mWidth = display.getWidth();
        for(int i=0;i<al_title.size();i++) {
            HorizontalScrollView hsv = new HorizontalScrollView(this);
            TextView title = new TextView(this);
            title.setId(i);
            title.setClickable(true);
            title.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onClickNews(v);
                }
            });
            title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
            title.setPadding(30, 20, 30, 20);
            if ((i % 2) == 0) {
                title.setBackgroundColor(Color.parseColor("#E6E6E6"));
            }
            title.setText(al_title.get(i).toString());
            title.setLayoutParams(vlp);
            title.setMinimumWidth(mWidth);
            hsv.addView(title);
            hsv.setHorizontalScrollBarEnabled(false);
            ll_news.addView(hsv);
        }
        Log.i("Initial","Initial get RSS success");
    }

    private void updateRSS(){
        class GetDataJSON extends AsyncTask<String,Void,String> {
            private SharedPreferences pref = getApplicationContext().getSharedPreferences("Student", 0);
            private String department = pref.getString("department",null);
            private HttpURLConnection urlConnection = null;
            String strJSON;
            protected String doInBackground(String... params) {
                try {
                    if(department == null){
                        SQLiteDatabase Profile_db = openOrCreateDatabase("Profile",MODE_PRIVATE,null);
                        Cursor resultSet = Profile_db.rawQuery("SELECT * FROM Profile",null);
                        resultSet.moveToFirst();
                        department =  resultSet.getString(resultSet.getColumnIndex("department"));
                        resultSet.close();
                        Profile_db.close();
                    }
                    URL url = new URL("http://54.169.58.93/RSS_UpdateFeed.php?department="+department+"&date="+params[0]);
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
                Log.i("INFO", "Loading...");
                dialog = new Dialog(Page_news.this);
                dialog = getDialogLoading();
                dialog.show();
                Log.i("Setup","RSS update...");
                SQLiteDatabase RSS_db = openOrCreateDatabase("RSS",MODE_PRIVATE,null);
                try{
                    JSONArray data = new JSONArray(strJSON);
                    if(data.length()>0){
                        for(int i=0;i<data.length();i++){
                            JSONObject c = data.getJSONObject(i);
                            RSS_db.execSQL("INSERT INTO RSS(id, title, description, pubDate, count) SELECT * FROM (SELECT '"+c.getInt("rss_id")+"','"+encodeUnicode(c.getString("rss_title"))+"','"+encodeUnicode(c.getString("rss_description"))+"','"+c.getString("rss_createdate")+"','"+c.getInt("rss_count")+"') AS tmp WHERE NOT EXISTS (SELECT * FROM RSS WHERE id='"+c.getInt("rss_id")+"');");
                            RSS_db.execSQL("UPDATE RSS SET count='"+c.getInt("rss_count")+"' WHERE id='"+c.getInt("rss_id")+"';");
                        }
                        Log.i("Setup","RSS updated");
                    }else{
                        Log.i("Setup","RSS not updated");
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }finally {
                    RSS_db.close();
                    getRSS();
                }
                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        dialog.cancel();
                    }
                }, 1000);
                Log.i("INFO", "Loading complete");
            }
        }
        SQLiteDatabase RSS_db = openOrCreateDatabase("RSS",MODE_PRIVATE,null);
        Cursor resultSet = RSS_db.rawQuery("SELECT MAX(pubDate) FROM RSS;",null);
        resultSet.moveToFirst();
        String pubDate = resultSet.getString(resultSet.getColumnIndex("MAX(pubDate)"));
        resultSet.close();
        RSS_db.close();
        new GetDataJSON().execute(pubDate);
    }

    private String encodeUnicode(String str) {
        String strEncoded = "";
        try {
            byte[] bytes = str.getBytes("UTF-8");
            strEncoded = new String(bytes, "UTF-8");
        }catch(Exception e){
            e.printStackTrace();
        }
        return strEncoded;
    }

    private void onClickNews(View v){
        int idv = v.getId();
        String title = al_title.get(idv).toString();
        String desc = al_desc.get(idv).toString();
        Intent intent = new Intent(Page_news.this, News.class);
        intent.putExtra("from","Page_news");
        intent.putExtra("title",title);
        intent.putExtra("desc", desc);
        startActivity(intent);
        Log.i("OC","On click news");
    }

    public void refreshRSS(View v){
        updateRSS();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)  {
        if (Integer.parseInt(android.os.Build.VERSION.SDK) > 5
                && keyCode == KeyEvent.KEYCODE_BACK
                && event.getRepeatCount() == 0) {
            Log.d("CDA", "onKeyDown Called");
            Intent setIntent = new Intent(this, Main.class);
            setIntent.addCategory(Intent.CATEGORY_HOME);
            setIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(setIntent);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    public void gotoTodo(View v){
        Intent intent = new Intent(this, Todo_List.class);
        startActivity(intent);
        Log.i("GT","Go to Todo");
    }

    public void gotoSetting(View v){
        Intent intent = new Intent(this, more_setting.class);
        startActivity(intent);
        Log.i("GT","Go to Setting");
    }

    public void gotoAbout(View v){
        Intent intent = new Intent(this,About.class);
        startActivity(intent);
    }

    public void gotoHome(View v){
        Intent intent = new Intent(this, Main.class);
        startActivity(intent);
        Log.i("GT","Go to Home");
    }

    public void gotoNoti(View v){
        Intent intent = new Intent(this, Noti.class);
        startActivity(intent);
        Log.i("GT","Go to Notification");
    }

    public void gotoElean(View v){
        Intent intent = new Intent(this, Elearning.class);
        startActivity(intent);
        Log.i("GT","Go to Elearning");
    }
}
