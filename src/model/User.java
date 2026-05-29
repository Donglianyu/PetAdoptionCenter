package model;

import javax.swing.*;
import java.io.Serializable;

public abstract class User implements Serializable {
    private static final long serialVersionUID = 1L;

    protected String userId;
    protected String name;
    protected String contactInfo;
    protected String password;

    public User() {}

    public User(String userId, String name, String contactInfo, String password) {
        this.userId = userId;
        this.name = name;
        this.contactInfo = contactInfo;
        this.password = password;
    }

    // Abstract method: permission check
    public abstract boolean hasPermission(String operation);

    // Abstract method: get the authorized panel for this user (for dynamic UI rendering)
    public abstract JPanel getAuthorizedPanel(JFrame parentFrame);

    // Abstract method: display role
    public abstract String displayRole();

    // Validate password
    public boolean validatePassword(String inputPassword) {
        return this.password.equals(inputPassword);
    }

    // Getters and Setters
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getContactInfo() { return contactInfo; }
    public void setContactInfo(String contactInfo) { this.contactInfo = contactInfo; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    @Override
    public String toString() {
        return String.format("%s: %s (%s)", userId, name, displayRole());
    }
}

