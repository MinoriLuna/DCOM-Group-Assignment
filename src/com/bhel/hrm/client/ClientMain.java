package com.bhel.hrm.client;

import com.bhel.hrm.model.Employee;
import com.bhel.hrm.model.LeaveApplication;
import com.bhel.hrm.remote.HRMService;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Map;
import java.util.Scanner;
import javax.rmi.ssl.SslRMIClientSocketFactory;

public class ClientMain {
    public static void main(String[] args) {
        try {
            // Setting SSL Properties
            System.setProperty("javax.net.ssl.trustStore", "keystore.jks");
            System.setProperty("javax.net.ssl.trustStorePassword", "password123");

            // Connect to Registry
            // SslRMIClientSocketFactory so we speak SSL to the server.
            Registry reg = LocateRegistry.getRegistry(
                    "localhost",
                    1099,
                    new SslRMIClientSocketFactory()
            );

            // Lookup Service
            // Grab the "HRMService" object from the registry so we can use it.
            HRMService svc = (HRMService) reg.lookup("HRMService");

            Scanner sc = new Scanner(System.in);
            System.out.println("--- BHEL HRM Distributed System ---");
            System.out.println("Commands: register, get, update, family, apply, report, exit");

            // Loop for main CLI
            while (true) {
                System.out.print("\nCommand > ");
                String cmd = sc.next();

                // Register
                if ("register".equalsIgnoreCase(cmd)) {
                    System.out.print("IC Number: ");
                    String ic = sc.next();
                    System.out.print("First Name: ");
                    String fn = sc.next();
                    System.out.print("Last Name: ");
                    String ln = sc.next();

                    //Validation response
                    Employee e = new Employee(ic, fn, ln);
                    boolean ok = svc.registerEmployee(e);
                    System.out.println(ok ? ">> Success: Registered." : ">> Error: Already exists.");

                    // Get details of employee
                } else if ("get".equalsIgnoreCase(cmd)) {
                    System.out.print("IC Number: ");
                    String ic = sc.next();
                    Employee e = svc.getEmployee(ic);

                    if (e == null) {
                        System.out.println(">> Not found.");
                    } else {
                        System.out.println(">> Found: " + e.getFirstName() + " " + e.getLastName());
                        System.out.println("   Leave Balance: " + e.getLeaveBalance());

                        // --- NEW PRETTY PRINTING CODE ---
                        Map<String, String> family = e.getFamilyDetails();
                        if (family.isEmpty()) {
                            System.out.println("   Family Details: (None)");
                        } else {
                            System.out.println("   Family Details:");
                            // Loop through every family member and print them on a new line
                            for (Map.Entry<String, String> entry : family.entrySet()) {
                                System.out.println("     - " + entry.getKey() + ": " + entry.getValue());
                            }
                        }
                    }

                    // Update
                } else if ("update".equalsIgnoreCase(cmd)) {
                    System.out.print("IC Number to update: ");
                    String ic = sc.next();
                    Employee e = svc.getEmployee(ic);
                    //Validation check
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
                    // Update family map inside the employee object
                    System.out.print("IC Number: ");
                    String ic = sc.next();
                    Employee e = svc.getEmployee(ic);

                    if (e == null) {
                        System.out.println(">> Employee not found.");
                    } else {
                        // Ask for family details
                        System.out.print("Relationship (e.g. Spouse/Mother): ");
                        String relation = sc.next();

                        System.out.print("Name: ");
                        String name = sc.next();

                        // Update the map inside the Employee object
                        Map<String, String> details = e.getFamilyDetails();
                        details.put(relation, name);
                        e.setFamilyDetails(details);

                        svc.updateProfile(e);
                        System.out.println(">> Family details updated.");
                    }

                    // Approve leave
                } else if ("apply".equalsIgnoreCase(cmd)) {
                    System.out.print("IC Number: ");
                    String ic = sc.next();

                    System.out.print("Days to apply: ");
                    int d = sc.nextInt();

                    LeaveApplication la = svc.applyLeave(ic, d);
                    if (la == null) System.out.println(">> Employee not found.");
                    else System.out.println(">> Application Status: " + la.getStatus());

                    // Get the report from server and print it
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