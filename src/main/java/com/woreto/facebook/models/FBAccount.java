package com.woreto.facebook.models;

public class FBAccount {
    String emailId;
    String password;
    String fullName;

    public FBAccount(String emailId, String password, String fullName) {
        this.emailId = emailId;
        this.password = password;
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

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
}
