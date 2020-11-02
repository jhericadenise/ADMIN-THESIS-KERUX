package com.kerux.admin_thesis_kerux.dbutility;
import android.annotation.SuppressLint;
import android.os.StrictMode;
import android.util.Log;

import com.kerux.admin_thesis_kerux.security.Security;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectionClass implements DBUtility{

    String driverName = DBUtility.jdbcDriverName;
    String url= jdbcUrl;
    String un= DBUtility.dbUserName;
    String password= DBUtility.dbPassword;

    @SuppressLint("NewApi")
    public Connection CONN(){
        StrictMode.ThreadPolicy policy=new StrictMode.ThreadPolicy.Builder()
                .permitAll().build();
        StrictMode.setThreadPolicy(policy);
        Security sec=new Security();

        Connection conn = null;
        String ConnURL = null;
        try{
            Class.forName(driverName);
            conn= DriverManager.getConnection(url,un,password);
        }catch(SQLException se){
            Log.e("ERROR", se.getMessage());
        }catch(ClassNotFoundException e){
            Log.e("ERROR", e.getMessage());
        }catch(Exception e){
            Log.e("ERROR", e.getMessage());
        }
        return conn;
    }
}