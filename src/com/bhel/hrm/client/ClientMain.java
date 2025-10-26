package com.bhel.hrm.client;

import com.bhel.hrm.model.Employee;
import com.bhel.hrm.model.LeaveApplication;
import com.bhel.hrm.remote.HRMService;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Scanner;

public class ClientMain {
    public static void main(String[] args) {
        try {
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