package com.kerux.admin_thesis_kerux.dbutility;

public interface DBUtility {
    String jdbcDriverName = "com.mysql.jdbc.Driver";//vxcd9lOiVlb9DcyuaKAzLr5qD7AQB+5gr7zwfl1MXhY=
    String jdbcUrl ="jdbc:mysql://192.168.1.13/kerux";//jdbc:mysql://192.168.1.1/keruxdb
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
    String SELECT_LIST_DOC = "SELECT clinic.clinicName, department.Name, doctor.FirstName, doctor.LastName from clinic " +
            "INNER JOIN doctor ON clinic.Clinic_ID = doctor.Clinic_ID " +
            "INNER JOIN department ON department.Department_ID = doctor.Department_ID " +
            "WHERE doctor.Status = 'Active'";
    String SELECT_LIST_QM = "select clinic.clinicName, department.Name, queuemanager.FirstName, queuemanager.LastName from clinic " +
            "INNER JOIN queuemanager ON clinic.Clinic_ID = queuemanager.Clinic_ID " +
            "INNER JOIN department ON department.Department_ID = queuemanager.Department_ID " +
            "WHERE queuemanager.Status = 'Active'";
    String SELECT_ACCOUNTS_LIST = "select distinct FirstName, LastName from patient WHERE Status = 'Active'";
    String SELECT_BLOCKED_USERS = "select distinct FirstName, LastName from patient WHERE Status = 'Blocked'";
    String SELECT_ADMIN_LOGIN = "select admin_id, username, password from admin where username = ? AND password = ?";

    String SELECT_UNENROLLED_DEPT = "SELECT * from department where Status =?";
    String SELECT_UNENROLLED_DOC = "SELECT * from doctor where Status = ?";
    String SELECT_UNENROLLED_QM = "SELECT * from queuemanager where Status = ?";

    String SELECT_DEPT = "select name from department where status = 'Active' AND Clinic_ID = ?";

    String INSERT_DOCTOR = "insert into doctor (DoctorType_ID, Clinic_ID, reasonrevoke_id, FirstName, LastName, Department_ID, " +
            "RoomNo, Schedule1, Schedule2, Days, Status) values " +
            "(?,?,?,?,?,?,?,?,?,?,?)";
    String INSERT_DEPT = "insert into department (Clinic_ID, ReasonRevoke_ID, Name, Status) values (?,?,?,?)";
    String INSERT_QM = "insert into queuemanager (Clinic_ID, Department_ID, Username, " +
            "Password, FirstName, LastName, Email, Status) values (?,?,?,?,?,?,?,?)";

    String INSERT_DEPT_ENROLLMENT = "INSERT INTO department_enrollment (Admin_ID, Department_ID, Clinic_ID) values (?,?,?)";
    String INSERT_QM_ENROLLMENT = "INSERT INTO qmenrollment (QueueManager_ID, Admin_ID, Department_ID, Clinic_ID) values (?,?,?,?)";
    String INSERT_DOC_ENROLLMENT = "INSERT INTO doctor_enrollment (Admin_ID, Clinic_ID, Doctor_ID) values (?,?,?)";

    String INSERT_AUDIT_LOG = "INSERT INTO audit_log (TableName, EventType, SqlCommand, OldData, NewData, LoginName)" +
            "values (?,?,?,?,?,?)";

    String VALIDATION_DEPT = "Select * from department where name = ? AND Status = 'Active' AND Clinic_ID = ?";
    String VALIDATION_DOCTOR = "Select * from doctor where firstName = ? AND lastName = ? AND Status = 'Active'";
    String VALIDATION_QM = "Select * from queuemanager where name = ? AND Status = 'Active'";

    String UNENROLL_QM = "UPDATE queuemanager SET Status = 'Inactive' WHERE FirstName = ?";
    String UNENROLL_DOCTOR = "UPDATE doctor SET Status = 'Inactive' WHERE FirstName = ?";
    String UNENROLL_DOC_REASON = "UPDATE doctor SET doctor.Reasonrevoke_id=(SELECT reason_revoke.reasonrevoke_id " +
            "FROM reason_revoke WHERE reason_revoke.reason=? ) WHERE doctor.firstname=?";
    String UNENROLL_DEPT_REASON = "UPDATE department SET department.ReasonRevoke_ID=(SELECT reason_revoke.reasonrevoke_id " +
            "FROM reason_revoke WHERE reason_revoke.reason=? ) WHERE doctor.firstname=?";
    String UNENROLL_QM_REASON = "UPDATE queuemanager SET queuemanager.Reasonrevoke_id=(SELECT reason_revoke.reasonrevoke_id " +
            "FROM reason_revoke WHERE reason_revoke.reason=? ) WHERE doctor.firstname=?";
    String UNENROLL_DEPT = "UPDATE department SET Status = 'Inactive' WHERE Name = ?";
    String BLOCK_PRIVILEGES = "UPDATE patient SET Status = 'Blocked' WHERE FirstName = ?";

    String EDIT_PROFILE="select email, password, patienttype_id, name, contactno from patient" +
            "where patient_id = ?";
    String UPDATE_PROFILE="update patient set email = ?, password = ?, firstname = ?, lastname = ?, contactno = ? where patient_id =  ?";

    String SELECT_STAT = "SELECT * from statistics";
}