package com.kerux.admin_thesis_kerux.dbutility;

public interface DBUtility {

    String jdbcDriverName = "com.mysql.jdbc.Driver";//vxcd9lOiVlb9DcyuaKAzLr5qD7AQB+5gr7zwfl1MXhY=
    String jdbcUrl ="jdbc:mysql://192.168.1.13/kerux-db";//jdbc:mysql://192.168.1.1/keruxdb
    String dbUserName = "user";//user//o9gPQILs8mlgWTtuaBMBFA==
    String dbPassword = "admin";//admin//oCeOPEBYh4uhgDL4d2Q/8g==

/*  String jdbcDriverName = "com.mysql.jdbc.Driver";
    String jdbcUrl ="jdbc:mysql://10.70.0.17/keruxdbupdate";
    String dbUserName = "KeruxAdmin";
    String dbPassword = "admin";*/

    //LIST VIEW DISPLAY
    //Department
    String SELECT_LIST_DEPT = "SELECT clinic.clinicName, department.Name, department.Status from clinic " +
            "INNER JOIN department ON clinic.Clinic_ID = department.Clinic_ID WHERE department.Status = 'Active'";
    //Doctor
    String SELECT_LIST_DOC = "select clinic.clinicName, department.Name, doctor.FirstName, doctor.LastName " +
            "from clinic INNER JOIN doctor ON clinic.Clinic_ID = doctor.Clinic_ID " +
            "INNER JOIN department ON department.Department_ID = doctor.Department_ID WHERE doctor.Status = 'Active'";
    //Queue Manager
    String SELECT_LIST_QM = "SELECT department.Name, queuemanager.FirstName, queuemanager.LastName from department INNER JOIN queuemanager ON department.Department_ID = queuemanager.Department_ID WHERE queuemanager.Status = 'Active'";
    //Accounts Patient
    String SELECT_ACCOUNTS_LIST = "select patient_type.Type, patient.ContactNo, patient.FirstName, patient.LastName, patient.Email from patient_type " +
            "INNER JOIN patient ON patient_type.PatientType_ID = patient.PatientType_ID WHERE patient.Status = 'Active'";
    //Blocked Patients
    String SELECT_BLOCKED_USERS = "select FirstName, LastName, Status from patient WHERE Status = 'Blocked'";

    //Audit Log
    String SELECT_AUDIT = "SELECT Log_ID, TableName, EventType, SqlCommand, OldData, NewData, LoginName, TimeStamp from audit_log";

    //For Logging in
    String SELECT_ADMIN_LOGIN = "SELECT admin.Admin_ID, admin.FirstName, admin.LastName, admin.Email, admin_enrollment.Clinic_ID, admin.Username from admin " +
            "INNER JOIN admin_enrollment ON admin.Admin_ID = admin_enrollment.Admin_ID WHERE admin.Username =? and admin.Password=?";

    //LIST VIEW DISPLAY UNENROLLED USERS
    String SELECT_UNENROLLED_DEPT = "SELECT * from department where Status =?";
    String SELECT_UNENROLLED_DOC = "SELECT * from doctor where Status = ?";
    String SELECT_UNENROLLED_QM = "SELECT * from queuemanager where Status = ?";

    //INSERTING RECORDS
    //Doctor
    String INSERT_DOCTOR = "INSERT INTO doctor (DoctorType_ID, Clinic_ID, reasonrevoke_id, FirstName, LastName, Department_ID, " +
            "RoomNo, Schedule1, Schedule2, Days, Status) values " +
            "(?,?,?,?,?,?,?,?,?,?,?)";
    //Department
    String INSERT_DEPT = "INSERT INTO department (Clinic_ID, ReasonRevoke_ID, Name, Status) values (?,?,?,?)";
    //Queue Manager
    String INSERT_QM = "INSERT INTO queuemanager (Clinic_ID, Department_ID, reasonrevoke_id, Username, " +
            "Password, FirstName, LastName, Email, Status) values (?,?,?,?,?,?,?,?,?)";

    String SELECT_NEW_DEPARTMENT_ID = "Select MAX(department_id) from department";
    String SELECT_NEW_DOCTOR_ID = "Select MAX(doctor_id) from doctor";
    String SELECT_NEW_QUEUEMANAGER_ID = "Select MAX(queuemanager_id) from queuemanager";

    //INSERTING RECORDS IN ENROLLMENT
    String INSERT_DEPT_ENROLLMENT = "INSERT INTO department_enrollment (Admin_ID, Department_ID, Clinic_ID) values (?,?,?)";
    String INSERT_QM_ENROLLMENT = "INSERT INTO qmenrollment (QueueManager_ID, Admin_ID, Department_ID, Clinic_ID) values (?,?,?,?)";
    String INSERT_DOC_ENROLLMENT = "INSERT INTO doctor_enrollment (Admin_ID, Clinic_ID, Department_ID, Doctor_ID) values (?,?,?,?)";
    String INSERT_DOC_TYPE_ENROLLMENT = "INSERT INTO doctor_type (DoctorType) values (?)";
    String INSERT_REASON = "INSERT INTO reason_revoke (Reason, TableName) values (?,?)";

    //INSERTING DATAS IN AUDIT
    String INSERT_AUDIT_LOG = "INSERT INTO audit_log (TableName, EventType, SqlCommand, OldData, NewData, LoginName)" +
            "values (?,?,?,?,?,?)";

    //VALIDATION
    String VALIDATION_DEPT = "SELECT * FROM department WHERE Name = ? AND Status = 'Active' AND Clinic_ID = ?";
    String VALIDATION_DOCTOR = "SELECT * FROM doctor WHERE firstName = ? AND lastName = ? AND Status = 'Active' AND Clinic_ID = ? AND Department_ID = ?";
    String VALIDATION_QM = "SELECT * FROM queuemanager WHERE Username = ? AND FirstName = ? AND LastName = ? AND Status = 'Active' AND Clinic_ID = ?";
    String VALIDATION_DOC_TYPE = "SELECT * FROM doctor_type WHERE DoctorType = ?";

    //UNENROLLING RECORDS
    String UNENROLL_QM = "UPDATE queuemanager SET Status = 'Inactive' WHERE FirstName = ?";
    String UNENROLL_DOCTOR = "UPDATE doctor SET Status = 'Inactive' WHERE FirstName = ?";
    String UNENROLL_DEPT = "UPDATE department SET Status = 'Inactive' WHERE Name = ?";
    String BLOCK_PRIVILEGES = "UPDATE patient SET Status = 'Blocked' WHERE FirstName = ?";
    //REASON
    String UNENROLL_DOC_REASON = "UPDATE doctor SET doctor.ReasonRevoke_ID = (SELECT reason_revoke.reasonrevoke_id " +
            "FROM reason_revoke WHERE reason_revoke.reason=? ) WHERE doctor.FirstName = ?";
    String UNENROLL_DEPT_REASON = "UPDATE department SET department.ReasonRevoke_ID = (SELECT reason_revoke.reasonrevoke_id " +
            "FROM reason_revoke WHERE reason_revoke.reason=? ) WHERE department.Name = ?";
    String UNENROLL_QM_REASON = "UPDATE queuemanager SET queuemanager.Reasonrevoke_id=(SELECT reason_revoke.reasonrevoke_id " +
            "FROM reason_revoke WHERE reason_revoke.reason=? ) WHERE queuemanager.firstname = ?";
    String BLOCK_ACC_REASON = "UPDATE queuemanager SET queuemanager.Reasonrevoke_id=(SELECT reason_revoke.reasonrevoke_id " +
            "FROM reason_revoke WHERE reason_revoke.reason=? ) WHERE queuemanager.firstname = ?";

    //UPDATE ADMIN PROFILE
    String UPDATE_PROFILE="UPDATE admin SET FirstName = ?, LastName = ?, Email = ?, Username = ?, Password = ? WHERE Admin_ID = ?";

    //sql statement for edit profile to not require the input of password when just editing basic patient information
    String UPDATE_PROFILE_PASS = "UPDATE admin SET password = ?";

    //sql statement to compare the "old password" the patient has inputted in the text field from the one in the database
    String CONFIRM_ADMIN_PASS = "SELECT Password FROM admin where Password = ?";

    //COUNTING RECORDS
    String TOTAL_NUM_LOGIN = "SELECT COUNT(TableName) from audit_log WHERE TableName = 'login'";
    String TOTAL_NUM_ENROLLMENT_DEPT = "SELECT COUNT(TableName) from audit_log WHERE TableName = 'department_enrollment'";
    String TOTAL_NUM_ENROLLMENT_DOC = "SELECT COUNT(TableName) from audit_log WHERE TableName = 'doctor_enrollment'";
    String TOTAL_NUM_ENROLLMENT_QM = "SELECT COUNT(TableName) from audit_log WHERE TableName = 'qmenrollment'";
    String TOTAL_NUM_UNENROLL_DEPT = "SELECT COUNT(TableName) from audit_log WHERE TableName = 'qmenrollment'";
    String TOTAL_NUM_UNENROLL_DOC = "SELECT COUNT(TableName) from audit_log WHERE TableName = 'unenroll department'";
    String TOTAL_NUM_UNENROLL_QM = "SELECT COUNT(TableName) from audit_log WHERE TableName = 'unenroll queue manager'";
    String SELECT_AUDIT_LIST = "SELECT TableName, EventType, TimeStamp FROM audit_log";

    //GENERATE STATISTIC
    String QUEUES_SERVED = "SELECT QueuesServed, TimeEnd FROM statistics";
    String QUEUES_CANCELLED = "SELECT QueuesCancelled, TimeEnd FROM statistics";
    String HIGHEST_DOC_QUEUES = "SELECT HighestDocQueues, TimeEnd FROM statistics";
    String HIGHEST_DEPT_QUEUES = "SELECT HighestDeptQueues, TimeEnd FROM statistics";

    String INSERT_STAT = "INSERT INTO statistics (QueuesServed, QueuesCancelled, HighestDocQueues, HighestDeptQueues, TimeStart, TimeEnd) SELECT " +
            "(SELECT COUNT(ql.QueueList_ID) FROM queuelist ql INNER JOIN queue q on q.Queue_ID = ql.Queue_ID INNER JOIN queueconnector qc on qc.Queue_ID = q.Queue_ID INNER JOIN queuemanager qm on qm.QueueManager_ID = qc.QueueManager_ID WHERE qm.Clinic_ID = ? AND ql.Status='Served'), " +
            "(SELECT COUNT(ql.QueueList_ID) FROM queuelist ql INNER JOIN queue q on q.Queue_ID = ql.Queue_ID INNER JOIN queueconnector qc on qc.Queue_ID = q.Queue_ID INNER JOIN queuemanager qm on qm.QueueManager_ID = qc.QueueManager_ID WHERE qm.Clinic_ID = ? AND ql.Status='Cancelled'), " +
            "(SELECT CONCAT(Doctor.FirstName, Doctor.LastName) FROM DOCTOR INNER JOIN queue on queue.Doctor_ID = doctor.Doctor_ID WHERE queue.Queue_ID = (SELECT z.queueid FROM (SELECT w.Queue_ID as queueid, MAX(w.num) FROM (SELECT Queue_ID as Queue_ID, count(*) as num FROM instance GROUP BY Queue_ID) w) z)), " +
            "(SELECT Department.Name FROM department INNER JOIN queue on queue.Department_ID = department.Department_ID WHERE queue.Queue_ID = (SELECT x.queueid FROM (SELECT y.Queue_ID as queueid, MAX(y.num) FROM (SELECT Queue_ID as Queue_ID, count(*) as num FROM instance GROUP BY Queue_ID) y) x)), CURRENT_TIME, CURRENT_TIME";
    String SELECT_STAT="SELECT QueuesServed, QueuesCancelled, HighestDocQueues, HighestDeptQueues from statistics ORDER BY statistics_id desc limit 1";

    //Dashboard count
    String DB_DEPT = "SELECT COUNT(Department_ID) from department WHERE Status = 'Active'";
    String DB_QM = "SELECT COUNT(QueueManager_ID) from queuemanager WHERE Status = 'Active'";
}