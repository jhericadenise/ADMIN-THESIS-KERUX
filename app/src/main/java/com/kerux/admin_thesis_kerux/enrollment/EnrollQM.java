package com.kerux.admin_thesis_kerux.enrollment;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.kerux.admin_thesis_kerux.R;
import com.kerux.admin_thesis_kerux.dbutility.ConnectionClass;
import com.kerux.admin_thesis_kerux.dbutility.DBUtility;
import com.kerux.admin_thesis_kerux.email.SendMailTask;
import com.kerux.admin_thesis_kerux.navigation.EnrollmentPage;
import com.kerux.admin_thesis_kerux.navigation.MainActivity;
import com.kerux.admin_thesis_kerux.navigation.ManageAccounts;
import com.kerux.admin_thesis_kerux.security.Security;
import com.kerux.admin_thesis_kerux.spinner.Downloader;
import com.kerux.admin_thesis_kerux.unenrollment.UnenrollDoc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Random;

public class EnrollQM extends AppCompatActivity implements DBUtility{

    private EditText qmFirstName;
    private EditText qmLastName;
    private EditText qmUname;
    private EditText qmPw;
    private EditText qmEmail;
    private Spinner spinnerClinic;
    private Spinner spinnerDept;
    Button generatePass;
    ConnectionClass connectionClass;
    private static String urlClinicSpinner = "http://10.70.0.17:8081/kerux/clinicSpinner.php";
    private static String urlDeptSpinner = "http://10.70.0.17:8081/kerux/departmentSpinner.php";

    DrawerLayout drawerLayout;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enroll_qm);
        connectionClass = new ConnectionClass(); //create ConnectionClass

        drawerLayout = findViewById(R.id.drawer_layout);

        Button bttnEnrollQM = findViewById(R.id.bttnEnrollQM);
        spinnerClinic = (Spinner) findViewById(R.id.spinnerClinic);
        spinnerDept = (Spinner) findViewById(R.id.spinnerDept);

        bttnEnrollQM.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EnrollQM.DoEnrollQM doenroll = new DoEnrollQM();
                doenroll.execute();
                sendEmail();
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

        /*generatePass = findViewById(R.id.bttnGenerate);
        generatePass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                qmPw.setText(generateString(12));
            }
        });*/
        Downloader clinic = new Downloader(EnrollQM.this, urlClinicSpinner, spinnerClinic, "clinicName");
        clinic.execute();
        Downloader dep = new Downloader(EnrollQM.this, urlDeptSpinner, spinnerDept, "Name");
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

    public void ClickLogout(View view){
        MainActivity.logout(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        //close drawer
        MainActivity.closeDrawer(drawerLayout);
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
    private void sendEmail() {
        String email = qmEmail.getText().toString().trim();
        String subject = "Kerux Queue Manager Enrollment Credentials";
        String message = "Good day!\n" +
                "We've successfully enrolled you as a Queue Manager\n\n\n" +
                "Here are your credentials" + "\n\nUsername: " + qmUname.getText().toString().trim() +
                "\nPassword: " + qmPw.getText().toString().trim() + "\n\n You can now login on the kerux app using this credentials. \n\n Thank you!";

        SendMailTask sm = new SendMailTask(this, email, subject, message);
        sm.execute();
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
