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
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EditQm extends AppCompatActivity implements DBUtility {
    ConnectionClass connectionClass;
    DrawerLayout drawerLayout;
    private ListView qmlist;
    private ListAdapter listAdapter;
    Button bttnEditQM;
    Button bttnDisplayQM;

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
                final String selectedFromList = getQMString(String.valueOf((qmlist.getItemAtPosition(position))));
                Toast.makeText(getApplicationContext(), "You selected: " + selectedFromList, Toast.LENGTH_LONG).show();
                //Dialog box, for unenrolling
                AlertDialog.Builder builder = new AlertDialog.Builder(EditQm.this);
                builder.setMessage("Are you sure you want to edit?")
                        .setCancelable(false)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                Toast.makeText(getApplicationContext(), selectedFromList, Toast.LENGTH_LONG).show();
                                Toast.makeText(getApplicationContext(), "Successfully revoked privilege", Toast.LENGTH_LONG).show();

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

    }

    public String getQMString(String rowFromListView){
        String name = rowFromListView.substring(1, rowFromListView.length()-1);

        String qmString1=name.replaceAll(".*second=", "");


        return qmString1;
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

    public void updateQM(String firstName, String lastName, String email, String password){

        try {
            URL url = new URL("https://isproj2a.benilde.edu.ph/Sympl/UpdateQMServlet");
            URLConnection connection = url.openConnection();

            connection.setReadTimeout(10000);
            connection.setConnectTimeout(15000);
            connection.setDoInput(true);
            connection.setDoOutput(true);

            Uri.Builder builder = new Uri.Builder()
                    .appendQueryParameter("firstName", firstName)
                    .appendQueryParameter("lastName", lastName)
                    .appendQueryParameter("lastName", email)
                    .appendQueryParameter("lastName", password);
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
        } catch (Exception e) {
            e.printStackTrace();
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
                String result = "Database Connection Successful\n";
                Statement st = con.createStatement();
                ResultSet rset = st.executeQuery(SELECT_LIST_QM);
                ResultSetMetaData rsmd = rset.getMetaData();

                List<Map<String, String>> data = null;
                data = new ArrayList<Map<String, String>>();

                while (rset.next()) {
                    Map<String, String> datanum = new HashMap<String, String>();
                    datanum.put("first", rset.getString(1).toString());
                    datanum.put("second", rset.getString(2).toString());
                    datanum.put("third", rset.getString(3).toString());
                    datanum.put("fourth", rset.getString(4).toString());
                    data.add(datanum);
                }

                listAdapter = new SimpleAdapter (EditQm.this, data,
                        R.layout.listview_row, new String[] {"first", "second", "third", "fourth"},
                        new int[] {R.id.FIRST_COL, R.id.SECOND_COL, R.id.THIRD_COL, R.id.FOURTH_COL});
                while (rset.next()) {
                    result += rset.getString(2).toString();
                }
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