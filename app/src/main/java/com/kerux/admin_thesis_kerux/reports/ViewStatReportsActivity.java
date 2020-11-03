package com.kerux.admin_thesis_kerux.reports;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

import com.kerux.admin_thesis_kerux.R;
import com.kerux.admin_thesis_kerux.dbutility.ConnectionClass;
import com.kerux.admin_thesis_kerux.dbutility.DBUtility;
import com.kerux.admin_thesis_kerux.navigation.EditProfile;
import com.kerux.admin_thesis_kerux.navigation.EnrollmentPage;
import com.kerux.admin_thesis_kerux.navigation.MainActivity;
import com.kerux.admin_thesis_kerux.navigation.ManageAccounts;
import com.kerux.admin_thesis_kerux.unenrollment.UnenrollDoc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class ViewStatReportsActivity extends AppCompatActivity implements DBUtility {

    DrawerLayout drawerLayout;
    ConnectionClass connectionClass;
    StatisticModel statModel;
    Button bttnDisplayStat;
    TextView txtServed;
    TextView txtCancelled;
    TextView docQueue;
    TextView deptQueue;
    TextView txtdate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_stat_reports);
        drawerLayout = findViewById(R.id.drawer_layout);
        connectionClass=new ConnectionClass();
        statModel = new StatisticModel(getApplicationContext());

        bttnDisplayStat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                generateStat();
            }
        });
        txtServed = findViewById(R.id.txtQueuesServed);
        txtCancelled = findViewById(R.id.txtQueuesCancelled);
        docQueue = findViewById(R.id.txtHighestDocQueue);
        deptQueue = findViewById(R.id.txtHighestDeptQueue);
        txtdate = findViewById(R.id.txtTimeStamp);

        txtdate.setText(timeStamp());

    }
    //Getting time stamp
    public String timeStamp() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss");
        return sdf.format(calendar.getTime());
    }
    public void generateStat() {

        txtServed.setText(statModel.getQueuesServed());
        txtCancelled.setText(statModel.getQueuesCancelled());
        docQueue.setText(statModel.getHighestDoctorQueues());
        deptQueue.setText(statModel.getHighestDeptQueues());

        String query = SELECT_STAT;
        Connection con = connectionClass.CONN();
        PreparedStatement ps = null;
        try {
            ps = con.prepareStatement(query);
            ResultSet rs = ps.executeQuery();
            /*z=secweb.encrypt(uname) + " " + secweb.encrypt(pw);*/

           /* while (rs.next()) {
                rs.getString(1);
                rs.getString(2);
                rs.getString(3);
                rs.getString(4);

            }*/
        } catch (SQLException throwables) {
            throwables.printStackTrace();
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

    public void ClickDashboard(View view){
        //Redirect activity to dashboard
        MainActivity.redirectActivity(this, MainActivity.class);
    }

    public void ClickEditProfile(View view){
        //Redirect activity to dashboard
        MainActivity.redirectActivity(this, EditProfile.class);
    }

    public void ClickManageAccounts(View view){
        //Redirect activity to manage accounts
        MainActivity.redirectActivity(this, ManageAccounts.class);
    }

    public void ClickEnrollment(View view){
        MainActivity.redirectActivity(this, EnrollmentPage.class);
    }

    public void ClickRevoke(View view){
        //redirect activity to revoke page
        MainActivity.redirectActivity(this, UnenrollDoc.class);
    }

    public void ClickViewStat(View view){
        recreate();
    }

    public void ClickViewAudit(View view){
        MainActivity.redirectActivity(this, ViewAuditReportsActivity.class);
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