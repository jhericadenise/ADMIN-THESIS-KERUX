package com.kerux.admin_thesis_kerux.navigation;

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
import com.kerux.admin_thesis_kerux.reports.ViewAuditReportsActivity;
import com.kerux.admin_thesis_kerux.reports.ViewStatReportsActivity;
import com.kerux.admin_thesis_kerux.security.Security;
import com.kerux.admin_thesis_kerux.session.KeruxSession;
import com.kerux.admin_thesis_kerux.spinner.Downloader;
import com.kerux.admin_thesis_kerux.unenrollment.UnenrollDoc;

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
    private static String urlReasonSpinner = "http://192.168.1.13:89/kerux/reasonSpinnerPatient.php";
    KeruxSession session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate ( savedInstanceState );
        setContentView ( R.layout.activity_manage_accounts );
        connectionClass = new ConnectionClass (); //create ConnectionClass
        drawerLayout = findViewById(R.id.drawer_layout);
        session=new KeruxSession(getApplicationContext());
        spinnerReasonPatient = findViewById(R.id.spinnerAccReason);

        accountsList = (ListView) findViewById(R.id.listAccounts);
        displayAccounts = (Button) findViewById(R.id.bttnViewAcc);
        blockedList = (ListView) findViewById(R.id.listBlocked);
        displayBlocked = (Button) findViewById(R.id.bttnViewBlocked);
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
                                        DoEnrollReasonAcc doEnrollReasonAcc = new DoEnrollReasonAcc();
                                        doEnrollReasonAcc.execute();
                                        //for refreshing the spinner
                                        Downloader dept = new Downloader(ManageAccounts.this, urlReasonSpinner, spinnerReasonPatient, "Reason", "Choose Reason to Revoke");
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
        Downloader dept = new Downloader(ManageAccounts.this, urlReasonSpinner, spinnerReasonPatient, "Reason", "Choose Reason to Revoke");
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
            String queryAUDIT = INSERT_AUDIT_LOG;
            PreparedStatement psAUDIT = con.prepareStatement(queryAUDIT);
            psAUDIT.setString(1, "patient accounts");
            psAUDIT.setString(2, "block user patient");
            psAUDIT.setString(3, BLOCK_PRIVILEGES);
            psAUDIT.setString(4,  "Status = " + statusActive);
            psAUDIT.setString(5, "Status = " + statusInactive + ", " + "Reason = " + reason);
            psAUDIT.setString(6, session.getusername());
            psAUDIT.executeUpdate();
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }

    //blocking user
    public void blockPrivileges(String firstName, String reason){

        Connection con = connectionClass.CONN();
        PreparedStatement ps = null;

        String query = BLOCK_ACC_REASON;
        PreparedStatement ps1 = null;

        try {
            ps = con.prepareStatement(BLOCK_PRIVILEGES);
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
                accountsList = (ListView) findViewById(R.id.listAccounts);
                String result = "Database Connection Successful\n";
                Statement st = con.createStatement();
                ResultSet rset = st.executeQuery(SELECT_ACCOUNTS_LIST);
                ResultSetMetaData rsmd = rset.getMetaData();

                List<Map<String, String>> data = null;
                data = new ArrayList<Map<String, String>>();

                while (rset.next()) {
                    HashMap<String, String> datanum = new HashMap<String, String>();
                    datanum.put("first", rset.getString(1).toString());
                    datanum.put("second", rset.getString(2).toString());
                    datanum.put("third", rset.getString(3).toString());
                    data.add(datanum);
                }

                listAdapterAccounts = new SimpleAdapter(ManageAccounts.this, data,
                        R.layout.listview_row, new String[]{"first", "second", "third"}, new int[]{R.id.FIRST_COL, R.id.SECOND_COL, R.id.THIRD_COL});

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
                blockedList = (ListView) findViewById(R.id.listBlocked);
                String result = "Database Connection Successful\n";
                Statement st = con.createStatement();
                ResultSet rset = st.executeQuery(SELECT_BLOCKED_USERS);
                ResultSetMetaData rsmd = rset.getMetaData();

                List<Map<String, String>> data = null;
                data = new ArrayList<Map<String, String>>();

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


                /*int[] viewswhere = {R.id.lblDeptList};*/
                listAdapterBlockedAcc = new SimpleAdapter(ManageAccounts.this, data,
                        R.layout.listview_row, new String[]{"first", "second", "third"}, new int[]{R.id.FIRST_COL, R.id.SECOND_COL, R.id.THIRD_COL});

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
                Intent intent = new Intent(ManageAccounts.this, ManageAccounts.class);
                startActivity(intent);
            }

        }

    }
}
