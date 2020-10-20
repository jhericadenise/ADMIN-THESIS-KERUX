package com.kerux.admin_thesis_kerux.dbutility;

public interface DBUtility {
    String jdbcDriverName = "com.mysql.jdbc.Driver";//vxcd9lOiVlb9DcyuaKAzLr5qD7AQB+5gr7zwfl1MXhY=
    String jdbcUrl ="jdbc:mysql://192.168.1.13/keruxdb";//jdbc:mysql://192.168.1.1/keruxdb
    String dbUserName = "user";//user//o9gPQILs8mlgWTtuaBMBFA==
    String dbPassword = "admin";//admin//oCeOPEBYh4uhgDL4d2Q/8g==

/*    String jdbcDriverName = "com.mysql.jdbc.Driver";
    String jdbcUrl ="jdbc:mysql://10.70.0.17/keruxdbupdate";
    String dbUserName = "KeruxAdmin";
    String dbPassword = "admin";*/

    String SELECT_LIST_DEPT = "SELECT clinic.clinicName, department.Name, department.Status from clinic " +
            "INNER JOIN department ON " +
            "clinic.Clinic_ID = department.Clinic_ID WHERE department.Status = 'Active'";
            /*"select department.Name, department.Status from department" +
            "INNER JOIN clinic clinic.clinicName ON department.Clinic_ID = clinic.Clinic_ID";*/
    String SELECT_CLINIC_NAME = "select clinicName from clinic WHERE Status = 'Active'";
    String SELECT_LIST_DOC = "select FirstName, LastName from doctor WHERE Status = 'Active'";
    String SELECT_LIST_QM = "select FirstName, LastName from queuemanager WHERE Status = 'Active'";
    String SELECT_ACCOUNTS_LIST = "select distinct FirstName, LastName from patient WHERE Status = 'Active'";
    String SELECT_BLOCKED_USERS = "select distinct FirstName, LastName from patient WHERE Status = 'Blocked'";
    String SELECT_ADMIN_LOGIN = "select admin_id, username, password from admin where username = ? AND password = ?";

    String SELECT_UNENROLLED_DEPT = "SELECT * from department where Status =?";
    String SELECT_UNENROLLED_DOC = "SELECT * from doctor where Status = ?";
    String SELECT_UNENROLLED_QM = "SELECT * from queuemanager where Status = ?";

    String SELECT_DEPT = "select name from department where status = 'Active' AND Clinic_ID = ?";

    String INSERT_DOCTOR = "insert into doctor (DoctorType_ID, Clinic_ID, FirstName, LastName, Department_ID, " +
            "RoomNo, Schedule1, Schedule2, Days, Status) values " +
            "(?,?,?,?,?,?,?,?,?,?)";
    String INSERT_DEPT = "insert into department (Name, Clinic_ID, Status) values (?,?,?)";
    String INSERT_QM = "insert into queuemanager (Clinic_ID, Department_ID, Username, " +
            "Password, FirstName, LastName, Email, Status) values (?,?,?,?,?,?,?,?)";

    String VALIDATION_DEPT = "Select * from department where name = ? AND Status = 'Active' AND Clinic_ID = ?";
    String VALIDATION_DOCTOR = "Select * from doctor where firstName = ? AND lastName = ? AND Status = 'Active'";
    String VALIDATION_QM = "Select * from queuemanager where name = ? AND Status = 'Active'";

    String UNENROLL_QM = "UPDATE queuemanager SET Status = 'Inactive' WHERE FirstName = ?";
    String UNENROLL_DOCTOR = "UPDATE doctor SET Status = 'Inactive' WHERE FirstName = ?";
    String UNENROLL_DEPT = "UPDATE department SET Status = 'Inactive' WHERE Name = ?";
    String BLOCK_PRIVILEGES = "UPDATE patient SET Status = 'Blocked' WHERE FirstName = ?";
}
