package com.example.a747.smartlearningmanager;

import android.app.Activity;
import android.os.AsyncTask;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by 747 on 25-Aug-16.
 */
public class LDAPRequest extends AsyncTask<String, Void, String> {

    private String res;

    @Override
    protected String doInBackground(String... params) {
        StringBuilder stringBuilder = null;
        try {
            URL url = new URL("http://localhost/slm/TestLDAP.php");
            HttpURLConnection client = (HttpURLConnection) url.openConnection();
            client.setRequestMethod("POST");
            String urlParameters = "username=" + params[0] + "&password=" + params[1];
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

    }
}
