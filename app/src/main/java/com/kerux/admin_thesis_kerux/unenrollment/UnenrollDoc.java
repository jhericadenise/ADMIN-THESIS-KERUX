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

    private ListView docList;
    private ListAdapter listAdapter;
    Button docDisplayList;

    ConnectionClass connectionClass;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_unenroll_doctor);
        connectionClass = new ConnectionClass(); //create ConnectionClass

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.nav_view);
        navigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.navigation_dashboard:
                        Intent a = new Intent( UnenrollDoc.this, MainActivity.class);
                        startActivity(a);
                        break;
                    case R.id.navigation_enrollment:
                        Intent b = new Intent(UnenrollDoc.this, EnrollmentPage.class);
                        startActivity(b);
                        break;
                    case R.id.navigation_accounts:
                        Intent c = new Intent(UnenrollDoc.this, ManageAccounts.class);
                        startActivity(c);
                        break;
                }
                return false;
            }
        });
        Button bttnBack = findViewById(R.id.bttnBackDoc);
        docList = (ListView) findViewById(R.id.listEnrolledDoc);
        docDisplayList = (Button) findViewById(R.id.bttnDisplayDoc);


        docDisplayList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UnenrollDoc.ListDoctor docListDisp = new UnenrollDoc.ListDoctor();
                docListDisp.execute();
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
                                String name = selectedFromList.substring(3, selectedFromList.length()-1);

                                Toast.makeText(getApplicationContext(),"Deleted",Toast.LENGTH_LONG).show();
                                unenrollDoctor(name);
                                UnenrollDoc.ListDoctor docListDisp = new UnenrollDoc.ListDoctor();
                                docListDisp.execute();
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
    public void unenrollDoctor(String name){

        Connection con = connectionClass.CONN();
        PreparedStatement ps = null;
        try {
            ps = con.prepareStatement(UNENROLL_DOCTOR);
            ps.setString(1, name);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public void goBack() {
        Intent intent = new Intent(this, EnrollmentPage.class);
        startActivity(intent);
    }

/*
    public boolean checkDocRecord() {
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
    }
*/

    private class ListDoctor extends AsyncTask<String, String, String> {
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
                Security sec = new Security ();
                String result = "Database Connection Successful\n";
                Statement st = con.createStatement();
                ResultSet rset = st.executeQuery(SELECT_LIST_DOC);
                ResultSetMetaData rsmd = rset.getMetaData();

                List<Map<String, String>> data = null;
                data = new ArrayList<Map<String, String>> ();

                while (rset.next()) {
                    Map<String, String> datanum = new HashMap<String, String> ();
                    datanum.put("A", rset.getString(1).toString() + " " + rset.getString(2).toString());
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
