package com.bhel.hrm.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Constructors and getters/setters
public class Employee implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String icNumber; // IC/Passport used as ID
    private String firstName;
    private String lastName;
    private Map<String,String> familyDetails = new HashMap<>(); // simple key->value
    private int leaveBalance = 25; // default annual balance
    private List<LeaveApplication> leaveHistory = new ArrayList<>();

    public Employee(String icNumber, String firstName, String lastName) {
        this.icNumber = icNumber;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    // getters/setters
    public String getIcNumber() {
        return icNumber; }

    public String getFirstName() {
        return firstName; }

    public void setFirstName(String firstName) {
        this.firstName = firstName; }

    public String getLastName() {
        return lastName; }

    public void setLastName(String lastName) {
        this.lastName = lastName; }

    public Map<String,String> getFamilyDetails() {
        return familyDetails; }

    public void setFamilyDetails(Map<String,String> familyDetails) {
        this.familyDetails = familyDetails; }

    public int getLeaveBalance() {
        return leaveBalance; }

    public void setLeaveBalance(int leaveBalance) {
        this.leaveBalance = leaveBalance; }

    public List<LeaveApplication> getLeaveHistory() {
        return leaveHistory; }

    public void addLeaveApplication(LeaveApplication app) {
        this.leaveHistory.add(app); }

    @Override
    public String toString() {
        return "Employee{" +
                "icNumber='" + icNumber + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", leaveBalance=" + leaveBalance +
                '}';
    }
}