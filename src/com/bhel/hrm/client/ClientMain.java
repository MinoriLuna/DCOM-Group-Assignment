package com.bhel.hrm.client;

import com.bhel.hrm.model.Employee;
import com.bhel.hrm.model.LeaveApplication;
import com.bhel.hrm.remote.HRMService;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Map;
import java.util.Scanner;

public class ClientMain {
    public static void main(String[] args) {
        try {
            // Connect to the registry
            Registry reg = LocateRegistry.getRegistry("localhost", 1099);
            HRMService svc = (HRMService) reg.lookup("HRMService");

            Scanner sc = new Scanner(System.in);
            System.out.println("--- BHEL HRM Distributed System ---");
            System.out.println("Commands: register, get, update, family, apply, report, exit");

            while (true) {
                System.out.print("\nCommand > ");
                String cmd = sc.next();

                if ("register".equalsIgnoreCase(cmd)) {
                    System.out.print("IC Number: ");
                    String ic = sc.next();
                    System.out.print("First Name: ");
                    String fn = sc.next();
                    System.out.print("Last Name: ");
                    String ln = sc.next();
                    Employee e = new Employee(ic, fn, ln);
                    boolean ok = svc.registerEmployee(e);
                    System.out.println(ok ? ">> Success: Registered." : ">> Error: Already exists.");

                } else if ("get".equalsIgnoreCase(cmd)) {
                    System.out.print("IC Number: ");
                    String ic = sc.next();
                    Employee e = svc.getEmployee(ic);
                    if (e == null) {
                        System.out.println(">> Not found.");
                    } else {
                        System.out.println(">> Found: " + e.getFirstName() + " " + e.getLastName());
                        System.out.println("   Leave Balance: " + e.getLeaveBalance());
                        System.out.println("   Family Details: " + e.getFamilyDetails());
                    }

                } else if ("update".equalsIgnoreCase(cmd)) {
                    // REQUIREMENT: Update profiles and details
                    System.out.print("IC Number to update: ");
                    String ic = sc.next();
                    Employee e = svc.getEmployee(ic);
                    if (e == null) {
                        System.out.println(">> Employee not found.");
                    } else {
                        System.out.print("New First Name: ");
                        e.setFirstName(sc.next());
                        System.out.print("New Last Name: ");
                        e.setLastName(sc.next());
                        boolean ok = svc.updateProfile(e);
                        System.out.println(ok ? ">> Profile Updated." : ">> Update failed.");
                    }

                } else if ("family".equalsIgnoreCase(cmd)) {
                    // REQUIREMENT: Populate data for Yearly Report
                    System.out.print("IC Number: ");
                    String ic = sc.next();
                    Employee e = svc.getEmployee(ic);
                    if (e == null) {
                        System.out.println(">> Employee not found.");
                    } else {
                        System.out.print("Relationship (e.g., Spouse/Child): ");
                        String relation = sc.next();
                        System.out.print("Name: ");
                        String name = sc.next();

                        Map<String, String> details = e.getFamilyDetails();
                        details.put(relation, name);
                        e.setFamilyDetails(details);

                        svc.updateProfile(e);
                        System.out.println(">> Family details updated.");
                    }

                } else if ("apply".equalsIgnoreCase(cmd)) {
                    System.out.print("IC Number: ");
                    String ic = sc.next();
                    System.out.print("Days to apply: ");
                    int d = sc.nextInt();
                    LeaveApplication la = svc.applyLeave(ic, d);
                    if (la == null) System.out.println(">> Employee not found.");
                    else System.out.println(">> Application Status: " + la.getStatus());

                } else if ("report".equalsIgnoreCase(cmd)) {
                    System.out.print("IC Number: ");
                    String ic = sc.next();
                    String r = svc.generateYearlyReport(ic);
                    System.out.println("---------------- REPORT ----------------");
                    System.out.println(r);
                    System.out.println("----------------------------------------");

                } else if ("exit".equalsIgnoreCase(cmd)) {
                    break;
                } else {
                    System.out.println(">> Unknown command.");
                }
            }
            sc.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}