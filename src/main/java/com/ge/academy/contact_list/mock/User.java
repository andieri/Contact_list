package com.ge.academy.contact_list.mock;

/**
 * Created by 212566304 on 6/14/2016.
 */
public class User {
    private String username;
    private String password;

    public User() {
        this.username = "user";
        this.password = "password";
    }

    public User(User u){
        this.password = u.password;
        this.username = u.username;
    }

    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
