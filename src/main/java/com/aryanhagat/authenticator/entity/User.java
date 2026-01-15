package com.aryanhagat.authenticator.entity;

import jakarta.persistence.*;
        import java.time.LocalDateTime;

@Entity //Tells Hibernate to make a table out of this class
@Table(name = "users") // Specifies the table name in the database
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private boolean twoFactorEnabled = false;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @PrePersist // Method to set createdAt before persisting
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    // getters and setters (generate via IntelliJ)
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isTwoFactorEnabled() {
        return twoFactorEnabled;
    }

    public void setTwoFactorEnabled(boolean twoFactorEnabled) {
        this.twoFactorEnabled = twoFactorEnabled;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}