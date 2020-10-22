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
import android.widget.TextView;

import com.kerux.admin_thesis_kerux.R;
import com.kerux.admin_thesis_kerux.enrollment.EnrollDept;
import com.kerux.admin_thesis_kerux.enrollment.EnrollDoctor;
import com.kerux.admin_thesis_kerux.enrollment.EnrollQM;
import com.kerux.admin_thesis_kerux.unenrollment.UnenrollDoc;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    DrawerLayout drawerLayout;
    NavigationView navigationView;
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        drawerLayout = findViewById(R.id.drawer_layout_main);
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

        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setCheckedItem(R.id.nav_dashboard);

        TextView titleDate = (TextView) findViewById(R.id.txtDate);
        titleDate.setText(giveDate());
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
                break;
            case R.id.nav_enrollment:
                Intent intent = new Intent(MainActivity.this, EnrollmentPage.class);
                startActivity(intent);
                break;
            case R.id.nav_enrollment_dept:
                Intent intent1 = new Intent(MainActivity.this, EnrollDept.class);
                startActivity(intent1);
                break;
            case R.id.nav_enrollment_doctor:
                Intent intent2 = new Intent(MainActivity.this, EnrollDoctor.class);
                startActivity(intent2);
                break;
            case R.id.nav_enrollment_qm:
                Intent intent3 = new Intent(MainActivity.this, EnrollQM.class);
                startActivity(intent3);
                break;
            case R.id.nav_revoke:
                Intent intent4 = new Intent(MainActivity.this, UnenrollDoc.class);
                startActivity(intent4);
                break;
            case R.id.nav_accounts:
                Intent intent5 = new Intent(MainActivity.this, ManageAccounts.class);
                startActivity(intent5);
                break;

        }

        drawerLayout.closeDrawer(GravityCompat.START);

        return true;
    }

    public String giveDate() {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("EEE, MMM d, yyyy");
        return sdf.format(cal.getTime());
    }

}
