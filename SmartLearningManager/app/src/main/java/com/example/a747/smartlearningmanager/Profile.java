package com.example.a747.smartlearningmanager;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;

import android.widget.ImageView;
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
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

public class Profile extends AppCompatActivity {

    List<String> monday = new ArrayList<>();
    List<String> tuesday = new ArrayList<>();
    List<String> wednesday = new ArrayList<>();
    List<String> thursday = new ArrayList<>();
    List<String> friday = new ArrayList<>();
    List<String> saturday = new ArrayList<>();
    List<String> sunday = new ArrayList<>();

    ExpandableListView expandableListView;
    ExpandableListAdapter expandableListAdapter;
    List<String> expandableListTitle;
    HashMap<String, List<String>> expandableListDetail;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences pref = getApplicationContext().getSharedPreferences("Student", 0);
        String std_id = pref.getString("std_id", null);
        if(std_id != null){
            super.onCreate(savedInstanceState);
            setContentView(R.layout.profile);
        }else{
            Intent intent = new Intent(this, Login.class);
            startActivity(intent);
        }

        getProfile(std_id);
        getSchedule();

        expandableListView = (ExpandableListView) findViewById(R.id.expandableListView);
        expandableListTitle = new ArrayList<>(expandableListDetail.keySet());
        expandableListAdapter = new CustomExpandableListAdapter(this, expandableListTitle, expandableListDetail);
        expandableListView.setAdapter(expandableListAdapter);
        expandableListView.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {

            @Override
            public void onGroupExpand(int groupPosition) {

            }
        });

        expandableListView.setOnGroupCollapseListener(new ExpandableListView.OnGroupCollapseListener() {

            @Override
            public void onGroupCollapse(int groupPosition) {


            }
        });

        expandableListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v,
                                        int groupPosition, int childPosition, long id) {
                Toast.makeText(
                        getApplicationContext(),
                        expandableListTitle.get(groupPosition)
                                + " -> "
                                + expandableListDetail.get(
                                expandableListTitle.get(groupPosition)).get(
                                childPosition), Toast.LENGTH_SHORT
                ).show();
                return false;
            }
        });
    }

    public void getProfile(final String std_id){
        class GetDataJSON extends AsyncTask<String,Void,String> {
            HttpURLConnection urlConnection = null;
            public String strJSON;
            protected String doInBackground(String... params) {
                try {
                    URL url = new URL("http://54.169.58.93//Profile.php?std_id="+params[0]);
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
                try{
                    JSONArray data = new JSONArray(strJSON);
                    JSONObject c = data.getJSONObject(0);
                    TextView tv_pf;
                    ImageView tv_pfi;
                    tv_pf = (TextView) findViewById(R.id.tv_pf_std_id);
                    tv_pf.setText(c.getString("std_id"));
                    tv_pf = (TextView) findViewById(R.id.tv_pf_fristname);
                    tv_pf.setText(c.getString("firstname"));
                    tv_pf = (TextView) findViewById(R.id.tv_pf_lastname);
                    tv_pf.setText(c.getString("lastname"));
                    tv_pf = (TextView) findViewById(R.id.tv_pf_grade);
                    tv_pf.setText(c.getString("grade"));
                    tv_pf = (TextView) findViewById(R.id.tv_pf_email);
                    tv_pf.setText(c.getString("email"));
                    tv_pf = (TextView) findViewById(R.id.tv_pf_teleno);
                    tv_pf.setText(c.getString("phonenum"));
                    tv_pfi = (ImageView) findViewById(R.id.tv_pf_image);
                    new ImageLoadTask("http://54.169.58.93/student_image/"+std_id+".jpg",tv_pfi).execute();
                    //tv_pfi.setImageResource(c.getString("image"));
                }catch(Exception e){
                    e.printStackTrace();
                }
            }
        }
        new GetDataJSON().execute(std_id);
    }

    public void getSchedule(){
        SQLiteDatabase mydatabase = openOrCreateDatabase("Schedule",MODE_PRIVATE,null);
        Cursor resultSet = mydatabase.rawQuery("SELECT * FROM Schedule;",null);
        resultSet.moveToFirst();
        while(!resultSet.isAfterLast()){
            switch (resultSet.getString(resultSet.getColumnIndex("subject_date"))){
                case "1" :
                    monday.add("CODE: "+resultSet.getString(resultSet.getColumnIndex("subject_code")));
                    monday.add("NAME: "+resultSet.getString(resultSet.getColumnIndex("subject_name")));
                    monday.add("SCHEDULE: "+resultSet.getString(resultSet.getColumnIndex("subject_time_start"))+" - "+resultSet.getString(resultSet.getColumnIndex("subject_time_ended")));
                    monday.add("ATTEND / TOTAL CLASS:      /      ");
                    monday.add("---------------------------------");
                    break;
                case "2" :
                    tuesday.add("CODE: "+resultSet.getString(resultSet.getColumnIndex("subject_code")));
                    tuesday.add("NAME: "+resultSet.getString(resultSet.getColumnIndex("subject_name")));
                    tuesday.add("SCHEDULE: "+resultSet.getString(resultSet.getColumnIndex("subject_time_start"))+" - "+resultSet.getString(resultSet.getColumnIndex("subject_time_ended")));
                    tuesday.add("ATTEND / TOTAL CLASS:      /      ");
                    tuesday.add("---------------------------------");
                    break;
                case "3" :
                    wednesday.add("CODE: "+resultSet.getString(resultSet.getColumnIndex("subject_code")));
                    wednesday.add("NAME: "+resultSet.getString(resultSet.getColumnIndex("subject_name")));
                    wednesday.add("SCHEDULE: "+resultSet.getString(resultSet.getColumnIndex("subject_time_start"))+" - "+resultSet.getString(resultSet.getColumnIndex("subject_time_ended")));
                    wednesday.add("ATTEND / TOTAL CLASS:      /      ");
                    wednesday.add("---------------------------------");
                    break;
                case "4" :
                    thursday.add("CODE: "+resultSet.getString(resultSet.getColumnIndex("subject_code")));
                    thursday.add("NAME: "+resultSet.getString(resultSet.getColumnIndex("subject_name")));
                    thursday.add("SCHEDULE: "+resultSet.getString(resultSet.getColumnIndex("subject_time_start"))+" - "+resultSet.getString(resultSet.getColumnIndex("subject_time_ended")));
                    thursday.add("ATTEND / TOTAL CLASS:      /      ");
                    thursday.add("---------------------------------");
                    break;
                case "5" :
                    friday.add("CODE: "+resultSet.getString(resultSet.getColumnIndex("subject_code")));
                    friday.add("NAME: "+resultSet.getString(resultSet.getColumnIndex("subject_name")));
                    friday.add("SCHEDULE: "+resultSet.getString(resultSet.getColumnIndex("subject_time_start"))+" - "+resultSet.getString(resultSet.getColumnIndex("subject_time_ended")));
                    friday.add("ATTEND / TOTAL CLASS:      /      ");
                    friday.add("---------------------------------");
                    break;
                case "6" :
                    saturday.add("CODE: "+resultSet.getString(resultSet.getColumnIndex("subject_code")));
                    saturday.add("NAME: "+resultSet.getString(resultSet.getColumnIndex("subject_name")));
                    saturday.add("SCHEDULE: "+resultSet.getString(resultSet.getColumnIndex("subject_time_start"))+" - "+resultSet.getString(resultSet.getColumnIndex("subject_time_ended")));
                    saturday.add("ATTEND / TOTAL CLASS:      /      ");
                    saturday.add("---------------------------------");
                    break;
                case "7" :
                    sunday.add("CODE: "+resultSet.getString(resultSet.getColumnIndex("subject_code")));
                    sunday.add("NAME: "+resultSet.getString(resultSet.getColumnIndex("subject_name")));
                    sunday.add("SCHEDULE: "+resultSet.getString(resultSet.getColumnIndex("subject_time_start"))+" - "+resultSet.getString(resultSet.getColumnIndex("subject_time_ended")));
                    sunday.add("ATTEND / TOTAL CLASS:      /      ");
                    sunday.add("---------------------------------");
                    break;
            }
            resultSet.moveToNext();
        }
        mydatabase.close();
        expandableListDetail  = new LinkedHashMap<>();
        expandableListDetail.put("Monday", monday);
        expandableListDetail.put("Tuesday", tuesday);
        expandableListDetail.put("Wednesday", wednesday);
        expandableListDetail.put("Thursday", thursday);
        expandableListDetail.put("Friday", friday);
        expandableListDetail.put("Saturday", saturday);
        expandableListDetail.put("Sunday", sunday);
    }

    public void onClickBack(View v) {
        Intent intent = new Intent(this,more_setting.class);
        startActivity(intent);
    }

}

class ImageLoadTask extends AsyncTask<Void, Void, Bitmap> {

    private String url;
    private ImageView imageView;

    public ImageLoadTask(String url, ImageView imageView) {
        this.url = url;
        this.imageView = imageView;
    }

    @Override
    protected Bitmap doInBackground(Void... params) {
        try {
            URL urlConnection = new URL(url);
            HttpURLConnection connection = (HttpURLConnection) urlConnection
                    .openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap myBitmap = BitmapFactory.decodeStream(input);
            return myBitmap;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Bitmap result) {
        super.onPostExecute(result);
        imageView.setImageBitmap(result);
    }

}
