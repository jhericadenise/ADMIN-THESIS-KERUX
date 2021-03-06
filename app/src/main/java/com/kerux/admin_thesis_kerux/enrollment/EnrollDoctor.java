package com.kerux.admin_thesis_kerux.enrollment;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

import com.kerux.admin_thesis_kerux.R;
import com.kerux.admin_thesis_kerux.dbutility.DBUtility;
import com.kerux.admin_thesis_kerux.edit.EditDoctor;
import com.kerux.admin_thesis_kerux.edit.EditQm;
import com.kerux.admin_thesis_kerux.navigation.EditProfile;
import com.kerux.admin_thesis_kerux.navigation.EnrollmentPage;
import com.kerux.admin_thesis_kerux.navigation.MainActivity;
import com.kerux.admin_thesis_kerux.navigation.ManageAccounts;
import com.kerux.admin_thesis_kerux.reports.ViewAuditReportsActivity;
import com.kerux.admin_thesis_kerux.reports.ViewRatingReportsActivity;
import com.kerux.admin_thesis_kerux.reports.ViewStatReportsActivity;
import com.kerux.admin_thesis_kerux.security.Security;
import com.kerux.admin_thesis_kerux.security.SecurityWEB;
import com.kerux.admin_thesis_kerux.session.KeruxSession;
import com.kerux.admin_thesis_kerux.spinner.Downloader;
import com.kerux.admin_thesis_kerux.spinner.DownloaderDocType;
import com.kerux.admin_thesis_kerux.unenrollment.UnenrollDoc;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class EnrollDoctor extends AppCompatActivity implements DBUtility{

    private static final String urlDeptSpinner = "https://isproj2a.benilde.edu.ph/Sympl/departmentSpinnerServlet"; /*10.0.2.2:89*/
    private static final String urlDocTypeSpinner = "https://isproj2a.benilde.edu.ph/Sympl/doctorTypeSpinnerServlet";
    private EditText doctorFName;
    private EditText doctorLName;
    private EditText roomNo;
    private EditText schedule1;
    private EditText schedule2;
    private EditText prcNo;
    private EditText email;
    private EditText docType;
    private CheckBox monday;
    private CheckBox tuesday;
    private CheckBox wednesday;
    private CheckBox thursday;
    private CheckBox friday;
    private CheckBox saturday;
    private Spinner spinnerDocType;
    private Spinner spinnerDep;
    private String uriPhoto;

    KeruxSession session;
    DrawerLayout drawerLayout;

    ImageView docImage;

    private static final int GALLERY_REQUEST_CODE = 105;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enroll_doctor);
        final Context context = this;
        session=new KeruxSession(getApplicationContext());
        uriPhoto="";

        drawerLayout = findViewById(R.id.drawer_layout);
        docImage=findViewById(R.id.docPhoto);
        Button bttnEnrollDoc = findViewById(R.id.bttnEnrollDoc);
        Button bttnAdd = findViewById(R.id.bttnAddDocType);
        doctorFName = findViewById(R.id.txtboxDocFName);
        doctorLName = findViewById(R.id.txtboxDocLName);
        roomNo = findViewById(R.id.txtboxRoomNo);
        schedule1 = findViewById(R.id.txtboxSched1);
        schedule2 = findViewById(R.id.txtboxSched2);
        prcNo = findViewById(R.id.txtboxPRC);
        email = findViewById(R.id.txtboxDocEmail);
        monday = findViewById(R.id.cBoxMon);
        tuesday = findViewById(R.id.cBoxTues);
        wednesday = findViewById(R.id.cBoxWed);
        thursday = findViewById(R.id.cBoxThurs);
        friday = findViewById(R.id.cBoxFriday);
        saturday = findViewById(R.id.cBoxSat);
        spinnerDocType = findViewById(R.id.spinnerDocType);
        spinnerDep = findViewById(R.id.spinnerDepType);

        bttnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // get prompts.xml view
                LayoutInflater li = LayoutInflater.from(context);
                View promptsView = li.inflate(R.layout.activity_enroll_doctor_type, null);

                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);

                // set prompts.xml to alertdialog builder
                alertDialogBuilder.setView(promptsView);

                docType = promptsView.findViewById(R.id.txtboxDoctorType);

                // set dialog message
                alertDialogBuilder
                        .setCancelable(false)
                        .setPositiveButton("ENROLL",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,int id) {
                                        // get user input and set it to result
                                        // edit text
                                        DoEnrollDocType doEnrollDocType = new DoEnrollDocType();
                                        doEnrollDocType.execute();
                                        DownloaderDocType docType = new DownloaderDocType(EnrollDoctor.this, urlDocTypeSpinner, spinnerDocType, "doctor_type", "Choose Doctor Type");
                                        docType.execute();
                                    }
                                })
                        .setNegativeButton("CANCEL",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,int id) {
                                        dialog.cancel();
                                    }
                                });

                // create alert dialog
                AlertDialog alertDialog = alertDialogBuilder.create();
                // show it
                alertDialog.show();
            }
        });

        bttnEnrollDoc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!validateFName() || !validateLName() || !validateRoomNo() || !validateSched1() || !validateSched2()) {
                    confirmInput();
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(EnrollDoctor.this);
                    builder.setMessage("Are you sure you want to Enroll?")
                            .setCancelable(false)
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    EnrollDoctor.DoEnrollDoc doenroll = new EnrollDoctor.DoEnrollDoc();
                                    doenroll.execute();
                                    doctorFName.getText().clear();
                                    doctorLName.getText().clear();
                                    roomNo.getText().clear();
                                    email.getText().clear();
                                    prcNo.getText().clear();
                                    schedule1.getText().clear();
                                    schedule2.getText().clear();
                                }
                            })
                            .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            });
                    AlertDialog alert = builder.create();
                    alert.show();
                }
            }
        });


        docImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent gallery = new Intent(Intent.ACTION_OPEN_DOCUMENT, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(gallery, GALLERY_REQUEST_CODE);

            }
        });

        Downloader dep = new Downloader(EnrollDoctor.this, urlDeptSpinner, spinnerDep, "name", session.getclinicid(), "Choose Department");
        dep.execute();
        DownloaderDocType docType = new DownloaderDocType(EnrollDoctor.this, urlDocTypeSpinner, spinnerDocType, "doctor_type", "Choose Doctor Type");
        docType.execute();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GALLERY_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                Uri contentUri = data.getData();
                String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                String imageFileName = "JPEG_" + timeStamp + "." + getFileExt(contentUri);
                Log.d("tag", "onActivityResult: Gallery Image Uri: " + imageFileName);
                docImage.setImageURI(contentUri);

//                File file = new File(contentUri.getPath());//create path from uri
//                final String[] split = file.getPath().split(":");//split the path.
//                uriPhoto = split[0];//assign it to a string(your choice).

//                    Cursor cursor = this.getContentResolver().query(contentUri, new String[] {MediaStore.Images.Media.DATA}, null, null, null);
//                    cursor.moveToFirst();
//                    uriPhoto=cursor.getString(0);
//                    cursor.close();

//               String path = getImageRealPath(getContentResolver(), contentUri, null);
                uriPhoto=contentUri.toString();
            }
        }
    }

    private String getImageRealPath(ContentResolver contentResolver, Uri uri, String whereClause) {
        String ret = "";

        // Query the uri with condition.
        Cursor cursor = contentResolver.query(uri, null, whereClause, null, null);

        if (cursor != null) {
            boolean moveToFirst = cursor.moveToFirst();
            if (moveToFirst) {

                // Get columns name by uri type.
                String columnName = MediaStore.Images.ImageColumns.DATA;
                Log.d("UriTest: ", "uri: " + uri);
                if (uri == MediaStore.Images.Media.EXTERNAL_CONTENT_URI) {
                    columnName = MediaStore.Images.Media.DATA;
                }


                // Get column index.
                int imageColumnIndex = cursor.getColumnIndex(columnName);

                // Get column value which is the uri related file local path.
                ret = cursor.getString(imageColumnIndex);
            }
        }

        return ret;
    }

    private String getFileExt(Uri contentUri){
        ContentResolver c =getContentResolver();
        MimeTypeMap mime =MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(c.getType(contentUri));
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
        //Recreate activity
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
        MainActivity.redirectActivity(this, ViewAuditReportsActivity.class);
    }

    public void ClickEditQM(View view){
        MainActivity.redirectActivity(this, EditQm.class);
    }

    public void ClickEditDoctor(View view){
        MainActivity.redirectActivity(this, EditDoctor.class);
    }

    public void ClickViewRating(View view){
        MainActivity.redirectActivity(this, ViewRatingReportsActivity.class);
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

    public void goBack() {
        Intent intent = new Intent(this, EnrollmentPage.class);
        startActivity(intent);
    }

    private boolean validateFName() {
        String firstname = doctorFName.getText().toString().trim();

        if(firstname.isEmpty()){
            doctorFName.setError("Field can't be empty");
            return false;
        } else if (firstname.length() < 3){
            doctorFName.setError("First Name too short");
            return false;
        } else if(firstname.matches("^[0-9]+$")){
            doctorFName.setError("First name cannot contain number values");
            return false;
        } else {
            doctorFName.setError(null);
            return true;
        }
    }

    private boolean validateLName() {
        String lastname = doctorLName.getText().toString().trim();

        if(lastname.isEmpty()){
            doctorLName.setError("Field can't be empty");
            return false;
        } else if (lastname.length() < 2){
            doctorLName.setError("Last Name too short");
            return false;
        } else if(lastname.matches("^[0-9]+$")){
            doctorLName.setError("Last name cannot contain number values");
            return false;
        }  else {
            doctorLName.setError(null);
            return true;
        }
    }

    private boolean validateSched1() {
        String sched1 = schedule1.getText().toString().trim();

        if(sched1.isEmpty()){
            schedule1.setError("Field can't be empty");
            return false;
        } else {
            schedule1.setError(null);
            return true;
        }
    }

    private boolean validateSched2() {
        String sched2 = schedule2.getText().toString().trim();

       if(sched2.isEmpty()){
            schedule2.setError("Field can't be empty");
            return false;
        } else {
            schedule2.setError(null);
            return true;
        }
    }

    private boolean validateRoomNo() {
        String room = roomNo.getText().toString().trim();

        if(room.isEmpty()){
            roomNo.setError("Field can't be empty");
            return false;
        } else {
            roomNo.setError(null);
            return true;
        }
    }

    public boolean confirmInput() {
        String input = "First Name: " + doctorFName.getText().toString();
        input += "\n";
        input += "Last Name: " + doctorLName.getText().toString();
        input += "Schedule: " + schedule1.getText().toString();
        input += "Room No: " + roomNo.getText().toString();
        Toast.makeText(this, input, Toast.LENGTH_SHORT).show();
        return false;
    }

    public static String uploadFile(String actionURL, String[] filePaths) {

        String towHyphens = "--";   // Define connection strings
        String boundary = "******"; // Define the delimitation string
        String end = "\r\n";    //Define end newline string
        try {
            // Create URL objects
            URL url = new URL(actionURL);
            // Get the connection object
            URLConnection urlConnection = url.openConnection();
            // Set the input stream allowed to input data to the local machine
            urlConnection.setDoOutput(true);
            // Setting allows output streams to output data to servers
            urlConnection.setDoInput(true);
            // Setting up not to use caching
            urlConnection.setUseCaches(false);
            // Set the content type in the request parameter to be multipart/form-data and the demarcation line of the request content to be ******
            urlConnection.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);

            // Getting the output stream from the connection object
            OutputStream outputStream = urlConnection.getOutputStream();
            // Instantiate the data output stream object to input the output stream
            DataOutputStream dataOutputStream = new DataOutputStream(outputStream);

            // Traversing the length of the file path, writing all the files of the path under the path array to the output stream
            for (int i = 0; i < filePaths.length; i++) {
                // Remove file path
                String filePath = filePaths[i];
                // Get the file name
                String fileName = filePath.substring(filePath.lastIndexOf("/") + 1);
                // Write a partitioner to the data output stream
                dataOutputStream.writeBytes(towHyphens + boundary + end);
                // Write the file parameter name and file name to the data output stream
                dataOutputStream.writeBytes("Content-Disposition:form-data;name=file;filename=" + fileName + end);
                // Write the end flag to the data output stream
                dataOutputStream.writeBytes(end);

                // Instantiate file input stream objects, pass file paths in, and read files on disk into memory
                FileInputStream fileInputStream = new FileInputStream(filePath);
                // Define buffer size
                int bufferSize = 1024;
                // Define byte array objects to read buffer data
                byte[] buffer = new byte[bufferSize];
                // Define an integer to store the length of the file currently read
                int length;
                // The loop reads 1024 bytes of data from the file output stream and assigns the length of each read to the length variable until the file is read, with the value of -1 ending the loop.
                while ((length = fileInputStream.read(buffer)) != -1) {
                    // Write data to the data output stream
                    dataOutputStream.write(buffer, 0, length);
                }
                // Each time a complete file stream is written, an end flag is written to the data output stream.
                dataOutputStream.writeBytes(end);
                // Close File Input Stream
                fileInputStream.close();

            }
            // Write a delimiter into the data output stream
            dataOutputStream.writeBytes(towHyphens + boundary + towHyphens + end);
            // Refresh data output stream
            dataOutputStream.flush();

            // Getting byte input stream from connection object
            InputStream inputStream = urlConnection.getInputStream();
            // Instantiate character input stream objects, wrapping byte streams into character streams
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            // Create an input buffer object that passes in the input character stream object
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

            // Create a string object to receive each string read from the input buffer
            String line;
            // Create a variable string object to load the final data of the buffer object, and store all the data in the response in the object using string addition
            StringBuilder stringBuilder = new StringBuilder();
            // Read the buffer data line by line using a loop, and assign one line of string data to the line string variable each time until the read behavior space-time identifies the end of the loop.
            while ((line = bufferedReader.readLine()) != null) {
                // Append the data read by the buffer to the variable character object
                stringBuilder.append(line);
            }

            // Close the open input stream in turn
            bufferedReader.close();
            inputStreamReader.close();
            inputStream.close();

            // Close the open output stream in turn
            dataOutputStream.close();
            outputStream.close();

            // Returns the data that the server responds to
            return stringBuilder.toString();

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    //method for enrolling a doctor into the database
    private class DoEnrollDoc extends AsyncTask<String, String, String> {

        Security sec = new Security();
        SecurityWEB secw = new SecurityWEB();
        boolean isSuccess = false;
        boolean hasRecord = false;
        String message = "";
        int reason = 0;
        String docFName = doctorFName.getText().toString();
        String docLName = doctorLName.getText().toString();
        String roomNum = roomNo.getText().toString();
        String sched1 = schedule1.getText().toString();
        String sched2 = schedule2.getText().toString();
        String cboxMon = monday.getText().toString();
        String cboxTues = tuesday.getText().toString();
        String cboxWed = wednesday.getText().toString();
        String cboxThurs = thursday.getText().toString();
        String cboxFri = friday.getText().toString();
        String cboxSat = saturday.getText().toString();
        String docDays="";
        String newdocid;
        String docEmail = email.getText().toString();
        int docPRC = Integer.parseInt(prcNo.getText().toString());
        int docType = (int)spinnerDocType.getSelectedItemId();
        int dept = (int)spinnerDep.getSelectedItemId();
        int clinic = Integer.parseInt(session.getclinicid());
        String status = "Active";

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            try {

                if(cboxMon!=null){
                    docDays+=cboxMon;
                }
                if(cboxTues!=null){
                    docDays+=cboxTues;
                }
                if(cboxWed!=null){
                    docDays+=cboxWed;
                }
                if(cboxThurs!=null){
                    docDays+=cboxThurs;
                }
                if(cboxFri!=null){
                    docDays+=cboxFri;
                }
                if(cboxSat!=null){
                    docDays+=cboxSat;
                }

                URL url = new URL("https://isproj2a.benilde.edu.ph/Sympl/DoEnrollDoctor");
                URLConnection connection = url.openConnection();

                connection.setReadTimeout(10000);
                connection.setConnectTimeout(15000);
                connection.setDoInput(true);
                connection.setDoOutput(true);

                Uri.Builder builder = new Uri.Builder()
                        .appendQueryParameter("docFName", docFName)
                        .appendQueryParameter("docLName", docLName)
                        .appendQueryParameter("clinic", Integer.toString(clinic))
                        .appendQueryParameter("dept", Integer.toString(dept))
                        .appendQueryParameter("docType",  Integer.toString(docType))
                        .appendQueryParameter("reason", Integer.toString(reason))
                        .appendQueryParameter("roomNum", roomNum)
                        .appendQueryParameter("sched1", sched1)
                        .appendQueryParameter("sched2", sched2)
                        .appendQueryParameter("docDays", docDays)
                        .appendQueryParameter("status", status)
                        .appendQueryParameter("prclicense", Integer.toString(docPRC))
                        .appendQueryParameter("email", docEmail)
                        .appendQueryParameter("photo", uriPhoto)
                        .appendQueryParameter("getadminid", session.getadminid())
                        .appendQueryParameter("getclinicid", session.getclinicid());
                String query = builder.build().getEncodedQuery();

                OutputStream os = connection.getOutputStream();
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(os, StandardCharsets.UTF_8));
                writer.write(query);
                writer.flush();
                writer.close();
                os.close();

                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String returnString="";
                ArrayList<String> output=new ArrayList<String>();
                while ((returnString = in.readLine()) != null)
                {
                    isSuccess=true;
                    Log.d("returnString", returnString);
                    output.add(returnString);
                }
                for (int i = 0; i < output.size(); i++) {
                    String line=output.get(i);
                    String notnullline = line.replaceAll("null", "0");
                    String [] words=notnullline.split("\\s\\|\\s");
                    message=words[0];
                    if(!words[1].isEmpty()){
                        newdocid=words[1];
                    }
                }
                in.close();
                insertAudit();
            }catch(Exception e){
                message="Exceptions"+e;
            }

            return message;
        }

        @Override
        protected void onPostExecute(String s) {
            Toast.makeText(getBaseContext(), "" + message, Toast.LENGTH_LONG).show();

            if(isSuccess) {
                try{
                    Toast.makeText(getBaseContext(),""+message,Toast.LENGTH_LONG).show();
                }catch(Exception e){
                    Log.d("insertAudit", e.getMessage());
                }

            }

        }

        public void insertAudit(){

            try {
                URL url = new URL("https://isproj2a.benilde.edu.ph/Sympl/InsertAuditAdminServlet");
                URLConnection connection = url.openConnection();

                connection.setReadTimeout(10000);
                connection.setConnectTimeout(15000);
                connection.setDoInput(true);
                connection.setDoOutput(true);

                Uri.Builder builder = new Uri.Builder()
                        .appendQueryParameter("first", SecurityWEB.encrypt("Doctor Enrollment").trim())
                        .appendQueryParameter("second", SecurityWEB.encrypt("Insert").trim())
                        .appendQueryParameter("third", SecurityWEB.encrypt("Insert doctor record").trim())
                        .appendQueryParameter("fourth", SecurityWEB.encrypt("none").trim())
                        .appendQueryParameter("fifth", SecurityWEB.encrypt("Doctor ID: " + newdocid).trim())
                        .appendQueryParameter("sixth", SecurityWEB.encrypt(session.getusername()).trim());
                String query = builder.build().getEncodedQuery();

                OutputStream os = connection.getOutputStream();
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(os, StandardCharsets.UTF_8));
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
                in.close();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private class DoEnrollDocType extends AsyncTask<String, String, String> {

        Security sec = new Security();
        boolean isSuccess = false;
        String message = "";
        String enrollDoctorType = docType.getText().toString();
        boolean hasRecord = false;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }
        @Override
        protected String doInBackground(String... params) {

            try {
                URL url = new URL("https://isproj2a.benilde.edu.ph/Sympl/DoEnrollDocType");
                URLConnection connection = url.openConnection();

                connection.setReadTimeout(10000);
                connection.setConnectTimeout(15000);
                connection.setDoInput(true);
                connection.setDoOutput(true);

                Uri.Builder builder = new Uri.Builder()
                        .appendQueryParameter("enrollDoctorType", enrollDoctorType);
                String query = builder.build().getEncodedQuery();

                OutputStream os = connection.getOutputStream();
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(os, StandardCharsets.UTF_8));
                writer.write(query);
                writer.flush();
                writer.close();
                os.close();

                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String returnString="";
                ArrayList<String> output=new ArrayList<String>();
                while ((returnString = in.readLine()) != null)
                {
                    isSuccess=true;
                    Log.d("returnString", returnString);
                    output.add(returnString);
                }
                for (int i = 0; i < output.size(); i++) {
                    message=output.get(i);

                }
                in.close();
            }catch(Exception e){
                message="Exceptions"+e;
            }

            return message;
        }

        @Override
        protected void onPostExecute(String s) {

            Toast.makeText(getBaseContext(), "" + message, Toast.LENGTH_LONG).show();

            if (isSuccess) {
               /* Intent intent = new Intent(EnrollDoctor.this, EnrollDoctor.class);
                startActivity(intent);*/

                Toast.makeText(getBaseContext(), "Enrolled Successfully", Toast.LENGTH_LONG).show();
            }

        }



    }
}
