package com.example.a747.smartlearningmanager;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

public class todo_item extends AppCompatActivity {
    private EditText desc;
    private EditText todoDate;
    private EditText todoTime;
    private TextView topic;
    private ArrayList<todoObj> items;
    private todoObj recObj;
    private ArrayList<String> show;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.todo_item);
        Intent intent = getIntent();
        items = new ArrayList<todoObj>();
        topic = (TextView)findViewById(R.id.topic);
        desc = (EditText)findViewById(R.id.todoDesc);
        todoTime =(EditText)findViewById(R.id.todoTime);
        todoDate = (EditText)findViewById(R.id.todoDate);
        show = new ArrayList<String>();
        if(intent.getParcelableExtra("todo")!=null) {
            recObj = intent.getParcelableExtra("todo");
            topic.setText(recObj.getTopic());
            desc.setText(recObj.getDesc());
        }else{
            topic.setHint("Enter Topic Here");
            topic.requestFocus();
        }
    }

    public void onClickAddTodo(View view) {

        writeItems();
        Intent intent = new Intent(this,Todo.class);
        startActivity(intent);
    }


    private void readItems() {
        File filesDir = getFilesDir();
        File todoFile = new File(filesDir, "todo.txt");
        items = new ArrayList<todoObj>();
        try {
            FileInputStream fis = new FileInputStream(todoFile);
            ObjectInputStream ois = new ObjectInputStream(fis);
            while(fis.available()>0){
                todoObj temp = (todoObj)ois.readObject();
                items.add(temp);
                show.add(temp.getTopic());
            }

        } catch (IOException e) {
            items = new ArrayList<todoObj>();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void writeItems() {
        File filesDir = getFilesDir();
        File todoFile = new File(filesDir, "todo.txt");
        try {
            todoObj temp = new todoObj();
            temp.setTopic(topic.getText().toString());
            temp.setDesc(desc.getText().toString());
         //   temp.setDate(todoDate.getText().toString());
            ObjectOutputStream ois = new ObjectOutputStream(new FileOutputStream(todoFile));
            ois.writeObject(temp);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
