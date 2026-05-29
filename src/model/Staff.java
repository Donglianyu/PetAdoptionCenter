package model;

import javax.swing.*;

public class Staff extends User {
    private String department;
    private String staffLevel;
    
    public Staff() {
        super();
    }
    
    public Staff(String userId, String name, String contactInfo, String password,
                 String department, String staffLevel) {
        super(userId, name, contactInfo, password);
        this.department = department;
        this.staffLevel = staffLevel;
    }
    
    @Override
    public boolean hasPermission(String operation) {
        // Staff has full management permissions
        return operation.equals("ADD_PET") ||
               operation.equals("EDIT_PET") ||
               operation.equals("DELETE_PET") ||
               operation.equals("REVIEW_APPLICATION") ||
               operation.equals("VIEW_ANALYTICS") ||
               operation.equals("BROWSE_PETS");
    }
    
    @Override
    public JPanel getAuthorizedPanel(JFrame parentFrame) {
        return null; // actually handled in MainFrame
    }
    
    @Override
    public String displayRole() {
        return "Shelter Staff (" + staffLevel + ")";
    }
    
    // Getters and Setters
    public String getDepartment() {
        return department;
    }
    
    public void setDepartment(String department) {
        this.department = department;
    }
    
    public String getStaffLevel() {
        return staffLevel;
    }
    
    public void setStaffLevel(String staffLevel) {
        this.staffLevel = staffLevel;
    }
}

