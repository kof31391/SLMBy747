package com.example.a747.smartlearningmanager;


import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.content.SharedPreferences;

import com.android.volley.RequestQueue;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutionException;

public class Login extends AppCompatActivity {   //อันนี้คือหน้า login
    TextView uid;
    TextView pwd;
    Button btn;
    String uidString ;
    String passString ;
    TextView Err;
    RequestQueue req;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
        SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", 0);
        String std_id = pref.getString("std_id", null);
        if(std_id != null){
            Intent intent = new Intent(Login.this, Main.class);
            startActivity(intent);
        }
    }
    public void onClickLogin(View view) throws IOException, ExecutionException, InterruptedException {

        uid= (TextView)findViewById(R.id.IdForm);
        pwd = (TextView)findViewById(R.id.PwdForm);
        btn = (Button)findViewById(R.id.LoginBtn);
        Err = (TextView)findViewById(R.id.err);
        uidString = uid.getText().toString();
        passString = pwd.getText().toString();
        LDAPRequests ldap = new LDAPRequests();
        ldap.execute(uidString,passString);

    }

    class LDAPRequests extends AsyncTask<String, Void, String> {

        private String res;

        @Override
        protected String doInBackground(String... params) {
            StringBuilder stringBuilder = null;
            try {
                URL url = new URL("http://54.169.58.93/TestLdap.php");
                HttpURLConnection client = (HttpURLConnection) url.openConnection();
                client.setRequestMethod("POST");
                String urlParameters = "username=" + params[0] + "&password=" + params[1];
                System.out.println("USERNAME "+ params[0]);
                client.setDoOutput(true);
                DataOutputStream wr = new DataOutputStream(client.getOutputStream());
                wr.writeBytes(urlParameters);
                wr.flush();
                wr.close();
                client.connect();
                BufferedReader reader = new BufferedReader(new InputStreamReader(client.getInputStream()));
                stringBuilder = new StringBuilder();
                String line = null;
                while ((line = reader.readLine()) != null) {
                    stringBuilder.append(line + "\n");
                }
                System.out.println(stringBuilder.toString());
                res = stringBuilder.toString();
            } catch (IOException ex) {
                System.out.println(ex);
            }
            return res;
        }

        public String getRes() {
            return res;
        }

        @Override
        protected void onPostExecute(String message) {
            Intent intent = new Intent(getApplicationContext(), Main.class);
            if(message.trim().equals("true")) {
                intent.putExtra("msg", message);
                intent.putExtra("msg", message);
                startActivity(intent);
                SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", 0);
                SharedPreferences.Editor editor = pref.edit();
                editor.putString("std_id", uidString);
                editor.commit();
                finish();
            }else{
                Err.setText("Wrong Username or password");
            }
        }
    }
}
