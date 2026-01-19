package com.bananabill.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "otps")
public class Otp {

    @Id
    private String id;

    private String mobileNumber;
    private String otpHash;
    private Instant expiry;
    private String action; // e.g., "login", "reset_password"
    private boolean used;

    public Otp() {
    }

    public Otp(String mobileNumber, String otpHash, Instant expiry, String action) {
        this.mobileNumber = mobileNumber;
        this.otpHash = otpHash;
        this.expiry = expiry;
        this.action = action;
        this.used = false;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }

    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }

    public String getOtpHash() {
        return otpHash;
    }

    public void setOtpHash(String otpHash) {
        this.otpHash = otpHash;
    }

    public Instant getExpiry() {
        return expiry;
    }

    public void setExpiry(Instant expiry) {
        this.expiry = expiry;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public boolean isUsed() {
        return used;
    }

    public void setUsed(boolean used) {
        this.used = used;
    }
}
