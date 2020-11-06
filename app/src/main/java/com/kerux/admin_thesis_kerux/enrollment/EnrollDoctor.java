package com.kerux.admin_thesis_kerux.enrollment;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
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
import com.kerux.admin_thesis_kerux.spinner.Downloader;
import com.kerux.admin_thesis_kerux.unenrollment.UnenrollDoc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class EnrollDoctor extends AppCompatActivity implements DBUtility{

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


        bttnEnrollDoc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!validateFName() || !validateLName() ||!validateRoomNo() || !validateSched()) {
                    confirmInput();
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(EnrollDoctor.this);
                    builder.setMessage("Are you sure you want to Enroll?")
                            .setCancelable(false)
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    EnrollDoctor.DoEnrollDoc doenroll = new EnrollDoctor.DoEnrollDoc();
                                    doenroll.execute();
                                    doctorFName.getText().clear();
                                    doctorLName.getText().clear();
                                    roomNo.getText().clear();
                                    schedule1.getText().clear();
                                    schedule2.getText().clear();
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

    private boolean validateFName() {
        String firstname = doctorFName.getText().toString().trim();

        if(firstname.isEmpty()){
            doctorFName.setError("Field can't be empty");
            return false;
        } else if (firstname.length() < 3){
            doctorFName.setError("First Name too short");
            return false;
        } else if(firstname.matches("^[A-Za-z]+$")) {
            doctorLName.setError("Last name cannot have number values");
            return false;
        } else {
            doctorFName.setError(null);
            return true;
        }
    }

    private boolean validateLName() {
        String lastname = doctorLName.getText().toString().trim();

        if(lastname.isEmpty()){
            doctorLName.setError("Field can't be empty");
            return false;
        } else if (lastname.length() < 2){
            doctorLName.setError("Last Name too short");
            return false;
        } else if(lastname.matches("^[A-Za-z]+$")) {
            doctorLName.setError("Last name cannot have number values");
            return false;
        }
        else {
            doctorLName.setError(null);
            return true;
        }
    }

    private boolean validateSched() {
        String sched1 = schedule1.getText().toString().trim();
        String sched2 = schedule2.getText().toString().trim();

        if(sched1.isEmpty()){
            schedule1.setError("Field can't be empty");
            return false;
        } else if(sched2.isEmpty()){
            schedule2.setError("Field can't be empty");
            return false;
        } else {
            schedule1.setError(null);
            schedule2.setError(null);
            return true;
        }
    }

    private boolean validateRoomNo() {
        String room = roomNo.getText().toString().trim();

        if(room.isEmpty()){
            roomNo.setError("Field can't be empty");
            return false;
        } else {
            roomNo.setError(null);
            return true;
        }
    }

    public boolean confirmInput() {
        String input = "First Name: " + doctorFName.getText().toString();
        input += "\n";
        input += "Last Name: " + doctorLName.getText().toString();
        input += "Schedule: " + schedule1.getText().toString();
        input += "Room No: " + roomNo.getText().toString();
        Toast.makeText(this, input, Toast.LENGTH_SHORT).show();
        return false;
    }

    //method for enrolling a doctor into the database
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

            //checking if the record exists on the database
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
                        //inserting doctor in the database
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

                        String query2=SELECT_NEW_DOCTOR_ID;
                        PreparedStatement ps2 = con.prepareStatement(query2);
                        ResultSet rs1 = ps2.executeQuery();
                        while(rs1.next()){
                            String newdocid=rs1.getString(1);
                            String newdeptid=rs1.getString(1);

                            //inserting into doctor_enrollment
                            String query3=INSERT_DOC_ENROLLMENT;
                            PreparedStatement ps3 = con.prepareStatement(query3);
                            ps3.setString(1, session.getadminid());
                            ps3.setString(2, session.getclinicid());
                            ps3.setString(3, newdeptid);
                            ps3.setString(4, newdocid);
                            ps3.executeUpdate();

                            //inserting to audit log
                            String queryAUDIT=INSERT_AUDIT_LOG;
                            PreparedStatement psAUDIT=con.prepareStatement(queryAUDIT);
                            psAUDIT.setString(1, "doctor");
                            psAUDIT.setString(2, "insert");
                            psAUDIT.setString(3, "Insert doctor record");
                            psAUDIT.setString(4, "none");
                            psAUDIT.setString(5, String.valueOf(clinic)+", "+reason+", "+dept+", "+status);
                            psAUDIT.setString(6, session.getusername());
                            psAUDIT.executeUpdate();
                            //inserting to audit log
                            PreparedStatement psAUDIT1=con.prepareStatement(queryAUDIT);
                            psAUDIT.setString(1, "doctor_enrollment");
                            psAUDIT.setString(2, "insert");
                            psAUDIT.setString(3, "Insert into doctor_enrollment table record");
                            psAUDIT.setString(4, "none");
                            psAUDIT.setString(5, session.getadminid()+", "+newdocid+", "+ ", "+ newdeptid + ", " + session.getclinicid());
                            psAUDIT.setString(6, session.getusername());
                            psAUDIT.executeUpdate();
                        }
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
