package com.bhel.hrm.server;

import com.bhel.hrm.model.Employee;
import com.bhel.hrm.model.LeaveApplication;
import com.bhel.hrm.remote.HRMService;

import java.io.*;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class HRMServiceImpl extends UnicastRemoteObject implements HRMService {
    private static final long serialVersionUID = 1L;
    private static final String FILE_NAME = "hrm_data.ser"; // Persistence file
    private Map<String, Employee> store = new ConcurrentHashMap<>();

    protected HRMServiceImpl() throws RemoteException {
        super();
        loadData(); // Load data on server startup
    }

    // --- Persistence Helper Methods ---
    private void saveData() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(FILE_NAME))) {
            oos.writeObject(store);
            System.out.println("Data saved to disk.");
        } catch (IOException e) {
            System.err.println("Error saving data: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private void loadData() {
        File f = new File(FILE_NAME);
        if (f.exists()) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(f))) {
                store = (Map<String, Employee>) ois.readObject();
                System.out.println("Data loaded from disk. Records: " + store.size());
            } catch (Exception e) {
                System.err.println("Error loading data: " + e.getMessage());
            }
        } else {
            System.out.println("No previous data found. Starting fresh.");
        }
    }

    @Override
    public boolean registerEmployee(Employee emp) throws RemoteException {
        if (emp == null || emp.getIcNumber() == null) return false;
        if (store.putIfAbsent(emp.getIcNumber(), emp) == null) {
            saveData(); // Save on change
            return true;
        }
        return false;
    }

    @Override
    public boolean updateProfile(Employee emp) throws RemoteException {
        if (emp == null || emp.getIcNumber() == null) return false;
        store.put(emp.getIcNumber(), emp);
        saveData(); // Save on change
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

        synchronized (e) {
            if (e.getLeaveBalance() >= days) {
                e.setLeaveBalance(e.getLeaveBalance() - days);
                app.setStatus(LeaveApplication.Status.APPROVED);
            } else {
                app.setStatus(LeaveApplication.Status.REJECTED);
            }
            e.addLeaveApplication(app);
        }
        saveData(); // Save on change
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
        if(e.getFamilyDetails().isEmpty()){
            sb.append(" - No family details recorded.\n");
        } else {
            e.getFamilyDetails().forEach((k,v) -> sb.append(" - ").append(k).append(": ").append(v).append("\n"));
        }
        sb.append("Leave Balance: ").append(e.getLeaveBalance()).append("\n");
        sb.append("Leave History:\n");

        DateTimeFormatter fmt = DateTimeFormatter.ISO_DATE;
        if(e.getLeaveHistory().isEmpty()){
            sb.append("  (No leave taken)\n");
        }
        for (LeaveApplication la : e.getLeaveHistory()) {
            sb.append("  * ").append(la.getAppliedOn().format(fmt))
                    .append(" days=").append(la.getDays())
                    .append(" status=").append(la.getStatus()).append("\n");
        }
        return sb.toString();
    }
}