package com.woreto.linkedin.models;

public class LinkedinAccount {
    private String fullName;
    private String emailId;
    private String password;

    public LinkedinAccount(String emailId, String password, String fullName) {
        this.emailId = emailId;
        this.password = password;
        this.fullName = fullName;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmailId() {
        return emailId;
    }

    public void setEmailId(String emailId) {
        this.emailId = emailId;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
