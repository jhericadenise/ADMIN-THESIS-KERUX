package com.kerux.admin_thesis_kerux.enrollment;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

import com.kerux.admin_thesis_kerux.R;
import com.kerux.admin_thesis_kerux.dbutility.ConnectionClass;
import com.kerux.admin_thesis_kerux.dbutility.DBUtility;
import com.kerux.admin_thesis_kerux.edit.EditDoctor;
import com.kerux.admin_thesis_kerux.edit.EditQm;
import com.kerux.admin_thesis_kerux.email.SendMailTask;
import com.kerux.admin_thesis_kerux.navigation.EditProfile;
import com.kerux.admin_thesis_kerux.navigation.EnrollmentPage;
import com.kerux.admin_thesis_kerux.navigation.MainActivity;
import com.kerux.admin_thesis_kerux.navigation.ManageAccounts;
import com.kerux.admin_thesis_kerux.reports.ViewAuditReportsActivity;
import com.kerux.admin_thesis_kerux.reports.ViewStatReportsActivity;
import com.kerux.admin_thesis_kerux.security.Security;
import com.kerux.admin_thesis_kerux.security.SecurityWEB;
import com.kerux.admin_thesis_kerux.session.KeruxSession;
import com.kerux.admin_thesis_kerux.spinner.Downloader;
import com.kerux.admin_thesis_kerux.unenrollment.UnenrollDoc;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
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

public class EnrollQM extends AppCompatActivity implements DBUtility, View.OnClickListener {

    private EditText qmFirstName;
    private EditText qmLastName;
    private EditText qmPw;
    private EditText qmEmail;
    private Spinner deptSpinner;
    ConnectionClass connectionClass;
    private static final String urlDeptSpinner = "https://isproj2a.benilde.edu.ph/Sympl/departmentSpinnerServlet";
    Button generatePass;
    DrawerLayout drawerLayout;
    Button bttnAdd;

    KeruxSession session;

    //for uploading file
    private Button bttnAddPhotoDoc;
    private ImageView image;
    private final int IMG_REQUEST = 1;
    Bitmap bitmap;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enroll_qm);
        connectionClass = new ConnectionClass(); //create ConnectionClass
        final Context context = this;
        session=new KeruxSession(getApplicationContext());

        drawerLayout = findViewById(R.id.drawer_layout);
        bttnAddPhotoDoc  = findViewById(R.id.bttnAddPhoto);
        image = findViewById(R.id.imageView);

        bttnAddPhotoDoc.setOnClickListener(this);

        Button bttnEnrollQM = findViewById(R.id.bttnEnrollQM);
        deptSpinner = findViewById(R.id.spinnerDept);

        bttnEnrollQM.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!validateFName() || !validateLName() || !validateEmail()) {
                    confirmInput();
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(EnrollQM.this);
                    builder.setMessage("Are you sure you want to Enroll?")
                            .setCancelable(false)
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    EnrollQM.DoEnrollQM doenroll = new DoEnrollQM();
                                    doenroll.execute();
                                    /*sendEmail();*/
                                    qmFirstName.getText().clear();
                                    qmLastName.getText().clear();
                                    qmEmail.getText().clear();
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

        generatePass = findViewById(R.id.bttnGeneratePass);
        generatePass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                qmPw.setText(generateString(12));
            }
        });

        qmFirstName = findViewById(R.id.txtboxQMFname);
        qmLastName = findViewById(R.id.txtboxQmLname);
        qmEmail = findViewById(R.id.txtboxQMEmail);
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

    public void ClickEditQM(View view){
        MainActivity.redirectActivity(this, EditQm.class);
    }

    public void ClickEditDoctor(View view){
        MainActivity.redirectActivity(this, EditDoctor.class);
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

    private void sendEmail() {
        Resources res = getResources();
        String email = qmEmail.getText().toString().trim();
        String subject = res.getString(R.string.subjectEmail);
        String message = res.getString(R.string.bodyEmail) + "\n\n" +
                "Email: " + " " + qmEmail.getText().toString().trim() + "\n" +
                res.getString(R.string.password) + " " + qmPw.getText().toString().trim() + "\n\n" +
                res.getString(R.string.footerEmail);

        SendMailTask sm = new SendMailTask(this, email, subject, message);
        sm.execute();
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
        input += "First Name: " + qmFirstName.getText().toString();
        input += "\n";
        input += "Last Name: " + qmLastName.getText().toString();
        Toast.makeText(this, input, Toast.LENGTH_SHORT).show();
        return false;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId())
        {
            case R.id.bttnAddPhoto:
                break;
        }
    }

    private void selectImage(){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, IMG_REQUEST);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == IMG_REQUEST && resultCode == RESULT_OK && data!=null){
            Uri path = data.getData();
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), path);
                image.setImageBitmap(bitmap);
                image.setVisibility(View.VISIBLE);
            } catch (IOException e){
                e.printStackTrace();
            }
        }
    }

    //Enrolling or adding the record to the database for the queue manager
    private class DoEnrollQM extends AsyncTask<String, String, String> {
        SecurityWEB secw = new SecurityWEB();
        Security sec = new Security();
        boolean isSuccess = false;
        String message = "";
        String QMFname = qmFirstName.getText().toString();
        String QMLname = qmLastName.getText().toString();
        String QMEmail = qmEmail.getText().toString();
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
                            .appendQueryParameter("QMpw", QMpw)
                            .appendQueryParameter("secQMpw", sec.encrypt(QMpw))
                            .appendQueryParameter("QMFname", SecurityWEB.encrypt(QMFname))
                            .appendQueryParameter("QMLname", SecurityWEB.encrypt(QMLname))
                            .appendQueryParameter("QMEmail", SecurityWEB.encrypt(QMEmail))
                            .appendQueryParameter("status", status)
                            .appendQueryParameter("photo", "")
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
                    insertAudit();
                }catch(Exception e){
                    message="Exceptions"+e;
                }

            return message;
        }

        @Override
        protected void onPostExecute(String s) {
            Toast.makeText(getBaseContext(), "" + message, Toast.LENGTH_LONG).show();

            if(isSuccess) {
                try{
                    Toast.makeText(getBaseContext(),""+message,Toast.LENGTH_LONG).show();
                }catch(Exception e){
                    Log.d("insertAudit", e.getMessage());
                }

            }
        }

        public void insertAudit(){

            try {
                URL url = new URL("https://isproj2a.benilde.edu.ph/Sympl/InsertAuditAdminServlet");
                URLConnection connection = url.openConnection();

                connection.setReadTimeout(10000);
                connection.setConnectTimeout(15000);
                connection.setDoInput(true);
                connection.setDoOutput(true);

                Uri.Builder builder = new Uri.Builder()
                        .appendQueryParameter("first", SecurityWEB.encrypt("Queue Manager Enrollment").trim())
                        .appendQueryParameter("second", SecurityWEB.encrypt("Insert").trim())
                        .appendQueryParameter("third", SecurityWEB.encrypt("Insert queue manager record").trim())
                        .appendQueryParameter("fourth", SecurityWEB.encrypt("none").trim())
                        .appendQueryParameter("fifth", SecurityWEB.encrypt("Queue Manager ID: " + newqmid).trim())
                        .appendQueryParameter("sixth", SecurityWEB.encrypt(session.getusername()).trim());
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
