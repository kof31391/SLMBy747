package com.example.a747.smartlearningmanager;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OptionalDataException;
import java.io.StreamCorruptedException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class Noti extends AppCompatActivity {

    private ArrayList<NotificationObj> items;
    private ArrayAdapter<String> adapter;
    private MyListView lvitems;
    private String stdid;
    private ArrayList<String> show;
    private ArrayList<Integer> pos = new ArrayList();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.noti);
        lvitems = (MyListView)findViewById(R.id.noti);
        SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", 0);
        stdid = pref.getString("std_id", null);
        show = new ArrayList<>();
        items = new ArrayList<>();
        try {
            readItems();
        }catch(Exception e){
            new File(stdid+"Notification.txt");
            readItems();
        }
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, show);
        lvitems.setAdapter(adapter);
        setupListViewListener();
        registerForContextMenu(lvitems);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.add("Delete");
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        super.onContextItemSelected(item);
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        int index = info.position;
        if (item.getTitle().equals("Delete")) {
            items.remove(index);
            items.trimToSize();
            show.remove(index);
            adapter.notifyDataSetChanged();
            writeItems();
        }
        return true;
    }

    private void writeItems() {
        File filesDir = getFilesDir();
        File todoFile = new File(filesDir, stdid+"Notification.txt");
        try {
            ObjectOutputStream fis = new ObjectOutputStream(new FileOutputStream(todoFile));
            fis.writeObject(items);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void gotoAbout(View v){
        Intent intent = new Intent(this,About.class);
        startActivity(intent);
    }

    private void setupListViewListener(){
        lvitems.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                sendToDetail(pos.get(position));
            }
        });
    }

    private void sendToDetail(int pos){
            Intent intent = new Intent(this, Todo_View.class);
            intent.putExtra("todo", pos);
            intent.putExtra("check","noti");
            startActivity(intent);

    }

    public void ClearNotification(View v){
        File filesDir = getFilesDir();
        File todoFile = new File(filesDir, stdid+"Notification.txt");
        items = new ArrayList<>();
        try {
            ObjectOutputStream fis = new ObjectOutputStream(new FileOutputStream(todoFile));
            fis.writeObject(items);
            adapter.notifyDataSetChanged();
            startActivity(new Intent(this,Noti.class));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void readItems() {
        File filesDir = getFilesDir();
        File todoFile = new File(filesDir, stdid+"Notification.txt");
        items = new ArrayList<>();
        try {
            FileInputStream fis = new FileInputStream(todoFile);
            ObjectInputStream ois = new ObjectInputStream(fis);
            items = (ArrayList<NotificationObj>)ois.readObject();
            for(int j = 0 ;j<items.size();j++) {
                Date now = Calendar.getInstance().getTime();
                Date that = items.get(j).getDate();
                if(now.compareTo(that)>-1&&items.get(j).isFinish()==false) {
                    pos.add(j);
                    show.add(items.get(j).getTopic() + "\n" + items.get(j).getCategory());
                }
            }


        } catch (EOFException e) {

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (OptionalDataException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (StreamCorruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void gotoTodo(View v){
        Intent intent = new Intent(this, Todo_List.class);
        startActivity(intent);
    }

    public void gotoSetting(View v){
        Intent intent = new Intent(this, more_setting.class);
        startActivity(intent);
    }

    public void gotoHome(View v){
        Intent intent = new Intent(this, Main.class);
        startActivity(intent);
    }
    public void gotoNoti(View v){
        Intent intent = new Intent(this, Noti.class);
        startActivity(intent);
    }
    public void gotoElean(View v){
        Intent intent = new Intent(this, Elearning.class);
        startActivity(intent);
    }
}
