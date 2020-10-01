package com.app.pojo;
// Generated Sep 25, 2020 2:22:03 PM by Hibernate Tools 4.3.1

import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Temporal;

/**
 * Users generated by hbm2java
 */
@Entity
public class Users implements java.io.Serializable {

    @Id
    @GeneratedValue//(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private String id;
    private String name;
    private String email;
    private String password;
    @Temporal(javax.persistence.TemporalType.DATE)
    private Date createdAt;
    @Temporal(javax.persistence.TemporalType.DATE)
    private Date updatedAt;
    @Temporal(javax.persistence.TemporalType.DATE)
    private Date lastLogin;
    private String state;
    private String countryCode;

    public Users() {
    }

    public Users(String id) {
        this.id = id;
    }

    public Users(String id, String name, String email, String password) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
    }

    public Users(String id, String name, String email, String password, Date createdAt, Date updatedAt, Date lastLogin, String state, String countryCode) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.lastLogin = lastLogin;
        this.state = state;
        this.countryCode = countryCode;
    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return this.email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return this.password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Date getCreatedAt() {
        return this.createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return this.updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Date getLastLogin() {
        return this.lastLogin;
    }

    public void setLastLogin(Date lastLogin) {
        this.lastLogin = lastLogin;
    }

    public String getState() {
        return this.state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getCountryCode() {
        return this.countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

}
