package com.kerux.admin_thesis_kerux.enrollment;

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
import com.kerux.admin_thesis_kerux.reports.ViewRatingReportsActivity;
import com.kerux.admin_thesis_kerux.reports.ViewStatReportsActivity;
import com.kerux.admin_thesis_kerux.security.Security;
import com.kerux.admin_thesis_kerux.session.KeruxSession;
import com.kerux.admin_thesis_kerux.spinner.Downloader;
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
    private static String urlClinicSpinner = "http://192.168.1.13:89/kerux/clinicSpinner.php";

    ConnectionClass connectionClass;

    private KeruxSession session;

    DrawerLayout drawerLayout;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enroll_dept);

        session=new KeruxSession(getApplicationContext());

        connectionClass=new ConnectionClass(); //create ConnectionClass

        drawerLayout = findViewById(R.id.drawer_layout);

        Button bttnEnrollDept = findViewById(R.id.bttnEnrollDept);
        spinnerClinic = (Spinner) findViewById(R.id.clinicSpinner);
        deptName = (EditText)findViewById(R.id.txtboxDeptName);

        //what the button of enroll dept will do when its clicked
        bttnEnrollDept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DoEnroll doEnroll=new DoEnroll();
                doEnroll.execute();
                deptName.getText().clear();
            }
        });

        Downloader dep = new Downloader(EnrollDept.this, urlClinicSpinner, spinnerClinic, "clinicName", "Choose Clinic");
        dep.execute();
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

    public void ClickViewRating(View view){
        MainActivity.redirectActivity(this, ViewRatingReportsActivity.class);
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

    /*Function class for enrolling the department in to the db, inserting the records in the db*/
    private class DoEnroll extends AsyncTask<String, String, String> {

        Security sec = new Security();
        boolean isSuccess = false;
        String message = "";
        String depName = deptName.getText().toString();
        String Status = "Active";
        String timeStamp = timeStamp();
        boolean hasRecord = false;
        int clinicName = (int)spinnerClinic.getSelectedItemId();
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
                ps.setInt (1, clinicName);
                ps.setString(2, depName);

                ResultSet rs=ps.executeQuery();

                if(rs.next()){
                    hasRecord = true;
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

            message = "Record already exists";


            if (depName.trim().equals("")) {
                message = "Please enter all fields....";
            }
            else if (!depName.matches("^[A-Za-z]+$")) {
                message = "Check format";
            }
            else if (hasRecord){
                message = "Record already exists";
            }
            else
            {
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

                      /*  String queryJoin = "insert into department_enrollment (Admin_ID, Department_ID, Clinic_ID) "+
                                "SELECT '"+session.getusername () +"', Department_ID, Clinic_ID from Department order by Department_ID DESC LIMIT 1;";

                        Statement stmt2 = con.createStatement();
                        stmt2.executeUpdate(queryJoin);*/
                        con.close();
                        message = "ADDED SUCCESSFULLY!";
                    }
                }
                catch (Exception ex)
                {
                    isSuccess = false;
                    message = "Exceptions"+ex;
                    Log.d("ex", ex.getMessage () + " Jheca");
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

