package com.example.a747.smartlearningmanager;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.sql.Date;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;

public class Subject_elearnAll extends AppCompatActivity {

    String std_id;
    String subject_id;
    String status = "n";
    String telno;
    String email;
    String watch_status;
    String from;
    int c_absent = 0;
    TextView subjCode;
    TextView lecturer;
    TextView class_room;
    TextView class_Time;
    ImageButton imgB_call;
    ImageButton imgB_mail;
    ImageView lecturerImage;
    TextView subjName;
    TextView absent;
    private Material_Object material_object;
    private String m_id;
    private String m_name;
    private String m_link;
    private int m_amount;
    private ArrayList<Material_Object> al;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.subject_elearn);
        subjCode = (TextView) findViewById(R.id.subjCode);
        subjName = (TextView) findViewById(R.id.subjname);
        lecturer = (TextView) findViewById(R.id.lecturerName);
        class_room = (TextView) findViewById(R.id.class_room);
        class_Time = (TextView) findViewById(R.id.class_Time);
        imgB_call = (ImageButton) findViewById(R.id.imgB_call);
        imgB_mail = (ImageButton) findViewById(R.id.imgB_mail);
        lecturerImage = (ImageView) findViewById(R.id.lecturerImage);
        absent = (TextView) findViewById(R.id.absent);

        SharedPreferences pref = getApplicationContext().getSharedPreferences("Student", 0);
        std_id = pref.getString("std_id", null);

        Intent intent = getIntent();
        subject_id = intent.getExtras().getString("subject_id");
        System.out.println("subj: "+subject_id);
        from = intent.getExtras().getString("from");
        if (from.equals("Elearning")) {
            getSubjectDetial();
            getSubjectVideo();
        } else {
            getOtherSubject();
        }
        getMaterial();
    }

    private void getSubjectDetial() {
        Log.i("Setup", "Setup subject detail...");
        SQLiteDatabase Schedule_db = openOrCreateDatabase("Schedule", MODE_PRIVATE, null);
        final Cursor resultSet = Schedule_db.rawQuery("SELECT * FROM Subject_Lecturer sl JOIN Subject s ON sl.subject_id = s.subject_id JOIN Lecturer l ON sl.lecturer_id = l.lecturer_id WHERE s.subject_id='" + subject_id + "';", null);
        resultSet.moveToFirst();
        subjCode.setText(resultSet.getString(resultSet.getColumnIndex("subject_code")));
        subjName.setText(resultSet.getString(resultSet.getColumnIndex("subject_name")));
        lecturer.setText(resultSet.getString(resultSet.getColumnIndex("lecturer_fristname")) + " " + resultSet.getString(resultSet.getColumnIndex("lecturer_lastname")));
        //class_room.setText(resultSet.getString(resultSet.getColumnIndex("subject_room")));
        class_Time.setText(resultSet.getString(resultSet.getColumnIndex("subject_start_time")) + " - " + resultSet.getString(resultSet.getColumnIndex("subject_end_time")));
        telno = resultSet.getString(resultSet.getColumnIndex("lecturer_phone"));
        imgB_call.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent callIntent = new Intent(Intent.ACTION_DIAL);
                callIntent.setData(Uri.parse("tel:" + telno));
                callIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(callIntent);

            }
        });
        imgB_mail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
                emailIntent.setType("plain/text");
                emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{resultSet.getString(resultSet.getColumnIndex("lecturer_email"))});
                startActivity(emailIntent);
            }
        });
        String uri = "lecturer_" + resultSet.getString(resultSet.getColumnIndex("lecturer_fristname")).toLowerCase();
        int imageResource = getResources().getIdentifier(uri, "drawable", getPackageName());
        Drawable image = getResources().getDrawable(imageResource);
        lecturerImage.setImageDrawable(image);
        Log.i("Setup", "Setup subject detail success");
    }

    public void Help(View v) {
        new AlertDialog.Builder(Subject_elearnAll.this)
                .setTitle("Help")
                .setMessage("Highlight with red means you're absent on that class\n" +
                        "Highlight with green means you're already watched that video")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .setIcon(android.R.drawable.ic_menu_help)
                .show();
    }

    private void getSubjectVideo() {
        class GetDataJSON extends AsyncTask<String, Void, String> {
            HttpURLConnection urlConnection = null;
            private String strJSON;

            protected String doInBackground(String... params) {
                try {
                    URL url = new URL("http://54.169.58.93/Elearning_DateList.php?student_id=" + std_id + "&subject_id=" + subject_id + "&status=" + status);
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
                    Log.i("Setup", "Set video detail...");
                    JSONArray data = new JSONArray(strJSON);
                    SQLiteDatabase Elearning_db = openOrCreateDatabase("Elearning", MODE_PRIVATE, null);
                    Elearning_db.execSQL("DROP TABLE IF EXISTS Elearning");
                    Elearning_db.execSQL("CREATE TABLE IF NOT EXISTS Elearning(video_id VARCHAR, video_name VARCHAR, video_link VARCHAR, video_room VARCHAR, video_date VARCHAR, video_visitor_count VARCHAR, subject_id INT,subject_start_time VARCHAR);");
                    TableLayout tl_datelist = (TableLayout) findViewById(R.id.tl_datelist);
                    for (int i = 0; i < data.length(); i++) {
                        JSONObject c = data.getJSONObject(i);
                        Elearning_db.execSQL("INSERT INTO Elearning VALUES('" + c.getString("video_id") + "','" + c.getString("video_name") + "','" + c.getString("video_link") + "','" + c.getString("video_room") + "','" + c.getString("video_date") + "','" + c.getString("video_visitor_count") + "','" + c.getString("subject_id") + "','" + c.getString("subject_start_time") + "');");
                        TableRow row = new TableRow(Subject_elearnAll.this);
                        TextView cell = new TextView(Subject_elearnAll.this);
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
                        watch_status = "";
                        /*check stats*/
                        if (c.getString("class_check_status").equalsIgnoreCase("F") && c.getString("class_check_watch_video").equalsIgnoreCase("F")) {
                            cell.setBackgroundColor(Color.parseColor("#FFAAAE"));
                        } else if (c.getString("class_check_status").equalsIgnoreCase("F") && c.getString("class_check_watch_video").equalsIgnoreCase("T")) {
                            cell.setBackgroundColor(Color.parseColor("#FFAAAE"));
                            watch_status = "Watched.";
                        } else if (c.getString("class_check_status").equalsIgnoreCase("T") && c.getString("class_check_watch_video").equalsIgnoreCase("F")) {

                        } else if (c.getString("class_check_status").equalsIgnoreCase("T") && c.getString("class_check_watch_video").equalsIgnoreCase("T")) {
                            cell.setBackgroundColor(Color.parseColor("#FFADEBC7"));
                            watch_status = "Watched.";
                        }

                        if (c.getString("class_check_status").equalsIgnoreCase("F")) {
                            c_absent++;
                        }
                        absent.setText(String.valueOf(c_absent));
                        String date_temp = c.getString("video_date");
                        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
                        java.util.Date date = df.parse(date_temp);
                        df = new SimpleDateFormat("dd/MM/yyyy");
                        date_temp = df.format(date);
                        cell.setText(date_temp + "  " + c.getString("subject_start_time") + " " + watch_status);
                        cell.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT, 1f));
                        row.addView(cell);
                        row.setLayoutParams(new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.MATCH_PARENT, 1f));
                        tl_datelist.addView(row);
                    }
                    Elearning_db.close();
                    SQLiteDatabase Enrollment_db = openOrCreateDatabase("Enrollment", MODE_PRIVATE, null);
                    Cursor resultSet = Enrollment_db.rawQuery("SELECT * FROM Enrollment;", null);
                    resultSet.moveToFirst();
                    TextView tv_title_semester = (TextView) findViewById(R.id.title_semester);
                    tv_title_semester.setText(" Semester " + resultSet.getString(resultSet.getColumnIndex("enrollment_semester")) + "/" + resultSet.getString(resultSet.getColumnIndex("enrollment_year")));
                    resultSet.close();
                    Elearning_db.close();
                    Log.i("Setup", "Set video detail success");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        new GetDataJSON().execute();
    }

    public void getPastSubjectVideo(View v) {
        TableLayout tl_datelist = (TableLayout) findViewById(R.id.tl_datelist);
        tl_datelist.removeAllViews();
        if (status == "n") {
            status = "p";
            class GetDataJSON extends AsyncTask<String, Void, String> {
                HttpURLConnection urlConnection = null;
                private String strJSON;

                protected String doInBackground(String... params) {
                    try {
                        URL url = new URL("http://54.169.58.93/Elearning_DateList.php?student_id=" + std_id + "&subject_id=" + subject_id + "&status=" + status);
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
                        Log.i("Setup", "Set past video detail...");
                        JSONArray data = new JSONArray(strJSON);
                        if (data.length() > 0) {
                            SQLiteDatabase Elearning_db = openOrCreateDatabase("Elearning", MODE_PRIVATE, null);
                            Elearning_db.execSQL("DROP TABLE IF EXISTS Elearning");
                            Elearning_db.execSQL("CREATE TABLE IF NOT EXISTS Elearning(video_id VARCHAR, video_name VARCHAR, video_link VARCHAR, video_room VARCHAR, video_date VARCHAR, video_visitor_count VARCHAR, subject_id INT, subject_start_time VARCHAR);");
                            TableLayout tl_datelist = (TableLayout) findViewById(R.id.tl_datelist);
                            for (int i = 0; i < data.length(); i++) {
                                JSONObject c = data.getJSONObject(i);
                                Elearning_db.execSQL("INSERT INTO Elearning VALUES('" + c.getString("video_id") + "','" + c.getString("video_name") + "','" + c.getString("video_link") + "','" + c.getString("video_room") + "','" + c.getString("video_date") + "','" + c.getString("video_visitor_count") + "','" + c.getString("subject_id") + "','" + c.getString("subject_start_time") + "');");
                                TableRow row = new TableRow(Subject_elearnAll.this);
                                TextView cell = new TextView(Subject_elearnAll.this);
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
                                String date_temp = c.getString("video_date");
                                DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
                                java.util.Date date = df.parse(date_temp);
                                df = new SimpleDateFormat("dd/MM/yyyy");
                                date_temp = df.format(date);
                                cell.setText(date_temp + "  " + c.getString("subject_start_time"));
                                cell.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT, 1f));
                                row.addView(cell);
                                row.setLayoutParams(new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.MATCH_PARENT, 1f));
                                tl_datelist.addView(row);
                                absent.setText("0");
                                TextView tv_title_semester = (TextView) findViewById(R.id.title_semester);
                                tv_title_semester.setText(" Semester " + c.getString("enrollment_semester") + "/" + c.getString("enrollment_year"));
                            }
                        } else {
                            Toast.makeText(getApplicationContext(), "Not found", Toast.LENGTH_SHORT).show();
                            status = "n";
                            c_absent = 0;
                            getSubjectVideo();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            new GetDataJSON().execute();
        } else {
            status = "n";
            c_absent = 0;
            getSubjectVideo();
        }
    }

    protected void getOtherSubject() {
        class GetDataJSON extends AsyncTask<String, Void, String> {
            HttpURLConnection urlConnection = null;
            private String strJSON;
            protected String doInBackground(String... params) {
                try {
                    URL url = new URL("http://54.169.58.93/Search_VideoLink.php?subject_id=" + subject_id + "&status="+status+"");
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
                    Log.i("Setup", "Setup other subject...");
                    JSONArray data = new JSONArray(strJSON);
                    if(data.length()==0){
                        status = "p";
                        getOtherSubject();
                    }
                    SQLiteDatabase Elearning_db = openOrCreateDatabase("Elearning", MODE_PRIVATE, null);
                    Elearning_db.execSQL("DROP TABLE IF EXISTS Elearning");
                    Elearning_db.execSQL("CREATE TABLE IF NOT EXISTS Elearning(video_id VARCHAR, video_name VARCHAR, video_link VARCHAR, video_room VARCHAR, video_date VARCHAR, video_visitor_count VARCHAR, subject_id INT, subject_start_time VARCHAR);");
                    SQLiteDatabase Subject_db = openOrCreateDatabase("OtherSubject", MODE_PRIVATE, null);
                    Subject_db.execSQL("DROP TABLE IF EXISTS OtherSubject");
                    Subject_db.execSQL("CREATE TABLE IF NOT EXISTS OtherSubject(subject_id VARCHAR, subject_code VARCHAR, subject_name VARCHAR, subject_start_time VARCHAR);");
                    TableLayout tl_datelist = (TableLayout) findViewById(R.id.tl_datelist);
                    for (int i = 0; i < data.length(); i++) {
                        JSONObject c = data.getJSONObject(i);
                        if (i == 0) {
                            /*Set subject detail*/
                            subjCode.setText(c.getString("subject_code"));
                            subjName.setText(c.getString("subject_name"));
                            lecturer.setText(c.getString("lecturer_fristname") + " " + c.getString("lecturer_lastname"));
                            //class_room.setText(resultSet.getString(resultSet.getColumnIndex("subject_room")));
                            class_Time.setText(c.getString("subject_start_time") + " - " + c.getString("subject_end_time"));
                            telno = c.getString("lecturer_phone");
                            email = c.getString("lecturer_email");
                            imgB_call.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent callIntent = new Intent(Intent.ACTION_DIAL);
                                    callIntent.setData(Uri.parse("tel:" + telno));
                                    callIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(callIntent);
                                }
                            });
                            imgB_mail.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
                                    emailIntent.setType("plain/text");
                                    emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{email});
                                    startActivity(emailIntent);
                                }
                            });
                            String uri = "lecturer_" + c.getString("lecturer_fristname").toLowerCase();
                            int imageResource = getResources().getIdentifier(uri, "drawable", getPackageName());
                            Drawable image = getResources().getDrawable(imageResource);
                            lecturerImage.setImageDrawable(image);
                            Subject_db.execSQL("INSERT INTO OtherSubject VALUES('" + c.getString("subject_id") + "','" + c.getString("subject_code") + "','" + c.getString("subject_name") + "','" + c.getString("subject_start_time") + "');");

                            /*Set video datelist*/
                            Elearning_db.execSQL("INSERT INTO Elearning VALUES('" + c.getString("video_id") + "','" + c.getString("video_name") + "','" + c.getString("video_link") + "','" + c.getString("video_room") + "','" + c.getString("video_date") + "','" + c.getString("video_visitor_count") + "','" + c.getString("subject_id") + "','" + c.getString("subject_start_time") + "');");
                            TableRow row = new TableRow(Subject_elearnAll.this);
                            TextView cell = new TextView(Subject_elearnAll.this);
                            cell.setId(i);
                            cell.setClickable(true);
                            cell.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    gotoVideo2(v);
                                }
                            });
                            cell.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
                            cell.setPadding(20, 20, 0, 20);
                            String date_temp = c.getString("video_date");
                            DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
                            java.util.Date date = df.parse(date_temp);
                            df = new SimpleDateFormat("dd/MM/yyyy");
                            date_temp = df.format(date);
                            cell.setText(date_temp + "  " + c.getString("subject_start_time"));
                            cell.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT, 1f));
                            row.addView(cell);
                            row.setLayoutParams(new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.MATCH_PARENT, 1f));
                            tl_datelist.addView(row);
                            absent.setText("0");
                            TextView tv_title_semester = (TextView) findViewById(R.id.title_semester);
                            tv_title_semester.setText(" Semester " + c.getString("enrollment_semester") + "/" + c.getString("enrollment_year"));
                        } else {
                            /*Set video datelist*/
                            Elearning_db.execSQL("INSERT INTO Elearning VALUES('" + c.getString("video_id") + "','" + c.getString("video_name") + "','" + c.getString("video_link") + "','" + c.getString("video_room") + "','" + c.getString("video_date") + "','" + c.getString("video_visitor_count") + "','" + c.getString("subject_id") + "','" + c.getString("subject_start_time") + "');");
                            TableRow row = new TableRow(Subject_elearnAll.this);
                            TextView cell = new TextView(Subject_elearnAll.this);
                            cell.setId(i);
                            cell.setClickable(true);
                            cell.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    gotoVideo2(v);
                                }
                            });
                            cell.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
                            cell.setPadding(20, 20, 0, 20);
                            String date_temp = c.getString("video_date");
                            DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
                            java.util.Date date = df.parse(date_temp);
                            df = new SimpleDateFormat("dd/MM/yyyy");
                            date_temp = df.format(date);
                            cell.setText(date_temp + "  " + c.getString("subject_start_time"));
                            cell.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT, 1f));
                            row.addView(cell);
                            row.setLayoutParams(new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.MATCH_PARENT, 1f));
                            tl_datelist.addView(row);
                            absent.setText("0");
                            TextView tv_title_semester = (TextView) findViewById(R.id.title_semester);
                            tv_title_semester.setText(" Semester " + c.getString("enrollment_semester") + "/" + c.getString("enrollment_year"));
                        }
                    }
                    SQLiteDatabase Enrollment_db = openOrCreateDatabase("Enrollment", MODE_PRIVATE, null);
                    Cursor resultSet = Enrollment_db.rawQuery("SELECT * FROM Enrollment;", null);
                    resultSet.moveToFirst();
                    TextView tv_title_semester = (TextView) findViewById(R.id.title_semester);
                    tv_title_semester.setText(" Semester " + resultSet.getString(resultSet.getColumnIndex("enrollment_semester")) + "/" + resultSet.getString(resultSet.getColumnIndex("enrollment_year")));
                    resultSet.close();
                    Subject_db.close();
                    Elearning_db.close();
                    Log.i("Setup", "Setup other subject sucess");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        new GetDataJSON().execute();
    }

    public void gotoVideo(View v) {
        SQLiteDatabase Subject_db = openOrCreateDatabase("Schedule", MODE_PRIVATE, null);
        SQLiteDatabase Elearning_db = openOrCreateDatabase("Elearning", MODE_PRIVATE, null);
        TextView tv = (TextView) v;
        String text = tv.getText().toString();
        String date = text.substring(0, 10);
        DateFormat df = new SimpleDateFormat("dd/MM/yyyy");
        try {
            java.util.Date date_temp = df.parse(date);
            df = new SimpleDateFormat("yyyy-MM-dd");
            date = df.format(date_temp);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        String time = text.substring(12, 20);
        Cursor rs_elearning = Elearning_db.rawQuery("SELECT * FROM Elearning  WHERE video_date='" + date + "' AND subject_start_time='" + time + "';", null);
        rs_elearning.moveToFirst();
        String subject_id = rs_elearning.getString(rs_elearning.getColumnIndex("subject_id"));
        Cursor rs_subject = Subject_db.rawQuery("SELECT * FROM Subject WHERE subject_id='" + subject_id + "';", null);
        rs_subject.moveToFirst();
        Intent intent = new Intent(this, Video.class);
        intent.putExtra("id", rs_subject.getString(rs_subject.getColumnIndex("subject_id")));
        intent.putExtra("code", rs_subject.getString(rs_subject.getColumnIndex("subject_code")));
        intent.putExtra("name", rs_subject.getString(rs_subject.getColumnIndex("subject_name")));
        intent.putExtra("room", rs_elearning.getString(rs_elearning.getColumnIndex("video_room")));
        intent.putExtra("date", rs_elearning.getString(rs_elearning.getColumnIndex("video_date")));
        intent.putExtra("time", rs_subject.getString(rs_subject.getColumnIndex("subject_start_time")));
        intent.putExtra("count", rs_elearning.getString(rs_elearning.getColumnIndex("video_visitor_count")));
        intent.putExtra("link", rs_elearning.getString(rs_elearning.getColumnIndex("video_link")));
        intent.putExtra("lecturer", lecturer.getText());

        try {
            URL url = new URL("http://54.169.58.93/Elearning_UpdateCount.php?video_id=" + rs_elearning.getString(rs_elearning.getColumnIndex("video_id")));
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            if (urlConnection.getResponseCode() == 200) {
                Log.i("ELS", "Added watch count");
            } else {
                Log.i("ELS", "No added watch count");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        Intent temp = getIntent();
        String from = temp.getExtras().getString("from");
        intent.putExtra("from", from);
        startActivity(intent);
    }

    public void gotoVideo2(View v) {
        SQLiteDatabase Subject_db = openOrCreateDatabase("OtherSubject", MODE_PRIVATE, null);
        SQLiteDatabase Elearning_db = openOrCreateDatabase("Elearning", MODE_PRIVATE, null);
        TextView tv = (TextView) v;
        String text = tv.getText().toString();
        String date = text.substring(0, 10);
        DateFormat df = new SimpleDateFormat("dd/MM/yyyy");
        try {
            java.util.Date date_temp = df.parse(date);
            df = new SimpleDateFormat("yyyy-MM-dd");
            date = df.format(date_temp);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        String time = text.substring(12, 20);
        Cursor rs_elearning = Elearning_db.rawQuery("SELECT * FROM Elearning  WHERE video_date='" + date + "' AND subject_start_time='" + time + "';", null);
        rs_elearning.moveToFirst();
        String subject_id = rs_elearning.getString(rs_elearning.getColumnIndex("subject_id"));
        Cursor rs_subject = Subject_db.rawQuery("SELECT * FROM OtherSubject WHERE subject_id='" + subject_id + "';", null);
        rs_subject.moveToFirst();
        Intent intent = new Intent(this, Video.class);
        intent.putExtra("id", rs_subject.getString(rs_subject.getColumnIndex("subject_id")));
        intent.putExtra("code", rs_subject.getString(rs_subject.getColumnIndex("subject_code")));
        intent.putExtra("name", rs_subject.getString(rs_subject.getColumnIndex("subject_name")));
        intent.putExtra("room", rs_elearning.getString(rs_elearning.getColumnIndex("video_room")));
        intent.putExtra("date", rs_elearning.getString(rs_elearning.getColumnIndex("video_date")));
        intent.putExtra("time", rs_subject.getString(rs_subject.getColumnIndex("subject_start_time")));
        intent.putExtra("count", rs_elearning.getString(rs_elearning.getColumnIndex("video_visitor_count")));
        intent.putExtra("link", rs_elearning.getString(rs_elearning.getColumnIndex("video_link")));
        intent.putExtra("lecturer", lecturer.getText());

        try {
            URL url = new URL("http://54.169.58.93/Elearning_UpdateCount.php?video_id=" + rs_elearning.getString(rs_elearning.getColumnIndex("video_id")));
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            if (urlConnection.getResponseCode() == 200) {
                Log.i("ELS", "Added watch count");
            } else {
                Log.i("ELS", "No added watch count");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        Intent temp = getIntent();
        String from = temp.getExtras().getString("from");
        intent.putExtra("from", from);
        startActivity(intent);
    }

    public void gotoElean(View v) {
        Intent temp = getIntent();
        String from = temp.getExtras().getString("from");
        String dep = temp.getExtras().getString("department");
        if (from.equals("Elearning")) {
            Intent intent = new Intent(this, Elearning.class);
            startActivity(intent);
            Log.i("GT", "Go to Elearning");
        } else if (from.equals("After_allelearn")) {
            Intent intent = new Intent(this, After_allelearn.class);
            System.out.println("DEP: " + dep);
            intent.putExtra("department", dep);
            startActivity(intent);
            Log.i("GT", "Go to Afterall Elearn");
        } else {
            Intent intent = new Intent(this, Main.class);
            startActivity(intent);
            Log.i("GT", "Go to Main");
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (Integer.parseInt(android.os.Build.VERSION.SDK) > 5
                && keyCode == KeyEvent.KEYCODE_BACK
                && event.getRepeatCount() == 0) {
            Log.d("CDA", "onKeyDown Called");
            Intent intent = new Intent(this, Elearning.class);
            startActivity(intent);
            return true;
        }
        return super.onKeyDown(keyCode, event);
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

    private void getMaterial()  {
        class GetDataJSON extends AsyncTask<String, Void, String> {
            HttpURLConnection urlConnection = null;
            private String strJSON;

            protected String doInBackground(String... params) {
                try {
                    URL url = new URL("http://54.169.58.93/Material_List.php?subject_id="+subject_id);
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
                    Log.i("Setup", "Set Material");
                    JSONArray data = new JSONArray(strJSON);
                    TableLayout tl_material = (TableLayout)findViewById(R.id.material_list);
                    for (int i = 0; i < data.length(); i++) {
                        JSONObject c = data.getJSONObject(i);
                        TableRow row = new TableRow(Subject_elearnAll.this);
                        TextView cell = new TextView(Subject_elearnAll.this);
                        cell.setId(i);
                        cell.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
                        cell.setPadding(20, 20, 0, 20);
                        cell.setClickable(true);
                        cell.setMovementMethod(LinkMovementMethod.getInstance());
                        cell.setText(Html.fromHtml(
                                "<a href=\""+c.getString("material_link")+"\">"+encodeUnicode(c.getString("material_title"))+"</a> "));
                        cell.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT, 1f));
                        row.addView(cell);
                        row.setLayoutParams(new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.MATCH_PARENT, 1f));
                        tl_material.addView(row);
                    }
                    Log.i("Setup", "Set Material success");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        new GetDataJSON().execute();
    }
}
