package com.example.a747.smartlearningmanager;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
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
import java.util.HashMap;

public class Subject_elearn extends AppCompatActivity {
    String std_id;
    String subject;
    String path;
    TextView subjCode;
    TextView lecturer;
    TextView lecturerMail;
    TextView lecturerTel;
    TextView subjName;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.subject_elearn);
        subjCode = (TextView)findViewById(R.id.subjCode);
        subjName = (TextView)findViewById(R.id.subjname);
        lecturer = (TextView)findViewById(R.id.LecturerName);
        lecturerMail = (TextView)findViewById(R.id.leturerMail);

        Intent intent = getIntent();
        subject = intent.getExtras().getString("subject");

        SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", 0);
        std_id = pref.getString("std_id", null);

        getSubjectDetial(subject,std_id);

    }

    private void getSubjectDetial(final String subject, final String std_id){
        class GetDataJSON extends AsyncTask<String,Void,String> {
            HttpURLConnection urlConnection = null;
            private String strJSON;
            protected String doInBackground(String... params) {
                try {
                    String subj = subject.substring(0,6);
                    URL url = new URL("http://54.169.58.93/Elearning_datelist.php?subject="+subj+"&std_id="+std_id);
                    System.out.println("Link------------------------: "+url.toString());
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
                    Log.i("Setup","Set video detail...");
                    JSONArray data = new JSONArray(strJSON);

                    SQLiteDatabase mydatabase = openOrCreateDatabase("Elearning",MODE_PRIVATE,null);
                    mydatabase.execSQL("DROP TABLE IF EXISTS Elearning");
                    mydatabase.execSQL("CREATE TABLE IF NOT EXISTS Elearning(subject_code VARCHAR, subject_name VARCHAR, subject_room VARCHAR, e_date VARCHAR, e_time VARCHAR, e_link VARCHAR);");

                    TableLayout tl_datelist = (TableLayout) findViewById(R.id.tl_datelist);
                    TableRow.LayoutParams params1 = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT);
                    TableRow.LayoutParams params2=new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT);
                    for(int i=0;i<data.length();i++){
                        JSONObject c = data.getJSONObject(i);
                        mydatabase.execSQL("INSERT INTO Elearning VALUES('"+c.getString("subject_code")+"','"+c.getString("subject_name")+"','"+c.getString("subject_room")+"','"+c.getString("e_date")+"','"+c.getString("e_time")+"','"+c.getString("e_link")+"');");
                        TableRow row = new TableRow(Subject_elearn.this);
                        TextView title = new TextView(Subject_elearn.this);
                        title.setId(i);
                        title.setClickable(true);
                        title.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                gotoVideo(v);
                            }
                        });
                        title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
                        title.setPadding(20, 20, 0, 20);
                        if ((i % 2) == 0) {
                            title.setBackgroundColor(Color.parseColor("#E6E6E6"));
                            if(c.getString("check_watch_e").equalsIgnoreCase("null")){
                                title.setBackgroundColor(Color.parseColor("#FFFF99"));
                            }
                        }
                        subjCode.setText(c.getString("subject_code"));
                        subjName.setText(c.getString("subject_name"));
                        lecturer.setText(c.getString("lecturer"));
                        lecturerMail.setText(c.getString("email"));
                        title.setText(c.getString("e_date")+"  "+c.getString("e_time"));
                        title.setLayoutParams(params1);
                        row.addView(title);
                        row.setLayoutParams(params2);
                        tl_datelist.addView(row);
                    }
                    Log.i("Setup","Set video detail success");

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        new GetDataJSON().execute();
    }

    public void gotoVideo(View v){
        SQLiteDatabase mydatabase = openOrCreateDatabase("Elearning",MODE_PRIVATE,null);
        TextView tv = (TextView) v;
        String text = tv.getText().toString();
        String date = text.substring(0,10);
        String time = text.substring(12,20);
        Cursor resultSet = mydatabase.rawQuery("SELECT * FROM Elearning WHERE e_date='"+date+"' AND e_time='"+time+"';",null);
        resultSet.moveToFirst();
        Intent intent = new Intent(this,Video_elearning.class);
        intent.putExtra("code",resultSet.getString(resultSet.getColumnIndex("subject_code")));
        intent.putExtra("name",resultSet.getString(resultSet.getColumnIndex("subject_name")));
        intent.putExtra("room",resultSet.getString(resultSet.getColumnIndex("subject_room")));
        intent.putExtra("date",resultSet.getString(resultSet.getColumnIndex("e_date")));
        intent.putExtra("time",resultSet.getString(resultSet.getColumnIndex("e_time")));
        intent.putExtra("link",resultSet.getString(resultSet.getColumnIndex("e_link")));
        startActivity(intent);
    }

    public void gotoElean(View v){
        Intent temp = getIntent();
        String from = temp.getExtras().getString("from");
        System.out.println(from);
        if(from.equals("Elearning")){
            Intent intent = new Intent(this, Elearning.class);
            startActivity(intent);
            Log.i("GT","Go to Elearning");
        }else{
            Intent intent = new Intent(this, Main.class);
            startActivity(intent);
            Log.i("GT","Go to Main");
        }
    }
}
