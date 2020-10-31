package com.kerux.admin_thesis_kerux.navigation;

import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.kerux.admin_thesis_kerux.R;
import com.kerux.admin_thesis_kerux.dbutility.ConnectionClass;
import com.kerux.admin_thesis_kerux.dbutility.DBUtility;
import com.kerux.admin_thesis_kerux.reports.ViewAuditReportsActivity;
import com.kerux.admin_thesis_kerux.reports.ViewRatingReportsActivity;
import com.kerux.admin_thesis_kerux.reports.ViewStatReportsActivity;
import com.kerux.admin_thesis_kerux.security.Security;
import com.kerux.admin_thesis_kerux.unenrollment.UnenrollDoc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
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

    DrawerLayout drawerLayout;
    private Spinner spinnerReason;

    private static String urlReasonSpinner = "http://192.168.1.13:89/kerux/reasonSpinner.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_accounts);
        connectionClass = new ConnectionClass(); //create ConnectionClass

        String i = getIntent().getStringExtra("username");

        drawerLayout = findViewById(R.id.drawer_layout);

        accountsList = (ListView) findViewById(R.id.listAccounts);
        displayAccounts = (Button) findViewById(R.id.bttnViewAcc);
        blockedList = (ListView) findViewById(R.id.listBlocked);
        displayBlocked = (Button) findViewById(R.id.bttnViewBlocked);

        displayAccounts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ManageAccounts.ListAccounts viewAcc = new ManageAccounts.ListAccounts();
                viewAcc.execute();
            }
        });
        displayBlocked.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ManageAccounts.ViewBlockedUsers viewBlocked = new ManageAccounts.ViewBlockedUsers();
                viewBlocked.execute();
            }
        });

        accountsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                final String selectedFromList = String.valueOf((accountsList.getItemAtPosition(position)));
                Toast.makeText(getApplicationContext(), "You selected: " + selectedFromList, Toast.LENGTH_LONG).show();
                //Dialog box, for unenrolling
                AlertDialog.Builder builder = new AlertDialog.Builder(ManageAccounts.this);
                builder.setMessage("Block user privileges?")
                        .setCancelable(false)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                String name = selectedFromList.substring(3, selectedFromList.length() - 1);

                                Toast.makeText(getApplicationContext(), "Deleted", Toast.LENGTH_LONG).show();
                                blockPrivileges(name);
                                ManageAccounts.ListAccounts accListDisp = new ManageAccounts.ListAccounts();
                                accListDisp.execute();
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

    }

    public void blockPrivileges(String name){

        Connection con = connectionClass.CONN();
        PreparedStatement ps = null;
        try {
            ps = con.prepareStatement(BLOCK_PRIVILEGES);
            ps.setString(1, name);
            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public String giveDate() {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("EEE, MMM d, yyyy");
        return sdf.format(cal.getTime());
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
        //Recreate activity
        recreate();
    }

    public void ClickEnrollment(View view){
        //Redirect activity to manage accounts
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

    //function for displaying the enrolled user - patient
    private class ListAccounts extends AsyncTask<String, String, String> {
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
                Security sec = new Security ();
                //listview, list the names of all enrolled department
                String result = "Database Connection Successful\n";
                Statement st = con.createStatement();
                ResultSet rset = st.executeQuery(SELECT_ACCOUNTS_LIST);
                ResultSetMetaData rsmd = rset.getMetaData();

                List<Map<String, String>> data = null;
                data = new ArrayList<Map<String, String>>();

                while (rset.next()) {
                    Map<String, String> datanum = new HashMap<String, String>();
                    datanum.put("A", sec.decrypt(rset.getString(1).toString()) + rset.getString(2).toString());
                    data.add(datanum);
                }

                String[] fromwhere = {"A"};
                int[] viewswhere = {R.id.lblAccountsList};
                listAdapterAccounts = new SimpleAdapter(ManageAccounts.this, data,
                        R.layout.list_accounts_template, fromwhere, viewswhere);

                while (rset.next()) {
                    result += rset.getString(1).toString() + "\n";
                }
                message = "ADDED SUCCESSFULLY!";
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

    private class ViewBlockedUsers extends AsyncTask<String, String, String> {
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
                String result = "Database Connection Successful\n";
                Statement st = con.createStatement();
                ResultSet rset = st.executeQuery(SELECT_BLOCKED_USERS);
                ResultSetMetaData rsmd = rset.getMetaData();

                List<Map<String, String>> data = null;
                data = new ArrayList<Map<String, String>>();

                while (rset.next()) {
                    Map<String, String> datanum = new HashMap<String, String>();
                    datanum.put("A", rset.getString(1).toString());
                    data.add(datanum);
                }

                String[] fromwhere = {"A"};
                int[] viewswhere = {R.id.lblBlockedUsers};
                listAdapterBlockedAcc = new SimpleAdapter(ManageAccounts.this, data,
                        R.layout.list_blocked_users_template, fromwhere, viewswhere);

                while (rset.next()) {
                    result += rset.getString(1).toString() + "\n";
                }
                message = "ADDED SUCCESSFULLY!";
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
}
