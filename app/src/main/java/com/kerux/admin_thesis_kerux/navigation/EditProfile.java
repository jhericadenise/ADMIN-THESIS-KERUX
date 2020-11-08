package com.kerux.admin_thesis_kerux.navigation;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
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
import com.kerux.admin_thesis_kerux.security.SecurityWEB;
import com.kerux.admin_thesis_kerux.session.KeruxSession;
import com.kerux.admin_thesis_kerux.unenrollment.UnenrollDoc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class EditProfile extends AppCompatActivity implements DBUtility {

    private EditText adminFName;
    private EditText adminLName;
    private EditText adminNewPassword;
    private EditText adminOldPassword;
    private EditText adminEmail;
    private EditText adminUsername;
    private String AdminID;
    private Button saveChanges;
    private SecurityWEB secweb;
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

        /*TextView title = (TextView) findViewById(R.id.dateToday);
        title.setText(giveDate());*/
        secweb=new SecurityWEB();
        session = new KeruxSession(getApplicationContext());

        progressDialog = new ProgressDialog(this);

        adminFName = (EditText)findViewById(R.id.txtboxFirstName);
        adminLName = (EditText)findViewById(R.id.txtboxLastName);
        adminNewPassword = (EditText)findViewById(R.id.txtboxNewPassword);
        adminOldPassword = (EditText)findViewById(R.id.txtboxOldPassword);
        adminEmail = (EditText)findViewById(R.id.txtboxEmail);
        adminUsername = findViewById(R.id.txtboxUsername);

        adminFName.setText(session.getfirstname());
        adminLName.setText(session.getlastname());
        adminEmail.setText(secweb.decrypt(session.getemail()));
        adminUsername.setText(secweb.decrypt(session.getusername()));
        AdminID=session.getadminid();

        saveChanges = (Button)findViewById(R.id.button_save);

        saveChanges.setOnClickListener(new View.OnClickListener() {//
            @Override
            public void onClick(View v) {
                updateInfo updateAdmin=new updateInfo();
                updateAdmin.execute();
            }
        });

        drawerLayout = findViewById(R.id.drawer_layout);

    }

    public void ClickMenu (View view){
        //open drawer
        MainActivity.openDrawer(drawerLayout);
    }

    public void ClickLogo (View view){
        //Close drawer
        MainActivity.closeDrawer(drawerLayout);
    }

    public void ClickDashboard(View view){
        //Redirect activity to dashboard
        MainActivity.redirectActivity(this, MainActivity.class);
    }

    public void ClickEditProfile(View view){
        //Redirect activity to dashboard
        recreate();
    }

    public void ClickManageAccounts(View view){
        //Redirect activity to manage accounts
        MainActivity.redirectActivity(this, ManageAccounts.class);
    }

    public void ClickEnrollment(View view){
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

    private class updateInfo extends AsyncTask<String,String,String> {
        String adminFirstName= adminFName.getText().toString();
        String adminLastName = adminLName.getText().toString();
        String adminEditEmail = adminEmail.getText().toString();
        String adminEditUname = adminUsername.getText().toString();

        String z = "";
        boolean isSuccess = false;

        /*String pID;*/

        /*ArrayList<String> ar = new ArrayList<String>();*/

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
            if(adminFirstName.trim().equals("")||adminLastName.trim().equals(""))
                z = "Please enter values in the Name and Password....";
            else
            {
                try {
                    Connection con = connectionClass.CONN();
                    if (con == null) {
                        z = "Please check your internet connection";
                    } else {
                        try {

                            //String query1 = EDIT_PROFILE; //select
                            String query = UPDATE_PROFILE; //update

                            PreparedStatement ps = con.prepareStatement(query); // ps for query which is edit profile

                            ps.setString(1, AdminID);
                            ps.setString(2, adminFirstName);
                            ps.setString(3, adminLastName);
                            ps.setString(4, adminEditEmail);
                            ps.setString(5, secweb.encrypt(adminEditUname));

                            //*ps.setString(2, session.getusername());*//*
                            // Statement stmt = con.createStatement();
                            // stmt.executeUpdate(query);
                            ps.executeUpdate(); // rs used by ps which is edit profile
                            isSuccess = true;
                            z = "Profile Successfully edited";

                        } catch (Exception e) {
                            isSuccess = false;
                            Thread.dumpStack(); //always put this from sir mon
                        }
                    }
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
                Intent intent=new Intent(EditProfile.this, MainActivity.class);
                // intent.putExtra("name",usernam);
                //*intent.putExtra("NAME", pName);*//*
                startActivity(intent);

            }
            progressDialog.hide();
        }


    }
    //method to update the patient password

    private class updatePass extends AsyncTask<String,String,String> {

        String adminNewPass = adminNewPassword.getText().toString();
        String adminOldPass = adminOldPassword.getText().toString();

        String z = "";
        boolean isSuccess = false;

        /*String pID;*/

        /*ArrayList<String> ar = new ArrayList<String>();*/

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
            if(adminNewPass.trim().equals("")||adminOldPass.trim().equals(""))
                z = "Please enter your old and new password.";
            else
            {
                try {
                    Connection con = connectionClass.CONN();
                    if (con == null) {
                        z = "Please check your internet connection";
                    } else {
                        try {

                            String query = CONFIRM_ADMIN_PASS;

                            PreparedStatement ps = con.prepareStatement(query);
                            ps.setString(1, adminOldPass);

                            ResultSet rs = ps.executeQuery();

                            while (rs.next()) {
                                String oldPassword = rs.getString(1);
                                if(oldPassword.equals(adminOldPass)){
                                    String query1 = UPDATE_PROFILE_PASS;

                                    PreparedStatement ps1 = con.prepareStatement(query1);
                                    ps1.setString(1, adminNewPass);

                                    ps1.executeUpdate();

                                }

                            }

                            ps.executeUpdate(); // rs used by ps which is edit profile
                            isSuccess = true;
                            z = "Password successfully edited!";


                        } catch (Exception e) {
                            isSuccess = false;
                            Thread.dumpStack(); //always put this from sir mon
                        }
                    }
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
                Intent intent=new Intent(EditProfile.this,MainActivity.class);
                startActivity(intent);

            }
            progressDialog.hide();
        }


    }


}