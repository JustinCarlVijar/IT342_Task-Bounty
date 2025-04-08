package edu.cit.taskbounty.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;

@Document(collection = "users")
public class User implements UserDetails {

    @Id
    private String id;
    private String username;
    private String email;
    private String password; // Hashed password
    private String country;
    private Date createdAt;

    public User() {}

    public User(String username, String email, String password, String country, Date createdAt) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.country = country;
        this.createdAt = createdAt;
    }

    // --- UserDetails Interface Implementation ---

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.emptyList(); // No roles yet
    }

    @Override
    public String getPassword() {
        return password;
    }

    // Spring Security uses this as the primary unique identifier
    @Override
    public String getUsername() {
        return username; // Use email for login (you can change it to username if preferred)
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    // --- Getters and Setters ---

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
}
