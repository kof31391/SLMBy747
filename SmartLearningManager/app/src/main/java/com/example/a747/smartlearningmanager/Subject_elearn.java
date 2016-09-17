package com.example.a747.smartlearningmanager;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;

public class Subject_elearn extends AppCompatActivity {
    String std_id;
    String subject;
    String telno;
    int c_absent = 0;
    TextView subjCode;
    TextView lecturer;
    TextView lecturerMail;
    TextView lecturerTel;
    ImageView lecturerImage;
    TextView subjName;
    TextView absent;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.subject_elearn);
        subjCode = (TextView)findViewById(R.id.subjCode);
        subjName = (TextView)findViewById(R.id.subjname);
        lecturer = (TextView)findViewById(R.id.lecturerName);
        lecturerMail = (TextView)findViewById(R.id.leturerMail);
        lecturerTel = (TextView) findViewById(R.id.lecturerTel);
        lecturerImage = (ImageView) findViewById(R.id.lecturerImage) ;
        absent = (TextView) findViewById(R.id.absent);

        Intent intent = getIntent();
        subject = intent.getExtras().getString("subject");

        SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", 0);
        std_id = pref.getString("std_id", null);

        getSubjectDetial(subject);
        getSubjectVideo(subject);


    }

    private void getSubjectDetial(final String subject){
        String subj = subject.substring(0,6);
        SQLiteDatabase mydatabase = openOrCreateDatabase("Schedule",MODE_PRIVATE,null);
        Cursor resultSet = mydatabase.rawQuery("SELECT * FROM Subject_Lecturer sl JOIN Schedule s ON sl.subject_code = s.subject_code JOIN Lecturer l ON sl.lecturer_id = l.lecturer_id WHERE s.subject_code='"+subj+"';",null);
        resultSet.moveToFirst();
        subjCode.setText(resultSet.getString(resultSet.getColumnIndex("subject_code")));
        subjName.setText(resultSet.getString(resultSet.getColumnIndex("subject_name")));
        lecturer.setText(resultSet.getString(resultSet.getColumnIndex("lecturer_name"))+" "+resultSet.getString(resultSet.getColumnIndex("lecturer_lastname")));
        lecturerMail.setText(resultSet.getString(resultSet.getColumnIndex("lecturer_email")));
        lecturerTel.setText(resultSet.getString(resultSet.getColumnIndex("lecturer_tel")));
        telno = resultSet.getString(resultSet.getColumnIndex("lecturer_tel"));
        lecturerTel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent callIntent = new Intent(Intent.ACTION_DIAL);
                callIntent.setData(Uri.parse("tel:"+telno));
                callIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(callIntent);

            }
        });
        String uri = "lecturer_"+resultSet.getString(resultSet.getColumnIndex("lecturer_name")).toLowerCase();
        int imageResource = getResources().getIdentifier(uri, "drawable", getPackageName());
        Drawable image = getResources().getDrawable(imageResource);
        lecturerImage.setImageDrawable(image);
    }

    private void getSubjectVideo(final String subject){
        class GetDataJSON extends AsyncTask<String,Void,String> {
            HttpURLConnection urlConnection = null;
            private String strJSON;
            protected String doInBackground(String... params) {
                try {
                    String subj = subject.substring(0,6);
                    URL url = new URL("http://54.169.58.93/Elearning_datelist.php?std_id="+std_id+"&subject="+subj);
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
                    for(int i=0;i<data.length();i++){
                        JSONObject c = data.getJSONObject(i);
                        mydatabase.execSQL("INSERT INTO Elearning VALUES('"+c.getString("subject_code")+"','"+c.getString("subject_name")+"','"+c.getString("subject_room")+"','"+c.getString("e_date")+"','"+c.getString("e_time")+"','"+c.getString("e_link")+"');");
                        TableRow row = new TableRow(Subject_elearn.this);
                        TextView cell = new TextView(Subject_elearn.this);
                        cell.setId(i);
                        cell.setClickable(true);
                        cell.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                gotoVideo(v);
                            }
                        });
                        cell.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
                        cell.setPadding(20, 20, 0, 20);
                        if(c.getString("check_status").equalsIgnoreCase("N") && c.getString("check_watch_e").equalsIgnoreCase("null")){
                            cell.setBackgroundColor(Color.parseColor("#FFFF99"));
                        }
                        if(c.getString("check_watch_e").equalsIgnoreCase("Y")){
                            cell.setBackgroundColor(Color.parseColor("#66FF66"));
                        }
                        if(c.getString("check_status").equalsIgnoreCase("N")){
                            c_absent++;
                        }
                        absent.setText(String.valueOf(c_absent));
                        cell.setText(c.getString("e_date")+"  "+c.getString("e_time"));
                        cell.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT,TableRow.LayoutParams.WRAP_CONTENT, 1f));
                        row.addView(cell);
                        row.setLayoutParams(new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT,TableLayout.LayoutParams.MATCH_PARENT, 1f));
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

        Intent temp = getIntent();
        String from = temp.getExtras().getString("from");
        intent.putExtra("from",from);
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
