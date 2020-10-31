package com.kerux.admin_thesis_kerux.unenrollment;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
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
import com.kerux.admin_thesis_kerux.navigation.EditProfile;
import com.kerux.admin_thesis_kerux.navigation.EnrollmentPage;
import com.kerux.admin_thesis_kerux.navigation.MainActivity;
import com.kerux.admin_thesis_kerux.navigation.ManageAccounts;
import com.kerux.admin_thesis_kerux.reports.ViewAuditReportsActivity;
import com.kerux.admin_thesis_kerux.reports.ViewRatingReportsActivity;
import com.kerux.admin_thesis_kerux.reports.ViewStatReportsActivity;
import com.kerux.admin_thesis_kerux.spinner.Downloader;

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

public class UnenrollQm extends AppCompatActivity implements DBUtility {

    private ListView qmList;
    private ListAdapter listAdapter;
    Button qmDisplayList;
    private Spinner spinnerQMReason;
    ConnectionClass connectionClass;

    DrawerLayout drawerLayout;

    private static String urlReasonSpinner = "http://192.168.1.13:89/kerux/reasonSpinnerQM.php";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate ( savedInstanceState );
        setContentView ( R.layout.activity_unenroll_qm);
        connectionClass = new ConnectionClass (); //create ConnectionClass

        drawerLayout = findViewById(R.id.drawer_layout);
        spinnerQMReason = (Spinner) findViewById(R.id.spinnerQMReason);

        qmDisplayList = (Button) findViewById(R.id.bttnDisplayQm);
        qmList = (ListView) findViewById(R.id.listEnrolledQm);

        qmDisplayList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UnenrollQm.ListQM qmListdisp = new UnenrollQm.ListQM ();
                qmListdisp.execute();
            }
        });

        qmList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                final String selectedFromList = getQMString(String.valueOf((qmList.getItemAtPosition(position))));
                Toast.makeText(getApplicationContext(),"You selected: "+selectedFromList,Toast.LENGTH_LONG).show();
                //Dialog box, for unenrolling
                AlertDialog.Builder builder = new AlertDialog.Builder(UnenrollQm.this);
                builder.setMessage("Unenroll Queue Manager?")
                        .setCancelable(false)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                String firstName = selectedFromList.substring(3, selectedFromList.length()-1);

                                String reason = ((Spinner)findViewById(R.id.spinnerQMReason)).getSelectedItem().toString();
                                Toast.makeText(getApplicationContext(),selectedFromList,Toast.LENGTH_LONG).show();
                                Toast.makeText(getApplicationContext(),"Deleted",Toast.LENGTH_LONG).show();
                                unenrollQM (selectedFromList, reason);
                                UnenrollQm.ListQM qmListdisp = new UnenrollQm.ListQM ();
                                qmListdisp.execute();
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
        Button bttnDept = findViewById(R.id.bttnUnenrollDept);
        bttnDept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(checkQMList()) {
                    Intent intent5 = new Intent(UnenrollQm.this, UnenrollDept.class);
                    startActivity(intent5);
                }else{
                    Toast.makeText(getApplicationContext(), "Cannot go to Unenrollment of Department, Must UNENROLL all Queue Managers to proceed.", Toast.LENGTH_LONG).show();

                }
            }
        });

        Downloader qm = new Downloader(UnenrollQm.this, urlReasonSpinner, spinnerQMReason, "Reason", "Choose Reason to Revoke");
        qm.execute();
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

    public void unenrollQM(String firstName, String reason){

        Connection con = connectionClass.CONN();
        PreparedStatement ps = null;

        String query = UNENROLL_QM_REASON;
        PreparedStatement ps1 = null;

        try {
            ps = con.prepareStatement(UNENROLL_QM);
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
    //deleting a record in the database

    public boolean checkQMList(){
        boolean allInactiveRec = false;
        Connection con = connectionClass.CONN();
        String docStatus = "Active";

        if(con != null){ //means that we have a valid db connection
            try{//inserting records; called INSERT_REC from DBUtility.java
                // use of parameterized query such as PreparedStatement prevents SQL injection which is considered a way to
                //prevent threat in any web app
                String query = SELECT_UNENROLLED_QM;
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


    public String getQMString(String rowFromListView){
        String name = rowFromListView.substring(1, rowFromListView.length()-1);

        String qmString1=name.replaceAll("third=", "");
        String qmString2=qmString1.replaceAll(",.+", "");
        Log.d("QMSTRING:", qmString2);

        return qmString2;
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
                String result = "Database Connection Successful\n";
                Statement st = con.createStatement();
                ResultSet rset = st.executeQuery(SELECT_LIST_QM);
                ResultSetMetaData rsmd = rset.getMetaData();

                List<Map<String, String>> data = null;
                data = new ArrayList<Map<String, String>>();

                while (rset.next()) {
                    Map<String, String> datanum = new HashMap<String, String>();
                    datanum.put("first", rset.getString(1).toString());
                    datanum.put("second", rset.getString(2).toString());
                    datanum.put("third", rset.getString(3).toString());
                    datanum.put("fourth", rset.getString(4).toString());
                    data.add(datanum);
                }

                listAdapter = new SimpleAdapter (UnenrollQm.this, data,
                        R.layout.listview_row, new String[] {"first", "second", "third", "fourth"},
                        new int[] {R.id.FIRST_COL, R.id.SECOND_COL, R.id.THIRD_COL, R.id.FOURTH_COL});
                while (rset.next()) {
                    result += rset.getString(2).toString();
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
            qmList.setAdapter(listAdapter);
        }
    }
}
