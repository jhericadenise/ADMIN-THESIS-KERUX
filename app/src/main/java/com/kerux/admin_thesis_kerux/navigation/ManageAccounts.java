package com.kerux.admin_thesis_kerux.navigation;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.kerux.admin_thesis_kerux.R;
import com.kerux.admin_thesis_kerux.dbutility.ConnectionClass;
import com.kerux.admin_thesis_kerux.dbutility.DBUtility;
import com.kerux.admin_thesis_kerux.enrollment.EnrollDept;
import com.kerux.admin_thesis_kerux.enrollment.EnrollDoctor;
import com.kerux.admin_thesis_kerux.enrollment.EnrollQM;
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

public class ManageAccounts extends AppCompatActivity implements DBUtility, NavigationView.OnNavigationItemSelectedListener {

    ConnectionClass connectionClass;
    private ListAdapter listAdapterAccounts;
    private ListAdapter listAdapterBlockedAcc;
    private ListView accountsList;
    private ListView blockedList;
    Button displayAccounts;
    Button displayBlocked;
    DrawerLayout drawerLayout;
    NavigationView navigationView;
    Toolbar toolbar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_accounts);
        connectionClass = new ConnectionClass(); //create ConnectionClass

        TextView titleDate = (TextView) findViewById(R.id.txtAccDate);
        titleDate.setText(giveDate());

        String i = getIntent().getStringExtra("username");
        TextView adminName = (TextView) findViewById(R.id.txtAccAdmin);
        adminName.setText(i);
        drawerLayout = findViewById(R.id.drawer_layout_accounts);
        navigationView = findViewById(R.id.nav_view);
        toolbar = findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);

        //Hide or show login or logout
        Menu menu = navigationView.getMenu();
        menu.findItem(R.id.nav_logout).setVisible(false);

        navigationView.bringToFront();
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(ManageAccounts.this);
        navigationView.setCheckedItem(R.id.nav_accounts);


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

    @Override
    public void onBackPressed() {

        if(drawerLayout.isDrawerOpen(GravityCompat.START)){
            drawerLayout.closeDrawer(GravityCompat.START);
        }
        else{
            super.onBackPressed();
        }

    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        switch (menuItem.getItemId()){
            case R.id.nav_dashboard:
                Intent intent = new Intent(ManageAccounts.this, MainActivity.class);
                startActivity(intent);
                break;
            case R.id.nav_enrollment:
                Intent intent1 = new Intent(ManageAccounts.this, EnrollmentPage.class);
                startActivity(intent1);
                break;
            case R.id.nav_enrollment_dept:
                Intent intent5 = new Intent(ManageAccounts.this, EnrollDept.class);
                startActivity(intent5);
                break;
            case R.id.nav_enrollment_doctor:
                Intent intent2 = new Intent(ManageAccounts.this, EnrollDoctor.class);
                startActivity(intent2);
                break;
            case R.id.nav_enrollment_qm:
                Intent intent3 = new Intent(ManageAccounts.this, EnrollQM.class);
                startActivity(intent3);
                break;
            case R.id.nav_revoke:
                Intent intent4 = new Intent(ManageAccounts.this, UnenrollDoc.class);
                startActivity(intent4);
                break;
            case R.id.nav_accounts:
                break;

        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
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
