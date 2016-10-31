package com.example.a747.smartlearningmanager;

import android.app.Dialog;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.app.NotificationCompat;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

public class Subject_elearn extends AppCompatActivity {
    private String host = "http://54.254.251.65/";
    private String std_id;
    private String subject_id;
    private String status = "n";
    private String telno;
    private String watch_status;
    private int c_absent = 0;
    private TextView subjCode;
    private TextView lecturer;
    private TextView class_room;
    private TextView class_Time;
    private ImageButton imgB_call;
    private ImageButton imgB_mail;
    private ImageView lecturerImage;
    private TextView subjName;
    private TextView absent;
    private Dialog dialog;


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
        getSubjectDetial();
        getSubjectVideo();
        getMaterial();
    }

    private void getSubjectDetial() {
        Log.i("Setup", "Setup subject detail...");
        SQLiteDatabase Schedule_db = openOrCreateDatabase("Schedule", MODE_PRIVATE, null);
        final Cursor resultSet = Schedule_db.rawQuery("SELECT * FROM Subject_Lecturer sl JOIN Subject s ON sl.subject_id = s.subject_id JOIN Lecturer l ON sl.lecturer_id = l.lecturer_id WHERE s.subject_id='" + subject_id + "';", null);
        resultSet.moveToFirst();
        subjCode.setText(resultSet.getString(resultSet.getColumnIndex("subject_code")));
        subjName.setText(resultSet.getString(resultSet.getColumnIndex("subject_name")));
        lecturer.setText(resultSet.getString(resultSet.getColumnIndex("lecturer_firstname")) + " " + resultSet.getString(resultSet.getColumnIndex("lecturer_lastname")));
        class_room.setText(resultSet.getString(resultSet.getColumnIndex("subject_room")));
        class_Time.setText(resultSet.getString(resultSet.getColumnIndex("subject_start_time")).substring(0,5) + " - " + resultSet.getString(resultSet.getColumnIndex("subject_end_time")).substring(0,5));
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
        Bitmap bitmap = DownloadImage(host+"lecturer_image/lecturer_"+resultSet.getString(resultSet.getColumnIndex("lecturer_firstname")).toLowerCase()+".gif");
        lecturerImage.setImageBitmap(bitmap);
        Schedule_db.close();
        Log.i("Setup", "Setup subject detail success");
    }

    private InputStream OpenHttpConnection(String urlString) throws IOException {
        InputStream in = null;
        int response = -1;
        URL url = new URL(urlString);
        URLConnection conn = url.openConnection();
        if (!(conn instanceof HttpURLConnection))
            throw new IOException("Not an HTTP connection");
        try {
            HttpURLConnection httpConn = (HttpURLConnection) conn;
            httpConn.setAllowUserInteraction(false);
            httpConn.setInstanceFollowRedirects(true);
            httpConn.setRequestMethod("GET");
            httpConn.connect();
            response = httpConn.getResponseCode();
            if (response == HttpURLConnection.HTTP_OK) {
                in = httpConn.getInputStream();
            }
        } catch (Exception ex) {
            throw new IOException("Error connecting");
        }
        return in;
    }

    private Bitmap DownloadImage(String URL) {
        Bitmap bitmap = null;
        InputStream in;
        try {
            in = OpenHttpConnection(URL);
            bitmap = BitmapFactory.decodeStream(in);
            in.close();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        return bitmap;
    }

    public void Help(View v) {
        new AlertDialog.Builder(Subject_elearn.this)
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
                    URL url = new URL(host+"Elearning_DateList.php?student_id=" + std_id + "&subject_id=" + subject_id + "&status=" + status);
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
                Log.i("INFO", "Loading...");
                dialog = new Dialog(Subject_elearn.this);
                dialog = getDialogLoading();
                dialog.show();
                try {
                    Log.i("Setup", "Set video detail...");
                    JSONArray data = new JSONArray(strJSON);
                    SQLiteDatabase Elearning_db = openOrCreateDatabase("Elearning", MODE_PRIVATE, null);
                    Elearning_db.execSQL("DROP TABLE IF EXISTS Elearning");
                    Elearning_db.execSQL("CREATE TABLE IF NOT EXISTS Elearning(video_id VARCHAR, video_room VARCHAR, video_date VARCHAR, video_visitor_count VARCHAR, video_link VARCHAR, subject_id VARCHAR, subject_start_time VARCHAR);");
                    TableLayout tl_datelist = (TableLayout) findViewById(R.id.tl_datelist);
                    for (int i = 0; i < data.length(); i++) {
                        JSONObject c = data.getJSONObject(i);
                        Elearning_db.execSQL("INSERT INTO Elearning VALUES('"+c.getString("video_id")+"','" + c.getString("video_room") + "','" + c.getString("video_date") + "','" + c.getString("video_visitor_count") + "','" + c.getString("video_link") + "','" + c.getString("subject_id") + "','"+c.getString("subject_start_time")+"');");
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
                        cell.setText(date_temp + "  " + c.getString("subject_start_time").substring(0,5) + " " + watch_status);
                        cell.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT, 1f));
                        row.addView(cell);
                        row.setLayoutParams(new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.MATCH_PARENT, 1f));
                        tl_datelist.addView(row);
                        TextView tv_title_semester = (TextView) findViewById(R.id.title_semester);
                        tv_title_semester.setText(" semester "+c.getString("enrollment_semester")+" / "+c.getString("enrollment_year"));
                    }
                    Elearning_db.close();
                    Log.i("Setup", "Set video detail success");
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if(c_absent == 0){
                    LinearLayout ll_absent = (LinearLayout) findViewById(R.id.ll_absent);
                    ll_absent.setBackgroundColor(Color.parseColor("#269900"));
                    TextView tv_absent_tile = (TextView) findViewById(R.id.absent_tile);
                    tv_absent_tile.setTextColor(Color.parseColor("#ffffff"));
                    TextView tv_absent = (TextView) findViewById(R.id.absent);
                    tv_absent.setBackgroundColor(Color.parseColor("#269900"));
                    tv_absent.setTextColor(Color.parseColor("#ffffff"));
                }else if(c_absent == 1){
                    LinearLayout ll_absent = (LinearLayout) findViewById(R.id.ll_absent);
                    ll_absent.setBackgroundColor(Color.parseColor("#ffff66"));
                    TextView tv_absent_tile = (TextView) findViewById(R.id.absent_tile);
                    tv_absent_tile.setTextColor(Color.parseColor("#4d4d4d"));
                    TextView tv_absent = (TextView) findViewById(R.id.absent);
                    tv_absent.setBackgroundColor(Color.parseColor("#ffff66"));
                    tv_absent.setTextColor(Color.parseColor("#4d4d4d"));
                }else if(c_absent == 2){
                    LinearLayout ll_absent = (LinearLayout) findViewById(R.id.ll_absent);
                    ll_absent.setBackgroundColor(Color.parseColor("#ffad33"));
                    TextView tv_absent_tile = (TextView) findViewById(R.id.absent_tile);
                    tv_absent_tile.setTextColor(Color.parseColor("#404040"));
                    TextView tv_absent = (TextView) findViewById(R.id.absent);
                    tv_absent.setBackgroundColor(Color.parseColor("#ffad33"));
                    tv_absent.setTextColor(Color.parseColor("#404040"));
                }else{
                    TextView tv_subjcode = (TextView) findViewById(R.id.subjCode);
                    dialogAbsent(tv_subjcode.getText().toString()+" has been FE.");
                }
                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        dialog.cancel();
                    }
                }, 2000);
                Log.i("INFO", "Loading complete");
            }
        }
        new GetDataJSON().execute();
    }

    private Dialog getDialogLoading(){
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.loading);
        dialog.setCancelable(true);
        return  dialog;
    }

    private void dialogAbsent(String content){
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_absent);
        dialog.setCancelable(true);
        TextView tv_content = (TextView) dialog.findViewById(R.id.tv_da_content);
        tv_content.setText(content);
        Button done = (Button)dialog.findViewById(R.id.bt_da_done);
        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.cancel();
            }
        });
        dialog.show();
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
                        URL url = new URL(host+"Elearning_DateList.php?student_id=" + std_id + "&subject_id=" + subject_id + "&status=" + status);
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
                    Log.i("INFO", "Loading...");
                    dialog = new Dialog(Subject_elearn.this);
                    dialog = getDialogLoading();
                    dialog.show();
                    try {
                        Log.i("Setup", "Set past video detail...");
                        JSONArray data = new JSONArray(strJSON);
                        if (data.length() > 0) {
                            SQLiteDatabase Elearning_db = openOrCreateDatabase("Elearning", MODE_PRIVATE, null);
                            Elearning_db.execSQL("DROP TABLE IF EXISTS Elearning");
                            Elearning_db.execSQL("CREATE TABLE IF NOT EXISTS Elearning(video_id VARCHAR, video_room VARCHAR, video_date VARCHAR, video_visitor_count VARCHAR, video_link VARCHAR, subject_id VARCHAR);");
                            TableLayout tl_datelist = (TableLayout) findViewById(R.id.tl_datelist);
                            for (int i = 0; i < data.length(); i++) {
                                JSONObject c = data.getJSONObject(i);
                                Elearning_db.execSQL("INSERT INTO Elearning VALUES('"+c.getString("video_id")+"','" + c.getString("video_room") + "','" + c.getString("video_date") + "','" + c.getString("video_visitor_count") + "','" + c.getString("video_link") + "','" + c.getString("subject_id") + "');");
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
                                String date_temp = c.getString("video_date");
                                DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
                                java.util.Date date = df.parse(date_temp);
                                df = new SimpleDateFormat("dd/MM/yyyy");
                                date_temp = df.format(date);
                                cell.setText(date_temp + "  " + c.getString("subject_start_time").substring(0,5));
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
                            dialog.cancel();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
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
            new GetDataJSON().execute();
        } else {
            status = "n";
            c_absent = 0;
            getSubjectVideo();
        }
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
        String time = text.substring(12, 17)+":00";
        Cursor rs_elearning = Elearning_db.rawQuery("SELECT * FROM Elearning WHERE subject_id='" + subject_id + "' AND video_date='" + date + "' AND subject_start_time='" + time + "';", null);
        rs_elearning.moveToFirst();
        Cursor rs_subject = Subject_db.rawQuery("SELECT * FROM Subject WHERE subject_id='" + subject_id + "';", null);
        rs_subject.moveToFirst();
        Intent intent = new Intent(this, Video.class);
        intent.putExtra("id", rs_subject.getString(rs_subject.getColumnIndex("subject_id")));
        intent.putExtra("video_id", rs_elearning.getString(rs_elearning.getColumnIndex("video_id")));
        intent.putExtra("code", rs_subject.getString(rs_subject.getColumnIndex("subject_code")));
        intent.putExtra("name", rs_subject.getString(rs_subject.getColumnIndex("subject_name")));
        intent.putExtra("room", rs_elearning.getString(rs_elearning.getColumnIndex("video_room")));
        intent.putExtra("date", rs_elearning.getString(rs_elearning.getColumnIndex("video_date")));
        intent.putExtra("time", rs_subject.getString(rs_subject.getColumnIndex("subject_start_time")));
        intent.putExtra("count", rs_elearning.getString(rs_elearning.getColumnIndex("video_visitor_count")));
        intent.putExtra("link", rs_elearning.getString(rs_elearning.getColumnIndex("video_link")));
        intent.putExtra("lecturer", lecturer.getText());
        intent.putExtra("subject_id",subject_id);
        intent.putExtra("from","Subject_elearn");
        try {
            URL url = new URL(host+"Elearning_UpdateCount.php?video_id=" + rs_elearning.getString(rs_elearning.getColumnIndex("video_id")));
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
            intent.putExtra("department", dep);
            startActivity(intent);
            Log.i("GT", "Go to After all Elearn");
        } else if(from.equals("Main")){
            Intent intent = new Intent(this, Main.class);
            startActivity(intent);
            Log.i("GT", "Go to Main");
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
            Intent temp = getIntent();
            String from = temp.getExtras().getString("from");
            String dep = temp.getExtras().getString("department");
            if (from.equals("Elearning")) {
                Intent intent = new Intent(this, Elearning.class);
                startActivity(intent);
                Log.i("GT", "Go to Elearning");
            } else if (from.equals("After_allelearn")) {
                Intent intent = new Intent(this, After_allelearn.class);
                intent.putExtra("department", dep);
                startActivity(intent);
                Log.i("GT", "Go to After all Elearn");
            } else if(from.equals("Main")){
                Intent intent = new Intent(this, Main.class);
                startActivity(intent);
                Log.i("GT", "Go to Main");
            } else {
                Intent intent = new Intent(this, Main.class);
                startActivity(intent);
                Log.i("GT", "Go to Main");
            }
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
                    URL url = new URL(host+"Material_List.php?subject_id="+subject_id);
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
                        TableRow row = new TableRow(Subject_elearn.this);
                        TextView cell = new TextView(Subject_elearn.this);
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
