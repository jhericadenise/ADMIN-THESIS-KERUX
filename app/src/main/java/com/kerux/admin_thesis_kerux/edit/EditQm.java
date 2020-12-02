package com.kerux.admin_thesis_kerux.edit;

import android.content.DialogInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.kerux.admin_thesis_kerux.R;
import com.kerux.admin_thesis_kerux.dbutility.ConnectionClass;
import com.kerux.admin_thesis_kerux.dbutility.DBUtility;
import com.kerux.admin_thesis_kerux.navigation.EditProfile;
import com.kerux.admin_thesis_kerux.navigation.MainActivity;
import com.kerux.admin_thesis_kerux.navigation.ManageAccounts;
import com.kerux.admin_thesis_kerux.reports.ViewAuditReportsActivity;
import com.kerux.admin_thesis_kerux.reports.ViewStatReportsActivity;
import com.kerux.admin_thesis_kerux.unenrollment.UnenrollDoc;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class EditQm extends AppCompatActivity implements DBUtility {
    ConnectionClass connectionClass;
    DrawerLayout drawerLayout;
    private ListView qmlist;
    private ListAdapter listAdapter;
    Button bttnEditQM;
    Button bttnDisplayQM;
    Button bttnGenerate;

    private String qmidfin;
    private String fn;
    private String ln;
    private String email;
    private String pw;

    private EditText qmFirstName;
    private EditText qmLastName;
    private EditText qmEmail;
    private EditText qmPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_qm);
        connectionClass = new ConnectionClass (); //create ConnectionClass
        drawerLayout = findViewById(R.id.drawer_layout);
        qmlist = findViewById(R.id.listQM);
        bttnDisplayQM = findViewById(R.id.bttnDisplayEditQm);
        bttnEditQM = findViewById(R.id.bttnUpdateQM);

        bttnGenerate = findViewById(R.id.bttnGeneratePass3);
        bttnGenerate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                qmPassword.setText(generateString(8));
            }
        });

        qmFirstName=findViewById(R.id.txtboxQMFname3);
        qmLastName=findViewById(R.id.txtboxQmLname3);
        qmEmail=findViewById(R.id.txtboxQMEmail3);
        qmPassword = findViewById(R.id.txtboxQMpw3);
        qmidfin="";

        bttnDisplayQM.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ListQM listQM = new ListQM();
                listQM.execute();
            }
        });

        qmlist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d("QMM",String.valueOf((qmlist.getItemAtPosition(position))));
                final String selectedFromListQMID = getQMID(String.valueOf(qmlist.getItemAtPosition(position)));
                final String selectedFromListE = getQMStringEmail(String.valueOf((qmlist.getItemAtPosition(position))));
                final String selectedFromList = getQMStringLastname(String.valueOf((qmlist.getItemAtPosition(position))));
                final String selectedFromListFN = getQMStringFirstname(String.valueOf((qmlist.getItemAtPosition(position))));

                Toast.makeText(getApplicationContext(), "You selected: " + selectedFromList, Toast.LENGTH_LONG).show();
                //Dialog box, for unenrolling
                AlertDialog.Builder builder = new AlertDialog.Builder(EditQm.this);
                builder.setMessage("Are you sure you want to edit?")
                        .setCancelable(false)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                Toast.makeText(getApplicationContext(), selectedFromList, Toast.LENGTH_LONG).show();
                                qmFirstName.setText(selectedFromListFN);
                                qmLastName.setText(selectedFromList);
                                qmEmail.setText(selectedFromListE);
                                qmidfin=selectedFromListQMID;
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
        });

        bttnEditQM.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fn = qmFirstName.getText().toString().trim();
                ln = qmLastName.getText().toString().trim();
                email = qmEmail.getText().toString().trim();
                pw = qmPassword.getText().toString().trim();
                updateqm updqm = new updateqm();
                updqm.execute();

                qmFirstName.getText().clear();
                qmLastName.getText().clear();
                qmEmail.getText().clear();
                qmPassword.getText().clear();
            }
        });
    }

    private String generateString(int length){
        char[] chars = "QWERTYUIOPASDFGHJKLZXCVBNMmnbvcxzlkjhgfdsapoiuytrewq1234567890!@#$%^&*()".toCharArray();
        StringBuilder stringBuilder = new StringBuilder();

        Random random = new Random();
        for(int i = 0; i < length; i++){
            char c = chars[random.nextInt(chars.length)];
            stringBuilder.append(c);
        }
        return stringBuilder.toString();
    }

    public String getQMStringLastname(String rowFromListView){
        String name = rowFromListView.substring(1, rowFromListView.length()-1);
        String qmString1=name.replaceAll(".*second=", "");
        String qmString2=qmString1.replaceAll(",.*", "");

        return qmString2.trim();
    }

    public String getQMID(String rowFromListView){
        String name = rowFromListView.substring(1, rowFromListView.length()-1);
        String qmString1=name.replaceAll(".*fourth=", "");
        String qmString2=qmString1.replaceAll(",.*", "");

        return qmString2.trim();
    }

    public String getQMStringFirstname(String rowFromListView){
        String name = rowFromListView.substring(1, rowFromListView.length()-1);
        String qmString1=name.replaceAll(".*first=", "");
        String qmString2=qmString1.replaceAll(",.*", "");


        return qmString2.trim();
    }
    public String getQMStringEmail(String rowFromListView){
        String name = rowFromListView.substring(1, rowFromListView.length()-1);
        String qmString1=name.replaceAll(".*third=", "");
        String qmString2=qmString1.replaceAll(",.*", "");

        return qmString2.trim();
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


    private class updateqm extends AsyncTask<String, String, String> {

        String message = "";

        @Override
        protected void onPreExecute() {
            Toast.makeText(getBaseContext(),"Please wait..",Toast.LENGTH_LONG).show();
            super.onPreExecute();
        }
        @Override
        protected String doInBackground(String... strings) {
            try {
                URL url = new URL("https://isproj2a.benilde.edu.ph/Sympl/UpdateQMServlet");
                URLConnection connection = url.openConnection();

                connection.setReadTimeout(10000);
                connection.setConnectTimeout(15000);
                connection.setDoInput(true);
                connection.setDoOutput(true);
                Uri.Builder builder = new Uri.Builder()
                        .appendQueryParameter("QueueManager_ID", qmidfin)
                        .appendQueryParameter("firstname", fn)
                        .appendQueryParameter("lastname", ln)
                        .appendQueryParameter("email", email)
                        .appendQueryParameter("password", pw);
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
                    message=returnString;
                }
                in.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return message;
        }
        @Override
        protected void onPostExecute(String s) {
            Toast.makeText(getBaseContext(),""+s,Toast.LENGTH_LONG).show();
        }
    }


    //Displaying the list of enrolled queue manager in the database
    private class ListQM extends AsyncTask<String, String, String> {

        Connection con = connectionClass.CONN();
        boolean isSuccess = false;
        String message = "";

        @Override
        protected void onPreExecute() {
            Toast.makeText(getBaseContext(),"Please wait..",Toast.LENGTH_LONG).show();
            super.onPreExecute();
        }
        @Override
        protected String doInBackground(String... strings) {
            try {
                //listview, list the names of all enrolled department

                URL url = new URL("https://isproj2a.benilde.edu.ph/Sympl/ListEditQM");
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
                    receivedData.append(returnString+"\n");
                    output.add(returnString);
                }
                for (int i = 0; i < output.size(); i++) {
                    message = output.get(i);
                }
                in.close();
                String retrieved=receivedData.toString();
                List<Map<String, String>> data= new ArrayList<Map<String, String>>();

                data= (new Gson()).fromJson(retrieved, new TypeToken<List<Map<String, String>>>() {}.getType());


                listAdapter = new SimpleAdapter (EditQm.this, data,
                        R.layout.listview_row, new String[] {"first", "second", "third", "fourth"},
                        new int[] {R.id.FIRST_COL, R.id.SECOND_COL, R.id.THIRD_COL, R.id.FOURTH_COL});

                message = "ADDED SUCCESSFULLY!";
            } catch (Exception ex) {
                isSuccess = false;
                message = "Exceptions" + ex;
            }
            return message;
        }
        @Override
        protected void onPostExecute(String s) {
            qmlist.setAdapter(listAdapter);
        }
    }
}