# DCOM-Group-Assignment (minimal RMI base)

Functional requirements (what the system must do)

HR client must be able to:

Authenticate/login as HR.
Register a new employee with: First Name, Last Name, IC/Passport number (unique).
Update an employee profile (name, contact fields).
Add / update employee family details.
View an employee’s leave balance and leave history.
Generate a yearly report for an employee (profile + family details + leave history) and view it as text.
(Optional minimal) Approve or reject leave requests.
Employee client must be able to:

Authenticate/login as Employee.
View and update own profile (first name, last name, contact).
Add / update own family details.
View current leave balance.
Apply for leave (specify dates or number of days).
Check status of own leave application (PENDING / APPROVED / REJECTED).
Shared server functionality (RMI service) must:

Expose remote methods for all the client actions above (registerEmployee, updateProfile, getEmployee, applyLeave, getLeaveStatus, getLeaveHistory, generateYearlyReport, login).
Validate inputs and return success/failure results with clear messages or codes.
Persist all data (employees, family details, users, leaves) to a centralized database.
Persistence / database requirements

Use an embedded file-based DB (H2) or chosen DB.
Required tables: users, employees, family_members (or key/value in employees), leaves.
Data must persist across server restarts.
Security requirements

Require authentication for both HR and Employee operations (username/password).
At minimum: server-side authentication check for each request.
For minimal deliverable SSL/TLS is optional; for higher marks:
Secure RMI transport using SSL/TLS (keystore/truststore) or recommend TLS for future.
Store passwords securely (recommend hashing — bcrypt) — plain text acceptable for minimal demo but note limitation in report.
Concurrency / fault-tolerance requirements

Server must handle concurrent client calls (RMI concurrent requests).
Use thread-safe structures or DB transactions to avoid race conditions on leave balance updates.
Persisted data must allow recovery after server crash/restart.
API / RMI contract requirements (example minimal remote methods)

boolean login(String username, String password)
boolean registerEmployee(Employee emp)
boolean updateProfile(Employee emp)
Employee getEmployee(String icNumber)
LeaveApplication applyLeave(String icNumber, int days)
LeaveApplication getLeaveStatus(String requestId) or getLeaveHistory(String icNumber)
String generateYearlyReport(String icNumber)
DTO / model requirements

Employee (Serializable): ic/passport, firstName, lastName, leaveBalance, familyDetails, leaveHistory
LeaveApplication (Serializable): id, appliedOn, days, status
User (Serializable or DB row): username, password, role (HR / EMPLOYEE)

SSL : password123