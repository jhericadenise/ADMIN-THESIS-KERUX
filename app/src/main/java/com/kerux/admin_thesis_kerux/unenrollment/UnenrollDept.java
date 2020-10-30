package com.kerux.admin_thesis_kerux.unenrollment;

import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
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
import com.kerux.admin_thesis_kerux.navigation.EnrollmentPage;
import com.kerux.admin_thesis_kerux.navigation.MainActivity;
import com.kerux.admin_thesis_kerux.navigation.ManageAccounts;
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


public class UnenrollDept extends AppCompatActivity implements DBUtility {
    ConnectionClass connectionClass;
    private ListView deptList;
    private ListAdapter listAdapter;
    Button deptDisplayList;
    private Spinner spinnerDeptReason;

    DrawerLayout drawerLayout;
    private static String urlReasonSpinner = "http://192.168.1.13:89/kerux/reasonSpinner.php";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate ( savedInstanceState );
        setContentView ( R.layout.activity_unenroll_dept );
        connectionClass = new ConnectionClass (); //create ConnectionClass

        drawerLayout = findViewById(R.id.drawer_layout);

        deptDisplayList = (Button) findViewById(R.id.bttnDisplayDept);
        deptList = (ListView) findViewById(R.id.listEnrolledDept);

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
                                String reason = ((Spinner)findViewById(R.id.spinnerQMReason)).getSelectedItem().toString();
                                Toast.makeText(getApplicationContext(),selectedFromList,Toast.LENGTH_LONG).show();
                                unenrollDept(selectedFromList, reason);
                                ListDept deptListdisp = new ListDept();
                                deptListdisp.execute();
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

        Downloader dept = new Downloader(UnenrollDept.this, urlReasonSpinner, spinnerDeptReason, "Reason");
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

    public void ClickLogout(View view){
        MainActivity.logout(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        //close drawer
        MainActivity.closeDrawer(drawerLayout);
    }

    //deleting a record in the database
    public void unenrollDept(String name, String reason){

        Connection con = connectionClass.CONN();
        PreparedStatement ps = null;

        String query = UNENROLL_DEPT_REASON;
        PreparedStatement ps1 = null;

        try {
            ps = con.prepareStatement(UNENROLL_DEPT);
            ps.setString(1, name);

            ps1 = con.prepareStatement(query);
            ps1.setString(1, reason);
            ps1.setString(2, name);

            ps.executeUpdate();
            ps1.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public String getDeptString(String rowFromListView){
        String name = rowFromListView.substring(1, rowFromListView.length()-1);

        String deptString1=name.replaceAll("third=", "");
        String deptString2=deptString1.replaceAll(",.+", "");
        Log.d("DEPTSTRING:", deptString2);

        return deptString2;
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
                deptList = (ListView) findViewById(R.id.listEnrolledDept);
                String result = "Database Connection Successful\n";
                Statement st = con.createStatement();
                ResultSet rset = st.executeQuery(SELECT_LIST_DEPT);
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


                /*int[] viewswhere = {R.id.lblDeptList};*/
                listAdapter = new SimpleAdapter (UnenrollDept.this, data,
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
            deptList.setAdapter(listAdapter);
        }
    }

}
