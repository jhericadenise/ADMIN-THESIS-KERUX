package com.kerux.admin_thesis_kerux.reports;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

import com.kerux.admin_thesis_kerux.R;
import com.kerux.admin_thesis_kerux.dbutility.ConnectionClass;
import com.kerux.admin_thesis_kerux.dbutility.DBUtility;
import com.kerux.admin_thesis_kerux.navigation.EditProfile;
import com.kerux.admin_thesis_kerux.navigation.EnrollmentPage;
import com.kerux.admin_thesis_kerux.navigation.MainActivity;
import com.kerux.admin_thesis_kerux.navigation.ManageAccounts;
import com.kerux.admin_thesis_kerux.security.Security;
import com.kerux.admin_thesis_kerux.session.KeruxSession;
import com.kerux.admin_thesis_kerux.unenrollment.UnenrollDoc;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ViewAuditReportsActivity extends AppCompatActivity implements DBUtility {

    ConnectionClass connectionClass;
    DrawerLayout drawerLayout;
    private ListView listAudit;
    private Button bttnViewAuditReports;
    private Button bttnGenerateAuditReports;
    private ListAdapter listAdapter;

    KeruxSession keruxSes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_audit_reports);
        connectionClass = new ConnectionClass (); //create ConnectionClass

        drawerLayout = findViewById(R.id.drawer_layout);
        bttnViewAuditReports = findViewById(R.id.bttnViewAudit);
        bttnGenerateAuditReports = findViewById(R.id.bttnGenerateAudit);
        listAudit = findViewById(R.id.listAuditReports);

        bttnViewAuditReports.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ListAudit viewAudit = new ListAudit();
                viewAudit.execute();
            }
        });

        bttnGenerateAuditReports.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ViewAuditReportsActivity.this, GenerateReport.class);
                /*intent.putExtra("tableName", keruxSes.getTableName());
                intent.putExtra("Log_ID", keruxSes.getAuditID());*/
                startActivity(intent);
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
        recreate();
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

    private class ListAudit extends AsyncTask<String, Void, String> {

        Connection con = connectionClass.CONN();
        boolean isSuccess = false;
        String message = "";

        Security sec = new Security();

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Toast.makeText(ViewAuditReportsActivity.this, "Please wait...", Toast.LENGTH_SHORT).show();

        }

        @Override
        protected String doInBackground(String... params) {
            try {
                listAudit = findViewById(R.id.listAuditReports);
                String result = "Database Connection Successful\n";
                Statement st = con.createStatement();
                ResultSet rset = st.executeQuery(SELECT_AUDIT);
                ResultSetMetaData rsmd = rset.getMetaData();

                List<Map<String, String>> data = null;
                data = new ArrayList<Map<String, String>> ();

                while (rset.next()) {
                    HashMap<String, String> datanum = new HashMap<String, String>();
                    datanum.put("first", rset.getString(1).toString());
                    datanum.put("second", rset.getString(2).toString());
                    datanum.put("third", rset.getString(3).toString());
                  /*  Log.d( "AUDIT", rset.getString(2));
                    Log.d( "AUDIT", rset.getString(3));*/
                    datanum.put("fourth", rset.getString(4).toString());
                    datanum.put("fifth", rset.getString(5).toString());
                    datanum.put("sixth", rset.getString(6).toString());
                    datanum.put("seventh", rset.getString(7).toString());
                    datanum.put("eight", rset.getString(8).toString());
                    data.add(datanum);
                }


                /*int[] viewswhere = {R.id.lblDeptList};*/
                listAdapter = new SimpleAdapter (ViewAuditReportsActivity.this, data,
                        R.layout.listview_row_audit, new String[] {"first", "second", "third", "fourth", "fifth", "sixth", "seventh", "eight"},
                        new int[] {R.id.FIRST_COL, R.id.SECOND_COL, R.id.THIRD_COL, R.id.FOURTH_COL, R.id.FIFTH_COL, R.id.SIXTH_COL,
                                    R.id.SEVENTH_COL, R.id.EIGHT_COL});


                while (rset.next()) {
                    result += rset.getString(1).toString() + "\n";
                }
                message = result;
            } catch (Exception e) {
                e.printStackTrace();
                message = e.toString();
            }
            return message;
        }

        @Override
        protected void onPostExecute(String result) {
            listAudit.setAdapter(listAdapter);
        }
    }
}