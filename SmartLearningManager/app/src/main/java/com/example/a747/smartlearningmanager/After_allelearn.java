package com.example.a747.smartlearningmanager;

import android.app.Dialog;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
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

public class After_allelearn extends AppCompatActivity {
    private String host = "http://10.4.56.17/";
    private String department;
    private Dialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i("INFO", "Loading...");
        dialog = new Dialog(this);
        dialog = getDialogLoading();
        dialog.show();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.after_allelearn);
        if(isNetworkConnected()) {
            Intent intent = getIntent();
            department = intent.getExtras().getString("department");
            setTitlebar();
            getDepSubject();
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
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        if (activeNetwork != null && activeNetwork.isConnected()) {
            try {
                URL url = new URL(host);
                HttpURLConnection urlc = (HttpURLConnection)url.openConnection();
                urlc.setRequestProperty("User-Agent", "test");
                urlc.setRequestProperty("Connection", "close");
                urlc.setConnectTimeout(1000); // mTimeout is in seconds
                urlc.connect();
                if (urlc.getResponseCode() == 200) {
                    return true;
                } else {
                    return false;
                }
            } catch (Exception e) {
                Log.i("warning", "Error checking internet connection");
                return false;
            }
        }
        return false;
    }

    private Dialog getDialogLoading(){
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.loading);
        dialog.setCancelable(true);
        return  dialog;
    }

    protected void setTitlebar(){
        TextView tv_titlebar = (TextView) findViewById(R.id.title_dept);
        tv_titlebar.setText(department);
    }

    protected void getDepSubject(){
        class GetDataJSON extends AsyncTask<String,Void,String> {
            HttpURLConnection urlConnection = null;
            private String strJSON;
            @Override
            protected String doInBackground(String... strings) {
                try {
                    URL url = new URL(host+"Subject_List.php?department="+department);
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

            @Override
            protected void onPostExecute(String s) {
                try{
                    Log.i("Setup", "Set subject list...");
                    JSONArray data = new JSONArray(strJSON);
                    SQLiteDatabase Subjectlist_db = openOrCreateDatabase("Subject_List",MODE_PRIVATE,null);
                    Subjectlist_db.execSQL("DROP TABLE IF EXISTS Subject_List");
                    Subjectlist_db.execSQL("CREATE TABLE IF NOT EXISTS Subject_List(subject_id VARCHAR, subject_code VARCHAR, subject_name VARCHAR);");
                    if(data.length() > 0){
                        LinearLayout tl_subjectlist = (LinearLayout) findViewById(R.id.tl_subjectlist);
                        Display display = getWindowManager().getDefaultDisplay();
                        int mWidth = display.getWidth();
                        for (int i = 0; i < data.length(); i++) {
                            JSONObject c = data.getJSONObject(i);
                            Subjectlist_db.execSQL("INSERT INTO Subject_List VALUES ('"+c.getString("subject_id")+"','"+c.getString("subject_code")+"','"+c.getString("subject_name")+"');");
                            HorizontalScrollView row = new HorizontalScrollView(After_allelearn.this);
                            TextView cell = new TextView(After_allelearn.this);
                            cell.setId(i);
                            cell.setClickable(true);
                            cell.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    String temp = ((TextView)v).getText().toString();
                                    String subject_code = temp.substring(0,temp.indexOf(" "));
                                    SQLiteDatabase Subjectlist_db = openOrCreateDatabase("Subject_List",MODE_PRIVATE,null);
                                    Cursor resultSet = Subjectlist_db.rawQuery("SELECT * FROM Subject_List WHERE Subject_code='"+subject_code+"';",null);
                                    resultSet.moveToFirst();
                                    temp = resultSet.getString(resultSet.getColumnIndex("subject_id"));
                                    Intent intent = new Intent(After_allelearn.this,Subject_elearnAll.class);
                                    intent.putExtra("subject_id",temp);
                                    intent.putExtra("department",department);
                                    startActivity(intent);
                                }
                            });
                            cell.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
                            cell.setPadding(20, 25, 20, 25);
                            if ((i % 2) == 0) {
                                cell.setBackgroundColor(Color.parseColor("#E6E6E6"));
                            }
                            cell.setText(c.getString("subject_code")+"  "+c.getString("subject_name"));
                            cell.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT, 1f));
                            cell.setMinimumWidth(mWidth);
                            row.addView(cell);
                            row.setHorizontalScrollBarEnabled(false);
                            tl_subjectlist.addView(row);
                        }
                        Subjectlist_db.close();
                    }
                    Log.i("Setup", "Set subject list success");
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
        new GetDataJSON().execute();
    }
    protected void gotoElearn(View v){
        Intent intent = new Intent(this,Elearning.class);
        startActivity(intent);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (Integer.parseInt(android.os.Build.VERSION.SDK) > 5
                && keyCode == KeyEvent.KEYCODE_BACK
                && event.getRepeatCount() == 0) {
            Log.d("CDA", "onKeyDown Called");
            Intent intent = new Intent(this, Elearning.class);
            startActivity(intent);
            Log.i("GT", "Go to Main");
        }
        return true;
    }
}
