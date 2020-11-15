package com.kerux.admin_thesis_kerux.enrollment;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
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
import com.kerux.admin_thesis_kerux.spinner.Downloader;
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
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EnrollQM extends AppCompatActivity implements DBUtility{

    private EditText qmFirstName;
    private EditText qmLastName;
    private EditText qmUname;
    private EditText qmPw;
    private EditText qmEmail;
    private Spinner deptSpinner;
    ConnectionClass connectionClass;
    private static final String urlDeptSpinner = "https://isproj2a.benilde.edu.ph/Sympl/departmentSpinnerServlet";

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
        deptSpinner = findViewById(R.id.spinnerDept);

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

        qmFirstName = findViewById(R.id.txtboxQMFname);
        qmLastName = findViewById(R.id.txtboxQmLname);
        qmEmail = findViewById(R.id.txtboxQMEmail);
        qmUname = findViewById(R.id.txtboxQMun);
        qmPw = findViewById(R.id.txtboxQMpw);


        Downloader dep = new Downloader(EnrollQM.this, urlDeptSpinner, deptSpinner, "name", session.getclinicid(), "Choose Department");
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

        String expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";
        CharSequence inputStr = emailInputQM;

        Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(inputStr);

        if (emailInputQM.isEmpty()) {
            qmEmail.setError("Field can't be empty");
            return false;
        } else if (!matcher.matches()) {
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
        } else if(firstname.matches("^[0-9]+$")){
            qmFirstName.setError("Last name cannot contain number values");
            return false;
        }  else {
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
        }else if(lastname.matches("^[0-9]+$")){
            qmLastName.setError("Last name cannot contain number values");
            return false;
        }  else {
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
        String newqmid="";
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

            try {
                    URL url = new URL("https://isproj2a.benilde.edu.ph/Sympl/DoEnrollQMServlet");
                    URLConnection connection = url.openConnection();

                    connection.setReadTimeout(10000);
                    connection.setConnectTimeout(15000);
                    connection.setDoInput(true);
                    connection.setDoOutput(true);

                    Uri.Builder builder = new Uri.Builder()
                            .appendQueryParameter("clinic", Integer.toString(clinic))
                            .appendQueryParameter("dept", Integer.toString(dept))
                            .appendQueryParameter("reason", Integer.toString(reason))
                            .appendQueryParameter("QMuname", QMuname)
                            .appendQueryParameter("QMpw", QMpw)
                            .appendQueryParameter("secQMuname", sec.encrypt(QMuname))
                            .appendQueryParameter("secQMpw", sec.encrypt(QMpw))
                            .appendQueryParameter("QMFname", QMFname)
                            .appendQueryParameter("QMLname", QMLname)
                            .appendQueryParameter("QMEmail", QMEmail)
                            .appendQueryParameter("status", status)
                            .appendQueryParameter("getadminid", session.getadminid());
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
                        isSuccess=true;
                        Log.d("returnString", returnString);
                        output.add(returnString);
                    }
                    for (int i = 0; i < output.size(); i++) {
                        String line=output.get(i);
                        String notnullline = line.replaceAll("null", "0");
                        String [] words=notnullline.split("\\s\\|\\s");
                        for (int x=0; x<words.length;x++){
                            if(x==0){
                                message=words[x];
                            }
                            if(x==1){
                                newqmid=words[1];
                            }
                        }


                    }
                    in.close();
                }catch(Exception e){
                    message="Exceptions"+e;
                }

            return message;
        }

        @Override
        protected void onPostExecute(String s) {
            Toast.makeText(getBaseContext(), "" + message, Toast.LENGTH_LONG).show();

            if (isSuccess) {
                try{
                    insertAudit( sec.encrypt("queue manager"),  sec.encrypt("insert"),  sec.encrypt("Inserting Queue Manager record"),  sec.encrypt("none"),  sec.encrypt(clinic + ", " + reason + ", " + dept + ", " + status),  sec.encrypt(session.getusername()));
                    insertAudit( sec.encrypt("qmenrollment"),  sec.encrypt("insert"),  sec.encrypt("Insert into qmenrollment table"),  sec.encrypt("none"),  sec.encrypt(session.getadminid() + ", " + newqmid + ", " + ", " + newqmid + ", " + session.getclinicid()),  sec.encrypt(session.getusername()));
                }catch(Exception e){
                    Log.d("insertAudit", e.getMessage());
                }

//                Intent intent = new Intent(EnrollQM.this, EnrollQM.class);
//                startActivity(intent);
            }

        }

        public void insertAudit(String first, String second, String third, String fourth, String fifth, String sixth){

            try {
                URL url = new URL("https://isproj2a.benilde.edu.ph/Sympl/InsertAuditAdminServlet");
                URLConnection connection = url.openConnection();

                connection.setReadTimeout(10000);
                connection.setConnectTimeout(15000);
                connection.setDoInput(true);
                connection.setDoOutput(true);

                Uri.Builder builder = new Uri.Builder()
                        .appendQueryParameter("first", first)
                        .appendQueryParameter("second", second)
                        .appendQueryParameter("third", third)
                        .appendQueryParameter("fourth", fourth)
                        .appendQueryParameter("fifth", fifth)
                        .appendQueryParameter("sixth", sixth);
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

    }
}
