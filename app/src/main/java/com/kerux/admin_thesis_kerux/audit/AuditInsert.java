package com.kerux.admin_thesis_kerux.audit;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Spinner;
import android.widget.Toast;

import com.kerux.admin_thesis_kerux.security.SecurityWEB;
import com.kerux.admin_thesis_kerux.spinner.Connector;
import com.kerux.admin_thesis_kerux.spinner.Parser;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class AuditInsert extends AsyncTask<Void,Void,String> {//TO BE USED WHEN WE HAVE A SERVER
    String first;
    String second;
    String third;
    String fourth;
    String fifth;
    String sixth;

    public AuditInsert(String first, String second, String third, String fourth, String fifth, String sixth) {
        this.first = first;
        this.second = second;
        this.third = third;
        this.fourth = fourth;
        this.fifth = fifth;
        this.sixth = sixth;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

    }

    @Override
    protected String doInBackground(Void... params) {
        try {
            URL url = new URL("https://isproj2a.benilde.edu.ph/Sympl/InsertAuditAdminServlet");
            URLConnection connection = url.openConnection();

            connection.setReadTimeout(30000);
            connection.setConnectTimeout(60000);
            connection.setDoInput(true);
            connection.setDoOutput(true);

            Uri.Builder builder = new Uri.Builder()
                    .appendQueryParameter("first", SecurityWEB.encrypt(first))
                    .appendQueryParameter("second", SecurityWEB.encrypt(second))
                    .appendQueryParameter("third", SecurityWEB.encrypt(third))
                    .appendQueryParameter("fourth", SecurityWEB.encrypt(fourth))
                    .appendQueryParameter("fifth", SecurityWEB.encrypt(fifth))
                    .appendQueryParameter("sixth", SecurityWEB.encrypt(sixth));
            String query = builder.build().getEncodedQuery();

            OutputStream os = connection.getOutputStream();
            BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(os, StandardCharsets.UTF_8));
            writer.write(query);
            writer.flush();
            writer.close();
            os.close();

            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String returnString="";
            ArrayList<String> output=new ArrayList<String>();
            while ((returnString = in.readLine()) != null)
            {
                Log.d("returnString", returnString);
                output.add(returnString);
            }
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
            return "notinserted";
        }
        return "inserted";
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
    }


}
