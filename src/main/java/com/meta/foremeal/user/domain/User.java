package com.meta.foremeal.user.domain;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "email", nullable = false, unique = true, length = 255)
    private String email;

    @Column(name = "password", nullable = false, length = 255)
    private String password;

    @Column(name = "username", nullable = false, length = 100)
    private String username;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    private UserRole role;

    @Column(name = "password_changed_at")
    private LocalDateTime passwordChangedAt;

    protected User() {
    }

    public User(String email, String password, String username, LocalDate birthDate, UserRole role) {
        this.email = email;
        this.password = password;
        this.username = username;
        this.birthDate = birthDate;
        this.role = role;
    }

    public Long getUserId() {
        return userId;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getUsername() {
        return username;
    }

    public Integer getBirthYear() {
        return birthDate == null ? null : birthDate.getYear();
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public UserRole getRole() {
        return role;
    }

    public LocalDateTime getPasswordChangedAt() {
        return passwordChangedAt;
    }

    public void update(String username, LocalDate birthDate) {
        this.username = username;
        this.birthDate = birthDate;
    }

    public void changePassword(String password) {
        this.password = password;
        this.passwordChangedAt = LocalDateTime.now();
    }

    public void changeRole(UserRole role) {
        this.role = role;
    }
}
