package com.kerux.admin_thesis_kerux.login;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
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
import com.kerux.admin_thesis_kerux.session.KeruxSession;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class Login extends AppCompatActivity implements DBUtility {
    private static EditText username;
    private static EditText password;
    private static TextView attempt;
    private static Button button_login;
    int attempt_counter = 5;

    private KeruxSession session;

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

        session = new KeruxSession(getApplicationContext());

        button_login.setOnClickListener(new View.OnClickListener() {//
            @Override
            public void onClick(View v) {
                Dologin dologin=new Dologin();
                dologin.execute();
            }
        });
    }

    private class Dologin extends AsyncTask<String,String,String> {

        String uname=username.getText().toString(   );
        String pw=password.getText().toString();
        String adminId;
        String z="";

        boolean isSuccess=false;

        String nm,pass;

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
                    Connection con = connectionClass.CONN();
                    if (con == null) {
                        z = "Please check your internet connection";
                    } else {

                        String query= SELECT_ADMIN_LOGIN;

                        PreparedStatement ps = con.prepareStatement(query);
                        ps.setString(1, uname);
                        ps.setString(2, pw);

                        ResultSet rs=ps.executeQuery();

                        while (rs.next()) {
                            adminId=rs.getString(1);
                            nm = rs.getString(2);
                            pass = rs.getString(3);

                            if (nm.equals(uname) && pass.equals(pw)) {
                                session.setusername(adminId);
                                isSuccess = true;
                                z = "Login successfull";
                            } else {
                                isSuccess = false;
                            }
                        }
                    }
                }
                catch (Exception ex)
                {
                    isSuccess = false;
                    z = "Exceptions"+ex;
                }
            }
            return z;
        }

        @Override
        protected void onPostExecute(String s) {
            Toast.makeText(getBaseContext(),""+z,Toast.LENGTH_LONG).show();

            if(isSuccess) {
                Intent intent=new Intent(Login.this, MainActivity.class);
                intent.putExtra("username",uname);
                startActivity(intent);
            }
            progressDialog.hide();
        }
    }
}
