package com.kerux.admin_thesis_kerux.enrollment;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.kerux.admin_thesis_kerux.navigation.EnrollmentPage;
import com.kerux.admin_thesis_kerux.navigation.MainActivity;
import com.kerux.admin_thesis_kerux.navigation.ManageAccounts;
import com.kerux.admin_thesis_kerux.R;
import com.kerux.admin_thesis_kerux.security.Security;
import com.kerux.admin_thesis_kerux.dbutility.ConnectionClass;
import com.kerux.admin_thesis_kerux.dbutility.DBUtility;
import com.kerux.admin_thesis_kerux.spinner.Downloader;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Random;

public class EnrollQM extends AppCompatActivity implements DBUtility {

    private EditText qmFirstName;
    private EditText qmLastName;
    private EditText qmUname;
    private EditText qmPw;
    private EditText qmEmail;
    private Spinner spinnerClinic;
    private Spinner spinnerDept;
    Button generatePass;
    ConnectionClass connectionClass;
    private static String urlClinicSpinner = "http://10.0.2.2:89/kerux/clinicSpinner.php";
    private static String urlDeptSpinner = "http://10.0.2.2:89/kerux/departmentSpinner.php";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enroll_qm);
        connectionClass = new ConnectionClass(); //create ConnectionClass

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.nav_view);
        navigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.navigation_dashboard:
                        Intent a = new Intent(EnrollQM.this, MainActivity.class);
                        startActivity(a);
                        break;
                    case R.id.navigation_enrollment:
                        Intent b = new Intent(EnrollQM.this, EnrollmentPage.class);
                        startActivity(b);
                        break;
                    case R.id.navigation_accounts:
                        Intent c = new Intent(EnrollQM.this, ManageAccounts.class);
                        startActivity(c);
                        break;
                }
                return false;
            }
        });

        Button bttnBack = findViewById(R.id.bttnQMgoback);
        Button bttnEnrollQM = findViewById(R.id.bttnEnrollQM);
        spinnerClinic = (Spinner) findViewById(R.id.spinnerClinic);
        spinnerDept = (Spinner) findViewById(R.id.spinnerDept);

        bttnBack.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                goBack();
            }
        });

        bttnEnrollQM.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EnrollQM.DoEnrollQM doenroll = new DoEnrollQM();
                doenroll.execute();
                qmFirstName.getText().clear();
                qmLastName.getText().clear();
                qmUname.getText().clear();
                qmPw.getText().clear();
                qmEmail.getText().clear();
            }
        });

        qmFirstName = (EditText) findViewById(R.id.txtboxQMFname);
        qmLastName = (EditText) findViewById(R.id.txtboxQmLname);
        qmEmail = (EditText) findViewById(R.id.txtboxQMEmail);
        qmUname = (EditText) findViewById(R.id.txtboxQMun);
        qmPw = (EditText) findViewById(R.id.txtboxQMpw);

        generatePass = findViewById(R.id.bttnGenerate);
        generatePass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                qmPw.setText(generateString(12));
            }
        });
        Downloader clinic = new Downloader(EnrollQM.this, urlClinicSpinner, spinnerClinic, "clinicName");
        clinic.execute();
        Downloader dep = new Downloader(EnrollQM.this, urlDeptSpinner, spinnerDept, "Name");
        dep.execute();
    }

    private String generateString(int length){
        char[] chars = "QWERTYUIOPASDFGHJKLZXCVBNMmnbvcxzlkjhgfdsapoiuytrewq1234567890!@#$%^&*()".toCharArray();
        StringBuilder stringBuilder = new StringBuilder();

        Random random = new Random();
        for(int i = 0; i < length; i++){
            char c = chars[random.nextInt(chars.length)];
            stringBuilder.append(c);
        }
        return stringBuilder.toString();
    }

    //go back to the previous page
    public void goBack() {
        Intent intent = new Intent(this, EnrollmentPage.class);
        startActivity(intent);
    }

    //Enrolling or adding the record to the database for the queue manager
    private class DoEnrollQM extends AsyncTask<String, String, String> {

        Security sec = new Security();
        boolean isSuccess = false;
        String message = "";
        String QMFname = qmFirstName.getText().toString();
        String QMLname = qmLastName.getText().toString();
        String QMEmail = qmEmail.getText().toString();
        String QMuname = qmUname.getText().toString();
        String QMpw = qmPw.getText().toString();
        String status = "Active";
        boolean hasRecord = false;
        int clinicName = (int)spinnerClinic.getSelectedItemId();
        int deptName = (int)spinnerDept.getSelectedItemId();

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            Connection con = connectionClass.CONN();
            PreparedStatement ps = null;
            /*try {
                ps = con.prepareStatement(VALIDATION_QM);
                ps.setString(1, QMname);

                ResultSet rs = ps.executeQuery();

                if (rs.next()) {
                    hasRecord = true;
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            message = "Record already exists";*/

            if (QMFname.trim().equals("") || QMLname.trim().equals("") || QMEmail.trim().equals("") || QMuname.trim().equals("") || QMpw.trim().equals("")) {
                message = "Please enter all fields....";

            }
            else if (!QMFname.matches("^[A-Za-z]+$") && !QMLname.matches("^[A-Za-z]+$")) {
                message = "Check format";
            }
            else if (hasRecord){
                message = "Record already exists";
            }
            else {
                try {
                    if (con == null) {
                        message = "Unsuccessful";
                    } else {
                        String query = INSERT_QM;
                        PreparedStatement ps1 = con.prepareStatement(query);
                        ps1.setString(1, String.valueOf(clinicName));
                        ps1.setString(2, String.valueOf(deptName));
                        ps1.setString(3, sec.encrypt(QMuname));
                        ps1.setString(4, String.valueOf(QMpw));
                        ps1.setString(5, QMFname);
                        ps1.setString(6, QMLname);
                        ps1.setString(7, QMEmail);
                        ps1.setString(8, status);

                        ps1.execute();
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
                Intent intent = new Intent(EnrollQM.this, EnrollQM.class);
                startActivity(intent);
            }

        }

    }
}
