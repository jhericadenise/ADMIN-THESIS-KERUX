package com.kerux.admin_thesis_kerux.dbutility;

public interface DBUtility {
    String jdbcDriverName = "vxcd9lOiVlb9DcyuaKAzLr5qD7AQB+5gr7zwfl1MXhY=";
    String jdbcUrl ="jdbc:mysql://192.168.1.13/keruxdb";//jdbc:mysql://192.168.1.1/keruxdb
    String dbUserName = "o9gPQILs8mlgWTtuaBMBFA==";//user
    String dbPassword = "oCeOPEBYh4uhgDL4d2Q/8g==";//admin

    String SELECT_LIST_DEPT = "select distinct Name from department WHERE Status = 'Active'";
    String SELECT_LIST_DOC = "select distinct Name from doctor WHERE Status = 'Active'";
    String SELECT_LIST_QM = "select distinct Name from queuemanager WHERE Status = 'Active'";
    String SELECT_ACCOUNTS_LIST = "select distinct name from patient WHERE Status = 'Active'";
    String SELECT_BLOCKED_USERS = "select distinct name from patient WHERE Status = 'Blocked'";
    String SELECT_ADMIN_LOGIN = "select admin_id, username, password from admin where username = ? AND password = ?";

    String INSERT_DOCTOR = "insert into doctor (Name, DoctorType_ID, Department_ID, RoomNo, Schedule1, Schedule2, Days, Status) values " +
            "(?,?,?,?,?,?,?,?)";
    String INSERT_DEPT = "insert into department (Name, Status) values (?,?)";
    String INSERT_QM = "insert into queuemanager (Username, Password, Name, Status) values (?,?,?,?)";

    String VALIDATION_DEPT = "Select * from department where name = ? AND Status = 'Active'";
    String VALIDATION_DOCTOR = "Select * from doctor where name = ? AND Status = 'Active'";
    String VALIDATION_QM = "Select * from queuemanager where name = ? AND Status = 'Active'";

    String UNENROLL_QM = "UPDATE queuemanager SET Status = 'Unenrolled' WHERE Name = ?";
    String UNENROLL_DOCTOR = "UPDATE doctor SET Status = 'Unenrolled' WHERE Name = ?";
    String UNENROLL_DEPT = "UPDATE department SET Status = 'Unenrolled' WHERE Name = ?";
    String BLOCK_PRIVILEGES = "UPDATE patient SET Status = 'Blocked' WHERE Name = ?";
}
