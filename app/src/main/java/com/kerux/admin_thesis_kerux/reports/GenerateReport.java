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
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;
import com.kerux.admin_thesis_kerux.BuildConfig;
import com.kerux.admin_thesis_kerux.R;
import com.kerux.admin_thesis_kerux.dbutility.ConnectionClass;
import com.kerux.admin_thesis_kerux.dbutility.DBUtility;
import com.kerux.admin_thesis_kerux.security.Security;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class GenerateReport extends AppCompatActivity implements DBUtility {

    private String tableName;
    private String auditID;
    private Button generateReport;
    File myFile;
    ProgressDialog progressDialog;
    ConnectionClass connectionClass;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_generate_report);

        progressDialog=new ProgressDialog(this);
        connectionClass =new ConnectionClass();

        Intent i= getIntent();
        tableName=i.getStringExtra("TableName");
        auditID=i.getStringExtra("Log_ID");

        generateReport=(Button)findViewById(R.id.btnGenerateReport);
        generateReport.setOnClickListener(new View.OnClickListener() {//
            @Override
            public void onClick(View v) {
                GenerateRep generateRep = new GenerateRep();
                generateRep.execute();
            }
        });

    }

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
        document.add(new Paragraph("KERUX QUEUE REPORT"));
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
        Uri uri = FileProvider.getUriForFile(GenerateReport.this,
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
        Uri uri = FileProvider.getUriForFile(GenerateReport.this,
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

//    private void promptForNextAction()
//    {
//        final String[] options = { getString(R.string.label_email), getString(R.string.label_preview),
//                getString(R.string.label_cancel) };
//
//        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
//        builder.setTitle("Note Saved, What Next?");
//        builder.setItems(options, new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                if (options[which].equals(getString(R.string.label_email))){
//                    emailNote();
//                }else if (options[which].equals(getString(R.string.label_preview))){
//                    viewPdf();
//                }else if (options[which].equals(getString(R.string.label_cancel))){
//                    dialog.dismiss();
//                }
//            }
//        });
//
//        builder.show();
//
//    }
}