package com.example.a747.smartlearningmanager;

import android.app.Activity;
import android.widget.TextView;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

/**
 * Created by 747 on 25-Aug-16.
 */
public class Test extends Activity {
    public static void main(String[] args) throws IOException {
        URL url = new URL("http://192.168.1.35/slm/TestLDAP.php");
        HttpURLConnection client = (HttpURLConnection) url.openConnection();
        client.setRequestMethod("POST");
        String urlParameters = "username=56130500094&password=56130500094";
        client.setDoOutput(true);
        DataOutputStream wr = new DataOutputStream(client.getOutputStream());
        wr.writeBytes(urlParameters);
        wr.flush();
        wr.close();
        client.connect();
        BufferedReader reader = new BufferedReader(new InputStreamReader(client.getInputStream()));
        StringBuilder stringBuilder = new StringBuilder();
        String line = null;
        while ((line = reader.readLine()) != null) {
            stringBuilder.append(line + "\n");

        }
        System.out.println(stringBuilder.toString());
     /*   StringBuilder stringBuilder = null;
        String params[] = {"56130500094","56130500094"};
            URL url = new URL("http://192.168.1.35/slm/TestLDAP.php");
            HttpURLConnection client = (HttpURLConnection) url.openConnection();
            System.out.println(client.getResponseMessage());
            client.setRequestMethod("POST");
            String urlParameters = "username=" + params[0] + "&password=" + params[1];
             System.out.println(urlParameters);
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
            System.out.println("///////"+stringBuilder.toString());
        }*/


    }
}
