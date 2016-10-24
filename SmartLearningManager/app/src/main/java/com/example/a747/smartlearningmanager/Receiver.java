package com.example.a747.smartlearningmanager;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;

/**
 * Created by lenovo on 23/10/2559.
 */

public class Receiver extends AppCompatActivity {
    private String student_id;

    private String getLastReceiveTime(){
        String receiveTime;
        SQLiteDatabase Receiver_db = openOrCreateDatabase("Receiver",MODE_PRIVATE,null);
        Receiver_db.execSQL("DROP TABLEIF EXISTS Receiver");
        Receiver_db.execSQL("CREATE TABLE IF NOT EXISTS Receiver(receive_id INT, receive_topic_id INT, receive_description VARCHAR, receive_time DATETIME, receive_status INT, reply_time DATETIME);");
        Cursor cursor = Receiver_db.rawQuery("SELECT MAX(receive_time) FROM Receiver;",null);
        cursor.moveToFirst();
        receiveTime = cursor.getString(cursor.getColumnIndex("MAX(receive_time)"));
        cursor.close();
        Receiver_db.close();
        return receiveTime;
    }

    protected void setStudent_id(String student_id) {
        this.student_id = student_id;
    }

    protected void setProfile(){

    }

    protected void setRSS(){
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
                    URL url = new URL("http://54.169.58.93/RSS_UpdateFeed.php?department="+department+"&date="+params[0].replaceAll(" ","%20"));
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
                Log.i("Setup","RSS update...");
                SQLiteDatabase RSS_db = openOrCreateDatabase("RSS",MODE_PRIVATE,null);
                try{
                    JSONArray data = new JSONArray(strJSON);
                    if(data.length()>0){
                        for(int i=0;i<data.length();i++){
                            JSONObject c = data.getJSONObject(i);
                            RSS_db.execSQL("INSERT INTO RSS(id, title, description, pubDate, count) SELECT * FROM (SELECT '"+c.getInt("rss_id")+"','"+encodeUnicode(c.getString("rss_title"))+"','"+encodeUnicode(c.getString("rss_description"))+"','"+c.getString("rss_createdate")+"','"+c.getInt("rss_count")+"') AS tmp WHERE NOT EXISTS (SELECT * FROM RSS WHERE id='"+c.getInt("rss_id")+"');");
                            RSS_db.execSQL("UPDATE RSS SET count='"+c.getInt("rss_count")+"' WHERE id='"+c.getInt("rss_id")+"';");
                            Calendar calendar = Calendar.getInstance();
                            Notification notification = getNotification("SLM: New news",c.getString("rss_title"),calendar.getTimeInMillis());
                            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                            notificationManager.notify(1000,notification);
                        }
                        Log.i("Setup","RSS updated");
                    }else{
                        Log.i("Setup","RSS not updated");
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }finally {
                    RSS_db.close();
                }
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

    protected void setSchedule(){

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

    private Notification getNotification(String title, String content, long time) {
        Intent intent = new Intent(this, Receiver.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(Main.class);
        stackBuilder.addNextIntent(intent);
        PendingIntent pendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        Notification notification = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(content)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setWhen(time)
                .setSound(alarmSound)
                .build();
        return notification;
    }

    private void updateReceive(){
        class GetDataJSON extends AsyncTask<String,Void,String> {
            HttpURLConnection urlConnection = null;
            private String strJSON;

            protected String doInBackground(String... params) {
                try {
                    URL url = new URL("http://54.169.58.93/Messenger_getFeed.php?student_id=" + params[0] + "&datetime=" + params[1].replaceAll(" ", "%20"));
                    urlConnection = (HttpURLConnection) url.openConnection();
                    int code = urlConnection.getResponseCode();
                    if (code == 200) {
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
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    urlConnection.disconnect();
                }
                return strJSON;
            }

            protected void onPostExecute(String strJSON) {
                try {
                    JSONArray data = new JSONArray(strJSON);
                    if (data.length() > 0) {
                        Log.i("Receiver", "Receive message...");
                        SQLiteDatabase Receiver_db = openOrCreateDatabase("Receiver",MODE_PRIVATE,null);
                        for (int i = 0; i < data.length(); i++) {
                            JSONObject c = data.getJSONObject(i);
                            Log.i("Receiver", "Receive key: " + c.getString("messenger_topic_key") + " - " + c.getString("messenger_description"));
                            Receiver_db.execSQL("INSERT INTO Receiver VALUES('" + c.getString("messenger_id") + "','" + c.getString("messenger_topic_key") + "','" + c.getString("messenger_description") + "','" + c.getString("messenger_send_time") + "','" + c.getString("messenger_status") + "','" + c.getString("messenger_reply_time") + "');");
                            Receiver r = new Receiver();
                            switch (c.getInt("messenger_topic_key")) {
                                case 1:
                                    r.setProfile();
                                    break;
                                case 2:
                                    r.setRSS();
                                    break;
                                case 3:
                                    r.setSchedule();
                                    break;
                            }
                        }
                        Receiver_db.close();
                        Log.i("Receiver", "Receiver has new message");
                    }else{
                        Log.i("Receiver", "Receiver has no message");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        new GetDataJSON().execute(student_id,getLastReceiveTime());
    }
}