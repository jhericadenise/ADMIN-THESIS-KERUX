package com.kerux.admin_thesis_kerux.session;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class KeruxSession {
    private final SharedPreferences prefs;

    public KeruxSession(Context cntx) {
        // TODO Auto-generated constructor stub
        prefs = PreferenceManager.getDefaultSharedPreferences(cntx);
    }

    public void setusername(String username) {

        prefs.edit().putString("username", username).commit();
    }

    public String getusername() {
        String username = prefs.getString("username","");
        return username;
    }

    public void setemail(String email) {

        prefs.edit().putString("email", email).commit();
    }

    public String getemail() {
        String email = prefs.getString("email","");
        return email;
    }

    public void setadminid(String adminid) {
        prefs.edit().putString("adminid", adminid).commit();
    }

    public String getadminid() {
        String adminid = prefs.getString("adminid","");
        return adminid;
    }

    public String getdeptid() {
        String deptid = prefs.getString("deptid","");
        return deptid;
    }

    public String getdocid() {
        String docid = prefs.getString("docid","");
        return docid;
    }

    public void setfirstname(String firstname) {
        prefs.edit().putString("firstname", firstname).commit();
    }

    public String getfirstname() {
        String firstname = prefs.getString("firstname","");
        return firstname;
    }

    public void setlastname(String lastname) {
        prefs.edit().putString("lastname", lastname).commit();
    }

    public String getlastname() {
        String lastname = prefs.getString("lastname","");
        return lastname;
    }

    public void setclinicid(String clinicid){
        prefs.edit().putString("clinicid", clinicid).commit();
    }

    public String getclinicid() {
        String clinicid = prefs.getString("clinicid","");
        return clinicid;
    }

    public String getTableName(){
        String tableName = prefs.getString("tableName", "");
        return  tableName;
    }

    public void setTableName(String tableName){
        prefs.edit().putString("tableName", tableName).commit();
    }
    public String getAuditID(){
        String auditID = prefs.getString("Log_ID", "");
        return auditID;
    }

    public void setAuditID(String auditID){
        prefs.edit().putString("Log_ID", auditID).commit();
    }

    public String getQMCount(){
        String QMCount = prefs.getString("QMCount", "");
        return  QMCount;
    }

    public void setQMCount(String QMCount){
        prefs.edit().putString("QMCount", QMCount).commit();
    }

    public String getDepCount(){
        String DepCount = prefs.getString("DepCount", "");
        return  DepCount;
    }

    public void setDepCount(String DepCount){
        prefs.edit().putString("DepCount", DepCount).commit();
    }

}
