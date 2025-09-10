package com.whatsapp.service.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_sessions")
@Data
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
    @Column
    private Long amount;
    @Column
    private String cardNo;
    @Column
    private Long cvv;

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

    
    public String getCurrentState() {
        return currentState;
    }
    
    public void setCurrentState(String currentState) {
        this.currentState = currentState;
    }

    
    public String getSelectedMerchant() {
        return selectedMerchant;
    }
    
    public void setSelectedMerchant(String selectedMerchant) {
        this.selectedMerchant = selectedMerchant;
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
