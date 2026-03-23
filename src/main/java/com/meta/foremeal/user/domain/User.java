package com.meta.foremeal.user.domain;

import jakarta.persistence.*;

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

    @Column(name = "birth_year")
    private Integer birthYear;

    protected User() {
    }

    public User(String email, String password, String username, Integer birthYear) {
        this.email = email;
        this.password = password;
        this.username = username;
        this.birthYear = birthYear;
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
        return birthYear;
    }

    public void update(String username, Integer birthYear) {
        this.username = username;
        this.birthYear = birthYear;
    }

    public void changePassword(String password) {
        this.password = password;
    }
}