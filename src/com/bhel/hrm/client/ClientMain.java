package com.bhel.hrm.client;

import com.bhel.hrm.model.Employee;
import com.bhel.hrm.remote.HRMService;

import java.io.Serializable;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.time.LocalDate;
import java.util.Scanner;
import java.util.UUID;

public class ClientMain {
    public static void main(String[] args) {
        try {
            // Optional: set truststore if using SSL
            // System.setProperty("javax.net.ssl.trustStore", "clienttruststore.jks");
            // System.setProperty("javax.net.ssl.trustStorePassword", "changeit");

            Registry reg = LocateRegistry.getRegistry("localhost", 1099);
            HRMService svc = (HRMService) reg.lookup("HRMService");

            Scanner sc = new Scanner(System.in);
            System.out.println("Minimal HRM client. Commands: register, get, apply, report, exit");
            while (true) {
                System.out.print("> ");
                String cmd = sc.next();
                if ("register".equalsIgnoreCase(cmd)) {
                    System.out.print("IC: ");
                    String ic = sc.next();
                    System.out.print("First: ");
                    String fn = sc.next();
                    System.out.print("Last: ");
                    String ln = sc.next();
                    Employee e = new Employee(ic, fn, ln);
                    boolean ok = svc.registerEmployee(e);
                    System.out.println(ok ? "Registered" : "Already exists");
                } else if ("get".equalsIgnoreCase(cmd)) {
                    System.out.print("IC: ");
                    String ic = sc.next();
                    Employee e = svc.getEmployee(ic);
                    System.out.println(e == null ? "Not found" : e.getFirstName() + " " + e.getLastName() + " leave=" + e.getLeaveBalance());
                } else if ("apply".equalsIgnoreCase(cmd)) {
                    System.out.print("IC: ");
                    String ic = sc.next();
                    System.out.print("Days: ");
                    int d = sc.nextInt();
                    LeaveApplication la = svc.applyLeave(ic, d);
                    System.out.println(la == null ? "Employee not found" : "Status: " + la.getStatus());
                } else if ("report".equalsIgnoreCase(cmd)) {
                    System.out.print("IC: ");
                    String ic = sc.next();
                    String r = svc.generateYearlyReport(ic);
                    System.out.println(r);
                } else if ("exit".equalsIgnoreCase(cmd)) {
                    break;
                } else {
                    System.out.println("unknown");
                }
            }
            sc.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
package com.bhel.hrm.model;;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.UUID;

public class LeaveApplication implements Serializable {
    private static final long serialVersionUID = 1L;

    public enum Status {PENDING, APPROVED, REJECTED}

    private final String id = UUID.randomUUID().toString();
    private final LocalDate appliedOn = LocalDate.now();
    private final int days;
    private Status status = Status.PENDING;

        public LeaveApplication(int days) {
        this.days = days;
    }plication(int days) {
        this.days = days;
    }

    public String getId() {
        return id;
    }

    public LocalDate getAppliedOn() {
        return appliedOn;
    }

    public int getDays() {
        return days;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }
}

