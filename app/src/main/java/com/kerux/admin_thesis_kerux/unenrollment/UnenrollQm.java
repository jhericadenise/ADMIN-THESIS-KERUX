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

public class UnenrollQm extends AppCompatActivity implements DBUtility {

    private ListView qmList;
    private ListAdapter listAdapter;
    Button qmDisplayList;
    private Spinner spinnerQMReason;
    ConnectionClass connectionClass;

    DrawerLayout drawerLayout;

    Button addReasonQm;
    private EditText otherReason;
    private EditText table;
    final Context context = this;

    private static String urlReasonSpinner = "http://192.168.1.13:89/kerux/reasonSpinnerQM.php";
    KeruxSession session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate ( savedInstanceState );
        setContentView ( R.layout.activity_unenroll_qm);
        connectionClass = new ConnectionClass (); //create ConnectionClass
        session=new KeruxSession(getApplicationContext());

        drawerLayout = findViewById(R.id.drawer_layout);
        spinnerQMReason = (Spinner) findViewById(R.id.spinnerQMReason);

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
                                        DoEnrollReasonQM doEnrollReasonQM = new DoEnrollReasonQM();
                                        doEnrollReasonQM.execute();
                                        //for refreshing the spinner
                                        DownloaderDocType qm = new DownloaderDocType(UnenrollQm.this, urlReasonSpinner, spinnerQMReason, "Reason", "Choose Reason to Revoke");
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

        qmDisplayList = (Button) findViewById(R.id.bttnDisplayQm);
        qmList = (ListView) findViewById(R.id.listEnrolledQm);

        qmDisplayList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UnenrollQm.ListQM qmListdisp = new UnenrollQm.ListQM();
                qmListdisp.execute();
            }
        });

        qmList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                final String selectedFromList = getQMString(String.valueOf((qmList.getItemAtPosition(position))));
                Toast.makeText(getApplicationContext(), "You selected: " + selectedFromList, Toast.LENGTH_LONG).show();
                //Dialog box, for unenrolling
                AlertDialog.Builder builder = new AlertDialog.Builder(UnenrollQm.this);
                builder.setMessage("Are you sure you want to revoke the privilege of this queue manager" +
                        "?")
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

        DownloaderDocType qm = new DownloaderDocType(UnenrollQm.this, urlReasonSpinner, spinnerQMReason, "Reason", "Choose Reason to Revoke");
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
        Connection con = connectionClass.CONN();
        PreparedStatement ps = null;
        Security sec = new Security();

        String statusActive = "Active";
        String statusInactive = "Inactive";
        String reason = ((Spinner)findViewById(R.id.spinnerQMReason)).getSelectedItem().toString();

        try {
            String queryAUDIT = INSERT_AUDIT_LOG;
            PreparedStatement psAUDIT = con.prepareStatement(queryAUDIT);
            psAUDIT.setString(1, sec.encrypt("queue manager"));
            psAUDIT.setString(2, sec.encrypt("unenroll queue manager"));
            psAUDIT.setString(3, sec.encrypt("Unenrolling a queue manager record"));
            psAUDIT.setString(4, sec.encrypt("Status = " + statusActive));
            psAUDIT.setString(5, sec.encrypt("Status = " + statusInactive + ", " + "Reason = " + reason));
            psAUDIT.setString(6, session.getusername());
            psAUDIT.executeUpdate();
        }
        catch (SQLException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
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
        String qmStatus = "Active";

        if(con != null){ //means that we have a valid db connection
            try{//inserting records; called INSERT_REC from DBUtility.java
                // use of parameterized query such as PreparedStatement prevents SQL injection which is considered a way to
                //prevent threat in any web app
                String query = SELECT_UNENROLLED_QM;
                PreparedStatement ps = con.prepareStatement(query);
                ps.setString(1, qmStatus);

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
                qmList = (ListView) findViewById(R.id.listEnrolledQm);
                String result = "Database Connection Successful\n";
                Statement st = con.createStatement();
                ResultSet rset = st.executeQuery(SELECT_LIST_QM);
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

                listAdapter = new SimpleAdapter (UnenrollQm.this, data,
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
                Intent intent = new Intent(UnenrollQm.this, UnenrollQm.class);
                startActivity(intent);
            }

        }

    }
}
