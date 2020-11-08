package com.kerux.admin_thesis_kerux.enrollment;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Patterns;
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
import com.kerux.admin_thesis_kerux.email.SendMailTask;
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
import java.util.Random;
import java.util.regex.Pattern;

public class EnrollQM extends AppCompatActivity implements DBUtility{

    private EditText qmFirstName;
    private EditText qmLastName;
    private EditText qmUname;
    private EditText qmPw;
    private EditText qmEmail;
    private Spinner deptSpinner;
    ConnectionClass connectionClass;
    private static String urlDeptSpinner = "http://192.168.1.13:89/kerux/departmentSpinner.php";

    DrawerLayout drawerLayout;

    KeruxSession session;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enroll_qm);
        connectionClass = new ConnectionClass(); //create ConnectionClass

        session=new KeruxSession(getApplicationContext());

        drawerLayout = findViewById(R.id.drawer_layout);

        Button bttnEnrollQM = findViewById(R.id.bttnEnrollQM);
        deptSpinner = (Spinner) findViewById(R.id.spinnerDept);

        bttnEnrollQM.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!validateFName() || !validateLName() || !validateEmail() || !validateUsername() || !validatePassword()) {
                    confirmInput();
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(EnrollQM.this);
                    builder.setMessage("Are you sure you want to Enroll?")
                            .setCancelable(false)
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    EnrollQM.DoEnrollQM doenroll = new DoEnrollQM();
                                    doenroll.execute();
                                    sendEmail();
                                    qmFirstName.getText().clear();
                                    qmLastName.getText().clear();
                                    qmEmail.getText().clear();
                                    qmUname.getText().clear();
                                    qmPw.getText().clear();
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

        qmFirstName = (EditText) findViewById(R.id.txtboxQMFname);
        qmLastName = (EditText) findViewById(R.id.txtboxQmLname);
        qmEmail = (EditText) findViewById(R.id.txtboxQMEmail);
        qmUname = (EditText) findViewById(R.id.txtboxQMun);
        qmPw = (EditText) findViewById(R.id.txtboxQMpw);


        Downloader dep = new Downloader(EnrollQM.this, urlDeptSpinner, deptSpinner, "Name", "Choose Department");
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
                "\nPassword: " + qmPw.getText().toString().trim() + "\n\n You can now login on the kerux app using this credentials. \n" +
                "Please change your password immediately after receiving this email\n\n" +
                "\n\n Thank you!";

        SendMailTask sm = new SendMailTask(this, email, subject, message);
        sm.execute();
    }
    
    private static final Pattern PASSWORD_PATTERN =
            Pattern.compile("^" +
                    //"(?=.*[0-9])" +         //at least 1 digit
                    //"(?=.*[a-z])" +         //at least 1 lower case letter
                    //"(?=.*[A-Z])" +         //at least 1 upper case letter
                    "(?=.*?[A-Z])" +      //any letter
                    "(?=.*?[a-z])" +    //at least 1 special character
                    "(?=.*?[0-9])" +           //no white spaces
                    "(?=.*?[#?!@$%^&*-])"+
                    ".{8,}"+
                    "$");

    private boolean validatePassword() {
        String qmpassword = qmPw.getText().toString().trim();
        if (qmpassword.isEmpty()) {
            qmPw.setError("Field can't be empty");
            return false;
        } else if (!PASSWORD_PATTERN.matcher(qmpassword).matches()) {
            qmPw.setError("Password too weak");
            return false;
        } else {
            qmPw.setError(null);
            return true;
        }
    }

    private boolean validateEmail() {
        String emailInputQM = qmEmail.getText().toString().trim();
        if (emailInputQM.isEmpty()) {
            qmEmail.setError("Field can't be empty");
            return false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(emailInputQM).matches()) {
            qmEmail.setError("Please enter a valid email address");
            return false;
        } else {
            qmEmail.setError(null);
            return true;
        }
    }
    private boolean validateUsername() {
        String usernameInputQM = qmUname.getText().toString().trim();
        if (usernameInputQM.isEmpty()) {
            qmUname.setError("Field can't be empty");
            return false;
        } else if (usernameInputQM.length() > 15) {
            qmUname.setError("Username too long");
            return false;
        } else {
            qmUname.setError(null);
            return true;
        }
    }

    private boolean validateFName() {
        String firstname = qmFirstName.getText().toString().trim();

        if(firstname.isEmpty()){
            qmFirstName.setError("Field can't be empty");
            return false;
        } else if (firstname.length() < 3){
            qmFirstName.setError("First Name too short");
            return false;
        } else if(firstname.matches("[^a-zA-Z]")) {
            qmFirstName.setError("First name cannot have number values");
            return false;
        } else {
            qmFirstName.setError(null);
            return true;
        }
    }

    private boolean validateLName() {
        String lastname = qmLastName.getText().toString().trim();

        if(lastname.isEmpty()){
            qmLastName.setError("Field can't be empty");
            return false;
        } else if (lastname.length() < 2){
            qmLastName.setError("Last Name too short");
            return false;
        } else if(lastname.matches("^[^a-zA-Z]")) {
            qmLastName.setError("Last name cannot have number values");
            return false;
        } else {
            qmLastName.setError(null);
            return true;
        }
    }

    public boolean confirmInput() {
        String input = "Email: " + qmEmail.getText().toString();
        input += "\n";
        input += "Username: " + qmUname.getText().toString();
        input += "\n";
        input += "Password: " + qmPw.getText().toString();
        input += "\n";
        input += "First Name: " + qmFirstName.getText().toString();
        input += "\n";
        input += "Last Name: " + qmLastName.getText().toString();
        Toast.makeText(this, input, Toast.LENGTH_SHORT).show();
        return false;
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
        int reason = 0;
        int clinic = Integer.parseInt(session.getclinicid());
        int dept = (int)deptSpinner.getSelectedItemId();

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            Connection con = connectionClass.CONN();
            PreparedStatement ps = null;
            try {
                ps = con.prepareStatement(VALIDATION_QM);
                ps.setInt (1, clinic);
                ps.setString(2, QMFname);
                ps.setString(3, QMLname);

                ResultSet rs=ps.executeQuery();

                if(rs.next()){
                    hasRecord = true;
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            message = "Record already exists";

                try {
                    if (con == null) {
                        message = "Unsuccessful";
                    } else {
                        String query = INSERT_QM;
                        PreparedStatement ps1 = con.prepareStatement(query);
                        ps1.setInt(1, clinic);
                        ps1.setInt(2, dept);
                        ps1.setInt(3, reason);
                        ps1.setString(4, sec.encrypt(QMuname));
                        ps1.setString(5, sec.encrypt(QMpw));
                        ps1.setString(6, QMFname);
                        ps1.setString(7, QMLname);
                        ps1.setString(8, QMEmail);
                        ps1.setString(9, status);

                        ps1.execute();

                        String query2 = SELECT_NEW_QUEUEMANAGER_ID;
                        PreparedStatement ps2 = con.prepareStatement(query2);
                        ResultSet rs1 = ps2.executeQuery();
                        while (rs1.next()) {
                            String newqmid = rs1.getString(1);
                            String newdeptid = rs1.getString(1);

                            //inserting to qm enrollment
                            String query3 = INSERT_QM_ENROLLMENT;
                            PreparedStatement ps3 = con.prepareStatement(query3);
                            ps3.setString(1, newqmid);
                            ps3.setString(2, session.getadminid());
                            ps3.setString(3, String.valueOf(dept));
                            ps3.setString(4, String.valueOf(clinic));
                            ps3.executeUpdate();

                            //inserting to audit log
                            String queryAUDIT = INSERT_AUDIT_LOG;
                            PreparedStatement psAUDIT = con.prepareStatement(queryAUDIT);
                            psAUDIT.setString(1, sec.encrypt("queue manager"));
                            psAUDIT.setString(2, sec.encrypt("insert"));
                            psAUDIT.setString(3, sec.encrypt("Inserting Queue Manager record"));
                            psAUDIT.setString(4, sec.encrypt("none"));
                            psAUDIT.setString(5, sec.encrypt(String.valueOf(clinic) + ", " + reason + ", " + dept + ", " + status));
                            psAUDIT.setString(6, sec.encrypt(session.getusername()));
                            psAUDIT.executeUpdate();
                            //inserting to audit log
                            PreparedStatement psAUDIT1 = con.prepareStatement(queryAUDIT);
                            psAUDIT.setString(1, sec.encrypt("qmenrollment"));
                            psAUDIT.setString(2, sec.encrypt("insert"));
                            psAUDIT.setString(3, sec.encrypt("Insert into qmenrollment table"));
                            psAUDIT.setString(4, sec.encrypt("none"));
                            psAUDIT.setString(5, sec.encrypt(session.getadminid() + ", " + newqmid + ", " + ", " + newdeptid + ", " + session.getclinicid()));
                            psAUDIT.setString(6, sec.encrypt(session.getusername()));
                            psAUDIT.executeUpdate();
                        }
                        con.close();

                    }
                } catch (Exception ex) {
                    isSuccess = false;
                    message = "Exceptions" + ex;
                }
            return message;
        }

        @Override
        protected void onPostExecute(String s) {
/*
            Toast.makeText(getBaseContext(), "" + message, Toast.LENGTH_LONG).show();
*/

            if (isSuccess) {
                Intent intent = new Intent(EnrollQM.this, EnrollQM.class);
                startActivity(intent);
            }

        }

    }
}
