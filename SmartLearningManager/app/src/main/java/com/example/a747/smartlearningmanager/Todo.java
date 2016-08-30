package com.example.a747.smartlearningmanager;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
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
import java.util.ArrayList;

public class Todo extends AppCompatActivity {
    private ArrayList<todoObj> items;
    private ListView lvItems;
    private int pos;
    private ArrayAdapter<String> adapter;
    private String topic  = "";
    private ArrayList<String> show;
    private todoObj temp;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.todo);
        lvItems = (ListView) findViewById(R.id.lvItems);
        show = new ArrayList<String>();
        items = new ArrayList<todoObj>();
        readItems();
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, show);
        lvItems.setAdapter(adapter);
        setupListViewListener();
        registerForContextMenu(lvItems);

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
        if (item.getTitle().equals("Delete")) {
            items.remove(pos);
            adapter.notifyDataSetChanged();
            writeItems();
        }
        return true;
    }

    private void setupListViewListener(){
        lvItems.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                todoObj obj = ((todoObj)items.get(position));
                sendToEditor(obj);
            }
        });
    }
    private  void sendToEditor(todoObj obj){
        Intent intent = new Intent(this,Todo_View.class);
        intent.putExtra("todo", (Parcelable) obj);
        startActivity(intent);
    }

    public void gotoEditor(View v){
    Intent intent = new Intent(this,Todo_View.class);
    startActivity(intent);
    }



    private void readItems() {
        int i = 1;
        File filesDir = getFilesDir();
        File todoFile = new File(filesDir, "todo.txt");
        items = new ArrayList<todoObj>();
        try {
            FileInputStream fis = new FileInputStream(todoFile);
            ObjectInputStream ois = new ObjectInputStream(fis);

                items = (ArrayList<todoObj>)ois.readObject();
            for(int j = 0 ;j<items.size();j++) {
                show.add(items.get(j).getTopic());
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
        File todoFile = new File(filesDir, "todo.txt");
        ArrayList<todoObj> obj = new ArrayList<todoObj>();
        try {
            ObjectOutputStream fis = new ObjectOutputStream(new FileOutputStream(todoFile));
            fis.writeObject(items);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void gotoTodo(View v){
        Intent intent = new Intent(this, Todo.class);
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





}
