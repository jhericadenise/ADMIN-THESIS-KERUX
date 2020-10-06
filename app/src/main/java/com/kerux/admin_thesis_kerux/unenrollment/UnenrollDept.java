package com.kerux.admin_thesis_kerux.unenrollment;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
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
import com.kerux.admin_thesis_kerux.enrollment.EnrollDept;
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
    private Spinner spinnerEnrolledDept;
    private Spinner spinnerClinic;
    private Spinner spinnerReasonRevoke;
    private static String urlDeptSpinner = "http://192.168.1.11:89/kerux/unenrollDeptSpinner.php";
    private static String urlClinicSpinner = "http://192.168.1.11:89/kerux/clinicSpinner.php";
    private static String urlReasonSpinner = "http://192.168.1.11:89/kerux/reasonRevokeSpinner.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate ( savedInstanceState );
        setContentView ( R.layout.activity_unenroll_dept );
        connectionClass = new ConnectionClass (); //create ConnectionClass

        BottomNavigationView navigation = (BottomNavigationView) findViewById ( R.id.nav_view );
        navigation.setOnNavigationItemSelectedListener ( new BottomNavigationView.OnNavigationItemSelectedListener () {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId ()) {
                    case R.id.navigation_dashboard:
                        Intent a = new Intent ( UnenrollDept.this, MainActivity.class );
                        startActivity ( a );
                        break;
                    case R.id.navigation_enrollment:
                        Intent b = new Intent ( UnenrollDept.this, EnrollmentPage.class );
                        startActivity ( b );
                        break;
                    case R.id.navigation_accounts:
                        Intent c = new Intent ( UnenrollDept.this, ManageAccounts.class );
                        startActivity ( c );
                        break;
                }
                return false;
            }
        } );

        Button unenrollDept = findViewById ( R.id.bttnUnenrollDept );
        unenrollDept.setOnClickListener ( new View.OnClickListener () {
            @Override
            public void onClick(View v) {
                unenrollDept( String.valueOf ( v ) );
            }
        } );

        Button showRecords = findViewById ( R.id.bttnShow );
        showRecords.setOnClickListener ( new View.OnClickListener () {
            @Override
            public void onClick(View v) {

            }
        } );

        spinnerEnrolledDept = (Spinner) findViewById(R.id.spinnerEnrolledDept);
        spinnerClinic = (Spinner) findViewById(R.id.spinnerClinic);
        spinnerClinic.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener () {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                // your code here
                Downloader dep = new Downloader( UnenrollDept.this, urlDeptSpinner, spinnerEnrolledDept, "Name");
                dep.execute();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
            }

        });

        spinnerReasonRevoke = (Spinner) findViewById(R.id.spinnerReason);

        Downloader clinic = new Downloader(UnenrollDept.this, urlClinicSpinner, spinnerClinic, "clinicName");
        clinic.execute();
        Downloader reason = new Downloader(UnenrollDept.this, urlReasonSpinner, spinnerReasonRevoke, "reason");
        reason.execute();
    }

    public void showRecords(){

    }

    //deleting a record in the database
    public void unenrollDept(String name){
        Connection con = connectionClass.CONN();
        PreparedStatement ps = null;
        try {
            ps = con.prepareStatement(UNENROLL_DEPT);
            ps.setString(1, name);
            ps.executeUpdate();
            Toast.makeText(getBaseContext(),"Unenrolled",Toast.LENGTH_LONG).show();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
