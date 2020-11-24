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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class UnenrollDept extends AppCompatActivity implements DBUtility {
    ConnectionClass connectionClass;
    private ListView deptList;
    private ListAdapter listAdapter;
    Button deptDisplayList;
    private Spinner spinnerDeptReason;

    Button bttnBack;
    Button addReasonDept;
    private EditText otherReason;
    private EditText table;
    final Context context = this;

    DrawerLayout drawerLayout;
    private static final String urlReasonSpinner = "http://192.168.1.22:8080/RootAdmin/reasonSpinnerDeptServlet";
    KeruxSession session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate ( savedInstanceState );
        setContentView ( R.layout.activity_unenroll_dept );
        connectionClass = new ConnectionClass (); //create ConnectionClass
        session=new KeruxSession(getApplicationContext());

        drawerLayout = findViewById(R.id.drawer_layout);

        deptDisplayList = findViewById(R.id.bttnDisplayDept);
        deptList = findViewById(R.id.listEnrolledDept);

        addReasonDept = findViewById(R.id.bttnAddDeptReason);
        addReasonDept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // get prompts.xml view
                LayoutInflater li = LayoutInflater.from(context);
                View promptsView = li.inflate(R.layout.activity_add_reason_dept, null);

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
                                        DoEnrollReasonDept doEnrollReasonDept = new DoEnrollReasonDept();
                                        doEnrollReasonDept.execute();
                                        //for refreshing the spinner
                                        DownloaderDocType dept = new DownloaderDocType(UnenrollDept.this, urlReasonSpinner, spinnerDeptReason, "reason", "Choose Reason to Revoke");
                                        dept.execute();

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

        spinnerDeptReason = findViewById(R.id.spinnerDeptReason);

        deptDisplayList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ListDept deptListdisp = new ListDept();
                deptListdisp.execute();
            }
        });

        deptList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                final String selectedFromList = String.valueOf((deptList.getItemAtPosition(position)));
                final String selectedFromList =  getDeptString(String.valueOf((deptList.getItemAtPosition(position))));
//                Toast.makeText(getApplicationContext(),selected,Toast.LENGTH_LONG).show();
                //Dialog box, for unenrolling
                AlertDialog.Builder builder = new AlertDialog.Builder(UnenrollDept.this);
                builder.setMessage("Unenroll Department?")
                        .setCancelable(false)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
//                                String name = selectedFromList.substring(2, selectedFromList.length()-3);
                               /* String clinicName = selectedFromList.substring(1, selectedFromList.length()-1);
                                String status = selectedFromList.substring(3, selectedFromList.length()-1);*/
                                String reason = ((Spinner)findViewById(R.id.spinnerDeptReason)).getSelectedItem().toString();
                                Toast.makeText(getApplicationContext(),selectedFromList,Toast.LENGTH_LONG).show();
                                unenrollDept(selectedFromList, reason);
                                ListDept deptListdisp = new ListDept();
                                deptListdisp.execute();
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

        bttnBack = findViewById(R.id.bttnGoBack);
        bttnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goBack();
            }
        });

        DownloaderDocType dept = new DownloaderDocType(UnenrollDept.this, urlReasonSpinner, spinnerDeptReason, "reason", "Choose Reason to Revoke");
        dept.execute();

    }

    //go back to the previous page
    public void goBack() {
        Intent intent = new Intent(this, UnenrollQm.class);
        startActivity(intent);
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

    public void insertAudit(){

        Security sec = new Security();

        String statusActive = "Active";
        String statusInactive = "Inactive";
        String reason = ((Spinner)findViewById(R.id.spinnerDeptReason)).getSelectedItem().toString();

        try {
            URL url = new URL("http://192.168.1.22:8080/RootAdmin/InsertAuditAdminServlet");
            URLConnection connection = url.openConnection();

            connection.setReadTimeout(10000);
            connection.setConnectTimeout(15000);
            connection.setDoInput(true);
            connection.setDoOutput(true);

            Uri.Builder builder = new Uri.Builder()
                    .appendQueryParameter("first", sec.encrypt("Unenroll Department").trim())
                    .appendQueryParameter("second", sec.encrypt("delete").trim())
                    .appendQueryParameter("third", sec.encrypt("Unenrolling a department record").trim())
                    .appendQueryParameter("fourth", sec.encrypt("Status = " + statusActive).trim())
                    .appendQueryParameter("fifth", sec.encrypt("Status = " + statusInactive + ", " + "Reason = " + reason).trim())
                    .appendQueryParameter("sixth", sec.encrypt(session.getusername()).trim());
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

    //deleting a record in the database
    public void unenrollDept(String name, String reason){

        try {
            URL url = new URL("http://192.168.1.22:8080/RootAdmin/UnenrollDepServlet");
            URLConnection connection = url.openConnection();

            connection.setReadTimeout(10000);
            connection.setConnectTimeout(15000);
            connection.setDoInput(true);
            connection.setDoOutput(true);

            Uri.Builder builder = new Uri.Builder()
                    .appendQueryParameter("name", name)
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

    public String getDeptString(String rowFromListView){
        String name = rowFromListView.substring(1, rowFromListView.length()-1);

        String deptString = name.replaceAll(".*second=", "");
        return deptString;
    }



    //function for displaying the enrolled department
    private class ListDept extends AsyncTask<String, String, String> {
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
                deptList = findViewById(R.id.listEnrolledDept);
                URL url = new URL("http://192.168.1.22:8080/RootAdmin/ListDepServlet");
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
                    receivedData.append(returnString+"\n");
                    output.add(returnString);
                }
                for (int i = 0; i < output.size(); i++) {
                    message = output.get(i);
                }
                in.close();
                String retrieved=receivedData.toString();
                Log.d("STRRRING", retrieved);
                List<Map<String, String>> data= new ArrayList<Map<String, String>>();

                data= (new Gson()).fromJson(retrieved, new TypeToken<List<Map<String, String>>>() {}.getType());

                /*int[] viewswhere = {R.id.lblDeptList};*/
                listAdapter = new SimpleAdapter (UnenrollDept.this, data,
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
            deptList.setAdapter(listAdapter);
        }
    }
    private class DoEnrollReasonDept extends AsyncTask<String, String, String> {

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
                    URL url = new URL("http://192.168.1.22:8080/RootAdmin/DoEnrollReasonDept");
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
                /*Intent intent = new Intent(UnenrollDept.this, UnenrollDept.class);
                startActivity(intent);*/
                Toast.makeText(getBaseContext(), "Unenrolled Successfully", Toast.LENGTH_LONG).show();
            }

        }

    }
}
