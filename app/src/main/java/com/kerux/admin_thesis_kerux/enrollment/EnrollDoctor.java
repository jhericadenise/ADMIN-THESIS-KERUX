package com.kerux.admin_thesis_kerux.enrollment;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

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

public class EnrollDoctor extends AppCompatActivity implements DBUtility{

    private static String urlClinicSpinner = "http://192.168.1.13:89/kerux/clinicSpinner.php";
    private static String urlDeptSpinner = "http://192.168.1.13:89/kerux/departmentSpinner.php"; /*10.0.2.2:89*/
    private static String urlDocTypeSpinner = "http://192.168.1.13:89/kerux/doctorTypeSpinner.php";
    private EditText doctorFName;
    private EditText doctorLName;
    private EditText roomNo;
    private EditText schedule1;
    private EditText schedule2;
    private CheckBox monday;
    private CheckBox tuesday;
    private CheckBox wednesday;
    private CheckBox thursday;
    private CheckBox friday;
    private CheckBox saturday;
    private Spinner spinnerDocType;
    private Spinner spinnerDep;
    private Spinner spinnerClinic;

    ConnectionClass connectionClass;

    KeruxSession session;

    DrawerLayout drawerLayout;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enroll_doctor);
        connectionClass = new ConnectionClass(); //create ConnectionClass

        session=new KeruxSession(getApplicationContext());

        drawerLayout = findViewById(R.id.drawer_layout);

        Button bttnEnrollDoc = findViewById(R.id.bttnEnrollDoc);
        doctorFName = (EditText) findViewById(R.id.txtboxDocFName);
        doctorLName = (EditText) findViewById(R.id.txtboxDocLName);
        roomNo = (EditText) findViewById(R.id.txtboxRoomNo);
        schedule1 = (EditText) findViewById(R.id.txtboxSched1);
        schedule2 = (EditText) findViewById(R.id.txtboxSched2);
        monday = (CheckBox) findViewById(R.id.cBoxMon);
        tuesday = (CheckBox) findViewById(R.id.cBoxTues);
        wednesday = (CheckBox) findViewById(R.id.cBoxWed);
        thursday = (CheckBox) findViewById(R.id.cBoxThurs);
        friday = (CheckBox) findViewById(R.id.cBoxFriday);
        saturday = (CheckBox) findViewById(R.id.cBoxSat);
        spinnerDocType = (Spinner) findViewById(R.id.spinnerDocType);
        spinnerDep = (Spinner) findViewById(R.id.spinnerDepType);
        spinnerClinic = (Spinner) findViewById(R.id.spinnerClinic);


        bttnEnrollDoc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EnrollDoctor.DoEnrollDoc doenroll = new EnrollDoctor.DoEnrollDoc();
                doenroll.execute();
            }
        });

        Downloader clinic = new Downloader(EnrollDoctor.this, urlClinicSpinner, spinnerClinic, "clinicName", "Choose Clinic");
        clinic.execute();

        Downloader dep = new Downloader(EnrollDoctor.this, urlDeptSpinner, spinnerDep, "Name", "Choose Department");
        dep.execute();
        Downloader docType = new Downloader(EnrollDoctor.this, urlDocTypeSpinner, spinnerDocType, "DoctorType", "Choose Doctor Type");
        docType.execute();

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

    public void goBack() {
        Intent intent = new Intent(this, EnrollmentPage.class);
        startActivity(intent);
    }

    public boolean checkDeptRecord() {
        boolean hasExistingDept = false;
        Connection con = connectionClass.CONN();
        String docFName = doctorFName.getText().toString();
        String docLName = doctorLName.getText().toString();

        if(con != null){ //means that we have a valid db connection
            try{//inserting records; called INSERT_REC from DBUtility.java
                // use of parameterized query such as PreparedStatement prevents SQL injection which is considered a way to
                //prevent threat in any web app
                String query = VALIDATION_DOCTOR;
                PreparedStatement ps = con.prepareStatement(query);
                ps.setString(1, docFName);
                ps.setString(2, docLName);

                ResultSet rs=ps.executeQuery();
                if(rs.next()){
                    hasExistingDept=true;
                    Toast.makeText(getApplicationContext(),"Record already exists",Toast.LENGTH_LONG).show();
                }
            } catch(SQLException sqle){
                System.err.println(sqle.getMessage());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return hasExistingDept;
    }


    private class DoEnrollDoc extends AsyncTask<String, String, String> {

        Security sec = new Security();
        boolean isSuccess = false;
        boolean hasRecord = false;
        String message = "";
        int reason = 0;
        String docFName = doctorFName.getText().toString();
        String docLName = doctorLName.getText().toString();
        String roomNum = roomNo.getText().toString();
        String sched1 = schedule1.getText().toString();
        String sched2 = schedule2.getText().toString();
        String cboxMon = monday.getText().toString();
        String cboxTues = tuesday.getText().toString();
        String cboxWed = wednesday.getText().toString();
        String cboxThurs = thursday.getText().toString();
        String cboxFri = friday.getText().toString();
        String cboxSat = saturday.getText().toString();
        String docDays="";
        int docType = (int)spinnerDocType.getSelectedItemId();
        int dept = (int)spinnerDep.getSelectedItemId();
        int clinic = Integer.parseInt(session.getclinicid());
        String status = "Active";

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            Connection con = connectionClass.CONN();
            PreparedStatement ps1 = null;
            try {
                ps1 = con.prepareStatement(VALIDATION_DOCTOR);
                ps1.setString(1, docFName);

                ResultSet rs = ps1.executeQuery();

                if (rs.next()) {
                    hasRecord=true;

                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

            if (docFName.trim().equals("") || docLName.trim().equals("") || roomNum.trim().equals("") || sched1.trim().equals("") || sched2.trim().equals("") ) {
                message = "Please enter all fields....";
            }
            if (!docFName.matches("^[A-Za-z]+$") || !docLName.matches("^[A-Za-z]+$")) {
                message = "Check format";
            }
            if (hasRecord){
                message = "Record already exists";
            }
            else {
                try {
                    if (con == null) {
                        message = "Unsuccessful";
                    } else {

                        if(cboxMon!=null){
                            docDays+=cboxMon;
                        }
                        if(cboxTues!=null){
                            docDays+=cboxTues;
                        }
                        if(cboxWed!=null){
                            docDays+=cboxWed;
                        }
                        if(cboxThurs!=null){
                            docDays+=cboxThurs;
                        }
                        if(cboxFri!=null){
                            docDays+=cboxFri;
                        }
                        if(cboxSat!=null){
                            docDays+=cboxSat;
                        }
                        String query = INSERT_DOCTOR;
                        PreparedStatement ps = con.prepareStatement(query);

                        ps.setInt (1, docType);
                        ps.setInt (2, clinic);
                        ps.setInt(3, reason);
                        ps.setString(4, docFName);
                        ps.setString(5, docLName);
                        ps.setInt (6, dept);
                        ps.setString(7, roomNum);
                        ps.setString(8, sched1);
                        ps.setString(9, sched2);
                        ps.setString(10, docDays);
                        ps.setString(11, status);

                        ps.execute();
                        con.close();
                        message = "ADDED";
                    }
                } catch (Exception ex) {
                    isSuccess = false;
                    message = "Exceptions" + ex;
                }
            }
            return message;
        }

        @Override
        protected void onPostExecute(String s) {
            Toast.makeText(getBaseContext(), "" + message, Toast.LENGTH_LONG).show();

            if (isSuccess) {
                Intent intent = new Intent(EnrollDoctor.this, EnrollDoctor.class);
                startActivity(intent);
            }

        }

    }
}
