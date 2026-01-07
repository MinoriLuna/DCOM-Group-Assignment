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

    private static Scanner sc = new Scanner(System.in);
    private static String userRole = "";

    public static void main(String[] args) {
        try {
            System.out.println(">> Initializing Client Security Context...");

            // SSL Configuration (TrustStore)
            System.setProperty("javax.net.ssl.trustStore", "keystore.jks");
            System.setProperty("javax.net.ssl.trustStorePassword", "password123");

            // Connect to Registry using SSL
            Registry reg = LocateRegistry.getRegistry(
                    "localhost",
                    1099,
                    new SslRMIClientSocketFactory()
            );

            System.out.println(">> Connecting to Server...");
            long start = System.currentTimeMillis();
            HRMService svc = (HRMService) reg.lookup("HRMService");

            long end = System.currentTimeMillis();
            System.out.println(">> Connection Established! Latency: " + (end - start) + "ms");
            System.out.println("------------------------------------------------");

            // Login Menu
            boolean isLoggedIn = false;

            while (!isLoggedIn) {
                System.out.println("\n=== BHEL SYSTEM LOGIN ===");
                System.out.println("1. HR Manager (Admin Access)");
                System.out.println("2. Employee (Restricted Access)");
                System.out.print("Select Role > ");
                String choice = sc.next();

                if (choice.equals("1")) {
                    System.out.print("Enter Admin Password: ");
                    String pass = sc.next();
                    if (pass.equals("admin123")) {
                        isLoggedIn = true;
                        userRole = "HR";
                        System.out.println(">> LOGIN SUCCESS: Welcome, HR Manager.");
                    } else {
                        System.out.println(">> Error: Wrong Password.");
                    }

                } else if (choice.equals("2")) {
                    System.out.print("Enter your IC Number: ");
                    String ic = sc.next();

                    // Verify if user exists on server
                    Employee e = svc.getEmployee(ic);
                    if (e != null) {
                        isLoggedIn = true;
                        userRole = "Employee";
                        System.out.println(">> LOGIN SUCCESS: Welcome, " + e.getFirstName());
                    } else {
                        System.out.println(">> Error: IC not found. Please contact HR.");
                    }

                } else {
                    System.out.println(">> Invalid option.");
                }
            }

            printMenu();

            // Main Loop
            while (true) {
                System.out.print("\n[" + userRole + "] Command > ");
                String cmd = sc.next().toLowerCase(); // Convert to lowercase

                switch (cmd) {
                    case "register":
                        //Only HR can register
                        if (checkPermission("HR")) handleRegister(svc);
                        break;
                    case "get":
                        handleGet(svc);
                        break;
                    case "update":
                        if (checkPermission("HR")) handleUpdate(svc);
                        break;
                    case "family":
                        handleFamilyUpdate(svc);
                        break;
                    case "payslip":
                        handlePayslip(svc);
                        break;
                    case "apply":
                        handleLeaveApplication(svc);
                        break;
                    case "report":
                        // Only HR can generate full reports
                        if (checkPermission("HR")) handleReport(svc);
                        break;
                    case "menu":
                        printMenu();
                        break;
                    case "exit":
                        System.out.println(">> Closing Secure Session...");
                        System.out.println(">> Goodbye!");
                        System.exit(0);
                    default:
                        System.out.println(">> Unknown command. Type 'menu' to see options.");
                }
            }
        } catch (Exception ex) {
            System.err.println("Client Error: " + ex.getMessage());
            ex.printStackTrace();
        } finally {
            sc.close();
        }
    }

    private static boolean checkPermission(String requiredRole) {
        if (userRole.equals(requiredRole)) {
            return true;
        } else {
            System.out.println(">> ACCESS DENIED: This command is for " + requiredRole + " only.");
            return false;
        }
    }

    private static void printMenu() {
        System.out.println("=========================================");
        System.out.println("      BHEL SECURE HRM SYSTEM (v1.0)      ");
        System.out.println("=========================================");
        System.out.println(" [register] Register New Employee");
        System.out.println(" [apply]    Apply for Leave");
        System.out.println(" [get]      Check Leave Balance / Info");
        System.out.println(" [family]   Update Family Info");
        System.out.println(" [update]   Update Name/Profile");
        System.out.println(" [payslip]  Generate Payslip (PRS)");
        System.out.println(" [report]   Generate Yearly Report");
        System.out.println(" [exit]     Exit System");
        System.out.println("=========================================");
    }

    // Main methods
    private static void handleRegister(HRMService svc) {
        System.out.println("=== New Employee Registration ===");
        String ic = "";

        // Loop until valid IC
        while (true) {
            System.out.print("IC Number: ");
            ic = sc.next();
            if (ic.matches("\\d{12}")) {
                break;
            } else {
                System.out.println(">> Invalid Format! IC must be exactly 12 numbers.");
            }
        }

        sc.nextLine();
        System.out.print("First Name: ");
        String fn = sc.nextLine();
        System.out.print("Last Name: ");
        String ln = sc.nextLine();

        try {
            Employee e = new Employee(ic, fn, ln);
            boolean ok = svc.registerEmployee(e);
            System.out.println(ok ? ">> Success: Registered." : ">> Error: ID already exists.");
        } catch (Exception ex) {
            System.out.println(">> Error contacting server: " + ex.getMessage());
        }
    }

    private static void handleGet(HRMService svc) throws Exception {
        System.out.print("IC Number: ");
        String ic = sc.next();
        Employee e = svc.getEmployee(ic);

        if (e == null) {
            System.out.println(">> Employee not found.");
        } else {
            System.out.println(">> Found: " + e.getFirstName() + " " + e.getLastName());
            System.out.println("   Leave Balance: " + e.getLeaveBalance());

            Map<String, String> family = e.getFamilyDetails();
            if (family == null || family.isEmpty()) {
                System.out.println("   Family Details: (None)");
            } else {
                System.out.println("   Family Details:");
                for (Map.Entry<String, String> entry : family.entrySet()) {
                    System.out.println("     - " + entry.getKey() + ": " + entry.getValue());
                }
            }
        }
    }

    private static void handleUpdate(HRMService svc) throws Exception {
        System.out.print("IC Number to update: ");
        String ic = sc.next();
        Employee e = svc.getEmployee(ic);

        if (e == null) {
            System.out.println(">> Employee not found.");
        } else {
            sc.nextLine();
            System.out.print("New First Name: ");
            e.setFirstName(sc.nextLine());
            System.out.print("New Last Name: ");
            e.setLastName(sc.nextLine());

            boolean ok = svc.updateProfile(e);
            System.out.println(ok ? ">> Profile Updated." : ">> Update failed.");
        }
    }

    private static void handleFamilyUpdate(HRMService svc) throws Exception {
        System.out.print("IC Number: ");
        String ic = sc.next();
        Employee e = svc.getEmployee(ic);

        if (e == null) {
            System.out.println(">> Employee not found.");
        } else {
            sc.nextLine();
            System.out.print("Relationship (e.g. Spouse/Mother): ");
            String relation = sc.nextLine();

            System.out.print("Name: ");
            String name = sc.nextLine();

            Map<String, String> details = e.getFamilyDetails();
            details.put(relation, name);
            e.setFamilyDetails(details);

            svc.updateProfile(e);
            System.out.println(">> Family details updated.");
        }
    }

    private static void handlePayslip(HRMService svc) {
        System.out.print("Enter IC Number for Payroll: ");
        String ic = sc.next();
        try {
            String slip = svc.generatePayslip(ic);
            System.out.println(slip);
        } catch (Exception e) {
            System.out.println("Error contacting PRS: " + e.getMessage());
        }
    }

    private static void handleLeaveApplication(HRMService svc) throws Exception {
        System.out.print("IC Number: ");
        String ic = sc.next();
        System.out.print("Days to apply: ");

        if (sc.hasNextInt()) {
            int d = sc.nextInt();
            LeaveApplication la = svc.applyLeave(ic, d);
            if (la == null) {
                System.out.println(">> Employee not found.");
            } else {
                System.out.println(">> Application Status: " + la.getStatus());
            }
        } else {
            System.out.println(">> Invalid input. Please enter a number.");
            sc.next(); // Clear invalid input
        }
    }

    private static void handleReport(HRMService svc) throws Exception {
        System.out.print("IC Number: ");
        String ic = sc.next();
        String r = svc.generateYearlyReport(ic);
        System.out.println("---------------- REPORT ----------------");
        System.out.println(r);
        System.out.println("----------------------------------------");
    }
}