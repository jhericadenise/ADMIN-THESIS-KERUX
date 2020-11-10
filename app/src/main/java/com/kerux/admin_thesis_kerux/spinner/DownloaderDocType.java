package com.kerux.admin_thesis_kerux.spinner;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.widget.Spinner;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;

public class DownloaderDocType extends AsyncTask<Void,Void,String> {//TO BE USED WHEN WE HAVE A SERVER

    Context c;
    String urlAddress;
    Spinner sp;
    ProgressDialog pd;
    String columnName;
    String headerValue;

    public DownloaderDocType(Context c, String urlAddress, Spinner sp,String columnName, String headerValue) {
        this.c = c;
        this.urlAddress = urlAddress;
        this.sp = sp;
        this.columnName = columnName;
        this.headerValue=headerValue;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        pd=new ProgressDialog(c);
        pd.setTitle("Downloading..");
        pd.setMessage("Retrieving data...Please wait");
        pd.show();
    }

    @Override
    protected String doInBackground(Void... params) {
        return this.retrieveData();
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);

        pd.dismiss();

        sp.setAdapter(null);

        if(s != null)
        {
            Parser p=new Parser(c,s,sp, columnName, headerValue);
            p.execute();

        }else {
            Toast.makeText(c,"No data retrieved",Toast.LENGTH_SHORT).show();
        }
    }

    private String retrieveData()
    {
        HttpURLConnection con=Connector.connect(urlAddress);
        if(con==null)
        {
            return null;
        }
        try
        {
            InputStream is=con.getInputStream();

            BufferedReader br=new BufferedReader(new InputStreamReader(is));
            String line;
            StringBuffer receivedData=new StringBuffer();

            while ((line=br.readLine()) != null)
            {
                receivedData.append(line+"n");
            }

            br.close();
            is.close();

            return receivedData.toString();

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}