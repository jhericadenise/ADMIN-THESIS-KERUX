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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
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
        bttnGenerateStatReports=(Button)findViewById(R.id.bttnGenerateStat);

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

        try {


            URL url = new URL("https://isproj2a.benilde.edu.ph/Sympl/GenerateStatServlet");
            URLConnection connection = url.openConnection();

            connection.setReadTimeout(10000);
            connection.setConnectTimeout(15000);
            connection.setDoInput(true);
            connection.setDoOutput(true);

            Uri.Builder builder = new Uri.Builder()
                    .appendQueryParameter("getclinicid", session.getclinicid());
            String query = builder.build().getEncodedQuery();

            OutputStream os = connection.getOutputStream();
            BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(os, "UTF-8"));
            writer.write(query);
            writer.flush();
            writer.close();
            os.close();

            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String returnString="";
            ArrayList<String> output=new ArrayList<String>();
            while ((returnString = in.readLine()) != null)
            {
                Log.d("returnString", returnString);
                output.add(returnString);
            }
            for (int i = 0; i < output.size(); i++) {
                String line=output.get(i);
                String [] words=line.split("\\s\\|\\s");
                if(!words[0].isEmpty()){
                    txtServed.setText(words[0]);
                }
                if(!words[1].isEmpty()){
                    txtCancelled.setText(words[1]);
                }
                if(!words[2].isEmpty()){
                    docQueue.setText(words[2]);
                }
                if(!words[3].isEmpty()){
                    deptQueue.setText(words[3]);
                }
            }

            in.close();


        }
        catch (Exception ex)
        {
            ex.printStackTrace();
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
                    createPdf();
                    emailNote();
                    z="Report Generated";

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


            URL url = new URL("https://isproj2a.benilde.edu.ph/Sympl/GeneratePDFDataAdminServlet");
            URLConnection connection = url.openConnection();

            connection.setReadTimeout(10000);
            connection.setConnectTimeout(15000);
            connection.setDoInput(true);
            connection.setDoOutput(true);



            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String returnString="";

            while ((returnString = in.readLine()) != null)
            {
                Log.d("returnString", returnString);
                data.add(returnString);
            }

            in.close();


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