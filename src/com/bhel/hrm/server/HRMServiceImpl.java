package com.bhel.hrm.server;

import com.bhel.hrm.model.Employee;
import com.bhel.hrm.model.LeaveApplication;
import com.bhel.hrm.remote.HRMService;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class HRMServiceImpl extends UnicastRemoteObject implements HRMService {
    private static final long serialVersionUID = 1L;
    private final Map<String, Employee> store = new ConcurrentHashMap<>(); // thread-safe

    protected HRMServiceImpl() throws RemoteException {
        super(); // ...export with defaults...
    }

    @Override
    public boolean registerEmployee(Employee emp) throws RemoteException {
        if (emp == null || emp.getIcNumber() == null) return false;
        return store.putIfAbsent(emp.getIcNumber(), emp) == null;
    }

    @Override
    public boolean updateProfile(Employee emp) throws RemoteException {
        if (emp == null || emp.getIcNumber() == null) return false;
        store.put(emp.getIcNumber(), emp);
        return true;
    }

    @Override
    public Employee getEmployee(String icNumber) throws RemoteException {
        return store.get(icNumber);
    }

    @Override
    public LeaveApplication applyLeave(String icNumber, int days) throws RemoteException {
        Employee e = store.get(icNumber);
        if (e == null) return null;
        LeaveApplication app = new LeaveApplication(days);
        // very simple policy: auto-approve if balance sufficient
        synchronized (e) {
            if (e.getLeaveBalance() >= days) {
                e.setLeaveBalance(e.getLeaveBalance() - days);
                app.setStatus(LeaveApplication.Status.APPROVED);
            } else {
                app.setStatus(LeaveApplication.Status.REJECTED);
            }
            e.addLeaveApplication(app);
        }
        return app;
    }

    @Override
    public List<LeaveApplication> getLeaveHistory(String icNumber) throws RemoteException {
        Employee e = store.get(icNumber);
        return e != null ? e.getLeaveHistory() : null;
    }

    @Override
    public String generateYearlyReport(String icNumber) throws RemoteException {
        Employee e = store.get(icNumber);
        if (e == null) return "Employee not found";
        StringBuilder sb = new StringBuilder();
        sb.append("Yearly Report for ").append(e.getFirstName()).append(" ").append(e.getLastName()).append("\n");
        sb.append("IC: ").append(e.getIcNumber()).append("\n");
        sb.append("Family Details:\n");
        e.getFamilyDetails().forEach((k,v) -> sb.append(" - ").append(k).append(": ").append(v).append("\n"));
        sb.append("Leave Balance: ").append(e.getLeaveBalance()).append("\n");
        sb.append("Leave History:\n");
        DateTimeFormatter fmt = DateTimeFormatter.ISO_DATE;
        for (LeaveApplication la : e.getLeaveHistory()) {
            sb.append("  * ").append(la.getAppliedOn().format(fmt))
              .append(" days=").append(la.getDays())
              .append(" status=").append(la.getStatus()).append("\n");
        }
        return sb.toString();
    }
}

