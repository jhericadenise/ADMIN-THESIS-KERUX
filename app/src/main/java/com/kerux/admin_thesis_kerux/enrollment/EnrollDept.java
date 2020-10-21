package com.kerux.admin_thesis_kerux.enrollment;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.kerux.admin_thesis_kerux.R;
import com.kerux.admin_thesis_kerux.dbutility.ConnectionClass;
import com.kerux.admin_thesis_kerux.dbutility.DBUtility;
import com.kerux.admin_thesis_kerux.navigation.EnrollmentPage;
import com.kerux.admin_thesis_kerux.navigation.MainActivity;
import com.kerux.admin_thesis_kerux.navigation.ManageAccounts;
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


public class EnrollDept extends AppCompatActivity implements DBUtility, NavigationView.OnNavigationItemSelectedListener {

    private EditText deptName;
    private Spinner spinnerClinic;
    private static String urlClinicSpinner = "http://192.168.1.13:89/kerux/clinicSpinner.php";

    ConnectionClass connectionClass;

    private KeruxSession session;

    DrawerLayout drawerLayout;
    NavigationView navigationView;
    Toolbar toolbar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enroll_dept);

        session=new KeruxSession(getApplicationContext());

        TextView titleDate = (TextView) findViewById(R.id.txtDateDep);
        titleDate.setText(giveDate());
        connectionClass=new ConnectionClass(); //create ConnectionClass

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        toolbar = findViewById(R.id.toolbar);

        /*setSupportActionBar(toolbar);*/

        //Hide or show login or logout
        Menu menu = navigationView.getMenu();
        menu.findItem(R.id.nav_logout).setVisible(false);

        navigationView.bringToFront();
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(EnrollDept.this);
        navigationView.setCheckedItem(R.id.nav_enrollment_dept);

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

        Downloader dep = new Downloader(EnrollDept.this, urlClinicSpinner, spinnerClinic, "clinicName");
        dep.execute();
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
    @Override
    public void onBackPressed() {

        if(drawerLayout.isDrawerOpen(GravityCompat.START)){
            drawerLayout.closeDrawer(GravityCompat.START);
        }
        else{
            super.onBackPressed();
        }

    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        switch (menuItem.getItemId()){
            case R.id.nav_dashboard:
                Intent intent = new Intent(EnrollDept.this, MainActivity.class);
                startActivity(intent);
                break;
            case R.id.nav_enrollment:
                Intent intent1 = new Intent(EnrollDept.this, EnrollmentPage.class);
                startActivity(intent1);
                break;
            case R.id.nav_enrollment_dept:
                break;
            case R.id.nav_enrollment_doctor:
                Intent intent2 = new Intent(EnrollDept.this, EnrollDoctor.class);
                startActivity(intent2);
                break;
            case R.id.nav_enrollment_qm:
                Intent intent3 = new Intent(EnrollDept.this, EnrollQM.class);
                startActivity(intent3);
                break;
            case R.id.nav_revoke:
                Intent intent4 = new Intent(EnrollDept.this, UnenrollDoc.class);
                startActivity(intent4);
                break;
            case R.id.nav_accounts:
                Intent intent5 = new Intent(EnrollDept.this, ManageAccounts.class);
                startActivity(intent5);
                break;

        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
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
                        ps1.setString(1, depName);
                        ps1.setInt(2, clinicName);
                        ps1.setString(3, Status);

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

