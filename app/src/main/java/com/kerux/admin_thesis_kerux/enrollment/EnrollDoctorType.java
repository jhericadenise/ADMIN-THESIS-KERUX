package com.kerux.admin_thesis_kerux.enrollment;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.kerux.admin_thesis_kerux.R;
import com.kerux.admin_thesis_kerux.dbutility.ConnectionClass;
import com.kerux.admin_thesis_kerux.dbutility.DBUtility;
import com.kerux.admin_thesis_kerux.security.Security;
import com.kerux.admin_thesis_kerux.session.KeruxSession;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class EnrollDoctorType extends AppCompatActivity implements DBUtility {

    private EditText docType;
    private Button bttnEnrollDocType;

    KeruxSession session;
    ConnectionClass connectionClass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enroll_doctor_type);
        connectionClass = new ConnectionClass();

        docType = findViewById(R.id.txtboxDoctorType);

     /*   bttnEnrollDocType.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DoEnrollDocType doEnrollDocType = new DoEnrollDocType();
                doEnrollDocType.execute();
                Intent intent = new Intent(EnrollDoctorType.this, EnrollDoctor.class);
                startActivity(intent);
            }
        });*/

    }


    private boolean validateDocType() {
        String doctorType = docType.getText().toString().trim();
        Connection con = connectionClass.CONN();
        boolean hasRecord = false;
        PreparedStatement ps = null;

        try {
            ps = con.prepareStatement(VALIDATION_DOC_TYPE);
            ps.setString(1, doctorType);

            ResultSet rs=ps.executeQuery();

            if(rs.next()) {
                if (doctorType.isEmpty()) {
                    docType.setError("Field can't be empty");
                    return false;
                } else if (doctorType.length() < 2) {
                    docType.setError("Last Name too short");
                    return false;
                } else if (hasRecord) {
                    docType.setError("Record Already Exists");
                } else {
                    docType.setError(null);
                    return true;
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return hasRecord;
    }

    //Enrolling or adding the record to the database for the queue manager
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
            Connection con = connectionClass.CONN();
          /*  PreparedStatement ps = null;
            try {
                ps = con.prepareStatement(VALIDATION_DOC_TYPE);
                ps.setString(1, enrollDoctorType);

                ResultSet rs=ps.executeQuery();

                if(rs.next()){
                    hasRecord = true;
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

            message = "Record already exists";
*/

            if (enrollDoctorType.trim().equals("")) {
                message = "Please enter all fields";
            }
            else if (hasRecord){
                message = "Record already exists";
            }
            else {
                try {
                    if (con == null) {
                        message = "CANNOT ADD RECORD";

                    } else {
                        //inserting data of department to the database
                        String query = INSERT_DOC_TYPE_ENROLLMENT;
                        PreparedStatement ps1 = null;
                        try {
                            ps1 = con.prepareStatement(query);
                            ps1.setString(1, enrollDoctorType);
                            ps1.executeUpdate();
                            message = "Added Successfully!";
                        } catch (SQLException throwables) {
                            throwables.printStackTrace();
                        }
                        con.close();
                    }
                }
                catch (Exception ex)
                {
                    isSuccess = false;
                    message = "Exceptions"+ex;
                    Log.d("ex", ex.getMessage () + " Jheca");
                }
            }
            return message;
        }

        @Override
        protected void onPostExecute(String s) {

            Toast.makeText(getBaseContext(), "" + message, Toast.LENGTH_LONG).show();

            if (isSuccess) {
                Intent intent = new Intent(EnrollDoctorType.this, EnrollDoctor.class);
                startActivity(intent);
            }

        }

    }
}