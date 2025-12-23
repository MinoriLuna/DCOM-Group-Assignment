package com.bhel.hrm.server;

import com.bhel.hrm.model.Employee;
import com.bhel.hrm.model.LeaveApplication;
import com.bhel.hrm.remote.HRMService;

import javax.rmi.ssl.SslRMIClientSocketFactory;
import javax.rmi.ssl.SslRMIServerSocketFactory;
import java.io.*;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class HRMServiceImpl extends UnicastRemoteObject implements HRMService {
    private static final long serialVersionUID = 1L;

    // File to simulate the "Centralized Database"
    private static final String FILE_NAME = "hrm_data.ser";

    // Thread-safe map to hold employee data
    private Map<String, Employee> store = new ConcurrentHashMap<>();

    protected HRMServiceImpl() throws RemoteException {
        // --- SECURITY UPDATE: Enable SSL Sockets ---
        // This tells RMI to use your keystore keys for encryption.
        super(0, new SslRMIClientSocketFactory(), new SslRMIServerSocketFactory());

        // Load existing data from disk when server starts
        loadData();
    }

    // --- Persistence Helper Methods ---
    private void saveData() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(FILE_NAME))) {
            oos.writeObject(store);
            System.out.println("[Server] Data saved to disk.");
        } catch (IOException e) {
            System.err.println("[Server] Error saving data: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private void loadData() {
        File f = new File(FILE_NAME);
        if (f.exists()) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(f))) {
                store = (Map<String, Employee>) ois.readObject();
                System.out.println("[Server] Data loaded. Records found: " + store.size());
            } catch (Exception e) {
                System.err.println("[Server] Error loading data: " + e.getMessage());
            }
        } else {
            System.out.println("[Server] No previous data found. Starting fresh.");
        }
    }

    // --- HRMService Implementation ---

    @Override
    public boolean registerEmployee(Employee emp) throws RemoteException {
        if (emp == null || emp.getIcNumber() == null) return false;

        // putIfAbsent returns null if the key was NOT there (success)
        if (store.putIfAbsent(emp.getIcNumber(), emp) == null) {
            saveData(); // Save changes
            return true;
        }
        return false;
    }

    @Override
    public boolean updateProfile(Employee emp) throws RemoteException {
        if (emp == null || emp.getIcNumber() == null) return false;

        // Check if employee exists first
        if (!store.containsKey(emp.getIcNumber())) return false;

        store.put(emp.getIcNumber(), emp);
        saveData(); // Save changes
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

        // Synchronized block to ensure thread safety when modifying balance
        synchronized (e) {
            if (e.getLeaveBalance() >= days) {
                e.setLeaveBalance(e.getLeaveBalance() - days);
                app.setStatus(LeaveApplication.Status.APPROVED);
            } else {
                app.setStatus(LeaveApplication.Status.REJECTED);
            }
            e.addLeaveApplication(app);
        }
        saveData(); // Save changes
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
        sb.append("=== Yearly Report ===\n");
        sb.append("Name: ").append(e.getFirstName()).append(" ").append(e.getLastName()).append("\n");
        sb.append("IC Number: ").append(e.getIcNumber()).append("\n");

        sb.append("Family Details:\n");
        if (e.getFamilyDetails().isEmpty()) {
            sb.append(" - (No records)\n");
        } else {
            e.getFamilyDetails().forEach((relation, name) ->
                    sb.append(" - ").append(relation).append(": ").append(name).append("\n"));
        }

        sb.append("Current Leave Balance: ").append(e.getLeaveBalance()).append("\n");

        sb.append("Leave History:\n");
        DateTimeFormatter fmt = DateTimeFormatter.ISO_DATE;
        if (e.getLeaveHistory().isEmpty()) {
            sb.append("  (No leave taken this year)\n");
        }
        for (LeaveApplication la : e.getLeaveHistory()) {
            sb.append("  * Date: ").append(la.getAppliedOn().format(fmt))
                    .append(" | Days: ").append(la.getDays())
                    .append(" | Status: ").append(la.getStatus()).append("\n");
        }

        return sb.toString();
    }
}