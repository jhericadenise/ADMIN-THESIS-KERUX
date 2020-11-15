 package com.kerux.admin_thesis_kerux.navigation;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.kerux.admin_thesis_kerux.R;
import com.kerux.admin_thesis_kerux.dbutility.ConnectionClass;
import com.kerux.admin_thesis_kerux.dbutility.DBUtility;
import com.kerux.admin_thesis_kerux.reports.ViewAuditReportsActivity;
import com.kerux.admin_thesis_kerux.reports.ViewStatReportsActivity;
import com.kerux.admin_thesis_kerux.session.KeruxSession;
import com.kerux.admin_thesis_kerux.unenrollment.UnenrollDoc;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

 public class MainActivity extends AppCompatActivity implements View.OnClickListener, DBUtility {
    //Initialize Variable
    DrawerLayout drawerLayout;
    TextView deptCount;
    TextView qmCount;
    ConnectionClass connectionClass;
     private KeruxSession session;
     String qmC;
     String depC;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Assign variable
        drawerLayout = findViewById(R.id.drawer_layout);
        session = new KeruxSession(getApplicationContext());
        connectionClass = new ConnectionClass (); //create ConnectionClass

        deptCount = findViewById(R.id.txtCountDept);
        qmCount = findViewById(R.id.txtCountQM);

        qmC="0";
        depC="0";
        ShowQMDept sm =new ShowQMDept();
        sm.execute();
        Handler handler = new Handler(Looper.getMainLooper());

        final Runnable r = new Runnable() {

            public void run() {

                deptCount.setText(depC);
                qmCount.setText(qmC);
            }
        };

        handler.postDelayed(r, 100);


    }


     private class ShowQMDept extends AsyncTask<String, String, String> {
         String message = "";

         @Override
         protected void onPreExecute() {
             Toast.makeText(getBaseContext(), "Please wait..", Toast.LENGTH_LONG).show();
             super.onPreExecute();
         }

         @Override
         protected String doInBackground(String... strings) {
             try {
                 URL url = new URL("https://isproj2a.benilde.edu.ph/Sympl/TotalQMDeptAdminServlet");
                 URLConnection connection = url.openConnection();

                 connection.setReadTimeout(10000);
                 connection.setConnectTimeout(15000);
                 connection.setDoInput(true);
                 connection.setDoOutput(true);

                 BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                 String returnString="";
                 ArrayList<String> output=new ArrayList<String>();
                 while ((returnString = in.readLine()) != null)
                 {
                     Log.d("returnString", returnString);
                     output.add(returnString);
                 }
                 for (int i = 0; i < output.size(); i++) {
                     if (i==0){
                         qmC=output.get(i);
                     } else if (i==1){
                         depC=output.get(i);
                     }

                    Log.d("ACACAC", output.get(i));
                 }

                 in.close();
             } catch (Exception e) {
                 e.printStackTrace();
             }

             return message;
         }

         @Override
         protected void onPostExecute(String s) {
//             Toast.makeText(getBaseContext(), "" + qmC+depC, Toast.LENGTH_LONG).show();
         }
     }


    public void ClickMenu (View view){
        //open drawer
        openDrawer(drawerLayout);
    }

    public static void openDrawer(DrawerLayout drawerLayout) {
        //open drawer layout
        drawerLayout.openDrawer(GravityCompat.START);
    }

    public void ClickLogo (View view){
        //Close drawer
        closeDrawer(drawerLayout);
    }

    public static void closeDrawer(DrawerLayout drawerLayout) {
        //Close drawer layout
        //check condition
        if(drawerLayout.isDrawerOpen(GravityCompat.START)){
            //When drawer is open
            //Close drawer
            drawerLayout.closeDrawer(GravityCompat.START);
        }
    }

    public void ClickDashboard(View view){
        //Recreate activity
        recreate();
    }
    public void ClickEditProfile(View view){
        //Redirect activity to dashboard
        MainActivity.redirectActivity(this, EditProfile.class);
    }

    public void ClickManageAccounts(View view){
        //Redirect activity to manage accounts
        redirectActivity(this, ManageAccounts.class);
    }

    public void ClickEnrollment(View view){
        //Redirect activity to enrollment
        redirectActivity(this, EnrollmentPage.class);
    }

    public void ClickRevoke(View view){
        //redirect activity to revoke page
        redirectActivity(this, UnenrollDoc.class);
    }

    public void ClickViewStat(View view){
        redirectActivity(this, ViewStatReportsActivity.class);
    }

    public void ClickViewAudit(View view){
        redirectActivity(this, ViewAuditReportsActivity.class);
    }

    public void ClickLogout(View view){
        logout(this);
    }

    public static void logout(final Activity activity) {
        //Initialize alert dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        //set title
        builder.setTitle("Logout");
        //set message
        builder.setMessage("Are you sure you want to logout?");
        //Positive yes button
        builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //Finish activity
                activity.finishAffinity();
                //exit app
                System.exit(0);
            }
        });

        //negative no button
        builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //dismiss dialog
                dialogInterface.dismiss();
            }
        });
        //show dialog
        builder.show();
    }


    public static void redirectActivity(Activity activity, Class aClass) {
        //Initialize intent
        Intent intent = new Intent(activity,aClass);
        //set flag
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        //start activity
        activity.startActivity(intent);
    }

    @Override
    protected void onPause() {
        super.onPause();
        //close drawer
        closeDrawer(drawerLayout);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.bttnViewStat:
                Intent intent = new Intent(this, ViewStatReportsActivity.class);
                startActivity(intent);
                break;
            case R.id.bttnViewAudit:
                Intent intent3 = new Intent(this, ViewAuditReportsActivity.class);
                startActivity(intent3);
                break;
        }
    }


}
