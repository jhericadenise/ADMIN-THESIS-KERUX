package com.kerux.admin_thesis_kerux.unenrollment;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.kerux.admin_thesis_kerux.R;
import com.kerux.admin_thesis_kerux.dbutility.ConnectionClass;
import com.kerux.admin_thesis_kerux.dbutility.DBUtility;
import com.kerux.admin_thesis_kerux.enrollment.EnrollDoctor;
import com.kerux.admin_thesis_kerux.navigation.EnrollmentPage;
import com.kerux.admin_thesis_kerux.navigation.MainActivity;
import com.kerux.admin_thesis_kerux.navigation.ManageAccounts;
import com.kerux.admin_thesis_kerux.security.Security;
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

public class UnenrollDoc  extends AppCompatActivity implements DBUtility {

    ConnectionClass connectionClass;
    private ListView docList;
    private ListAdapter listAdapter;
    Button docDisplayList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate ( savedInstanceState );
        setContentView ( R.layout.activity_unenroll_doctor );
        connectionClass = new ConnectionClass (); //create ConnectionClass

        BottomNavigationView navigation = (BottomNavigationView) findViewById ( R.id.nav_view );
        navigation.setOnNavigationItemSelectedListener ( new BottomNavigationView.OnNavigationItemSelectedListener () {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId ()) {
                    case R.id.navigation_dashboard:
                        Intent a = new Intent ( UnenrollDoc.this, MainActivity.class );
                        startActivity ( a );
                        break;
                    case R.id.navigation_enrollment:
                        Intent b = new Intent ( UnenrollDoc.this, EnrollmentPage.class );
                        startActivity ( b );
                        break;
                    case R.id.navigation_accounts:
                        Intent c = new Intent ( UnenrollDoc.this, ManageAccounts.class );
                        startActivity ( c );
                        break;
                }
                return false;
            }
        } );
        docDisplayList = (Button) findViewById(R.id.bttnDisplayDoc);
        docList = (ListView) findViewById(R.id.listEnrolledDoc);

        docDisplayList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UnenrollDoc.ListDoc docListdisp = new UnenrollDoc.ListDoc();
                docListdisp.execute();
            }
        });

        docList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                final String selectedFromList = String.valueOf((docList.getItemAtPosition(position)));
                Toast.makeText(getApplicationContext(),"You selected: "+selectedFromList,Toast.LENGTH_LONG).show();
                //Dialog box, for unenrolling
                AlertDialog.Builder builder = new AlertDialog.Builder(UnenrollDoc.this);
                builder.setMessage("Unenroll Doctor?")
                        .setCancelable(false)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                String firstName = selectedFromList.substring(3, selectedFromList.length()-1);
                                /*String lastName = selectedFromList.substring(3, selectedFromList.length()-2);*/

                                Toast.makeText(getApplicationContext(),"Deleted",Toast.LENGTH_LONG).show();
                                unenrollDoc(firstName);
                                UnenrollDoc.ListDoc docListdisp = new UnenrollDoc.ListDoc();
                                docListdisp.execute();
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
            Button bttnQM = findViewById(R.id.bttnUnenrollQM);
            bttnQM.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    checkDoctorList();
                    Intent intent5 = new Intent(UnenrollDoc.this, UnenrollQm.class);
                    startActivity(intent5);
                }
            });

    }
    //deleting a record in the database
    public void unenrollDoc(String firstName){

        Connection con = connectionClass.CONN();
        PreparedStatement ps = null;
        try {
            ps = con.prepareStatement(UNENROLL_DOCTOR);
            ps.setString(1, firstName);
            /*ps.setString(2, lastName);*/
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean checkDoctorList(){
        boolean allInactiveRec = false;
        Connection con = connectionClass.CONN();
        String docStatus = "Inactive";

        if(con != null){ //means that we have a valid db connection
            try{//inserting records; called INSERT_REC from DBUtility.java
                // use of parameterized query such as PreparedStatement prevents SQL injection which is considered a way to
                //prevent threat in any web app
                String query = SELECT_UNENROLLED_DOC;
                PreparedStatement ps = con.prepareStatement(query);
                ps.setString(1, docStatus);

                ResultSet rs=ps.executeQuery();
                if(rs.next()){
                    allInactiveRec=true;
                    Toast.makeText(getApplicationContext(), "Cannot go to Unenrollment of Queue Manager, Must UNENROLL all Doctors to proceed.", Toast.LENGTH_LONG).show();
                }
            } catch(SQLException sqle){
                System.err.println(sqle.getMessage());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return allInactiveRec;
    }
  /*  public boolean checkDocRecord() {
        boolean hasExistingDept = false;
        Connection con = connectionClass.CONN();
        String docFName = doctorFName.getText().toString();

        if(con != null){ //means that we have a valid db connection
            try{//inserting records; called INSERT_REC from DBUtility.java
                // use of parameterized query such as PreparedStatement prevents SQL injection which is considered a way to
                //prevent threat in any web app
                String query = VALIDATION_DOCTOR;
                PreparedStatement ps = con.prepareStatement(query);
                ps.setString(1, docFName);

                ResultSet rs=ps.executeQuery();
                if(rs.next()){
                    hasExistingDept=true;
                    Toast.makeText(getApplicationContext(),"Record already exists",Toast.LENGTH_LONG).show();
                }
            } catch(SQLException sqle){
                System.err.println(sqle.getMessage());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return hasExistingDept;
    }*/

    //function for displaying the enrolled department
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
                String result = "Database Connection Successful\n";
                Statement st = con.createStatement();
                ResultSet rset = st.executeQuery(SELECT_LIST_DOC);
                ResultSetMetaData rsmd = rset.getMetaData();

                List<Map<String, String>> data = null;
                data = new ArrayList<Map<String, String>> ();

                while (rset.next()) {
                    Map<String, String> datanum = new HashMap<String, String> ();
                    datanum.put("A", rset.getString(1).toString());
                    data.add(datanum);
                }

                String[] fromwhere = {"A"};

                int[] viewswhere = {R.id.lblDocList};
                listAdapter = new SimpleAdapter (UnenrollDoc.this, data,
                        R.layout.list_doc_template, fromwhere, viewswhere);

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
            docList.setAdapter(listAdapter);
        }
    }
}
