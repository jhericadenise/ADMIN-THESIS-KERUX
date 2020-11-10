package com.kerux.admin_thesis_kerux.enrollment;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

import com.kerux.admin_thesis_kerux.R;
import com.kerux.admin_thesis_kerux.dbutility.ConnectionClass;
import com.kerux.admin_thesis_kerux.dbutility.DBUtility;
import com.kerux.admin_thesis_kerux.navigation.EditProfile;
import com.kerux.admin_thesis_kerux.navigation.EnrollmentPage;
import com.kerux.admin_thesis_kerux.navigation.MainActivity;
import com.kerux.admin_thesis_kerux.navigation.ManageAccounts;
import com.kerux.admin_thesis_kerux.reports.ViewAuditReportsActivity;
import com.kerux.admin_thesis_kerux.reports.ViewStatReportsActivity;
import com.kerux.admin_thesis_kerux.security.Security;
import com.kerux.admin_thesis_kerux.session.KeruxSession;
import com.kerux.admin_thesis_kerux.unenrollment.UnenrollDoc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;


public class EnrollDept extends AppCompatActivity implements DBUtility{

    private EditText deptName;
    private Spinner spinnerClinic;
    ConnectionClass connectionClass;
    private KeruxSession session;
    DrawerLayout drawerLayout;

    private Security sec;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enroll_dept);

        session=new KeruxSession(getApplicationContext());

        connectionClass=new ConnectionClass(); //create ConnectionClass

        drawerLayout = findViewById(R.id.drawer_layout);

        Button bttnEnrollDept = findViewById(R.id.bttnEnrollDept);
        deptName = (EditText)findViewById(R.id.txtboxDeptName);

        //what the button of enroll dept will do when its clicked
        bttnEnrollDept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!validateDeptName()) {
                    confirmInput();
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(EnrollDept.this);
                    builder.setMessage("Are you sure you want to Enroll?")
                            .setCancelable(false)
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    DoEnroll doEnroll = new DoEnroll();
                                    doEnroll.execute();
                                    deptName.getText().clear();
                                }
                            })
                            .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            });
                    AlertDialog alert = builder.create();
                    alert.show();
                }
            }
        });
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
        MainActivity.redirectActivity(this, EditProfile.class);
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

    /*when goBack button is clicked, it will be redirected to the page stated in the function*/
    public void goBack() {
        Intent intent = new Intent(this, EnrollmentPage.class);
        startActivity(intent);
    }

    //Getting the current date
    public String giveDate() {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("EEE, MMM d, yyyy");
        return sdf.format(cal.getTime());
    }

    //Getting time stamp
    public String timeStamp() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss");
        return sdf.format(calendar.getTime());
    }

    private boolean validateDeptName() {
        String name = deptName.getText().toString().trim();

        if(name.isEmpty()){
            deptName.setError("Field can't be empty");
            return false;
        } else if (name.length() < 2){
            deptName.setError("Department name too short");
            return false;
        } else if(name.matches("^[0-9]+$")){
            deptName.setError("Department name cannot contain number values");
            return false;
        } else {
            deptName.setError(null);
            return true;
        }
    }

    public boolean confirmInput() {
        String input = "Department Name: " + deptName.getText().toString();
        Toast.makeText(this, input, Toast.LENGTH_SHORT).show();
        return false;
    }

    /*Function class for enrolling the department in to the db, inserting the records in the db*/
    private class DoEnroll extends AsyncTask<String, String, String> {

        Security sec = new Security();
        boolean isSuccess = false;
        String message = "";
        String depName = deptName.getText().toString();
        String Status = "Active";
        String timeStamp = timeStamp();
        boolean hasRecord = false;
        int clinicName = Integer.parseInt(session.getclinicid());
        int reason = 0;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }
        @Override
        protected String doInBackground(String... params) {
            Connection con = connectionClass.CONN();
            PreparedStatement ps = null;
            try {
                ps = con.prepareStatement(VALIDATION_DEPT);
                ps.setString(1, depName);
                ps.setInt(2, clinicName);

                ResultSet rs=ps.executeQuery();

                if(rs.next()){
                    hasRecord = true;
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }


            if (hasRecord){
                message = "Record already exists";
            }
            else {
                try {
                    if (con == null) {
                        message = "CANNOT ADD RECORD";

                    } else {
                        //inserting data of department to the database
                        String query = INSERT_DEPT;
                        PreparedStatement ps1 = con.prepareStatement(query);
                        ps1.setInt(1, clinicName);
                        ps1.setInt(2, reason);
                        ps1.setString(3, depName);
                        ps1.setString(4, Status);

                        ps1.executeUpdate();

                        String query2 = SELECT_NEW_DEPARTMENT_ID; //DON'T NEED TO LOG, IT JUST GETS THE DEP_ID OF THE NEWLY INSERTED DEPARTMENT
                        PreparedStatement ps2 = con.prepareStatement(query2);
                        ResultSet rs1 = ps2.executeQuery();
                        while (rs1.next()) {
                            String newdeptid = rs1.getString(1);

                            //insert department into department_enrollment table
                            String query3 = INSERT_DEPT_ENROLLMENT;
                            PreparedStatement ps3 = con.prepareStatement(query3);
                            ps3.setString(1, session.getadminid());
                            ps3.setString(2, newdeptid);
                            ps3.setString(3, session.getclinicid());
                            ps3.executeUpdate();
                            //insert to audit log table
                            String queryAUDIT = INSERT_AUDIT_LOG;
                            PreparedStatement psAUDIT = con.prepareStatement(queryAUDIT);
                            psAUDIT.setString(1, sec.encrypt("department"));
                            psAUDIT.setString(2, sec.encrypt("insert"));
                            psAUDIT.setString(3, sec.encrypt("Inserting a Department record"));
                            psAUDIT.setString(4, sec.encrypt("none"));
                            psAUDIT.setString(5, sec.encrypt(String.valueOf(clinicName) + ", " + reason + ", " + depName + ", " + Status));
                            psAUDIT.setString(6, sec.encrypt(session.getusername()));
                            psAUDIT.executeUpdate();
                            //inserting to audit log
                            PreparedStatement psAUDIT1 = con.prepareStatement(queryAUDIT);
                            psAUDIT.setString(1, sec.encrypt("department_enrollment"));
                            psAUDIT.setString(2, sec.encrypt("insert"));
                            psAUDIT.setString(3, sec.encrypt("Inserting into department_enrollment table"));
                            psAUDIT.setString(4, sec.encrypt("none"));
                            psAUDIT.setString(5, sec.encrypt(session.getadminid() + ", " + newdeptid + ", " + session.getclinicid()));
                            psAUDIT.setString(6, sec.encrypt(session.getusername()));
                            psAUDIT.executeUpdate();
                        }
                        con.close();
                        message = "ADDED SUCCESSFULLY!";
                    }
                } catch (Exception ex) {
                    isSuccess = false;
                    message = "Exceptions" + ex;
                    Log.d("ex", ex.getMessage() + " Jheca");
                }
            }
            return message;

        }
        @Override
        protected void onPostExecute(String s) {
            Toast.makeText(getBaseContext(),""+message,Toast.LENGTH_LONG).show();

            if(isSuccess) {
                Intent intent=new Intent(EnrollDept.this,EnrollDept.class);
                startActivity(intent);
            }

        }

    }
}

