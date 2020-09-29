package com.kerux.admin_thesis_kerux.navigation;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.TextView;

import com.kerux.admin_thesis_kerux.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView titleDate = (TextView) findViewById(R.id.txtDate);
        titleDate.setText(giveDate());

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.nav_view);
        navigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.navigation_dashboard:
                        Intent a = new Intent(MainActivity.this, MainActivity.class);
                        startActivity(a);
                        break;
                    case R.id.navigation_enrollment:
                        Intent b = new Intent(MainActivity.this, EnrollmentPage.class);
                        startActivity(b);
                        break;
                    case R.id.navigation_accounts:
                        Intent c = new Intent(MainActivity.this, ManageAccounts.class);
                        startActivity(c);
                        break;
                }
                return false;
            }
        });
    }

    public String giveDate() {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("EEE, MMM d, yyyy");
        return sdf.format(cal.getTime());
    }

}
