package com.kerux.admin_thesis_kerux.unenrollment;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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
    private static String urlReasonSpinner = "http://192.168.1.13:89/kerux/reasonSpinnerDoctor.php";

    KeruxSession session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate ( savedInstanceState );
        setContentView ( R.layout.activity_unenroll_doctor );
        connectionClass = new ConnectionClass (); //create ConnectionClass
        session=new KeruxSession(getApplicationContext());

        drawerLayout = findViewById(R.id.drawer_layout);

        docDisplayList = (Button) findViewById(R.id.bttnDisplayDoc);
        docList = (ListView) findViewById(R.id.listEnrolledDoc);

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

                otherReason = (EditText)promptsView.findViewById(R.id.txtboxReason);
                table = (EditText)promptsView.findViewById(R.id.txtboxTableName);

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
                                        DownloaderDocType doc = new DownloaderDocType(UnenrollDoc.this, urlReasonSpinner, spinnerReasonDoc, "Reason", "Choose Reason to Revoke");
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
        DownloaderDocType doc = new DownloaderDocType(UnenrollDoc.this, urlReasonSpinner, spinnerReasonDoc, "Reason", "Choose Reason to Revoke");
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
        Connection con = connectionClass.CONN();
        PreparedStatement ps = null;
        Security sec = new Security();

        String statusActive = "Active";
        String statusInactive = "Inactive";
        String reason = ((Spinner)findViewById(R.id.spinnerDocReason)).getSelectedItem().toString();

        try {
            String queryAUDIT = INSERT_AUDIT_LOG;
            PreparedStatement psAUDIT = con.prepareStatement(queryAUDIT);
            psAUDIT.setString(1, sec.encrypt("doctor"));
            psAUDIT.setString(2, sec.encrypt("unenroll doctor"));
            psAUDIT.setString(3, sec.encrypt("Unenrolling a doctor record"));
            psAUDIT.setString(4, sec.encrypt("Status = " + statusActive));
            psAUDIT.setString(5, sec.encrypt("Status = " + statusInactive + ", " + "Reason = " + reason));
            psAUDIT.setString(6, sec.encrypt(session.getusername()));
            psAUDIT.executeUpdate();
        }
        catch (SQLException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //unenrolling doctor records
    public void unenrollDoc(String firstName, String reason){

        Connection con = connectionClass.CONN();
        PreparedStatement ps = null;

        String query = UNENROLL_DOC_REASON;
        PreparedStatement ps1 = null;

        try {
            ps = con.prepareStatement(UNENROLL_DOCTOR);
            ps.setString(1, firstName);

            ps1 = con.prepareStatement(query);
            ps1.setString(1, reason);
            ps1.setString(2, firstName);

            ps.executeUpdate();
            ps1.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    //checking if doctor tables still have Active records
    public boolean checkDoctorList(){
        boolean allInactiveRec = false;
        Connection con = connectionClass.CONN();
        String docStatus = "Active";

        if(con != null){ //means that we have a valid db connection
            try{//inserting records; called INSERT_REC from DBUtility.java
                // use of parameterized query such as PreparedStatement prevents SQL injection which is considered a way to
                //prevent threat in any web app
                String query = SELECT_UNENROLLED_DOC;
                PreparedStatement ps = con.prepareStatement(query);
                ps.setString(1, docStatus);

                ResultSet rs=ps.executeQuery();
                if(rs.next()){
                    Log.d("WENT HERE", "DIDNT GO IN");
                }
                else{
                    Log.d("WENT HERE", "WENT IN");
                    allInactiveRec=true;

                }
            } catch(SQLException sqle){
                System.err.println(sqle.getMessage());
            } catch (Exception e) {
                e.printStackTrace();
            }
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
                docList = (ListView) findViewById(R.id.listEnrolledDoc);
                String result = "Database Connection Successful\n";
                Statement st = con.createStatement();
                ResultSet rset = st.executeQuery(SELECT_LIST_DOC);
                ResultSetMetaData rsmd = rset.getMetaData();

                List<Map<String, String>> data = null;
                data = new ArrayList<Map<String, String>> ();

                while (rset.next()) {
                    HashMap<String, String> datanum = new HashMap<String, String>();
                    datanum.put("first", rset.getString(1).toString());
                    datanum.put("second", rset.getString(2).toString());
                    datanum.put("third", rset.getString(3).toString());

                    /*datanum.put("A", "CLINIC NAME" + "\n"+rset.getString(1).toString() + "\n \n" + "DEPARTMENT NAME" +
                            "\n" + rset.getString(2).toString() +"\n \n"
                    + "STATUS" +"\n" + rset.getString(3).toString());*/

                    data.add(datanum);
                }

                listAdapter = new SimpleAdapter (UnenrollDoc.this, data,
                        R.layout.listview_row, new String[] {"first", "second", "third"}, new int[] {R.id.FIRST_COL, R.id.SECOND_COL, R.id.THIRD_COL});

                while (rset.next()) {
                    result += rset.getString(2) + "\n";
                }
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
                    if (con == null) {
                        message = "CANNOT ADD RECORD";

                    } else {
                        //inserting data of department to the database
                        String query = INSERT_REASON;
                        PreparedStatement ps1 = null;
                        try {
                            ps1 = con.prepareStatement(query);
                            ps1.setString(1, reason);
                            ps1.setString(2, tableName);
                            ps1.executeUpdate();
                            message = "Added Successfully!";
                        } catch (SQLException throwables) {
                            throwables.printStackTrace();
                        }
                        con.close();
                    }
                }
                catch (Exception ex)
                {
                    isSuccess = false;
                    message = "Exceptions"+ex;
                    Log.d("ex", ex.getMessage () + " Jheca");
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
