package com.kerux.admin_thesis_kerux.unenrollment;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
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
import com.kerux.admin_thesis_kerux.spinner.DownloaderDocType;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UnenrollDoc  extends AppCompatActivity implements DBUtility{

    ConnectionClass connectionClass;
    private ListView docList;
    private ListAdapter listAdapter;
    Button docDisplayList;
    private Spinner spinnerReasonDoc;
    final Context context = this;

    Button addReasonDoc;
    private EditText otherReason;
    private EditText table;

    DrawerLayout drawerLayout;
    private static final String urlReasonSpinner = "https://isproj2a.benilde.edu.ph/Sympl/reasonSpinnerDoctorServlet";

    KeruxSession session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate ( savedInstanceState );
        setContentView ( R.layout.activity_unenroll_doctor );
        connectionClass = new ConnectionClass (); //create ConnectionClass
        session=new KeruxSession(getApplicationContext());

        drawerLayout = findViewById(R.id.drawer_layout);

        docDisplayList = findViewById(R.id.bttnDisplayDoc);
        docList = findViewById(R.id.listEnrolledDoc);

        addReasonDoc = findViewById(R.id.bttnAddDocReason);
        addReasonDoc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // get prompts.xml view
                LayoutInflater li = LayoutInflater.from(context);
                View promptsView = li.inflate(R.layout.activity_add_reason_doc, null);

                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);

                // set prompts.xml to alertdialog builder
                alertDialogBuilder.setView(promptsView);

                otherReason = promptsView.findViewById(R.id.txtboxReason);
                table = promptsView.findViewById(R.id.txtboxTableName);

                // set dialog message
                alertDialogBuilder
                        .setCancelable(false)
                        .setPositiveButton("ENROLL",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,int id) {
                                        // get user input and set it to result
                                        // edit text
                                        DoEnrollReasonDoc doEnrollReason = new DoEnrollReasonDoc();
                                        doEnrollReason.execute();
                                        //for the spinner
                                        DownloaderDocType doc = new DownloaderDocType(UnenrollDoc.this, urlReasonSpinner, spinnerReasonDoc, "reason", "Choose Reason to Revoke");
                                        doc.execute();
                                    }
                                })
                        .setNegativeButton("CANCEL",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,int id) {
                                        dialog.cancel();
                                    }
                                });

                // create alert dialog
                AlertDialog alertDialog = alertDialogBuilder.create();
                // show it
                alertDialog.show();
            }
        });

        spinnerReasonDoc = findViewById(R.id.spinnerDocReason);

        //display enrolled doctor on listview
        docDisplayList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ListDoc docListdisp = new ListDoc();
                docListdisp.execute();
            }
        });

        //selecting on listview and deleting the data selected
        docList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                final String selectedFromList = getDocString(String.valueOf((docList.getItemAtPosition(position))));
                Toast.makeText(getApplicationContext(),"You selected: "+selectedFromList,Toast.LENGTH_LONG).show();
                //Dialog box, for unenrolling
                AlertDialog.Builder builder = new AlertDialog.Builder(UnenrollDoc.this);
                builder.setMessage("Are you sure you want to revoke the privilege of this doctor?")
                        .setCancelable(false)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                String firstName = selectedFromList.substring(3, selectedFromList.length()-1);
                                String reason = ((Spinner)findViewById(R.id.spinnerDocReason)).getSelectedItem().toString();
                                Toast.makeText(getApplicationContext(),selectedFromList,Toast.LENGTH_LONG).show();
                                Toast.makeText(getApplicationContext(),"Successfully revoked privilege",Toast.LENGTH_LONG).show();
                                unenrollDoc (selectedFromList, reason);
                                UnenrollDoc.ListDoc docListdisp = new UnenrollDoc.ListDoc ();
                                docListdisp.execute();
                                insertAudit();
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

        });

        //for the spinner
        DownloaderDocType doc = new DownloaderDocType(UnenrollDoc.this, urlReasonSpinner, spinnerReasonDoc, "reason", "Choose Reason to Revoke");
        doc.execute();

        //restrict going to another activity for unenroll
        Button bttnQM = findViewById(R.id.bttnGoToUnenrollQM);
        bttnQM.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(checkDoctorList()) {
                    Intent intent5 = new Intent(UnenrollDoc.this, UnenrollQm.class);
                    startActivity(intent5);
                }
                else{
                    Toast.makeText(getApplicationContext(), "Cannot go to Unenrollment of Queue Manager, Must UNENROLL all Doctors to proceed.", Toast.LENGTH_LONG).show();
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

    //insert to audit logs
    public void insertAudit(){

        Security sec = new Security();

        String statusActive = "Active";
        String statusInactive = "Inactive";
        String reason = ((Spinner)findViewById(R.id.spinnerDocReason)).getSelectedItem().toString();

        try {
            URL url = new URL("https://isproj2a.benilde.edu.ph/Sympl/InsertAuditAdminServlet");
            URLConnection connection = url.openConnection();

            connection.setReadTimeout(10000);
            connection.setConnectTimeout(15000);
            connection.setDoInput(true);
            connection.setDoOutput(true);

            Uri.Builder builder = new Uri.Builder()
                    .appendQueryParameter("first", sec.encrypt("doctor"))
                    .appendQueryParameter("second", sec.encrypt("unenroll doctor"))
                    .appendQueryParameter("third", sec.encrypt("Unenrolling a doctor record"))
                    .appendQueryParameter("fourth", sec.encrypt("Status = " + statusActive))
                    .appendQueryParameter("fifth", sec.encrypt("Status = " + statusInactive + ", " + "Reason = " + reason))
                    .appendQueryParameter("sixth", session.getusername());
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

    //unenrolling doctor records
    public void unenrollDoc(String firstName, String reason){

        try {
            URL url = new URL("https://isproj2a.benilde.edu.ph/Sympl/UnenrollDocServlet");
            URLConnection connection = url.openConnection();

            connection.setReadTimeout(10000);
            connection.setConnectTimeout(15000);
            connection.setDoInput(true);
            connection.setDoOutput(true);

            Uri.Builder builder = new Uri.Builder()
                    .appendQueryParameter("firstName", firstName)
                    .appendQueryParameter("reason", reason);
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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //checking if doctor tables still have Active records
    public boolean checkDoctorList(){
        boolean allInactiveRec = false;

        String docStatus = "Active";

        try {
            URL url = new URL("https://isproj2a.benilde.edu.ph/Sympl/CheckDocList");
            URLConnection connection = url.openConnection();

            connection.setReadTimeout(10000);
            connection.setConnectTimeout(15000);
            connection.setDoInput(true);
            connection.setDoOutput(true);

            Uri.Builder builder = new Uri.Builder()
                    .appendQueryParameter("docStatus", docStatus);
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
            String result=output.get(0);
            if (result.equals("true")){

            }
            else{
                allInactiveRec=true;
            }
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return allInactiveRec;
    }

    //go back to the previous page
    public void goBack() {
        Intent intent = new Intent(this, EnrollmentPage.class);
        startActivity(intent);
    }

    //removing unnecessary strings when clicking a record in listview and getting the id needed to unenroll
    public String getDocString(String rowFromListView){
        String name = rowFromListView.substring(1, rowFromListView.length()-1);

        String docString1=name.replaceAll("third=", "");
        String docString2=docString1.replaceAll(",.+", "");
        Log.d("DOCSTRING:", docString2);

        return docString2;
    }


    //function for displaying the enrolled doctors
    private class ListDoc extends AsyncTask<String, String, String> {
        Connection con = connectionClass.CONN();
        boolean isSuccess = false;
        String message = "";

        @Override
        protected void onPreExecute() {
            Toast.makeText(getBaseContext(),"Please wait..",Toast.LENGTH_LONG).show();
            super.onPreExecute();
        }
        @Override
        protected String doInBackground(String... strings) {
            try {
                //listview, list the names of all enrolled department
                docList = findViewById(R.id.listEnrolledDoc);
                URL url = new URL("https://isproj2a.benilde.edu.ph/Sympl/ListDocServlet");
                URLConnection connection = url.openConnection();

                connection.setReadTimeout(10000);
                connection.setConnectTimeout(15000);
                connection.setDoInput(true);
                connection.setDoOutput(true);


                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String returnString="";
                StringBuffer receivedData=new StringBuffer();
                ArrayList<String> output=new ArrayList<String>();
                while ((returnString = in.readLine()) != null)
                {
                    receivedData.append(returnString+"n");
                    output.add(returnString);
                }
                for (int i = 0; i < output.size(); i++) {
                    message = output.get(i);
                }
                in.close();
                String retrieved=receivedData.toString();
                List<Map<String, String>> data= new ArrayList<Map<String, String>>();

                data= (new Gson()).fromJson(retrieved, new TypeToken<List<Map<String, String>>>() {}.getType());
                listAdapter = new SimpleAdapter (UnenrollDoc.this, data,
                        R.layout.listview_row, new String[] {"first", "second", "third"}, new int[] {R.id.FIRST_COL, R.id.SECOND_COL, R.id.THIRD_COL});


                message = "DELETED";
            } catch (Exception ex) {
                isSuccess = false;
                message = "Exceptions" + ex;
            }
            return message;
        }
        @Override
        protected void onPostExecute(String s) {
            docList.setAdapter(listAdapter);
        }
    }

    private class DoEnrollReasonDoc extends AsyncTask<String, String, String> {

        Security sec = new Security();
        boolean isSuccess = false;
        String message = "";
        String reason = otherReason.getText().toString();
        String tableName = table.getText().toString();
        boolean hasRecord = false;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }
        @Override
        protected String doInBackground(String... params) {
            Connection con = connectionClass.CONN();

            if (reason.trim().equals("")) {
                message = "Please enter all fields";
            }
            else if (hasRecord){
                message = "Record already exists";
            }
            else {
                try {
                    URL url = new URL("https://isproj2a.benilde.edu.ph/Sympl/DoEnrollDocReasonServlet");
                    URLConnection connection = url.openConnection();

                    connection.setReadTimeout(10000);
                    connection.setConnectTimeout(15000);
                    connection.setDoInput(true);
                    connection.setDoOutput(true);

                    Uri.Builder builder = new Uri.Builder()
                            .appendQueryParameter("reason", reason)
                            .appendQueryParameter("tableName", tableName);
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
                        message = output.get(i);
                    }
                    in.close();
                } catch (Exception e) {
                    isSuccess = false;
                    message = "Exceptions"+e;
                    Log.d("ex", e.getMessage () + " Jheca");
                    e.printStackTrace();
                }

            }
            return message;
        }

        @Override
        protected void onPostExecute(String s) {

            Toast.makeText(getBaseContext(), "" + message, Toast.LENGTH_LONG).show();

            if (isSuccess) {
                Intent intent = new Intent(UnenrollDoc.this, UnenrollDoc.class);
                startActivity(intent);
            }

        }

    }
}
