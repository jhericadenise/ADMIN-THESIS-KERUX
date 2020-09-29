package com.kerux.admin_thesis_kerux.enrollment;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.kerux.admin_thesis_kerux.navigation.EnrollmentPage;
import com.kerux.admin_thesis_kerux.navigation.MainActivity;
import com.kerux.admin_thesis_kerux.navigation.ManageAccounts;
import com.kerux.admin_thesis_kerux.R;
import com.kerux.admin_thesis_kerux.security.Security;
import com.kerux.admin_thesis_kerux.dbutility.ConnectionClass;
import com.kerux.admin_thesis_kerux.dbutility.DBUtility;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class EnrollQM extends AppCompatActivity implements DBUtility {

    private EditText qmName;
    private EditText qmUname;
    private EditText qmPw;
    private ListView listQM;
    private ListAdapter listAdapter;
    Button qmDisplayList;
    Button generatePass;
    ConnectionClass connectionClass;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enroll_qm);
        connectionClass = new ConnectionClass(); //create ConnectionClass

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.nav_view);
        navigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.navigation_dashboard:
                        Intent a = new Intent(EnrollQM.this, MainActivity.class);
                        startActivity(a);
                        break;
                    case R.id.navigation_enrollment:
                        Intent b = new Intent(EnrollQM.this, EnrollmentPage.class);
                        startActivity(b);
                        break;
                    case R.id.navigation_accounts:
                        Intent c = new Intent(EnrollQM.this, ManageAccounts.class);
                        startActivity(c);
                        break;
                }
                return false;
            }
        });

        Button bttnBack = findViewById(R.id.bttnQMgoback);
        Button bttnEnrollQM = findViewById(R.id.bttnEnrollQM);
        bttnBack.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                goBack();
            }
        });

        bttnEnrollQM.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EnrollQM.DoEnrollQM doenroll = new DoEnrollQM();
                doenroll.execute();
                qmName.getText().clear();
                qmUname.getText().clear();
                qmPw.getText().clear();
            }
        });

        qmDisplayList = (Button)findViewById(R.id.bttnDisplayQM);
        qmDisplayList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EnrollQM.ListQM qmListDisp = new EnrollQM.ListQM();
                qmListDisp.execute();
            }
        });

        qmName = (EditText) findViewById(R.id.txtboxQMname);
        qmUname = (EditText) findViewById(R.id.txtboxQMun);
        qmPw = (EditText) findViewById(R.id.txtboxQMpw);
        listQM = (ListView) findViewById(R.id.listEnrolledQm);
        listQM.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                final String selectedFromList = String.valueOf((listQM.getItemAtPosition(position)));
                Toast.makeText(getApplicationContext(),"You selected: "+selectedFromList,Toast.LENGTH_LONG).show();
                //Dialog box, for unenrolling
                AlertDialog.Builder builder = new AlertDialog.Builder(EnrollQM.this);
                builder.setMessage("Unenroll Queue Manager?")
                        .setCancelable(false)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                String name = selectedFromList.substring(3, selectedFromList.length()-1);

                                Toast.makeText(getApplicationContext(),"Deleted",Toast.LENGTH_LONG).show();
                                unenrollQM(name);
                                EnrollQM.ListQM qmListDisp = new EnrollQM.ListQM();
                                qmListDisp.execute();
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
        generatePass = findViewById(R.id.bttnGenerate);
        generatePass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                qmPw.setText(generateString(12));
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

    //deleting a record in the database
    public void unenrollQM(String name){

        Connection con = connectionClass.CONN();
        PreparedStatement ps = null;
        try {
            ps = con.prepareStatement(UNENROLL_QM);
            ps.setString(1, name);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    //go back to the previous page
    public void goBack() {
        Intent intent = new Intent(this, EnrollmentPage.class);
        startActivity(intent);
    }

    //Enrolling or adding the record to the database for the queue manager
    private class DoEnrollQM extends AsyncTask<String, String, String> {

        Security sec = new Security();
        boolean isSuccess = false;
        String message = "";
        String QMname = qmName.getText().toString();
        String QMuname = qmUname.getText().toString();
        String QMpw = qmPw.getText().toString();
        String status = "Active";
        boolean hasRecord = false;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            Connection con = connectionClass.CONN();
            PreparedStatement ps = null;
            try {
                ps = con.prepareStatement(VALIDATION_QM);
                ps.setString(1, QMname);

                ResultSet rs = ps.executeQuery();

                if (rs.next()) {
                    hasRecord = true;
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            message = "Record already exists";

            if (QMname.trim().equals("") || QMuname.trim().equals("") || QMpw.trim().equals("") ||  !QMname.matches("^[A-Za-z]+$")) {
                message = "Please enter all fields....";

            }
            else if (!QMname.matches("^[A-Za-z]+$")) {
                message = "Check format";
            }
            else if (hasRecord){
                message = "Record already exists";
            }
            else {
                try {
                    if (con == null) {
                        message = "Unsuccessful";
                    } else {
                        String query = INSERT_QM;
                        PreparedStatement ps1 = con.prepareStatement(query);
                        ps1.setString(1, sec.encrypt(QMuname));
                        ps1.setString(2, String.valueOf(QMpw));
                        ps1.setString(3, QMname);
                        ps1.setString(4, status);

                        ps1.execute();
                        con.close();

                        message = "ADDED";
                    }
                } catch (Exception ex) {
                    isSuccess = false;
                    message = "Exceptions" + ex;
                }
            }
            return message;
        }

        @Override
        protected void onPostExecute(String s) {
            Toast.makeText(getBaseContext(), "" + message, Toast.LENGTH_LONG).show();

            if (isSuccess) {
                Intent intent = new Intent(EnrollQM.this, EnrollQM.class);
                startActivity(intent);
            }

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
                    datanum.put("A", rset.getString(1).toString());
                    data.add(datanum);
                }

                String[] fromwhere = {"A"};
                int[] viewswhere = {R.id.lblQMList};
                listAdapter = new SimpleAdapter(EnrollQM.this, data,
                        R.layout.list_qm_template, fromwhere, viewswhere);

                while (rset.next()) {
                    result += rset.getString(1).toString() + "\n";
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
            listQM.setAdapter(listAdapter);
        }
    }
}
