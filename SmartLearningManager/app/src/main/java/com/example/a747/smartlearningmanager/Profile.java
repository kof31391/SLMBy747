package com.example.a747.smartlearningmanager;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class Profile extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", 0);
        String std_id = pref.getString("std_id", null);
        if(std_id != null){
            super.onCreate(savedInstanceState);
            setContentView(R.layout.profile);
        }else{
            Intent intent = new Intent(this, Login.class);
            startActivity(intent);
        }
        getProfile(std_id);
    }
    public void getProfile(String std_id){
        class GetDataJSON extends AsyncTask<String,Void,String> {
            HttpURLConnection urlConnection = null;
            public String strJSON;
            protected String doInBackground(String... params) {
                try {
                    URL url = new URL("http://54.169.58.93//Profile.php?std_id="+params[0]);
                    urlConnection = (HttpURLConnection) url.openConnection();
                    int code = urlConnection.getResponseCode();
                    if(code==200){
                        InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                        if (in != null) {
                            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in));
                            String line = "";
                            while ((line = bufferedReader.readLine()) != null)
                                strJSON = line;
                        }
                        in.close();
                    }
                    return strJSON;
                }catch (Exception e){
                    e.printStackTrace();
                }finally {
                    urlConnection.disconnect();
                }
                return strJSON;
            }
            protected void onPostExecute(String strJSON) {
                try{
                    JSONArray data = new JSONArray(strJSON);
                    JSONObject c = data.getJSONObject(0);

                    TextView tv_pf;
                    tv_pf = (TextView) findViewById(R.id.tv_pf_std_id);
                    tv_pf.setText(c.getString("std_id"));
                    tv_pf = (TextView) findViewById(R.id.tv_pf_fristname);
                    tv_pf.setText(c.getString("firstname"));
                    tv_pf = (TextView) findViewById(R.id.tv_pf_lastname);
                    tv_pf.setText(c.getString("lastname"));
                    tv_pf = (TextView) findViewById(R.id.tv_pf_grade);
                    tv_pf.setText(c.getString("grade"));
                    tv_pf = (TextView) findViewById(R.id.tv_pf_email);
                    tv_pf.setText(c.getString("email"));
                    tv_pf = (TextView) findViewById(R.id.tv_pf_teleno);
                    tv_pf.setText(c.getString("phonenum"));
                    tv_pf = (TextView) findViewById(R.id.tv_pf_image);
                    tv_pf.setText(c.getString("image"));
                }catch(Exception e){
                    e.printStackTrace();
                }
            }
        }
        new GetDataJSON().execute(std_id);
    }
}
