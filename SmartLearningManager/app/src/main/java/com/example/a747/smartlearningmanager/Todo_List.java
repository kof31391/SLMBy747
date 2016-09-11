package com.example.a747.smartlearningmanager;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.ContextMenu;
import android.view.MenuItem;

import android.view.View;

import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;


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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class Todo_List extends AppCompatActivity {
    private ArrayList<todoObj> items;
    private ListView lvItems;
    private int pos;
    private ArrayAdapter<String> adapter;
    private ArrayList<String> show;
    private String stdid;
    private Spinner spinner;
    private EditText query;
    private SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
    private SimpleDateFormat time = new SimpleDateFormat("HH:mm");
    private ArrayList<Integer> posTemp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.todo_list);
        SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", 0);
        stdid = pref.getString("std_id", null);
        spinner = (Spinner)findViewById(R.id.spinner2);
        query = (EditText)findViewById(R.id.searchBox);
        try {
            lvItems = (MyListView) findViewById(R.id.lvItems);
            show = new ArrayList<>();
            items = new ArrayList<>();
            readItems();
            adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, show);
            lvItems.setAdapter(adapter);
            setupListViewListener();
            registerForContextMenu(lvItems);
        }catch(Exception e){
            new File(stdid+".txt");
            lvItems = (MyListView) findViewById(R.id.lvItems);
            show = new ArrayList<>();
            items = new ArrayList<>();
            readItems();
            adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, show);
            lvItems.setAdapter(adapter);
            setupListViewListener();
            registerForContextMenu(lvItems);
        }
        }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.add("Edit");
        menu.add("Delete");
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        super.onContextItemSelected(item);
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
               int index = info.position;
                if(posTemp==null) {
                    if (item.getTitle().equals("Delete")) {
                        items.remove(index);
                        items.trimToSize();
                        show.remove(index);
                        adapter.notifyDataSetChanged();
                        writeItems();
                    } else if (item.getTitle().equals("Edit")) {
                        sendToEditor(index);
                    }
                }else{
                    int pos = posTemp.get(index);
                    if (item.getTitle().equals("Delete")) {
                        items.remove(pos);
                        items.trimToSize();
                        adapter.notifyDataSetChanged();
                        writeItems();
                        Intent intent = new Intent(this,Todo_List.class);
                        startActivity(intent);
                    } else if (item.getTitle().equals("Edit")) {
                        sendToEditor(pos);
                    }
                }
                return true;
    }

    private void setupListViewListener(){
        lvItems.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                pos = position;
                sendToView(pos);
            }
        });
    }
    private  void sendToEditor(int pos){
        Intent intent = new Intent(this,Todo_edit.class);
        intent.putExtra("todo", pos);
        startActivity(intent);
    }

    private void sendToView(int pos){
        Intent intent = new Intent(this,Todo_View.class);
        intent.putExtra("todo", pos);
        startActivity(intent);
    }

    public void gotoEditor(View v){
    Intent intent = new Intent(this,Todo_Add.class);
        intent.putExtra("fileName",stdid+".txt");
    startActivity(intent);
    }


    private void readItems() {
        File filesDir = getFilesDir();
        File todoFile = new File(filesDir, stdid+".txt");
        try {
            FileInputStream fis = new FileInputStream(todoFile);
            ObjectInputStream ois = new ObjectInputStream(fis);
            Date date;
                items = (ArrayList<todoObj>)ois.readObject();
            for(int j = 0 ;j<items.size();j++) {
                date = items.get(j).getDate();
                show.add(items.get(j).getTopic()+"\n"+"Deadline: "+sdf.format(date)+" at "+time.format(date));
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

    private void writeItems() {
        File filesDir = getFilesDir();
        File todoFile = new File(filesDir, stdid+".txt");
        try {
            ObjectOutputStream fis = new ObjectOutputStream(new FileOutputStream(todoFile));
            fis.writeObject(items);
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

    public void Query(View v){
        String temp ;
        String catTemp;
        Date date;
        String category = String.valueOf(spinner.getSelectedItem());
        posTemp = new ArrayList<>();
        show.clear();
        for(int i = 0;i<items.size();i++){
            temp = items.get(i).getTopic();
            catTemp = items.get(i).getCategory();
            if(temp.contains(query.getText().toString())&&category.equals(catTemp)){
                date = items.get(i).getDate();
                show.add(items.get(i).getTopic()+"\n"+"Deadline: "+sdf.format(date)+" at "+time.format(date));
                posTemp.add(i);
            }else if(temp.contains(query.getText().toString())&&category.equals("Any")){
                date = items.get(i).getDate();
                show.add(items.get(i).getTopic()+"\n"+"Deadline: "+sdf.format(date)+" at "+time.format(date));
                posTemp.add(i);
            }
        }
        adapter.notifyDataSetChanged();
    }
}
