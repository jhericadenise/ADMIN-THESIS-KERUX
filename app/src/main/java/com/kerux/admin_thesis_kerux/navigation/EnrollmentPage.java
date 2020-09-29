package com.kerux.admin_thesis_kerux.navigation;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class EnrollmentPage extends AppCompatActivity implements View.OnClickListener, DBUtility {
    ConnectionClass connectionClass;
    private ListAdapter listAdapter;
    private ListView deptList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enrollment_page);
        connectionClass=new ConnectionClass(); //create ConnectionClass
        TextView titleDate = (TextView) findViewById(R.id.txtEnrollDate);
        titleDate.setText(giveDate());

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.nav_view);
        navigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.navigation_dashboard:
                        Intent a = new Intent(EnrollmentPage.this, MainActivity.class);
                        startActivity(a);
                        break;
                    case R.id.navigation_enrollment:
                        Intent b = new Intent(EnrollmentPage.this, EnrollmentPage.class);
                        startActivity(b);
                        break;
                    case R.id.navigation_accounts:
                        Intent c = new Intent(EnrollmentPage.this, ManageAccounts.class);
                        startActivity(c);
                        break;
                }
                return false;
            }
        });
        Button bttnQM = findViewById(R.id.bttnQm);
        Button bttnDept = findViewById(R.id.bttnDept);
        Button bttnDoctor = findViewById(R.id.bttnDoctor);

        bttnQM.setOnClickListener(this);
        bttnDept.setOnClickListener(this);
        bttnDoctor.setOnClickListener(this);
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
        }
    }
    public String giveDate() {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("EEE, MMM d, yyyy");
        return sdf.format(cal.getTime());
    }

}
