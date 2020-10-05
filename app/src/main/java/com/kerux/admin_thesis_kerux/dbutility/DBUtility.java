package com.kerux.admin_thesis_kerux.dbutility;

public interface DBUtility {
    String jdbcDriverName = "com.mysql.jdbc.Driver";//vxcd9lOiVlb9DcyuaKAzLr5qD7AQB+5gr7zwfl1MXhY=
    String jdbcUrl ="jdbc:mysql://192.168.1.11/keruxdb";//jdbc:mysql://192.168.1.1/keruxdb
    String dbUserName = "user";//user//o9gPQILs8mlgWTtuaBMBFA==
    String dbPassword = "admin";//admin//oCeOPEBYh4uhgDL4d2Q/8g==

    String SELECT_LIST_DEPT = "select distinct Name from department WHERE Status = 'Active'";
    String SELECT_LIST_DOC = "select distinct Name from doctor WHERE Status = 'Active'";
    String SELECT_LIST_QM = "select distinct Name from queuemanager WHERE Status = 'Active'";
    String SELECT_ACCOUNTS_LIST = "select distinct name from patient WHERE Status = 'Active'";
    String SELECT_BLOCKED_USERS = "select distinct name from patient WHERE Status = 'Blocked'";
    String SELECT_ADMIN_LOGIN = "select admin_id, username, password from admin where username = ? AND password = ?";

    String INSERT_DOCTOR = "insert into doctor (Name, DoctorType_ID, Department_ID, " +
            "RoomNo, Schedule1, Schedule2, Days, Status) values " +
            "(?,?,?,?,?,?,?,?)";
    String INSERT_DEPT = "insert into department (Name, Clinic_ID, Status) values (?,?,?)";
    String INSERT_QM = "insert into queuemanager (Clinic_ID, Department_ID, Username, " +
            "Password, FirstName, LastName, Email, Status) values (?,?,?,?,?,?,?,?)";

    String VALIDATION_DEPT = "Select * from department where name = ? AND Status = 'Active' AND Clinic_ID = ?";
    String VALIDATION_DOCTOR = "Select * from doctor where name = ? AND Status = 'Active'";
    String VALIDATION_QM = "Select * from queuemanager where name = ? AND Status = 'Active'";

    String UNENROLL_QM = "UPDATE queuemanager SET Status = 'Unenrolled' WHERE Name = ?";
    String UNENROLL_DOCTOR = "UPDATE doctor SET Status = 'Unenrolled' WHERE Name = ?";
    String UNENROLL_DEPT = "UPDATE department SET Status = 'Unenrolled' WHERE Name = ?";
    String BLOCK_PRIVILEGES = "UPDATE patient SET Status = 'Blocked' WHERE Name = ?";
}
