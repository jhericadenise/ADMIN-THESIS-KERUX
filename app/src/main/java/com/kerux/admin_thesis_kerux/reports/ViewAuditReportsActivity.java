package com.kerux.admin_thesis_kerux.reports;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;
import android.app.ProgressDialog;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.drawerlayout.widget.DrawerLayout;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;
import com.kerux.admin_thesis_kerux.BuildConfig;
import com.kerux.admin_thesis_kerux.R;
import com.kerux.admin_thesis_kerux.dbutility.ConnectionClass;
import com.kerux.admin_thesis_kerux.dbutility.DBUtility;
import com.kerux.admin_thesis_kerux.navigation.EditProfile;
import com.kerux.admin_thesis_kerux.navigation.EnrollmentPage;
import com.kerux.admin_thesis_kerux.navigation.MainActivity;
import com.kerux.admin_thesis_kerux.navigation.ManageAccounts;
import com.kerux.admin_thesis_kerux.security.Security;
import com.kerux.admin_thesis_kerux.session.KeruxSession;
import com.kerux.admin_thesis_kerux.unenrollment.UnenrollDoc;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ViewAuditReportsActivity extends AppCompatActivity implements DBUtility {

    ConnectionClass connectionClass;
    DrawerLayout drawerLayout;
    private ListView listAudit;
    private Button bttnViewAuditReports;
    private Button bttnGenerateAuditReports;
    private ListAdapter listAdapter;
    private String tableName;
    private String auditID;

    File myFile;
    ProgressDialog progressDialog;

    KeruxSession keruxSes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_audit_reports);
        connectionClass = new ConnectionClass (); //create ConnectionClass

        drawerLayout = findViewById(R.id.drawer_layout);
        bttnViewAuditReports = findViewById(R.id.bttnViewAudit);
        bttnGenerateAuditReports = findViewById(R.id.bttnGenerateAudit);
        listAudit = findViewById(R.id.listAuditReports);

        progressDialog=new ProgressDialog(this);

        Intent i= getIntent();
        tableName=i.getStringExtra("TableName");
        auditID=i.getStringExtra("Log_ID");


        bttnViewAuditReports.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ListAudit viewAudit = new ListAudit();
                viewAudit.execute();
            }
        });

        bttnGenerateAuditReports.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GenerateRep generateRep = new GenerateRep();
                generateRep.execute();
            }
        });

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
        MainActivity.redirectActivity(this, ViewStatReportsActivity.class);
    }

    public void ClickViewAudit(View view){
        recreate();
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

    private class ListAudit extends AsyncTask<String, Void, String> {

        Connection con = connectionClass.CONN();
        boolean isSuccess = false;
        String message = "";

        Security sec = new Security();

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Toast.makeText(ViewAuditReportsActivity.this, "Please wait...", Toast.LENGTH_SHORT).show();

        }

        @Override
        protected String doInBackground(String... params) {
            try {
                listAudit = findViewById(R.id.listAuditReports);
                String result = "Database Connection Successful\n";
                Statement st = con.createStatement();
                ResultSet rset = st.executeQuery(SELECT_AUDIT);
                ResultSetMetaData rsmd = rset.getMetaData();

                List<Map<String, String>> data = null;
                data = new ArrayList<Map<String, String>> ();

                while (rset.next()) {
                    HashMap<String, String> datanum = new HashMap<String, String>();
                    datanum.put("first", rset.getString(1).toString());
                    datanum.put("second", rset.getString(2).toString());
                    datanum.put("third", rset.getString(3).toString());
                  /*  Log.d( "AUDIT", rset.getString(2));
                    Log.d( "AUDIT", rset.getString(3));*/
                    datanum.put("fourth", rset.getString(4).toString());
                    datanum.put("fifth", rset.getString(5).toString());
                    datanum.put("sixth", rset.getString(6).toString());
                    datanum.put("seventh", rset.getString(7).toString());
                    datanum.put("eight", rset.getString(8).toString());
                    data.add(datanum);
                }


                /*int[] viewswhere = {R.id.lblDeptList};*/
                listAdapter = new SimpleAdapter (ViewAuditReportsActivity.this, data,
                        R.layout.listview_row_audit, new String[] {"first", "second", "third", "fourth", "fifth", "sixth", "seventh", "eight"},
                        new int[] {R.id.FIRST_COL, R.id.SECOND_COL, R.id.THIRD_COL, R.id.FOURTH_COL, R.id.FIFTH_COL, R.id.SIXTH_COL,
                                    R.id.SEVENTH_COL, R.id.EIGHT_COL});


                while (rset.next()) {
                    result += rset.getString(1).toString() + "\n";
                }
                message = result;
            } catch (Exception e) {
                e.printStackTrace();
                message = e.toString();
            }
            return message;
        }

        @Override
        protected void onPostExecute(String result) {
            listAudit.setAdapter(listAdapter);
        }
    }

    //generate pdf file
    private class GenerateRep extends AsyncTask<String, String, String> {
        String z="";
        @Override
        protected void onPreExecute() {
            progressDialog.setMessage("Loading...");
            progressDialog.show();

            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            z="";

            try {
                Connection con = connectionClass.CONN();
                Security sec =new Security();
                if (con == null) {
                    z = "Please check your internet connection";
                } else {
                    createPdf();
                    /*viewPdf();*/
                    emailNote();
                    z="Report Generated";
                }
            }
            catch (Exception ex)
            {
                z = "Exceptions"+ex;
            }
            return z;
        }

        @Override
        protected void onPostExecute(String s) {
            Toast.makeText(getBaseContext(),""+z, Toast.LENGTH_LONG).show();
            progressDialog.dismiss();
        }
    }

    //creating pdf file
    private void createPdf() throws FileNotFoundException, DocumentException {

        File pdfFolder = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOCUMENTS), "KERUX AUDIT LOG REPORT");
        if (!pdfFolder.exists()) {
            pdfFolder.mkdir();
            Log.i("LOG_TAG", "Pdf Directory created");
        }

        //Create time stamp
        Date date = new Date() ;
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(date);
        myFile = new File(pdfFolder + timeStamp + ".pdf");
        OutputStream output = new FileOutputStream(myFile);
        Document document = new Document(PageSize.LETTER);
        ArrayList<String> getData=retrieveData();
        PdfWriter.getInstance(document, output);

        document.open();
        document.add(new Paragraph("KERUX AUDIT LOG REPORT"));
        int counter=1;
        boolean secondphase=false;
        String line="";
        for (int i=0;i<getData.size();i++){
            if(getData.get(i).equals("---------------------------------")){
                secondphase=true;
                line="";
                counter=1;
                document.add(new Paragraph("  "));
                continue;
            }
            if (!secondphase){

                if(counter==3){
                    document.add(new Paragraph(line));
                    line="";
                    counter=1;
                }
                if(counter!=3){
                    if (counter==2){
                        line+=getData.get(i);
                    }
                    else{
                        line+=getData.get(i)+" | ";
                    }
                    counter++;
                }

            }else{
                if(counter==4){
                    document.add(new Paragraph(line));
                    line="";
                    counter=1;
                }
                if(counter!=4){
                    if (counter==3){
                        line+=getData.get(i);
                    }
                    else{
                        line+=getData.get(i)+" | ";
                    }
                    counter++;
                }
            }


        }



        document.close();
    }

    private void viewPdf(){
        Intent intent = new Intent(Intent.ACTION_VIEW);
        Uri uri = FileProvider.getUriForFile(ViewAuditReportsActivity.this,
                BuildConfig.APPLICATION_ID + ".provider",
                myFile);
        intent.setDataAndType(uri, "application/pdf");
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(intent);
    }

    private void emailNote()
    {
        Intent email = new Intent(Intent.ACTION_SEND);
        email.putExtra(Intent.EXTRA_SUBJECT,"SUBJECT");
        email.putExtra(Intent.EXTRA_TEXT, "TEXT");
        Uri uri = FileProvider.getUriForFile(ViewAuditReportsActivity.this,
                BuildConfig.APPLICATION_ID + ".provider",
                myFile);

        Log.d("EMAIL", String.valueOf(uri));

        email.putExtra(Intent.EXTRA_STREAM, uri);
        email.setType("message/rfc822");
        startActivity(email);
    }

    private ArrayList<String> retrieveData()
    {
        ArrayList<String> data=new ArrayList<>();
        String error="";
        try {
            Connection con = connectionClass.CONN();
            Security sec =new Security();
            if (con == null) {
                error="Please check your internet connection";
                Log.d("WENT HEHEHE", "Help");
            } else {

                //LOGIN TOTAL
                String totalLogin = TOTAL_NUM_LOGIN;
                PreparedStatement ps = con.prepareStatement(totalLogin);
//                ps.setString(1, tableName);

                ResultSet rs=ps.executeQuery();

                while (rs.next())
                {
                    data.add("Total Number of Logins to the app:");
                    data.add(rs.getString(1));
                    for(String num:data){
                        Log.d("MEN", num+"YYYYY");
                    }
                }

                //TOTAL ENROLLED DEPARTMENT
                String totalDept = TOTAL_NUM_ENROLLMENT_DEPT;
                PreparedStatement ps2 = con.prepareStatement(totalDept);
//                ps2.setString(1, tableName);

                ResultSet rs2=ps2.executeQuery();

                while (rs2.next())
                {
                    data.add("Total Number of Department enrolled:");
                    data.add(rs2.getString(1));
                }

                //TOTAL ENROLLED QUEUE MANAGER
                String totalQm = TOTAL_NUM_ENROLLMENT_QM;
                PreparedStatement ps3 = con.prepareStatement(totalQm);
//                ps3.setString(1, tableName);

                ResultSet rs3=ps3.executeQuery();

                while (rs3.next())
                {
                    data.add("Total Number of Queue Manager enrolled:");
                    data.add(rs3.getString(1));
                }

                //TOTAL  ENROLLED DOCTOR
                String totalDoc = TOTAL_NUM_ENROLLMENT_DOC;
                PreparedStatement ps4 = con.prepareStatement(totalDoc);
//                ps4.setString(1, tableName);

                ResultSet rs4=ps4.executeQuery();

                while (rs4.next())
                {
                    data.add("Total Number of Doctor enrolled:");
                    data.add(rs4.getString(1));
                }

                //TOTAL UNENROLLED DEPARTMENT
                String totalUnenrollDept = TOTAL_NUM_UNENROLL_DEPT;
                PreparedStatement ps5 = con.prepareStatement(totalUnenrollDept);
//                ps5.setString(1, tableName);

                ResultSet rs5 = ps5.executeQuery();

                while (rs5.next())
                {
                    data.add("Total Number of Department unenrolled:");
                    data.add(rs5.getString(1));
                }

                //TOTAL UNENROLLED QUEUE MANAGER
                String totalUnenrollQM = TOTAL_NUM_UNENROLL_QM;
                PreparedStatement ps6 = con.prepareStatement(totalUnenrollQM);
//                ps5.setString(1, tableName);

                ResultSet rs6 = ps6.executeQuery();

                while (rs6.next())
                {
                    data.add("Total Number of Queue Manager unenrolled:");
                    data.add(rs6.getString(1));
                }

                //TOTAL UNENROLLED DOCTOR
                String totalUnenrollDoc = TOTAL_NUM_UNENROLL_DOC;
                PreparedStatement ps7 = con.prepareStatement(totalUnenrollDoc);
//                ps5.setString(1, tableName);

                ResultSet rs7 = ps7.executeQuery();

                while (rs7.next())
                {
                    data.add("Total Number of Doctor unenrolled:");
                    data.add(rs7.getString(1));
                }

                //LIST
                String queryLIST=SELECT_AUDIT_LIST;

                PreparedStatement ps8 = con.prepareStatement(queryLIST);
//                ps8.setString(1, auditID);

                ResultSet rs8 = ps8.executeQuery();
                data.add("---------------------------------");
                data.add("Table Name");
                data.add("Event Type");
                data.add("Timestamp");

                while (rs8.next())
                {
                    data.add(rs8.getString(1));
                    data.add(rs8.getString(2));
                    data.add(rs8.getString(3));
                }

            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
        if(!error.equals("")){
            Toast.makeText(getBaseContext(),error, Toast.LENGTH_LONG).show();
        }
        Log.d("DATAAA", data.get(1));
        return data;

    }
}