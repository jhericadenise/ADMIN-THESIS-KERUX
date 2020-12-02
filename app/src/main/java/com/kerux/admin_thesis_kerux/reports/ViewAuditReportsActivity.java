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
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.drawerlayout.widget.DrawerLayout;

import com.github.mikephil.charting.BuildConfig;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;
import com.kerux.admin_thesis_kerux.R;
import com.kerux.admin_thesis_kerux.dbutility.ConnectionClass;
import com.kerux.admin_thesis_kerux.dbutility.DBUtility;
import com.kerux.admin_thesis_kerux.edit.EditDoctor;
import com.kerux.admin_thesis_kerux.edit.EditQm;
import com.kerux.admin_thesis_kerux.navigation.EditProfile;
import com.kerux.admin_thesis_kerux.navigation.EnrollmentPage;
import com.kerux.admin_thesis_kerux.navigation.MainActivity;
import com.kerux.admin_thesis_kerux.navigation.ManageAccounts;
import com.kerux.admin_thesis_kerux.security.SecurityWEB;
import com.kerux.admin_thesis_kerux.session.KeruxSession;
import com.kerux.admin_thesis_kerux.unenrollment.UnenrollDoc;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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

    KeruxSession session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_audit_reports);
        connectionClass = new ConnectionClass (); //create ConnectionClass
        session=new KeruxSession(getApplicationContext());

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

    public void ClickEditQM(View view){
        MainActivity.redirectActivity(this, EditQm.class);
    }

    public void ClickEditDoctor(View view){
        MainActivity.redirectActivity(this, EditDoctor.class);
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

        SecurityWEB secw = new SecurityWEB();

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Toast.makeText(ViewAuditReportsActivity.this, "Please wait...", Toast.LENGTH_SHORT).show();
        }

        @Override
        protected String doInBackground(String... params) {
            try {
                listAudit = findViewById(R.id.listAuditReports);
                URL url = new URL("https://isproj2a.benilde.edu.ph/Sympl/ListAuditAdminServlet");
                URLConnection connection = url.openConnection();

                connection.setReadTimeout(10000);
                connection.setConnectTimeout(15000);
                connection.setDoInput(true);


                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String returnString="";
                StringBuffer receivedData=new StringBuffer();
                ArrayList<String> output=new ArrayList<String>();

                while ((returnString = in.readLine()) != null)
                {

                    output.add(returnString);
                }
                for (int i = 0; i < output.size(); i++) {
                    try{
                        message = SecurityWEB.decrypt(output.get(i)).trim();//SecurityWEB dapat

                    }catch (Exception e){
                        Log.d("ErrorNOww", e.getMessage());
                        message = output.get(i);
                    }
                    Log.d("OUTPUT>GET", output.get(i));
                    receivedData.append(message+"\n");
                }
                in.close();
                String retrieved=receivedData.toString();
                Log.d("STRRRING", retrieved);
                List<Map<String, String>> data= new ArrayList<Map<String, String>>();

                data= (new Gson()).fromJson(retrieved, new TypeToken<List<Map<String, String>>>() {}.getType());

                listAdapter = new SimpleAdapter (ViewAuditReportsActivity.this, data,
                        R.layout.listview_row_audit, new String[] {"first", "second", "third"},
                        new int[] {R.id.FIRST_COL, R.id.SECOND_COL, R.id.THIRD_COL});

                message = "Audit Log";


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
                createPdf();
                /*viewPdf();*/
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
        document.add(new Paragraph(timeStamp));
        document.add(new Paragraph("Report generated by: " + " " + session.getusername()));
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


            URL url = new URL("https://isproj2a.benilde.edu.ph/Sympl/AuditReportAdminServlet");
            URLConnection connection = url.openConnection();

            connection.setReadTimeout(10000);
            connection.setConnectTimeout(15000);
            connection.setDoInput(true);



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