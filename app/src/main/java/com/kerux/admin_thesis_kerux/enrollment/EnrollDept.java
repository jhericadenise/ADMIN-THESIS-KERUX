package com.kerux.admin_thesis_kerux.enrollment;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.kerux.admin_thesis_kerux.navigation.EnrollmentPage;
import com.kerux.admin_thesis_kerux.session.KeruxSession;
import com.kerux.admin_thesis_kerux.navigation.MainActivity;
import com.kerux.admin_thesis_kerux.navigation.ManageAccounts;
import com.kerux.admin_thesis_kerux.R;
import com.kerux.admin_thesis_kerux.security.Security;
import com.kerux.admin_thesis_kerux.dbutility.ConnectionClass;
import com.kerux.admin_thesis_kerux.dbutility.DBUtility;

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


public class EnrollDept extends AppCompatActivity implements DBUtility {

    private EditText deptName;
    private ListView deptList;
    private ListAdapter listAdapter;
    Button deptDisplayList;

    ConnectionClass connectionClass;

    private KeruxSession session;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enroll_dept);

        session=new KeruxSession(getApplicationContext());

        TextView titleDate = (TextView) findViewById(R.id.txtDateDep);
        titleDate.setText(giveDate());
        connectionClass=new ConnectionClass(); //create ConnectionClass
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.nav_view);
        navigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.navigation_dashboard:
                        Intent a = new Intent(EnrollDept.this, MainActivity.class);
                        startActivity(a);
                        break;
                    case R.id.navigation_enrollment:
                        Intent b = new Intent(EnrollDept.this, EnrollmentPage.class);
                        startActivity(b);
                        break;
                    case R.id.navigation_accounts:
                        Intent c = new Intent(EnrollDept.this, ManageAccounts.class);
                        startActivity(c);
                        break;
                }
                return false;
            }
        });

        Button bttnBack = findViewById(R.id.bttnBackDept);
        Button bttnEnrollDept = findViewById(R.id.bttnEnrollDept);
        deptDisplayList = (Button) findViewById(R.id.bttnDisplayDept);
        deptName = (EditText)findViewById(R.id.txtboxDeptName);
        deptList = (ListView) findViewById(R.id.listEnrolledDept);

        //going back to the previous page
        bttnBack.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                goBack();
            }
        });

        //what the button of enroll dept will do when its clicked
        bttnEnrollDept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DoEnroll doEnroll=new DoEnroll();
                doEnroll.execute();
                deptName.getText().clear();
            }
        });

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

                final String selectedFromList = String.valueOf((deptList.getItemAtPosition(position)));
                Toast.makeText(getApplicationContext(),"You selected: "+selectedFromList,Toast.LENGTH_LONG).show();
                //Dialog box, for unenrolling
                AlertDialog.Builder builder = new AlertDialog.Builder(EnrollDept.this);
                builder.setMessage("Unenroll Department?")
                        .setCancelable(false)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                String name = selectedFromList.substring(3, selectedFromList.length()-1);

                                Toast.makeText(getApplicationContext(),"Deleted",Toast.LENGTH_LONG).show();
                                unenrollDept(name);
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
    }

    //deleting a record in the database
    public void unenrollDept(String name){

        Connection con = connectionClass.CONN();
        PreparedStatement ps = null;
        try {
            ps = con.prepareStatement(UNENROLL_DEPT);
            ps.setString(1, name);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    //Check if it has the same record
/*    public boolean checkDeptRecord() {
        boolean hasExistingAdmin = false;

    }*/

    /*when goBack button is clicked, it will be redirected to the page stated in the function*/
    public void goBack() {
        Intent intent = new Intent(this, EnrollmentPage.class);
        startActivity(intent);
    }

    //Getting the current date
    public String giveDate() {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("EEE, MMM d, yyyy");
        return sdf.format(cal.getTime());
    }

    //Getting time stamp
    public String timeStamp() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss");
        return sdf.format(calendar.getTime());
    }

    /*Function class for enrolling the department in to the db, inserting the records in the db*/
    private class DoEnroll extends AsyncTask<String, String, String> {

        Security sec = new Security();
        boolean isSuccess = false;
        String message = "";
        String depName = deptName.getText().toString();
        String Status = "Active";
        String timeStamp = timeStamp();
        boolean hasRecord = false;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }
        @Override
        protected String doInBackground(String... params) {
            Connection con = connectionClass.CONN();
            PreparedStatement ps = null;
            try {
                ps = con.prepareStatement(VALIDATION_DEPT);
                ps.setString(1, depName);

                ResultSet rs=ps.executeQuery();

                if(rs.next()){
                    hasRecord = true;
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

            message = "Record already exists";


            if (depName.trim().equals("")) {
                message = "Please enter all fields....";
            }
            else if (!depName.matches("^[A-Za-z]+$")) {
                message = "Check format";
            }
            else if (hasRecord){
                message = "Record already exists";
            }
            else
            {
                try {
                    if (con == null) {
                        message = "CANNOT ADD RECORD";

                    } else {
                        //inserting data of department to the database
                        String query = INSERT_DEPT;
                        PreparedStatement ps1 = con.prepareStatement(query);
                        ps1.setString(1, depName);
                        ps1.setString(2, Status);

                        ps1.executeUpdate();

                        String queryJoin = "insert into department_enrollment (Admin_ID, Department_ID) "+
                                "SELECT '"+session.getusername()+"', Department_ID from Department order by Department_ID DESC LIMIT 1;";


                        Statement stmt2 = con.createStatement();
                        stmt2.executeUpdate(queryJoin);
                        con.close();
                        message = "ADDED SUCCESSFULLY!";
                    }
                }
                catch (Exception ex)
                {
                    isSuccess = false;
                    message = "Exceptions"+ex;
                }
            }
            return message;

        }
        @Override
        protected void onPostExecute(String s) {
            Toast.makeText(getBaseContext(),""+message,Toast.LENGTH_LONG).show();

            if(isSuccess) {
                Intent intent=new Intent(EnrollDept.this,EnrollDept.class);
                startActivity(intent);
            }

        }

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
                String result = "Database Connection Successful\n";
                Statement st = con.createStatement();
                ResultSet rset = st.executeQuery(SELECT_LIST_DEPT);
                ResultSetMetaData rsmd = rset.getMetaData();

                List<Map<String, String>> data = null;
                data = new ArrayList<Map<String, String>>();

                while (rset.next()) {
                    Map<String, String> datanum = new HashMap<String, String>();
                    datanum.put("A", rset.getString(1).toString());
                    data.add(datanum);
                }

                String[] fromwhere = {"A"};
                int[] viewswhere = {R.id.lblDeptList};
                listAdapter = new SimpleAdapter(EnrollDept.this, data,
                        R.layout.list_dept_template, fromwhere, viewswhere);

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
            deptList.setAdapter(listAdapter);
        }
    }
}

