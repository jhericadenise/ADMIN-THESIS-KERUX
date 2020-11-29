package com.kerux.admin_thesis_kerux.navigation;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

import com.kerux.admin_thesis_kerux.R;
import com.kerux.admin_thesis_kerux.dbutility.ConnectionClass;
import com.kerux.admin_thesis_kerux.dbutility.DBUtility;
import com.kerux.admin_thesis_kerux.edit.EditDoctor;
import com.kerux.admin_thesis_kerux.edit.EditQm;
import com.kerux.admin_thesis_kerux.enrollment.EnrollDept;
import com.kerux.admin_thesis_kerux.enrollment.EnrollDoctor;
import com.kerux.admin_thesis_kerux.enrollment.EnrollQM;
import com.kerux.admin_thesis_kerux.reports.ViewAuditReportsActivity;
import com.kerux.admin_thesis_kerux.reports.ViewStatReportsActivity;
import com.kerux.admin_thesis_kerux.unenrollment.UnenrollDoc;

public class EnrollmentPage extends AppCompatActivity implements View.OnClickListener, DBUtility{

    ConnectionClass connectionClass;
    DrawerLayout drawerLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enrollment_page);
        connectionClass = new ConnectionClass(); //create ConnectionClass

        drawerLayout = findViewById(R.id.drawer_layout);

        Button bttnQM = findViewById(R.id.bttnQm);
        Button bttnDept = findViewById(R.id.bttnDept);
        Button bttnDoctor = findViewById(R.id.bttnDoctor);
        Button bttnUnenrollDoc = findViewById(R.id.bttnUnenrollDoc);

        bttnQM.setOnClickListener(this);
        bttnDept.setOnClickListener(this);
        bttnDoctor.setOnClickListener(this);
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
            case R.id.bttnUnenrollDoc:
                Intent intent6 = new Intent(this, UnenrollDoc.class);
                startActivity (intent6);
                break;
        }
    }

    public void ClickMenu (View view){
        //open drawer
        MainActivity.openDrawer(drawerLayout);
    }

    public void ClickLogo (View view){
        //Close drawer
        MainActivity.closeDrawer(drawerLayout);
    }

    public void ClickEditProfile(View view){
        //Redirect activity to dashboard
        MainActivity.redirectActivity(this, EditProfile.class);
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
        recreate();
    }

    public void ClickRevoke(View view){
        //redirect activity to revoke page
        MainActivity.redirectActivity(this, UnenrollDoc.class);
    }

    public void ClickViewStat(View view){
        MainActivity.redirectActivity(this, ViewStatReportsActivity.class);
    }

    public void ClickViewAudit(View view){
        MainActivity.redirectActivity(this, ViewAuditReportsActivity.class);
    }
    public void ClickEditQM(View view){
        MainActivity.redirectActivity(this, EditQm.class);
    }

    public void ClickEditDoctor(View view){
        MainActivity.redirectActivity(this, EditDoctor.class);
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
}
