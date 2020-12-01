package com.kerux.admin_thesis_kerux.login;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

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
import java.nio.charset.StandardCharsets;
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
    String deptNum;
    String qmNum;

    ProgressDialog progressDialog; //
    ConnectionClass connectionClass;

    private static final int PERMISSION_REQUEST_CODE = 1;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= 23)
        {
            if (checkPermission())
            {
                // Code for above or equal 23 API Oriented Device
                // Your Permission granted already .Do next code
            } else {
                requestPermission(); // Code for permission
            }
        }
        else
        {

            // Code for Below 23 API Oriented Device
            // Do next code
        }





        setContentView(R.layout.activity_login);
        connectionClass = new ConnectionClass();

        progressDialog=new ProgressDialog(this);//

        username = findViewById(R.id.txtboxUname);
        password = findViewById(R.id.txtboxPass);
        button_login = findViewById(R.id.bttnLogin);
        secweb=new SecurityWEB();
        session = new KeruxSession(getApplicationContext());

        button_login.setOnClickListener(new View.OnClickListener() {//
            @Override
            public void onClick(View v) {
                Dologin dologin=new Dologin();
                dologin.execute();
                insertAudit();
            }
        });

    }

    private boolean checkPermission() {
        int result = ContextCompat.checkSelfPermission(Login.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
        return result == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission() {

        if (ActivityCompat.shouldShowRequestPermissionRationale(Login.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            Toast.makeText(Login.this, "Write External Storage permission allows us to do store images. Please allow this permission in App Settings.", Toast.LENGTH_LONG).show();
        } else {
            ActivityCompat.requestPermissions(Login.this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.e("value", "Permission Granted, Now you can use local drive .");
                } else {
                    Log.e("value", "Permission Denied, You cannot use local drive .");
                }
                break;
        }
    }

    public void GoToDashboard(){
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    public void insertAudit(){

        try {
            URL url = new URL("https://isproj2a.benilde.edu.ph/Sympl/InsertAuditAdminServlet");
            URLConnection connection = url.openConnection();

            connection.setReadTimeout(30000);
            connection.setConnectTimeout(60000);
            connection.setDoInput(true);
            connection.setDoOutput(true);

            Uri.Builder builder = new Uri.Builder()
                    .appendQueryParameter("first", SecurityWEB.encrypt("login"))
                    .appendQueryParameter("second", SecurityWEB.encrypt("login"))
                    .appendQueryParameter("third", SecurityWEB.encrypt("Logging in to the app"))
                    .appendQueryParameter("fourth", SecurityWEB.encrypt("none"))
                    .appendQueryParameter("fifth", SecurityWEB.encrypt("login"))
                    .appendQueryParameter("sixth", SecurityWEB.encrypt(session.getusername()));
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
        }
    }

    private class Dologin extends AsyncTask<String,String,String> {

        String uname=username.getText().toString();
        String pw=password.getText().toString();
        int adminId;
        String clinicid;
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
                            .appendQueryParameter("username", SecurityWEB.encrypt(uname))
                            .appendQueryParameter("password", SecurityWEB.encrypt(pw));
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
                            clinicid=output.get(i);
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
                usernam=firstName+" "+lastName;
                session.setadminid(String.valueOf(adminId));
                session.setfirstname(firstName);
                session.setlastname(lastName);
                session.setemail(email);
                session.setclinicid(clinicid);
                session.setusername(usernam);
                session.setQMCount(qmNum);
                session.setDepCount(deptNum);
                Intent intent=new Intent(Login.this, MainActivity.class);
                intent.putExtra("adminname", firstName+" "+lastName);
                startActivity(intent);
            }
            progressDialog.hide();
        }
    }

}
