package com.example.a747.smartlearningmanager;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;


public class Video_object extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "Video";
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_CREATE = "CREATE TABLE IF NOT EXISTS Video (e_code VARCHAR,e_name VARCHAR,e_room VARCHAR,e_date VARCHAR,e_time VARCHAR,e_link VARCHAR,lastMinute VARCHAR);";

    private String v_id;
    private String e_code;
    private String e_name;
    private String e_room;
    private String e_date;
    private String e_time;
    private String e_link;
    private int lastMinute;

    public Video_object(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public String getV_id() {
        return v_id;
    }

    public String getE_code(){
        return e_code;
    }

    public String getE_name() {
        return e_name;
    }

    public String getE_room() {
        return e_room;
    }

    public String getE_date() {
        return e_date;
    }

    public String getE_time() {
        return e_time;
    }

    public String getE_link() {
        return e_link;
    }

    public int getLastMinute() {
        return lastMinute;
    }

    public void setV_id(String v_id) {
        this.v_id = v_id;
    }

    public void setE_code(String e_code) {
        this.e_code = e_code;
    }

    public void setE_name(String e_name) {
        this.e_name = e_name;
    }

    public void setE_room(String e_room) {
        this.e_room = e_room;
    }

    public void setE_date(String e_date) {
        this.e_date = e_date;
    }

    public void setE_time(String e_time) {
        this.e_time = e_time;
    }

    public void setE_link(String e_link) {
        this.e_link = e_link;
    }

    public void setLastMinute(int lastMinute) {
        this.lastMinute = lastMinute;
    }

    public long saveInstace(){
        long rows = 0;
        try{
            SQLiteDatabase db;
            db = this.getWritableDatabase();
            SQLiteStatement insertCmd;
            String strSQL = "INSERT INTO Video SELECT * FROM (SELECT ?,?,?,?,?,?,?) AS tmp WHERE NOT EXISTS (SELECT * FROM Video WHERE e_code=? AND e_date=? AND e_time=?);";
            insertCmd = db.compileStatement(strSQL);
            insertCmd.bindString(1,e_code);
            insertCmd.bindString(2,e_name);
            insertCmd.bindString(3,e_room);
            insertCmd.bindString(4,e_date);
            insertCmd.bindString(5,e_time);
            insertCmd.bindString(6,e_link);
            insertCmd.bindString(7,String.valueOf(lastMinute));
            insertCmd.bindString(8,e_code);
            insertCmd.bindString(9,e_date);
            insertCmd.bindString(10,e_time);
            rows = insertCmd.executeInsert();
            if(rows == -1){
                strSQL = "UPDATE Video SET lastMinute=? WHERE e_code=? AND e_date=? AND e_time=?";
                insertCmd = db.compileStatement(strSQL);
                insertCmd.bindString(1,String.valueOf(lastMinute));
                insertCmd.bindString(2,e_code);
                insertCmd.bindString(3,e_date);
                insertCmd.bindString(4,e_time);
                rows = insertCmd.executeInsert();
            }
            db.close();
        }catch (Exception e){
            e.printStackTrace();
        }
        return rows;
    }

    public Boolean readInstace(String e_code,String e_date,String e_time){
        Boolean isSet = false;
        try{
            SQLiteDatabase db;
            db = this.getReadableDatabase();
            Cursor cursor = db.rawQuery("SELECT * FROM Video WHERE e_code='"+e_code+"' AND e_date='"+e_date+"' AND e_time='"+e_time+"';",null);
            cursor.moveToFirst();
            if(cursor.getCount() >0) {
                e_code = cursor.getString(cursor.getColumnIndex("e_code"));
                e_name = cursor.getString(cursor.getColumnIndex("e_name"));
                e_room = cursor.getString(cursor.getColumnIndex("e_room"));
                e_date = cursor.getString(cursor.getColumnIndex("e_date"));
                e_time = cursor.getString(cursor.getColumnIndex("e_time"));
                e_link = cursor.getString(cursor.getColumnIndex("e_link"));
                lastMinute = cursor.getInt(cursor.getColumnIndex("lastMinute"));
                isSet = true;
            }
            cursor.close();
            db.close();
        }catch (Exception e){
            e.printStackTrace();
        }
        return isSet;
    }

    @Override
    public String toString() {
        return "Video_object{" +
                "e_code='" + e_code + '\'' +
                ", e_name='" + e_name + '\'' +
                ", e_room='" + e_room + '\'' +
                ", e_date='" + e_date + '\'' +
                ", e_time='" + e_time + '\'' +
                ", e_link='" + e_link + '\'' +
                ", lastMinute=" + lastMinute +
                '}';
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DATABASE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(DATABASE_NAME,"Upgread database version from version  "+oldVersion+" to "+newVersion+" ,which will destroy all old data");
        db.execSQL("DROP TABLE IF EXISTS Video");
        onCreate(db);
    }
}