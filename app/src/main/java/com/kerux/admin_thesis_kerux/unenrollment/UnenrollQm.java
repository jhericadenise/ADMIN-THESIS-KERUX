package com.kerux.admin_thesis_kerux.unenrollment;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
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
import com.kerux.admin_thesis_kerux.edit.EditDoctor;
import com.kerux.admin_thesis_kerux.edit.EditQm;
import com.kerux.admin_thesis_kerux.email.SendMailTask;
import com.kerux.admin_thesis_kerux.navigation.EditProfile;
import com.kerux.admin_thesis_kerux.navigation.EnrollmentPage;
import com.kerux.admin_thesis_kerux.navigation.MainActivity;
import com.kerux.admin_thesis_kerux.navigation.ManageAccounts;
import com.kerux.admin_thesis_kerux.reports.ViewAuditReportsActivity;
import com.kerux.admin_thesis_kerux.reports.ViewRatingReportsActivity;
import com.kerux.admin_thesis_kerux.reports.ViewStatReportsActivity;
import com.kerux.admin_thesis_kerux.security.Security;
import com.kerux.admin_thesis_kerux.security.SecurityWEB;
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

public class UnenrollQm extends AppCompatActivity implements DBUtility {

    private ListView qmList;
    private ListAdapter listAdapter;
    Button qmDisplayList;
    Button goBack;
    private Spinner spinnerQMReason;
    ConnectionClass connectionClass;

    DrawerLayout drawerLayout;

    Button addReasonQm;
    private EditText otherReason;
    private EditText table;
    final Context context = this;

    private static final String urlReasonSpinner = "https://isproj2a.benilde.edu.ph/Sympl/reasonSpinnerQMServlet";
    KeruxSession session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate ( savedInstanceState );
        setContentView ( R.layout.activity_unenroll_qm);
        connectionClass = new ConnectionClass (); //create ConnectionClass
        session=new KeruxSession(getApplicationContext());

        drawerLayout = findViewById(R.id.drawer_layout);
        spinnerQMReason = findViewById(R.id.spinnerQMReason);

        //bttn for adding other reason for unenrolling
        addReasonQm = findViewById(R.id.bttnAddQMReason);
        addReasonQm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // get prompts.xml view
                LayoutInflater li = LayoutInflater.from(context);
                View promptsView = li.inflate(R.layout.activity_add_reason_qm, null);

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
                                        DoEnrollReasonQM doEnrollReasonQM = new DoEnrollReasonQM();
                                        doEnrollReasonQM.execute();
                                        //for refreshing the spinner
                                        DownloaderDocType qm = new DownloaderDocType(UnenrollQm.this, urlReasonSpinner, spinnerQMReason, "reason", "Choose Reason to Revoke");
                                        qm.execute();

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

        //bttn for displaying enrolled queue manager
        qmDisplayList = findViewById(R.id.bttnDisplayQm);
        qmDisplayList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UnenrollQm.ListQM qmListdisp = new UnenrollQm.ListQM();
                qmListdisp.execute();
            }
        });

        //click on list view
        qmList = findViewById(R.id.listEnrolledQm);
        qmList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d("QMM",String.valueOf((qmList.getItemAtPosition(position))));
                final String selectedFromList = getQMString(String.valueOf((qmList.getItemAtPosition(position))));
                Toast.makeText(getApplicationContext(), "You selected: " + selectedFromList, Toast.LENGTH_LONG).show();
                //Dialog box, for unenrolling
                AlertDialog.Builder builder = new AlertDialog.Builder(UnenrollQm.this);
                builder.setMessage("Are you sure you want to revoke the privilege of this queue manager?")
                        .setCancelable(false)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                String reason = ((Spinner) findViewById(R.id.spinnerQMReason)).getSelectedItem().toString();
                                Toast.makeText(getApplicationContext(), selectedFromList, Toast.LENGTH_LONG).show();
                                Toast.makeText(getApplicationContext(), "Successfully revoked privilege", Toast.LENGTH_LONG).show();
                                unenrollQM(selectedFromList, reason);
                                UnenrollQm.ListQM qmListdisp = new UnenrollQm.ListQM();
                                qmListdisp.execute();
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

        //button for going to unenroll department activity
        Button bttnDept = findViewById(R.id.bttnUnenrollDept);
        bttnDept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(checkQMList()) {
                    Intent intent5 = new Intent(UnenrollQm.this, UnenrollDept.class);
                    startActivity(intent5);
                }else{
                    Toast.makeText(getApplicationContext(), "Cannot go to Unenrollment of Department, Must unenroll ALL Queue Managers to proceed.", Toast.LENGTH_LONG).show();
                }
            }
        });

        goBack = findViewById(R.id.bttnGoBack);
        goBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goBack();
            }
        });

        DownloaderDocType qm = new DownloaderDocType(UnenrollQm.this, urlReasonSpinner, spinnerQMReason, "reason", "Choose Reason to Revoke");
        qm.execute();
    }

    //navigation drawer
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

    private void sendEmail() {
        Resources res = getResources();
        String email = "";
        String subject = res.getString(R.string.unenrollSubject);
        String message = res.getString(R.string.unenrollEmail);

        SendMailTask sm = new SendMailTask(this, email, subject, message);
        sm.execute();
    }


    //method for inserting into audit table
    public void insertAudit(){
        SecurityWEB secw = new SecurityWEB();
        Security sec = new Security();

        String statusActive = "Active";
        String statusInactive = "Inactive";
        String reason = ((Spinner)findViewById(R.id.spinnerQMReason)).getSelectedItem().toString();

        try {
            URL url = new URL("https://isproj2a.benilde.edu.ph/Sympl/InsertAuditAdminServlet");
            URLConnection connection = url.openConnection();

            connection.setReadTimeout(10000);
            connection.setConnectTimeout(15000);
            connection.setDoInput(true);
            connection.setDoOutput(true);

            Uri.Builder builder = new Uri.Builder()
                    .appendQueryParameter("first", secw.encrypt("Unenroll Queue Manager").trim())
                    .appendQueryParameter("second", secw.encrypt("delete").trim())
                    .appendQueryParameter("third", secw.encrypt("Unenrolling a queue manager record").trim())
                    .appendQueryParameter("fourth", secw.encrypt("Status = " + statusActive).trim())
                    .appendQueryParameter("fifth", secw.encrypt("Status = " + statusInactive + ", " + "Reason = " + reason).trim())
                    .appendQueryParameter("sixth", secw.encrypt(session.getusername()).trim());
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
    public void unenrollQM(String firstName, String reason){

        try {
            URL url = new URL("https://isproj2a.benilde.edu.ph/Sympl/UnenrollQMServlet");
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

    //method for checking if there is still an active record of queue manager
    public boolean checkQMList(){
        boolean allInactiveRec = false;
        String qmStatus="Active";
        try {
            URL url = new URL("https://isproj2a.benilde.edu.ph/Sympl/CheckQMListServlet");
            URLConnection connection = url.openConnection();

            connection.setReadTimeout(10000);
            connection.setConnectTimeout(15000);
            connection.setDoInput(true);
            connection.setDoOutput(true);

            Uri.Builder builder = new Uri.Builder()
                    .appendQueryParameter("qmStatus", qmStatus)
                    .appendQueryParameter("clinicid", session.getclinicid());
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
        Intent intent = new Intent(this, UnenrollDoc.class);
        startActivity(intent);
    }

    public String getQMString(String rowFromListView){
        String name = rowFromListView.substring(1, rowFromListView.length()-1);

        String qmString1=name.replaceAll(".*second=", "");


        return qmString1;
    }

    //Displaying the list of enrolled queue manager in the database
    private class ListQM extends AsyncTask<String, String, String> {
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
                qmList = findViewById(R.id.listEnrolledQm);
                URL url = new URL("https://isproj2a.benilde.edu.ph/Sympl/ListQMServlet");
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

                listAdapter = new SimpleAdapter (UnenrollQm.this, data,
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
            qmList.setAdapter(listAdapter);
        }
    }

    private class DoEnrollReasonQM extends AsyncTask<String, String, String> {

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
                    URL url = new URL("https://isproj2a.benilde.edu.ph/Sympl/DoEnrollReasonQM");
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
                /*Intent intent = new Intent(UnenrollQm.this, UnenrollQm.class);
                startActivity(intent);*/
                Toast.makeText(getBaseContext(), "Unenrolled Successfully", Toast.LENGTH_LONG).show();
            }

        }

    }
}
