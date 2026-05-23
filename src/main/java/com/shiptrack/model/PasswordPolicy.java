package com.shiptrack.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class PasswordPolicy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int minLength;
    private int minUpperCase;
    private int minLowerCase;
    private int minDigit;
    private int minSpecialChars;
    private int maxLoginAttempts;

    public Long getId() {
        return id;
    }

    public int getMinLength() {
        return minLength;
    }

    public void setMinLength(int minLength) {
        this.minLength = minLength;
    }

    public int getMinUpperCase() {
        return minUpperCase;
    }

    public void setMinUpperCase(int minUpperCase) {
        this.minUpperCase = minUpperCase;
    }

    public int getMinLowerCase() {
        return minLowerCase;
    }

    public void setMinLowerCase(int minLowerCase) {
        this.minLowerCase = minLowerCase;
    }

    public int getMinDigit() {
        return minDigit;
    }

    public void setMinDigit(int minDigit) {
        this.minDigit = minDigit;
    }

    public int getMinSpecialChars() {
        return minSpecialChars;
    }

    public void setMinSpecialChars(int minSpecialChars) {
        this.minSpecialChars = minSpecialChars;
    }

    public int getMaxLoginAttempts() {
        return maxLoginAttempts;
    }

    public void setMaxLoginAttempts(int maxLoginAttempts) {
        this.maxLoginAttempts = maxLoginAttempts;
    }
}