package com.bhel.hrm.remote;

import com.bhel.hrm.model.Employee;
import com.bhel.hrm.model.LeaveApplication;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface HRMService extends Remote {
    boolean registerEmployee(Employee emp) throws RemoteException;
    boolean updateProfile(Employee emp) throws RemoteException;
    Employee getEmployee(String icNumber) throws RemoteException;
    LeaveApplication applyLeave(String icNumber, int days) throws RemoteException;
    List<LeaveApplication> getLeaveHistory(String icNumber) throws RemoteException;
    String generateYearlyReport(String icNumber) throws RemoteException; // returns plain text report
    String generatePayslip(String ic) throws RemoteException;
}

