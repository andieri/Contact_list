package com.ge.academy.contact_list.utils;

/**
 * Created by 212566304 on 6/16/2016.
 */
public class User {

    protected String username;
    protected String password;
    protected String role;
    protected String token;

    public User(String username, String password) {
        this.username = username;
        this.password = password;
        this.role = "USER";
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getRole() {
        return role;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
