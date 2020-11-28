package com.kerux.admin_thesis_kerux.navigation;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

import com.kerux.admin_thesis_kerux.R;
import com.kerux.admin_thesis_kerux.dbutility.ConnectionClass;
import com.kerux.admin_thesis_kerux.dbutility.DBUtility;
import com.kerux.admin_thesis_kerux.reports.ViewAuditReportsActivity;
import com.kerux.admin_thesis_kerux.reports.ViewStatReportsActivity;
import com.kerux.admin_thesis_kerux.security.Security;
import com.kerux.admin_thesis_kerux.security.SecurityWEB;
import com.kerux.admin_thesis_kerux.session.KeruxSession;
import com.kerux.admin_thesis_kerux.unenrollment.UnenrollDoc;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class EditProfile extends AppCompatActivity implements DBUtility {

    private EditText firstname;
    private EditText lastname;
    private EditText newPassword;
    private EditText oldPassword;
    private EditText userName;
    private EditText email;
    private String adminID;
    private SecurityWEB secweb;

    private Button saveChanges;
    private Button confirmPass;

    ConnectionClass connectionClass;
    private KeruxSession session;//global variable
    ProgressDialog progressDialog;

    DrawerLayout drawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        connectionClass = new ConnectionClass();

        session = new KeruxSession(getApplicationContext());

        progressDialog = new ProgressDialog(this);

        firstname = (EditText)findViewById(R.id.txtboxFirstName);
        lastname = (EditText)findViewById(R.id.txtboxLastName);
        newPassword = (EditText)findViewById(R.id.txtboxNewPassword);
        oldPassword = (EditText)findViewById(R.id.txtboxOldPassword);
        userName=(EditText)findViewById(R.id.txtboxUsername);
        email = (EditText)findViewById(R.id.txtboxEmail);
        secweb = new SecurityWEB();
        Security sec = new Security();

        firstname.setText(session.getfirstname());
        lastname.setText(session.getlastname());
        email.setText(secweb.decrypt(session.getemail()));
        userName.setText(secweb.decrypt(session.getusername()));
        adminID = session.getadminid();

        saveChanges = (Button)findViewById(R.id.button_save);
        confirmPass = (Button)findViewById(R.id.bttnConfirmPass);

        saveChanges.setOnClickListener(new View.OnClickListener() {//
            @Override
            public void onClick(View v) {
                updateAdminInfo updatepinfo=new updateAdminInfo();
                updatepinfo.execute();
                insertAudit();
            }
        });

        confirmPass.setOnClickListener(new View.OnClickListener() {//
            @Override
            public void onClick(View v) {
                updateAdminPass updateppass=new updateAdminPass();
                updateppass.execute();
                insertAudit();
            }
        });

        drawerLayout = findViewById(R.id.drawer_layout);

    }

    //insert to audit logs
    public void insertAudit(){

        Security sec = new Security();

        try {
            URL url = new URL("https://isproj2a.benilde.edu.ph/Sympl/InsertAuditAdminServlet");
            URLConnection connection = url.openConnection();

            connection.setReadTimeout(300000);
            connection.setConnectTimeout(300000);
            connection.setDoInput(true);
            connection.setDoOutput(true);

            Uri.Builder builder = new Uri.Builder()
                    .appendQueryParameter("first", "Edit Profile")
                    .appendQueryParameter("second", "Patient Edit profile")
                    .appendQueryParameter("third", "Patient editing profile")
                    .appendQueryParameter("fourth", "none")
                    .appendQueryParameter("fifth", "Patient ID: " + session.getadminid())
                    .appendQueryParameter("sixth", session.getadminid());
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
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void ClickMenu (View view){
        //open drawer
        MainActivity.openDrawer(drawerLayout);
    }

    public void ClickLogo (View view){
        //Close drawer
        MainActivity.closeDrawer(drawerLayout);
    }

    public void ClickEditProfile(View view){
        //Redirect activity to dashboard
        recreate();
    }

    public void ClickDashboard(View view){
        //Redirect activity to dashboard
        MainActivity.redirectActivity(this, MainActivity.class);
    }

    public void ClickManageAccounts(View view){
        //Redirect activity to manage accounts
        MainActivity.redirectActivity(this, ManageAccounts.class);
    }

    public void ClickEnrollment(View view){
        //Recreate activity
        MainActivity.redirectActivity(this, EnrollmentPage.class);
    }

    public void ClickRevoke(View view){
        //redirect activity to revoke page
        MainActivity.redirectActivity(this, UnenrollDoc.class);
    }

    public void ClickViewStat(View view){
        MainActivity.redirectActivity(this, ViewStatReportsActivity.class);
    }

    public void ClickViewAudit(View view){
        MainActivity.redirectActivity(this, ViewAuditReportsActivity.class);
    }

    public void ClickLogout(View view){
        MainActivity.logout(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        //close drawer
        MainActivity.closeDrawer(drawerLayout);
    }


    //=================================================================================================================

    private class updateAdminInfo extends AsyncTask<String,String,String> {
        Security sec = new Security();
        String adminFname =firstname.getText().toString();
        String adminLname=lastname.getText().toString();
        String adminEmail = email.getText().toString();
        String username=userName.getText().toString();

        String z = "";
        boolean isSuccess = false;

        @Override
        protected void onPreExecute() {
            progressDialog.setMessage("Loading...");
            progressDialog.show();

            super.onPreExecute();
        }

        /*sql retrieves current patient data using select method and place in corresponding text fields
                user will now have the chance to edit the said text fields
                user will press the update button
                sql update will get the entry from the fields and push the new data to the database
                make an intent that will either go back to the dashboard or stay at the edit profile page*/

        @Override
        protected String doInBackground(String... params) {
            if(adminFname.trim().equals("")||adminLname.trim().equals("")||adminEmail.trim().equals(""))
                z = "Please enter values in the First Name, Last Name, Email and Username";
            else
            {
                try {

                    URL url = new URL("https://isproj2a.benilde.edu.ph/Sympl/UpdateAdminProfile");
                    URLConnection connection = url.openConnection();

                    connection.setReadTimeout(300000);
                    connection.setConnectTimeout(300000);
                    connection.setDoInput(true);
                    connection.setDoOutput(true);

                    Uri.Builder builder = new Uri.Builder()
                            .appendQueryParameter("adminEmail", secweb.encrypt(adminEmail))
                            .appendQueryParameter("adminFname", adminFname)
                            .appendQueryParameter("adminLname", adminLname)
                            .appendQueryParameter("userName", secweb.encrypt(username))
                            .appendQueryParameter("adminID", adminID);
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
                        session.setemail(secweb.encrypt(adminEmail));
                        session.setfirstname(adminFname);
                        session.setlastname(adminLname);
                        session.setusername(secweb.encrypt(username));
                        isSuccess=true;
                        z = "Profile successfully edited!";
                        output.add(returnString);
                    }
                    in.close();

                }catch (Exception ex)
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

                Intent intent=new Intent(EditProfile.this,EditProfile.class);
                startActivity(intent);

            }
            progressDialog.hide();
        }
    }

    //method to update the patient password

    private class updateAdminPass extends AsyncTask<String,String,String> {

        String adminNewPw = newPassword.getText().toString();
        String adminOldPw = oldPassword.getText().toString();

        String z = "";
        boolean isSuccess = false;

        @Override
        protected void onPreExecute() {
            progressDialog.setMessage("Loading...");
            progressDialog.show();

            super.onPreExecute();
        }

        /*sql retrieves current patient data using select method and place in corresponding text fields
                user will now have the chance to edit the said text fields
                user will press the update button
                sql update will get the entry from the fields and push the new data to the database
                make an intent that will either go back to the dashboard or stay at the edit profile page*/

        @Override
        protected String doInBackground(String... params) {
            if(adminNewPw.trim().equals("")||adminOldPw.trim().equals(""))
                z = "Please enter your old and new password.";
            else
            {
                try {


                    URL url = new URL("https://isproj2a.benilde.edu.ph/Sympl/UpdateAdminPass");
                    URLConnection connection = url.openConnection();

                    connection.setReadTimeout(10000);
                    connection.setConnectTimeout(15000);
                    connection.setDoInput(true);
                    connection.setDoOutput(true);

                    Uri.Builder builder = new Uri.Builder()
                            .appendQueryParameter("adminID", adminID)
                            .appendQueryParameter("adminOldPw", secweb.encrypt(adminOldPw))
                            .appendQueryParameter("adminNewPw", secweb.encrypt(adminNewPw));
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
                        z = "Password successfully edited!";
                        output.add(returnString);
                    }
                    in.close();

                }catch (Exception ex)
                {
                    isSuccess = false;
                    z = "Exceptions"+ex;
                }
            }
            return z;        }

        @Override
        protected void onPostExecute(String s) {
            Toast.makeText(getBaseContext(),""+z,Toast.LENGTH_LONG).show();
            if(isSuccess) {
                Intent intent=new Intent(EditProfile.this,EditProfile.class);
                startActivity(intent);
            }
            progressDialog.hide();
        }


    }


}