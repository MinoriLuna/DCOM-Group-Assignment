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

    // We use this file to save our data. It acts like a simple database.
    // If we restart the server, we can read this file to get our data back.
    private static final String FILE_NAME = "hrm_data.ser";

    // This map holds all the employee data in memory while the server is running.
    // We use 'ConcurrentHashMap' because it's safer when multiple people use the system at once.
    private Map<String, Employee> store = new ConcurrentHashMap<>();

    protected HRMServiceImpl() throws RemoteException {
        // SECURITY UPDATE:
        // The '0' lets the system pick a random port.
        // The important part is the SSL factories—this forces the connection to be encrypted.
        super(0, new SslRMIClientSocketFactory(), new SslRMIServerSocketFactory());

        // When the server starts, try to load any existing data from the file.
        loadData();
    }

    // --- Helper Methods to Save and Load Data ---

    private void saveData() {
        // This method saves the entire 'store' map to a file on your hard drive.
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(FILE_NAME))) {
            oos.writeObject(store);
            System.out.println("[Server] Data saved to disk.");
        } catch (IOException e) {
            System.err.println("[Server] Error saving data: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private void loadData() {
        // This method checks if the file exists and reads the data back into memory.
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

    // --- The Real Features (Business Logic) ---

    @Override
    public boolean registerEmployee(Employee emp) throws RemoteException {
        // Check if the input is valid
        if (emp == null || emp.getIcNumber() == null) return false;

        // Try to add the employee.
        // putIfAbsent returns null if the key (IC Number) didn't exist before (Success).
        // If it returns something, it means that ID is already taken.
        if (store.putIfAbsent(emp.getIcNumber(), emp) == null) {
            saveData(); // Save changes immediately
            return true;
        }
        return false; // Registration failed (Duplicate)
    }

    @Override
    public boolean updateProfile(Employee emp) throws RemoteException {
        if (emp == null || emp.getIcNumber() == null) return false;

        // Make sure the employee actually exists before we update them
        if (!store.containsKey(emp.getIcNumber())) return false;

        store.put(emp.getIcNumber(), emp);
        saveData(); // Save changes
        return true;
    }

    @Override
    public Employee getEmployee(String icNumber) throws RemoteException {
        // Simple lookup from the map
        return store.get(icNumber);
    }

    @Override
    public LeaveApplication applyLeave(String icNumber, int days) throws RemoteException {
        Employee e = store.get(icNumber);
        if (e == null) return null; // Employee doesn't exist

        LeaveApplication app = new LeaveApplication(days);

        // THREAD SAFETY:
        // We use 'synchronized' here. This locks the employee object for a split second.
        // It prevents two requests from subtracting leave balance at the exact same time.
        synchronized (e) {
            if (e.getLeaveBalance() >= days) {
                e.setLeaveBalance(e.getLeaveBalance() - days);
                app.setStatus(LeaveApplication.Status.APPROVED);
            } else {
                app.setStatus(LeaveApplication.Status.REJECTED);
            }
            e.addLeaveApplication(app);
        }
        saveData(); // Save the new balance
        return app;
    }

    @Override
    public List<LeaveApplication> getLeaveHistory(String icNumber) throws RemoteException {
        Employee e = store.get(icNumber);
        // If employee exists, return their history list. Otherwise return null.
        return e != null ? e.getLeaveHistory() : null;
    }

    @Override
    public String generateYearlyReport(String icNumber) throws RemoteException {
        // This creates a nice text report to send back to the client.
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