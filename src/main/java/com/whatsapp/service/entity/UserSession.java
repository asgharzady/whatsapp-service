package com.whatsapp.service.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_sessions")
public class UserSession {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "phone_number", unique = true, nullable = false)
    private String phoneNumber;
    
    @Column(name = "current_state", nullable = false)
    private String currentState;
    
    @Column(name = "user_name")
    private String userName;
    
    @Column(name = "selected_merchant")
    private String selectedMerchant;
    
    @Column(name = "last_activity", nullable = false)
    private LocalDateTime lastActivity;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    // Default constructor
    public UserSession() {
        this.createdAt = LocalDateTime.now();
        this.lastActivity = LocalDateTime.now();
    }
    
    // Constructor
    public UserSession(String phoneNumber, String currentState) {
        this();
        this.phoneNumber = phoneNumber;
        this.currentState = currentState;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getPhoneNumber() {
        return phoneNumber;
    }
    
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
    
    public String getCurrentState() {
        return currentState;
    }
    
    public void setCurrentState(String currentState) {
        this.currentState = currentState;
    }
    
    public String getUserName() {
        return userName;
    }
    
    public void setUserName(String userName) {
        this.userName = userName;
    }
    
    public String getSelectedMerchant() {
        return selectedMerchant;
    }
    
    public void setSelectedMerchant(String selectedMerchant) {
        this.selectedMerchant = selectedMerchant;
    }
    
    public LocalDateTime getLastActivity() {
        return lastActivity;
    }
    
    public void setLastActivity(LocalDateTime lastActivity) {
        this.lastActivity = lastActivity;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    // Helper method to check if session is expired (5 minutes)
    public boolean isExpired() {
        return lastActivity.isBefore(LocalDateTime.now().minusMinutes(5));
    }
    
    // Helper method to update last activity
    public void updateLastActivity() {
        this.lastActivity = LocalDateTime.now();
    }
}
