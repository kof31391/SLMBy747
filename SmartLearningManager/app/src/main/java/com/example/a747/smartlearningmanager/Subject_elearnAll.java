package com.example.a747.smartlearningmanager;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

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

public class Subject_elearnAll extends AppCompatActivity {

    private String std_id;
    private String subject_id;
    private String status = "n";
    private String telno;
    private String email;
    private String from;
    private String department;
    private int last_enroll = 0;
    private String lecturer_fristname;
    private TextView subjCode;
    private TextView lecturer;
    private TextView class_room;
    private TextView class_Time;
    private ImageButton imgB_call;
    private ImageButton imgB_mail;
    private ImageView lecturerImage;
    private TextView subjName;
    private Dialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.subject_elearn_all);

        subjCode = (TextView) findViewById(R.id.subjCode);
        subjName = (TextView) findViewById(R.id.subjname);
        lecturer = (TextView) findViewById(R.id.lecturerName);
        class_room = (TextView) findViewById(R.id.class_room);
        class_Time = (TextView) findViewById(R.id.class_Time);
        imgB_call = (ImageButton) findViewById(R.id.imgB_call);
        imgB_mail = (ImageButton) findViewById(R.id.imgB_mail);
        lecturerImage = (ImageView) findViewById(R.id.lecturerImage);

        Intent intent = getIntent();
        subject_id = intent.getExtras().getString("subject_id");
        department = intent.getExtras().getString("department");

        getSubjectDetial();
        getEnrollment();
        getSubjectVideo();
        getMaterial();
    }

    private void getSubjectDetial() {
        class GetDataJSON extends AsyncTask<String, Void, String> {
            HttpURLConnection urlConnection = null;
            private String strJSON;

            protected String doInBackground(String... params) {
                try {
                    URL url = new URL("http://54.169.58.93/Search_SubjectDetail.php?subject_id=" + subject_id);
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
                dialog = new Dialog(Subject_elearnAll.this);
                dialog = getDialogLoading();
                dialog.show();
                try{
                    Log.i("Setup", "Set video detail...");
                    JSONArray data = new JSONArray(strJSON);
                    JSONObject c = data.getJSONObject(0);
                    SQLiteDatabase Subject_db = openOrCreateDatabase("OtherSubject", MODE_PRIVATE, null);
                    Subject_db.execSQL("DROP TABLE IF EXISTS OtherSubject");
                    Subject_db.execSQL("CREATE TABLE IF NOT EXISTS OtherSubject(subject_id VARCHAR, subject_code VARCHAR, subject_name VARCHAR, subject_start_time VARCHAR);");
                    Subject_db.execSQL("INSERT INTO OtherSubject VALUES('" + c.getString("subject_id") + "','" + c.getString("subject_code") + "','" + c.getString("subject_name") + "','" + c.getString("subject_start_time") + "');");
                    subjCode.setText(c.getString("subject_code"));
                    subjName.setText(c.getString("subject_name"));
                    lecturer.setText((lecturer_fristname = c.getString("lecturer_fristname"))+" "+c.getString("lecturer_lastname"));
                    class_Time.setText(c.getString("subject_start_time")+" - "+c.getString("subject_end_time"));
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
                    Bitmap bitmap = DownloadImage("http://54.169.58.93/lecturer_image/lecturer_"+c.getString("lecturer_fristname").toLowerCase()+".gif");
                    lecturerImage.setImageBitmap(bitmap);
                    Subject_db.close();
                    Log.i("Setup", "Set video detail success");
                }catch (Exception e){
                    e.printStackTrace();
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
        InputStream in = null;
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

    protected void getSubjectVideo() {
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
                    Log.i("Setup", "Set video detail...");
                    JSONArray data = new JSONArray(strJSON);
                    if(data.length()>0) {
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
                        }
                        Elearning_db.close();
                    }else{
                        Toast.makeText(getApplicationContext(), "Not found", Toast.LENGTH_SHORT).show();
                        last_enroll = 0;
                        getEnrollment();
                    }
                    Log.i("Setup", "Set video detail success");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        new GetDataJSON().execute();
    }

    protected void getEnrollment(){
        Log.i("Setup", "Set enrollment...");
        SQLiteDatabase Enrollment_db = openOrCreateDatabase("Enrollment", MODE_PRIVATE, null);
        Cursor resultSet = Enrollment_db.rawQuery("SELECT * FROM Enrollment WHERE enrollment_id = (SELECT MAX(enrollment_id) FROM enrollment)-"+last_enroll+";", null);
        resultSet.moveToFirst();
        TextView tv_title_semester = (TextView) findViewById(R.id.title_semester);
        tv_title_semester.setText(" Semester " + resultSet.getString(resultSet.getColumnIndex("enrollment_semester")) + "/" + resultSet.getString(resultSet.getColumnIndex("enrollment_year")));
        resultSet.close();
        Enrollment_db.close();
        Log.i("Setup", "Set enrollment success");
    }

    public void getPastSubjectVideo(View v){
        TableLayout tl_datelist = (TableLayout) findViewById(R.id.tl_datelist);
        tl_datelist.removeAllViews();
        if(status.equalsIgnoreCase("n")){
            status = "p";
            last_enroll = 1;
        }else{
            status = "n";
            last_enroll = 0;
        }
        getSubjectVideo();
    }

    public void gotoVideo(View v) {
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
        intent.putExtra("from","Subject_elearnAll");
        intent.putExtra("department",department);

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

        startActivity(intent);
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

    protected void gotoAllElearn(View v){
        Intent intent = new Intent(this,After_allelearn.class);
        intent.putExtra("department",department);
        startActivity(intent);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)  {
        if (Integer.parseInt(android.os.Build.VERSION.SDK) > 5
                && keyCode == KeyEvent.KEYCODE_BACK
                && event.getRepeatCount() == 0) {
            Log.d("CDA", "onKeyDown Called");
            Intent intent = new Intent(this,After_allelearn.class);
            intent.putExtra("department",department);
            startActivity(intent);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

}
