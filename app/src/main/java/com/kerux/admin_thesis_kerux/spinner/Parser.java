package com.kerux.admin_thesis_kerux.spinner;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class Parser extends AsyncTask<Void,Void,Integer> {//TO BE USED WHEN WE HAVE A SERVER

    Context c;
    String data;
    ArrayList names=new ArrayList();
    Spinner sp;
    String columnName;
    String headerValue;

    public Parser(Context c, String data,Spinner sp, String columnName, String headerValue) {
        this.c = c;
        this.data = data;
        this.sp=sp;
        this.columnName = columnName;
        this.headerValue = headerValue;
    }

    @Override
    protected Integer doInBackground(Void... params) {
        return this.parse();
    }

    @Override
    protected void onPostExecute(Integer integer) {
        super.onPostExecute(integer);

        if(integer>=1)
        {
            ArrayAdapter adapter=new ArrayAdapter(c,android.R.layout.simple_list_item_1,names);
            sp.setAdapter(adapter);

        }else {
            Toast.makeText(c,"Unable To Parse",Toast.LENGTH_SHORT).show();
        }
    }

    private int parse()
    {
        try
        {
            JSONArray ja=new JSONArray(data);
            JSONObject jo=null;
            names.add(headerValue);
            for (int i=0; i<ja.length(); i++)
            {
                jo=ja.getJSONObject(i);
                String name=jo.getString(columnName);
                names.add(name);

            }

            return 1;

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return 0;
    }
}
