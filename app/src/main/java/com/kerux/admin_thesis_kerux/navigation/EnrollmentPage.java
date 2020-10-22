package com.kerux.admin_thesis_kerux.navigation;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.kerux.admin_thesis_kerux.R;
import com.kerux.admin_thesis_kerux.dbutility.ConnectionClass;
import com.kerux.admin_thesis_kerux.dbutility.DBUtility;
import com.kerux.admin_thesis_kerux.enrollment.EnrollDept;
import com.kerux.admin_thesis_kerux.enrollment.EnrollDoctor;
import com.kerux.admin_thesis_kerux.enrollment.EnrollQM;
import com.kerux.admin_thesis_kerux.unenrollment.UnenrollDoc;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class EnrollmentPage extends AppCompatActivity implements View.OnClickListener, DBUtility, NavigationView.OnNavigationItemSelectedListener {
    ConnectionClass connectionClass;
    private ListAdapter listAdapter;
    private ListView deptList;
    DrawerLayout drawerLayout;
    NavigationView navigationView;
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enrollment_page);
        connectionClass = new ConnectionClass(); //create ConnectionClass
        TextView titleDate = (TextView) findViewById(R.id.txtEnrollDate);
        titleDate.setText(giveDate());

        drawerLayout = findViewById(R.id.drawer_layout_ep);
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

        navigationView.setNavigationItemSelectedListener(EnrollmentPage.this);
        navigationView.setCheckedItem(R.id.nav_enrollment);


        Button bttnQM = findViewById(R.id.bttnQm);
        Button bttnDept = findViewById(R.id.bttnDept);
        Button bttnDoctor = findViewById(R.id.bttnDoctor);
        /*Button bttnUnenrollDept = findViewById(R.id.bttnDisplayDept);*/
        /*Button bttnUnenrollQm = findViewById(R.id.bttnUnenrollQm);*/
        Button bttnUnenrollDoc = findViewById(R.id.bttnUnenrollDoc);

        bttnQM.setOnClickListener(this);
        bttnDept.setOnClickListener(this);
        bttnDoctor.setOnClickListener(this);
        /*bttnUnenrollDept.setOnClickListener(this);*/
        /*bttnUnenrollQm.setOnClickListener(this);*/
        bttnUnenrollDoc.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.bttnQm:
                Intent intent = new Intent(this, EnrollQM.class);
                startActivity(intent);
                break;
            case R.id.bttnDoctor:
                Intent intent2 = new Intent(this, EnrollDoctor.class);
                startActivity(intent2);
                break;
            case R.id.bttnDept:
                Intent intent3 = new Intent(this, EnrollDept.class);
                startActivity(intent3);
                break;
          /*  case R.id.bttnDisplayDept:
                Intent intent4 = new Intent(this, UnenrollDept.class);
                startActivity (intent4);
                break;*/
        /*    case R.id.bttnUnenrollQm:
                Intent intent5 = new Intent(this, UnenrollQm.class);
                startActivity (intent5);
                break;*/
            case R.id.bttnUnenrollDoc:
                Intent intent6 = new Intent(this, UnenrollDoc.class);
                startActivity (intent6);
                break;
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
                Intent intent = new Intent(EnrollmentPage.this, MainActivity.class);
                startActivity(intent);
                break;
            case R.id.nav_enrollment:
                break;
            case R.id.nav_enrollment_dept:
                Intent intent1 = new Intent(EnrollmentPage.this, EnrollDept.class);
                startActivity(intent1);
                break;
            case R.id.nav_enrollment_doctor:
                Intent intent2 = new Intent(EnrollmentPage.this, EnrollDoctor.class);
                startActivity(intent2);
                break;
            case R.id.nav_enrollment_qm:
                Intent intent3 = new Intent(EnrollmentPage.this, EnrollQM.class);
                startActivity(intent3);
                break;
            case R.id.nav_revoke:
                Intent intent4 = new Intent(EnrollmentPage.this, UnenrollDoc.class);
                startActivity(intent4);
                break;
            case R.id.nav_accounts:
                Intent intent5 = new Intent(EnrollmentPage.this, ManageAccounts.class);
                startActivity(intent5);
                break;

        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }
}
