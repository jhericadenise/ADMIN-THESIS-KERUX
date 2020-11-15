package com.kerux.admin_thesis_kerux.navigation;

import android.content.Context;
import android.content.DialogInterface;
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
import com.kerux.admin_thesis_kerux.reports.ViewAuditReportsActivity;
import com.kerux.admin_thesis_kerux.reports.ViewStatReportsActivity;
import com.kerux.admin_thesis_kerux.security.Security;
import com.kerux.admin_thesis_kerux.session.KeruxSession;
import com.kerux.admin_thesis_kerux.spinner.DownloaderDocType;
import com.kerux.admin_thesis_kerux.unenrollment.UnenrollDoc;

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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ManageAccounts extends AppCompatActivity implements DBUtility{

    ConnectionClass connectionClass;
    private ListAdapter listAdapterAccounts;
    private ListAdapter listAdapterBlockedAcc;
    private ListView accountsList;
    private ListView blockedList;
    Button displayAccounts;
    Button displayBlocked;

    Button addReasonAcc;
    private EditText otherReason;
    private EditText table;
    final Context context = this;

    DrawerLayout drawerLayout;
    private Spinner spinnerReasonPatient;
    private static final String urlReasonSpinner = "https://isproj2a.benilde.edu.ph/Sympl/reasonSpinnerPatientServlet";
    KeruxSession session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate ( savedInstanceState );
        setContentView ( R.layout.activity_manage_accounts );
        connectionClass = new ConnectionClass (); //create ConnectionClass
        drawerLayout = findViewById(R.id.drawer_layout);
        session=new KeruxSession(getApplicationContext());
        spinnerReasonPatient = findViewById(R.id.spinnerAccReason);

        accountsList = findViewById(R.id.listAccounts);
        displayAccounts = findViewById(R.id.bttnViewAcc);
        blockedList = findViewById(R.id.listBlocked);
        displayBlocked = findViewById(R.id.bttnViewBlocked);
        addReasonAcc = findViewById(R.id.bttnAddAccReason);
        addReasonAcc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // get prompts.xml view
                LayoutInflater li = LayoutInflater.from(context);
                View promptsView = li.inflate(R.layout.activity_add_reason_acc, null);

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
                                        DoEnrollReasonAcc doEnrollReasonAcc = new DoEnrollReasonAcc();
                                        doEnrollReasonAcc.execute();
                                        //for refreshing the spinner
                                        DownloaderDocType dept = new DownloaderDocType(ManageAccounts.this, urlReasonSpinner, spinnerReasonPatient, "reason", "Choose Reason to Revoke");
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

        //button for displaying enrolled accounts in a list view
        displayAccounts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ManageAccounts.ListEnrolledAcc viewAccs = new ManageAccounts.ListEnrolledAcc();
                viewAccs.execute();
            }
        });
        //button for displaying blocked accounts in a listview
        displayBlocked.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ManageAccounts.ListBlockedAccs viewBlockAcc = new ManageAccounts.ListBlockedAccs();
                viewBlockAcc.execute();
            }
        });

        //clicking on list view and deleting record
        accountsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final String selectedFromList =  getAccString(String.valueOf((accountsList.getItemAtPosition(position))));

                //Dialog box, for unenrolling
                AlertDialog.Builder builder = new AlertDialog.Builder(ManageAccounts.this);
                builder.setMessage("Block Privilege?")
                        .setCancelable(false)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                String reason = ((Spinner)findViewById(R.id.spinnerAccReason)).getSelectedItem().toString();
                                Toast.makeText(getApplicationContext(), "Deleted", Toast.LENGTH_LONG).show();
                                blockPrivileges(selectedFromList, reason);
                                ManageAccounts.ListEnrolledAcc accListdisp = new  ManageAccounts.ListEnrolledAcc();
                                accListdisp.execute();
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

        //spinner downloader
        DownloaderDocType dept = new DownloaderDocType(ManageAccounts.this, urlReasonSpinner, spinnerReasonPatient, "reason", "Choose Reason to Revoke");
        dept.execute();
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

    //insert to audit log table
    public void insertAudit(){
        Connection con = connectionClass.CONN();
        PreparedStatement ps = null;

        String statusActive = "Active";
        String statusInactive = "Inactive";
        String reason = ((Spinner)findViewById(R.id.spinnerAccReason)).getSelectedItem().toString();
        try {
            URL url = new URL("https://isproj2a.benilde.edu.ph/Sympl/InsertAuditAdminServlet");
            URLConnection connection = url.openConnection();

            connection.setReadTimeout(10000);
            connection.setConnectTimeout(15000);
            connection.setDoInput(true);
            connection.setDoOutput(true);

            Uri.Builder builder = new Uri.Builder()
                    .appendQueryParameter("first", "patient accounts")
                    .appendQueryParameter("second", "block user patient")
                    .appendQueryParameter("third", "blocking user account")
                    .appendQueryParameter("fourth", "Status = " + statusActive)
                    .appendQueryParameter("fifth", "Status = " + statusInactive + ", " + "Reason = " + reason)
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

    //blocking user
    public void blockPrivileges(String firstName, String reason){
        try {
            URL url = new URL("https://isproj2a.benilde.edu.ph/Sympl/BlockPrivilegesAdminServlet");
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
        }
         catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getAccString(String rowFromListView){
        String name = rowFromListView.substring(1, rowFromListView.length()-1);

        String accString1=name.replaceAll("third=", "");
        String accString2=accString1.replaceAll(",.+", "");
        Log.d("DEPTSTRING:", accString2);

        return accString2;
    }


    //function for displaying the enrolled accounts
    private class ListEnrolledAcc extends AsyncTask<String, String, String> {
        Connection con = connectionClass.CONN();
        boolean isSuccess = false;
        String message = "";

        @Override
        protected void onPreExecute() {
            Toast.makeText(getBaseContext(), "Please wait..", Toast.LENGTH_LONG).show();
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... strings) {
            try {
                //listview, list the names of all enrolled accounts
                accountsList = findViewById(R.id.listAccounts);
                URL url = new URL("https://isproj2a.benilde.edu.ph/Sympl/ListEnrolledAcctAdminServlet");
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
                List<Map<String, String>> data= new ArrayList<Map<String, String>>();

                data= (new Gson()).fromJson(retrieved, new TypeToken<List<Map<String, String>>>() {}.getType());

                listAdapterAccounts = new SimpleAdapter(ManageAccounts.this, data,
                        R.layout.listview_row, new String[]{"first", "second", "third"}, new int[]{R.id.FIRST_COL, R.id.SECOND_COL, R.id.THIRD_COL});


                message = "DELETED";
            } catch (Exception ex) {
                isSuccess = false;
                message = "Exceptions" + ex;
            }
            return message;
        }

        @Override
        protected void onPostExecute(String s) {
            accountsList.setAdapter(listAdapterAccounts);
        }
    }

    //function for displaying the enrolled department
    private class ListBlockedAccs extends AsyncTask<String, String, String> {
        Connection con = connectionClass.CONN();
        boolean isSuccess = false;
        String message = "";

        @Override
        protected void onPreExecute() {
            Toast.makeText(getBaseContext(), "Please wait..", Toast.LENGTH_LONG).show();
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... strings) {
            try {
                //listview, list the names of all enrolled department
                blockedList = findViewById(R.id.listBlocked);
                URL url = new URL("https://isproj2a.benilde.edu.ph/Sympl/ListBlockedAcctAdminServlet");
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
                List<Map<String, String>> data= new ArrayList<Map<String, String>>();

                data= (new Gson()).fromJson(retrieved, new TypeToken<List<Map<String, String>>>() {}.getType());


                /*int[] viewswhere = {R.id.lblDeptList};*/
                listAdapterBlockedAcc = new SimpleAdapter(ManageAccounts.this, data,
                        R.layout.listview_row, new String[]{"first", "second", "third"}, new int[]{R.id.FIRST_COL, R.id.SECOND_COL, R.id.THIRD_COL});


                message = "DELETED";
            } catch (Exception ex) {
                isSuccess = false;
                message = "Exceptions" + ex;
            }
            return message;
        }

        @Override
        protected void onPostExecute(String s) {
            blockedList.setAdapter(listAdapterBlockedAcc);
        }
    }

    private class DoEnrollReasonAcc extends AsyncTask<String, String, String> {

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
               /* Intent intent = new Intent(UnenrollDoc.this, UnenrollDoc.class);
                startActivity(intent);*/
                Toast.makeText(getBaseContext(), "Added Successfully", Toast.LENGTH_LONG).show();
            }

        }

    }
}
