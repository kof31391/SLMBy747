package com.example.a747.smartlearningmanager;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class Profile extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", 0);
        String std_id = pref.getString("std_id", null);
        if(std_id != null){
            super.onCreate(savedInstanceState);
            setContentView(R.layout.profile);
        }else{
            Intent intent = new Intent(this, Login.class);
            startActivity(intent);
        }
        getSchedule(std_id);
        getProfile(std_id);
    }
    public void getProfile(String std_id){
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
                            String line = "";
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
                    tv_pf = (TextView) findViewById(R.id.tv_pf_image);
                    tv_pf.setText(c.getString("image"));
                }catch(Exception e){
                    e.printStackTrace();
                }
            }
        }
        new GetDataJSON().execute(std_id);
    }
    public void getSchedule(String std_id){
        class GetDataJSON extends AsyncTask<String,Void,String> {
            HttpURLConnection urlConnection = null;
            private String strJSON;
            protected String doInBackground(String... params) {
                try {
                    URL url = new URL("http://54.169.58.93/SchOfWeek.php?std_id="+params[0]);
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
                    ArrayList<String> al_mon = new ArrayList<>();
                    ArrayList<String> al_tue = new ArrayList<>();
                    ArrayList<String> al_wed = new ArrayList<>();
                    ArrayList<String> al_thu = new ArrayList<>();
                    ArrayList<String> al_fri = new ArrayList<>();
                    ArrayList<String> al_sat = new ArrayList<>();
                    ArrayList<String> al_sun = new ArrayList<>();
                    JSONArray data = new JSONArray(strJSON);
                    for(int i=0;i<data.length();i++) {
                        JSONObject c = data.getJSONObject(i);
                        switch (c.getInt("subject_date")) {
                            case 1:
                                al_mon.add(c.getString("subject_code"));
                                al_mon.add(c.getString("subject_name"));
                                al_mon.add(c.getString("lecturer"));
                                al_mon.add(c.getString("subject_room"));
                                al_mon.add(c.getString("subject_time_start"));
                                al_mon.add(c.getString("subject_time_ended"));
                                break;
                            case 2:
                                al_tue.add(c.getString("subject_code"));
                                al_tue.add(c.getString("subject_name"));
                                al_tue.add(c.getString("lecturer"));
                                al_tue.add(c.getString("subject_room"));
                                al_tue.add(c.getString("subject_time_start"));
                                al_tue.add(c.getString("subject_time_ended"));
                                break;
                            case 3:
                                al_wed.add(c.getString("subject_code"));
                                al_wed.add(c.getString("subject_name"));
                                al_wed.add(c.getString("lecturer"));
                                al_wed.add(c.getString("subject_room"));
                                al_wed.add(c.getString("subject_time_start"));
                                al_wed.add(c.getString("subject_time_ended"));
                                break;
                            case 4:
                                al_thu.add(c.getString("subject_code"));
                                al_thu.add(c.getString("subject_name"));
                                al_thu.add(c.getString("lecturer"));
                                al_thu.add(c.getString("subject_room"));
                                al_thu.add(c.getString("subject_time_start"));
                                al_thu.add(c.getString("subject_time_ended"));
                                break;
                            case 5:
                                al_fri.add(c.getString("subject_code"));
                                al_fri.add(c.getString("subject_name"));
                                al_fri.add(c.getString("lecturer"));
                                al_fri.add(c.getString("subject_room"));
                                al_fri.add(c.getString("subject_time_start"));
                                al_fri.add(c.getString("subject_time_ended"));
                                break;
                            case 6:
                                al_sat.add(c.getString("subject_code"));
                                al_sat.add(c.getString("subject_name"));
                                al_sat.add(c.getString("lecturer"));
                                al_sat.add(c.getString("subject_room"));
                                al_sat.add(c.getString("subject_time_start"));
                                al_sat.add(c.getString("subject_time_ended"));
                                break;
                            case 7:
                                al_sun.add(c.getString("subject_code"));
                                al_sun.add(c.getString("subject_name"));
                                al_sun.add(c.getString("lecturer"));
                                al_sun.add(c.getString("subject_room"));
                                al_sun.add(c.getString("subject_time_start"));
                                al_sun.add(c.getString("subject_time_ended"));
                                break;
                        }
                    }
                    TableRow.LayoutParams params1 = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT);
                    TableRow.LayoutParams params2=new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT);
                    TableLayout tl_pf_sc_mon = (TableLayout) findViewById(R.id.tl_pf_sc_mon);
                    if(al_mon.size() != 0) {
                        for (int i = 0; i < al_mon.size(); i++) {
                            if (i < 6) {
                                TableRow row = new TableRow(Profile.this);
                                TextView code = new TextView(Profile.this);
                                code.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
                                code.setPadding(20, 20, 0, 20);
                                code.setText(al_mon.get(i).toString());
                                code.setLayoutParams(params1);
                                row.setBackgroundColor(Color.parseColor("#c7c7c7"));
                                row.addView(code);
                                row.setLayoutParams(params2);
                                tl_pf_sc_mon.addView(row);
                            } else if(i<12) {
                                TableRow row = new TableRow(Profile.this);
                                TextView code = new TextView(Profile.this);
                                code.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
                                code.setPadding(20, 20, 0, 20);
                                code.setText(al_mon.get(i).toString());
                                code.setLayoutParams(params1);
                                row.setBackgroundColor(Color.parseColor("#E6E6E6"));
                                row.addView(code);
                                row.setLayoutParams(params2);
                                tl_pf_sc_mon.addView(row);
                            }
                            else{
                                TableRow row = new TableRow(Profile.this);
                                TextView code = new TextView(Profile.this);
                                code.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
                                code.setPadding(20, 20, 0, 20);
                                code.setText(al_mon.get(i).toString());
                                code.setLayoutParams(params1);
                                row.setBackgroundColor(Color.parseColor("#c7c7c7"));
                                row.addView(code);
                                row.setLayoutParams(params2);
                                tl_pf_sc_mon.addView(row);
                            }
                        }
                    }
                    TableLayout tl_pf_sc_tue = (TableLayout) findViewById(R.id.tl_pf_sc_tue);
                    if(al_tue.size() != 0) {
                        for (int i = 0; i < al_thu.size(); i++) {
                            if (i < 6) {
                                TableRow row = new TableRow(Profile.this);
                                TextView code = new TextView(Profile.this);
                                code.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
                                code.setPadding(20, 20, 0, 20);
                                code.setText(al_tue.get(i).toString());
                                code.setLayoutParams(params1);
                                row.setBackgroundColor(Color.parseColor("#c7c7c7"));
                                row.addView(code);
                                row.setLayoutParams(params2);
                                tl_pf_sc_tue.addView(row);
                            } else if (i<12) {
                                TableRow row = new TableRow(Profile.this);
                                TextView code = new TextView(Profile.this);
                                code.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
                                code.setPadding(20, 20, 0, 20);
                                code.setText(al_tue.get(i).toString());
                                code.setLayoutParams(params1);
                                row.setBackgroundColor(Color.parseColor("#E6E6E6"));
                                row.addView(code);
                                row.setLayoutParams(params2);
                                tl_pf_sc_tue.addView(row);
                            }
                            else{
                                TableRow row = new TableRow(Profile.this);
                                TextView code = new TextView(Profile.this);
                                code.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
                                code.setPadding(20, 20, 0, 20);
                                code.setText(al_tue.get(i).toString());
                                code.setLayoutParams(params1);
                                row.setBackgroundColor(Color.parseColor("#c7c7c7"));
                                row.addView(code);
                                row.setLayoutParams(params2);
                                tl_pf_sc_tue.addView(row);
                            }
                        }
                    }
                    TableLayout tl_pf_sc_wed = (TableLayout) findViewById(R.id.tl_pf_sc_wed);
                    if(al_wed.size() != 0) {
                        for (int i = 0; i < al_wed.size(); i++) {
                            if (i < 6) {
                                TableRow row = new TableRow(Profile.this);
                                TextView code = new TextView(Profile.this);
                                code.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
                                code.setPadding(20, 20, 0, 20);
                                code.setText(al_wed.get(i).toString());
                                code.setLayoutParams(params1);
                                row.setBackgroundColor(Color.parseColor("#c7c7c7"));
                                row.addView(code);
                                row.setLayoutParams(params2);
                                tl_pf_sc_wed.addView(row);
                            } else if(i<12) {
                                TableRow row = new TableRow(Profile.this);
                                TextView code = new TextView(Profile.this);
                                code.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
                                code.setPadding(20, 20, 0, 20);
                                code.setText(al_wed.get(i).toString());
                                code.setLayoutParams(params1);
                                row.setBackgroundColor(Color.parseColor("#E6E6E6"));
                                row.addView(code);
                                row.setLayoutParams(params2);
                                tl_pf_sc_wed.addView(row);
                            }else{
                                TableRow row = new TableRow(Profile.this);
                                TextView code = new TextView(Profile.this);
                                code.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
                                code.setPadding(20, 20, 0, 20);
                                code.setText(al_wed.get(i).toString());
                                code.setLayoutParams(params1);
                                row.setBackgroundColor(Color.parseColor("#c7c7c7"));
                                row.addView(code);
                                row.setLayoutParams(params2);
                                tl_pf_sc_wed.addView(row);
                            }
                        }
                    }
                    TableLayout tl_pf_sc_thu = (TableLayout) findViewById(R.id.tl_pf_sc_thu);
                    if(al_thu.size() != 0) {
                        for (int i = 0; i < al_thu.size(); i++) {
                            if (i < 6) {
                                TableRow row = new TableRow(Profile.this);
                                TextView code = new TextView(Profile.this);
                                code.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
                                code.setPadding(20, 20, 0, 20);
                                code.setText(al_thu.get(i).toString());
                                code.setLayoutParams(params1);
                                row.setBackgroundColor(Color.parseColor("#c7c7c7"));
                                row.addView(code);
                                row.setLayoutParams(params2);
                                tl_pf_sc_thu.addView(row);
                            } else if (i<12) {
                                TableRow row = new TableRow(Profile.this);
                                TextView code = new TextView(Profile.this);
                                code.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
                                code.setPadding(20, 20, 0, 20);
                                code.setText(al_thu.get(i).toString());
                                code.setLayoutParams(params1);
                                row.setBackgroundColor(Color.parseColor("#E6E6E6"));
                                row.addView(code);
                                row.setLayoutParams(params2);
                                tl_pf_sc_thu.addView(row);
                            }else{
                                TableRow row = new TableRow(Profile.this);
                                TextView code = new TextView(Profile.this);
                                code.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
                                code.setPadding(20, 20, 0, 20);
                                code.setText(al_thu.get(i).toString());
                                code.setLayoutParams(params1);
                                row.setBackgroundColor(Color.parseColor("#c7c7c7"));
                                row.addView(code);
                                row.setLayoutParams(params2);
                                tl_pf_sc_thu.addView(row);
                            }
                        }
                    }
                    TableLayout tl_pf_sc_fri = (TableLayout) findViewById(R.id.tl_pf_sc_fri);
                    if(al_fri.size() != 0) {
                        for (int i = 0; i < al_fri.size(); i++) {
                            if (i < 6) {
                                TableRow row = new TableRow(Profile.this);
                                TextView code = new TextView(Profile.this);
                                code.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
                                code.setPadding(20, 20, 0, 20);
                                code.setText(al_fri.get(i).toString());
                                code.setLayoutParams(params1);
                                row.setBackgroundColor(Color.parseColor("#c7c7c7"));
                                row.addView(code);
                                row.setLayoutParams(params2);
                                tl_pf_sc_fri.addView(row);
                            } else if(i<12){
                                TableRow row = new TableRow(Profile.this);
                                TextView code = new TextView(Profile.this);
                                code.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
                                code.setPadding(20, 20, 0, 20);
                                code.setText(al_fri.get(i).toString());
                                code.setLayoutParams(params1);
                                row.setBackgroundColor(Color.parseColor("#E6E6E6"));
                                row.addView(code);
                                row.setLayoutParams(params2);
                                tl_pf_sc_fri.addView(row);
                            }else{
                                TableRow row = new TableRow(Profile.this);
                                TextView code = new TextView(Profile.this);
                                code.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
                                code.setPadding(20, 20, 0, 20);
                                code.setText(al_fri.get(i).toString());
                                code.setLayoutParams(params1);
                                row.setBackgroundColor(Color.parseColor("#c7c7c7"));
                                row.addView(code);
                                row.setLayoutParams(params2);
                                tl_pf_sc_fri.addView(row);
                            }
                        }
                    }
                    TableLayout tl_pf_sc_sat = (TableLayout) findViewById(R.id.tl_pf_sc_sat);
                    if(al_sat.size() != 0) {
                        for (int i = 0; i < al_sat.size(); i++) {
                            if (i < 6) {
                                TableRow row = new TableRow(Profile.this);
                                TextView code = new TextView(Profile.this);
                                code.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
                                code.setPadding(20, 20, 0, 20);
                                code.setText(al_sat.get(i).toString());
                                code.setLayoutParams(params1);
                                row.setBackgroundColor(Color.parseColor("#c7c7c7"));
                                row.addView(code);
                                row.setLayoutParams(params2);
                                tl_pf_sc_sat.addView(row);
                            } else if (i<12){
                                TableRow row = new TableRow(Profile.this);
                                TextView code = new TextView(Profile.this);
                                code.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
                                code.setPadding(20, 20, 0, 20);
                                code.setText(al_sat.get(i).toString());
                                code.setLayoutParams(params1);
                                row.setBackgroundColor(Color.parseColor("#E6E6E6"));
                                row.addView(code);
                                row.setLayoutParams(params2);
                                tl_pf_sc_sat.addView(row);
                            }else{
                                TableRow row = new TableRow(Profile.this);
                                TextView code = new TextView(Profile.this);
                                code.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
                                code.setPadding(20, 20, 0, 20);
                                code.setText(al_sat.get(i).toString());
                                code.setLayoutParams(params1);
                                row.setBackgroundColor(Color.parseColor("#c7c7c7"));
                                row.addView(code);
                                row.setLayoutParams(params2);
                                tl_pf_sc_sat.addView(row);
                            }
                        }
                    }
                    TableLayout tl_pf_sc_sun = (TableLayout) findViewById(R.id.tl_pf_sc_sun);
                    if(al_sun.size() != 0) {
                        for (int i = 0; i < al_sun.size(); i++) {
                            if (i < 6) {
                                TableRow row = new TableRow(Profile.this);
                                TextView code = new TextView(Profile.this);
                                code.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
                                code.setPadding(20, 20, 0, 20);
                                code.setText(al_sun.get(i).toString());
                                code.setLayoutParams(params1);
                                row.setBackgroundColor(Color.parseColor("#c7c7c7"));
                                row.addView(code);
                                row.setLayoutParams(params2);
                                tl_pf_sc_sun.addView(row);
                            } else if(i<12) {
                                TableRow row = new TableRow(Profile.this);
                                TextView code = new TextView(Profile.this);
                                code.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
                                code.setPadding(20, 20, 0, 20);
                                code.setText(al_sun.get(i).toString());
                                code.setLayoutParams(params1);
                                row.setBackgroundColor(Color.parseColor("#E6E6E6"));
                                row.addView(code);
                                row.setLayoutParams(params2);
                                tl_pf_sc_sun.addView(row);
                            }else{
                                TableRow row = new TableRow(Profile.this);
                                TextView code = new TextView(Profile.this);
                                code.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
                                code.setPadding(20, 20, 0, 20);
                                code.setText(al_sun.get(i).toString());
                                code.setLayoutParams(params1);
                                row.setBackgroundColor(Color.parseColor("#c7c7c7"));
                                row.addView(code);
                                row.setLayoutParams(params2);
                                tl_pf_sc_sun.addView(row);
                            }
                        }
                    }
                }catch(Exception e){
                    e.printStackTrace();
                }
            }
        }
        new GetDataJSON().execute(std_id);
    }
    public void onClickBack(View v) {
        Intent intent = new Intent(this,more_setting.class);
        startActivity(intent);
    }
}
