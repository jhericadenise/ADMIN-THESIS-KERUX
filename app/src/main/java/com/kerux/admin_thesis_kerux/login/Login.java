package com.kerux.admin_thesis_kerux.login;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.kerux.admin_thesis_kerux.R;
import com.kerux.admin_thesis_kerux.dbutility.ConnectionClass;
import com.kerux.admin_thesis_kerux.dbutility.DBUtility;
import com.kerux.admin_thesis_kerux.navigation.MainActivity;
import com.kerux.admin_thesis_kerux.security.Security;
import com.kerux.admin_thesis_kerux.security.SecurityWEB;
import com.kerux.admin_thesis_kerux.session.KeruxSession;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class Login extends AppCompatActivity implements DBUtility {
    private static EditText username;
    private static EditText password;
    private static TextView attempt;
    private static Button button_login;
    private static Button bttnDashboard;
    int attempt_counter = 5;
    private KeruxSession session;
    private SecurityWEB secweb;
    private Security sec;

    ProgressDialog progressDialog; //
    ConnectionClass connectionClass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        connectionClass = new ConnectionClass();

        progressDialog=new ProgressDialog(this);//

        username = (EditText)findViewById(R.id.txtboxUname);
        password = (EditText)findViewById(R.id.txtboxPass);
        button_login = (Button)findViewById(R.id.bttnLogin);
        secweb=new SecurityWEB();
        session = new KeruxSession(getApplicationContext());
        /*bttnDashboard = findViewById(R.id.bttnDashboard);*/

       /* bttnDashboard.setOnClickListener(new View.OnClickListener() {//
            @Override
            public void onClick(View v) {
                GoToDashboard();
            }
        });*/

        button_login.setOnClickListener(new View.OnClickListener() {//
            @Override
            public void onClick(View v) {
                Dologin dologin=new Dologin();
                dologin.execute();
                insertAudit();
            }
        });

    }

    public void GoToDashboard(){
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
    public void insertAudit(){

        try {
            URL url = new URL("https://isproj2a.benilde.edu.ph/Sympl/InsertAuditAdminServlet");
            URLConnection connection = url.openConnection();

            connection.setReadTimeout(10000);
            connection.setConnectTimeout(15000);
            connection.setDoInput(true);
            connection.setDoOutput(true);

            Uri.Builder builder = new Uri.Builder()
                    .appendQueryParameter("first", sec.encrypt("login"))
                    .appendQueryParameter("second", sec.encrypt("login"))
                    .appendQueryParameter("third", sec.encrypt("Logging in to the app"))
                    .appendQueryParameter("fourth", sec.encrypt("none"))
                    .appendQueryParameter("fifth", sec.encrypt("login"))
                    .appendQueryParameter("sixth", session.getusername());
            String query = builder.build().getEncodedQuery();

            OutputStream os = connection.getOutputStream();
            BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(os, "UTF-8"));
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
        }
        catch (SQLException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private class Dologin extends AsyncTask<String,String,String> {

        String uname=username.getText().toString();
        String pw=password.getText().toString();
        int adminId;
        int clinicid;
        String z="";

        boolean isSuccess=false;

        String firstName,lastName,email, usernam;

        @Override
        protected void onPreExecute() {
            progressDialog.setMessage("Loading...");
            progressDialog.show();

            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            if(uname.trim().equals("")||pw.trim().equals(""))
                z = "Please enter all fields....";
            else
            {
                try {
                    z = "Incorrect username or password";
                    URL url = new URL("https://isproj2a.benilde.edu.ph/Sympl/LoginAdminServlet");
                    URLConnection connection = url.openConnection();

                    connection.setReadTimeout(10000);
                    connection.setConnectTimeout(15000);
                    connection.setDoInput(true);
                    connection.setDoOutput(true);

                    Uri.Builder builder = new Uri.Builder()
                            .appendQueryParameter("username", secweb.encrypt(uname))
                            .appendQueryParameter("password", secweb.encrypt(pw));
                    String query = builder.build().getEncodedQuery();

                    OutputStream os = connection.getOutputStream();
                    BufferedWriter writer = new BufferedWriter(
                            new OutputStreamWriter(os, "UTF-8"));
                    writer.write(query);
                    writer.flush();
                    writer.close();
                    os.close();

                    BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    String returnString="";
                    ArrayList<String> output=new ArrayList<String>();
                    while ((returnString = in.readLine()) != null)
                    {
                        isSuccess=true;
                        z = "Logged in successfully!";
                        Log.d("returnString", returnString);
                        output.add(returnString);
                    }
                    for (int i = 0; i < output.size(); i++) {
                        if(i==0){
                            adminId=Integer.parseInt(output.get(i));
                        }
                        else if(i==1){
                            firstName=output.get(i);
                        }
                        else if(i==2){
                            lastName=output.get(i);
                        }
                        else if(i==3){
                            email=output.get(i);
                        }
                        else if(i==4){
                            clinicid=Integer.parseInt(output.get(i));
                        }
                        else if(i==5){
                            usernam=output.get(i);
                        }

                    }
                    in.close();
                }
                catch (Exception ex)
                {
                    isSuccess = false;
                    z = "Exceptions"+ex;
                }
/*                catch (Exception e) {
                    Thread.dumpStack(); //always put this from sir mon
                }*/
            }
            return z;
        }

        @Override
        protected void onPostExecute(String s) {
            Toast.makeText(getBaseContext(),""+z,Toast.LENGTH_LONG).show();

            if(isSuccess) {
                session.setadminid(String.valueOf(adminId));
                session.setfirstname(firstName);
                session.setlastname(lastName);
                session.setemail(email);
                session.setclinicid(String.valueOf(clinicid));
                session.setusername(usernam);
                Intent intent=new Intent(Login.this, MainActivity.class);
                intent.putExtra("adminname", firstName+" "+lastName);
                startActivity(intent);
            }
            progressDialog.hide();
        }
    }
}
