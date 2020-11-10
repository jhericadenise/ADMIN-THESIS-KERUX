package com.kerux.admin_thesis_kerux.reports;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

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
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class ViewStatReportsActivity extends AppCompatActivity implements DBUtility {

    DrawerLayout drawerLayout;
    ConnectionClass connectionClass;
    StatisticModel statModel;
    private Button bttnDisplayStat;
    private Button bttnGenerateStatReports;
    private TextView txtServed;
    private TextView txtCancelled;
    private TextView docQueue;
    private TextView deptQueue;
    private TextView txtdate;

    private String queuesServed;
    private String queuesCancelled;
    private String highestDocQueues;
    private String highestDeptQueues;
    private String statID;

    File myFile;
    ProgressDialog progressDialog;

    KeruxSession session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_stat_reports);
        drawerLayout = findViewById(R.id.drawer_layout);
        connectionClass=new ConnectionClass();
        session=new KeruxSession(getApplicationContext());
        statModel = new StatisticModel(getApplicationContext());
        bttnDisplayStat=(Button)findViewById(R.id.bttnRefresh);
        bttnDisplayStat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                generateStat();
            }
        });
        txtServed = (TextView)findViewById(R.id.txtQueuesServed);
        txtCancelled = (TextView)findViewById(R.id.txtQueuesCancelled);
        docQueue = (TextView)findViewById(R.id.txtHighestDocQueue);
        deptQueue = (TextView)findViewById(R.id.txtHighestDeptQueue);
        txtdate = (TextView)findViewById(R.id.txtTimeStamp);

        txtdate.setText(timeStamp());

        progressDialog=new ProgressDialog(this);

        Intent i= getIntent();
        queuesServed = i.getStringExtra("QueuesServed");
        queuesCancelled = i.getStringExtra("QueuesCancelled");
        highestDeptQueues = i.getStringExtra("HighestDocQueues");
        highestDocQueues = i.getStringExtra("HighestDeptQueues");
        statID=i.getStringExtra("Statistics_ID");

        bttnGenerateStatReports = findViewById(R.id.bttnGenerateStat);

        bttnGenerateStatReports.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GenerateRep generateRep = new GenerateRep();
                generateRep.execute();
            }
        });
    }

    //Getting time stamp
    public String timeStamp() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss");
        return sdf.format(calendar.getTime());
    }

    //Generate statistic reports
    public void generateStat() {

        String query = INSERT_STAT;
        Connection con = connectionClass.CONN();
        PreparedStatement ps = null;
        try {
            ps = con.prepareStatement(query);
            ps.setString(1, session.getclinicid());
            ps.setString(2, session.getclinicid());
            int i=ps.executeUpdate();

            if (i==1){
                String query1 = SELECT_STAT;

                PreparedStatement ps1 = con.prepareStatement(query1);
                ResultSet rs=ps1.executeQuery();
                while (rs.next()) {
                    txtServed.setText(rs.getString(1));
                    txtCancelled.setText(rs.getString(2));
                    docQueue.setText(rs.getString(3));
                    deptQueue.setText(rs.getString(4));

                }
            }

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
                    viewPdf();
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
                Environment.DIRECTORY_DOCUMENTS), "pdfdemo");
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
        document.add(new Paragraph("KERUX STATISTIC REPORT"));
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
        Uri uri = FileProvider.getUriForFile(ViewStatReportsActivity.this,
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
        Uri uri = FileProvider.getUriForFile(ViewStatReportsActivity.this,
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

                //QUEUES SERVED
                String queuesServed = QUEUES_SERVED;
                PreparedStatement ps = con.prepareStatement(queuesServed);
                ResultSet rs=ps.executeQuery();

                while (rs.next())
                {
                    data.add("Queues Served: ");
                    data.add(rs.getString(1));
                    data.add(rs.getString(2));
                    for(String num:data){
                        Log.d("MEN", num+"YYYYY");
                    }
                }

                //QUEUES CANCELLED
                String queuesCancelled = QUEUES_CANCELLED;
                PreparedStatement ps2 = con.prepareStatement(queuesCancelled);
                ResultSet rs2=ps2.executeQuery();

                while (rs2.next())
                {
                    data.add("Queues Cancelled: ");
                    data.add(rs2.getString(1));
                    data.add(rs2.getString(2));
                }

                //HIGHEST DOCTOR QUEUES
                String doctorQueues = HIGHEST_DOC_QUEUES;
                PreparedStatement ps3 = con.prepareStatement(doctorQueues);
                ResultSet rs3=ps3.executeQuery();

                while (rs3.next())
                {
                    data.add("Highest Doctor Queues: ");
                    data.add(rs3.getString(1));
                    data.add(rs3.getString(2));
                }

                //HIGHEST DEPARTMENT QUEUES
                String deptQueues = HIGHEST_DEPT_QUEUES;
                PreparedStatement ps4 = con.prepareStatement(deptQueues);
                ResultSet rs4=ps4.executeQuery();

                while (rs4.next())
                {
                    data.add("Highest Department Queues: ");
                    data.add(rs4.getString(1));
                    data.add(rs4.getString(2));
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