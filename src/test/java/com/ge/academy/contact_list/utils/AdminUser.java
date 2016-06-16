package com.ge.academy.contact_list.utils;

/**
 * Created by 212566304 on 6/16/2016.
 */
public class AdminUser extends User{

    public AdminUser(String username, String password) {
        super(username, password);
        super.role = "ADMIN";
    }
}
