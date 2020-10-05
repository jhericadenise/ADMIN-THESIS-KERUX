package com.kerux.admin_thesis_kerux.enrollment;

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
import android.widget.Toast;

import com.kerux.admin_thesis_kerux.navigation.EnrollmentPage;
import com.kerux.admin_thesis_kerux.navigation.MainActivity;
import com.kerux.admin_thesis_kerux.navigation.ManageAccounts;
import com.kerux.admin_thesis_kerux.R;
import com.kerux.admin_thesis_kerux.security.Security;
import com.kerux.admin_thesis_kerux.dbutility.ConnectionClass;
import com.kerux.admin_thesis_kerux.dbutility.DBUtility;
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

public class EnrollDoctor extends AppCompatActivity implements DBUtility {

    private static String urlClinicSpinner = "http://10.0.2.2:89/kerux/clinicSpinner.php";
    private static String urlDeptSpinner = "http://10.0.2.2:89/kerux/departmentSpinner.php"; /*10.0.2.2:89*/
    private static String urlDocTypeSpinner = "http://10.0.2.2:89/kerux/doctorTypeSpinner.php";
    private EditText doctorName;
    private EditText roomNo;
    private EditText schedule1;
    private EditText schedule2;
    private CheckBox monday;
    private CheckBox tuesday;
    private CheckBox wednesday;
    private CheckBox thursday;
    private CheckBox friday;
    private CheckBox saturday;
    private Spinner spinnerDocType;
    private Spinner spinnerDep;
    private Spinner spinnerClinic;
    private ListView docList;
    private ListAdapter listAdapter;
    Button docDisplayList;

    ConnectionClass connectionClass;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enroll_doctor);
        connectionClass = new ConnectionClass(); //create ConnectionClass

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.nav_view);
        navigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.navigation_dashboard:
                        Intent a = new Intent(EnrollDoctor.this, MainActivity.class);
                        startActivity(a);
                        break;
                    case R.id.navigation_enrollment:
                        Intent b = new Intent(EnrollDoctor.this, EnrollmentPage.class);
                        startActivity(b);
                        break;
                    case R.id.navigation_accounts:
                        Intent c = new Intent(EnrollDoctor.this, ManageAccounts.class);
                        startActivity(c);
                        break;
                }
                return false;
            }
        });
        Button bttnBack = findViewById(R.id.bttnBackDoc);
        Button bttnEnrollDoc = findViewById(R.id.bttnEnrollDoc);
        doctorName = (EditText) findViewById(R.id.txtboxDocFName );
        roomNo = (EditText) findViewById(R.id.txtboxRoomNo);
        schedule1 = (EditText) findViewById(R.id.txtboxSched1);
        schedule2 = (EditText) findViewById(R.id.txtboxSched2);
        monday = (CheckBox) findViewById(R.id.cBoxMon);
        tuesday = (CheckBox) findViewById(R.id.cBoxTues);
        wednesday = (CheckBox) findViewById(R.id.cBoxWed);
        thursday = (CheckBox) findViewById(R.id.cBoxThurs);
        friday = (CheckBox) findViewById(R.id.cBoxFriday);
        saturday = (CheckBox) findViewById(R.id.cBoxSat);
        spinnerDocType = (Spinner) findViewById(R.id.spinnerDocType);
        spinnerDep = (Spinner) findViewById(R.id.spinnerDepType);
        spinnerClinic = (Spinner) findViewById(R.id.spinnerClinic);
       /* docList = (ListView) findViewById(R.id.listEnrolledDoc);
        docDisplayList = (Button) findViewById(R.id.bttnDisplayDoc);*/

        bttnBack.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                goBack();
            }
        });

        bttnEnrollDoc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EnrollDoctor.DoEnrollDoc doenroll = new EnrollDoctor.DoEnrollDoc();
                doenroll.execute();
                doctorName.getText().clear();
            }
        });

       /* docDisplayList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EnrollDoctor.ListDoctor docListDisp = new EnrollDoctor.ListDoctor();
                docListDisp.execute();
            }
        });*/
   /*     docList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                final String selectedFromList = String.valueOf((docList.getItemAtPosition(position)));
                Toast.makeText(getApplicationContext(),"You selected: "+selectedFromList,Toast.LENGTH_LONG).show();
                //Dialog box, for unenrolling
                AlertDialog.Builder builder = new AlertDialog.Builder(EnrollDoctor.this);
                builder.setMessage("Unenroll Doctor?")
                        .setCancelable(false)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                String name = selectedFromList.substring(3, selectedFromList.length()-1);

                                Toast.makeText(getApplicationContext(),"Deleted",Toast.LENGTH_LONG).show();
                                unenrollDoctor(name);
                                EnrollDoctor.ListDoctor docListDisp = new EnrollDoctor.ListDoctor();
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

        });*/
        Downloader clinic = new Downloader(EnrollDoctor.this, urlClinicSpinner, spinnerClinic, "clinicName");
        clinic.execute();
        Downloader dep = new Downloader(EnrollDoctor.this, urlDeptSpinner, spinnerDep, "Name");
        dep.execute();
        Downloader docType = new Downloader(EnrollDoctor.this, urlDocTypeSpinner, spinnerDocType, "DoctorType");
        docType.execute();

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

    public boolean checkDeptRecord() {
        boolean hasExistingDept = false;
        Connection con = connectionClass.CONN();
        String docName = doctorName.getText().toString();

        if(con != null){ //means that we have a valid db connection
            try{//inserting records; called INSERT_REC from DBUtility.java
                // use of parameterized query such as PreparedStatement prevents SQL injection which is considered a way to
                //prevent threat in any web app
                String query = VALIDATION_DEPT;
                PreparedStatement ps = con.prepareStatement(query);
                ps.setString(1, docName);

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


    private class DoEnrollDoc extends AsyncTask<String, String, String> {

        Security sec = new Security();
        boolean isSuccess = false;
        boolean hasRecord = false;
        String message = "";
        String docName = doctorName.getText().toString();
        String roomNum = roomNo.getText().toString();
        String sched1 = schedule1.getText().toString();
        String sched2 = schedule2.getText().toString();
        String cboxMon = monday.getText().toString();
        String cboxTues = tuesday.getText().toString();
        String cboxWed = wednesday.getText().toString();
        String cboxThurs = thursday.getText().toString();
        String cboxFri = friday.getText().toString();
        String cboxSat = saturday.getText().toString();
        String docDays="";
        int docType = (int)spinnerDocType.getSelectedItemId();
        int dept = (int)spinnerDep.getSelectedItemId();
        int clinic = (int)spinnerClinic.getSelectedItemId();
        String status = "Active";

        @Override
        protected void onPreExecute() {
                super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            Connection con = connectionClass.CONN();
            PreparedStatement ps1 = null;
            try {
                ps1 = con.prepareStatement(VALIDATION_DOCTOR);
                ps1.setString(1, docName);

                ResultSet rs = ps1.executeQuery();

                if (rs.next()) {
                    hasRecord=true;

                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

            if (docName.trim().equals("") || roomNum.trim().equals("") || sched1.trim().equals("") || sched2.trim().equals("") ) {
                message = "Please enter all fields....";
            }
            else if ( !docName.matches("^[A-Za-z]+$")) {
                message = "Check format";
            }
            else if (hasRecord){
                message = "Record already exists";
            }
            else {
                try {
                    if (con == null) {
                        message = "Unsuccessful";
                    } else {

                        if(cboxMon!=null){
                            docDays+=cboxMon;
                        }
                        if(cboxTues!=null){
                            docDays+=cboxTues;
                        }
                        if(cboxWed!=null){
                            docDays+=cboxWed;
                        }
                        if(cboxThurs!=null){
                            docDays+=cboxThurs;
                        }
                        if(cboxFri!=null){
                            docDays+=cboxFri;
                        }
                        if(cboxSat!=null){
                            docDays+=cboxSat;
                        }
                        String query = INSERT_DOCTOR;
                        PreparedStatement ps = con.prepareStatement(query);
                        ps.setString(1, docName);
                        ps.setString(2, String.valueOf(docType));
                        ps.setString(3, String.valueOf(dept));
                        ps.setString(4, roomNum);
                        ps.setString(5, sched1);
                        ps.setString(6, sched2);
                        ps.setString(7, docDays);
                        ps.setString(8, status);

                        ps.execute();
                        con.close();
                        message = "ADDED";
                    }
                } catch (Exception ex) {
                    isSuccess = false;
                    message = "Exceptions" + ex;
                }
            }
                return message;
        }

        @Override
        protected void onPostExecute(String s) {
            Toast.makeText(getBaseContext(), "" + message, Toast.LENGTH_LONG).show();

            if (isSuccess) {
                Intent intent = new Intent(EnrollDoctor.this, EnrollDoctor.class);
                startActivity(intent);
            }

        }

    }
/*

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
                String result = "Database Connection Successful\n";
                Statement st = con.createStatement();
                ResultSet rset = st.executeQuery(SELECT_LIST_DOC);
                ResultSetMetaData rsmd = rset.getMetaData();

                List<Map<String, String>> data = null;
                data = new ArrayList<Map<String, String>>();

                while (rset.next()) {
                    Map<String, String> datanum = new HashMap<String, String>();
                    datanum.put("A", rset.getString(1).toString());
                    data.add(datanum);
                }

                String[] fromwhere = {"A"};
                int[] viewswhere = {R.id.lblDocList};
                listAdapter = new SimpleAdapter(EnrollDoctor.this, data,
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
*/

}
