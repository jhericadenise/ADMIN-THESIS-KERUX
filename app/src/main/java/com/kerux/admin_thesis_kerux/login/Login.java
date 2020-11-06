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
import com.kerux.admin_thesis_kerux.security.Security;
import com.kerux.admin_thesis_kerux.security.SecurityWEB;
import com.kerux.admin_thesis_kerux.session.KeruxSession;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Login extends AppCompatActivity implements DBUtility {
    private static EditText username;
    private static EditText password;
    private static TextView attempt;
    private static Button button_login;
    private static Button bttnDashboard;
    int attempt_counter = 5;

    private KeruxSession session;
    private SecurityWEB secweb;

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
        bttnDashboard = findViewById(R.id.bttnDashboard);

        bttnDashboard.setOnClickListener(new View.OnClickListener() {//
            @Override
            public void onClick(View v) {
                GoToDashboard();
            }
        });

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
        Connection con = connectionClass.CONN();
        PreparedStatement ps = null;
        Security sec = new Security();

        try {
            String queryAUDIT = INSERT_AUDIT_LOG;
            PreparedStatement psAUDIT = con.prepareStatement(queryAUDIT);
            psAUDIT.setString(1, sec.encrypt("login"));
            psAUDIT.setString(2, sec.encrypt("login"));
            psAUDIT.setString(3, sec.encrypt("Logging in to the app"));
            psAUDIT.setString(4, sec.encrypt("none"));
            psAUDIT.setString(5, sec.encrypt("login"));
            psAUDIT.setString(6, sec.encrypt(session.getusername()));
            psAUDIT.executeUpdate();
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
                    Connection con = connectionClass.CONN();
                    if (con == null) {
                        z = "Please check your internet connection";
                    } else {

                        String query= SELECT_ADMIN_LOGIN;

                        PreparedStatement ps = con.prepareStatement(query);
                        ps.setString(1, secweb.encrypt(uname));
                        ps.setString(2, secweb.encrypt(pw));

                        ResultSet rs=ps.executeQuery();
                        /*z=secweb.encrypt(uname) + " " + secweb.encrypt(pw);*/
                        z = "Successfully logged in";
                        while (rs.next()) {
                            adminId=rs.getInt(1);
                            firstName = rs.getString(2);
                            lastName = rs.getString(3);
                            email=rs.getString(4);
                            clinicid=rs.getInt(5);
                            usernam=rs.getString(6);

                            //SET SESSION
                            session.setadminid(String.valueOf(adminId));
                            session.setfirstname(firstName);
                            session.setlastname(lastName);
                            session.setemail(email);
                            session.setclinicid(String.valueOf(clinicid));
                            session.setusername(usernam);
                            isSuccess = true;
                        }
                    }
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
                Intent intent=new Intent(Login.this, MainActivity.class);
                intent.putExtra("adminname", firstName+" "+lastName);
                startActivity(intent);
            }
            progressDialog.hide();
        }
    }
}
