package com.ge.academy.contact_list.mock;

import java.time.LocalDateTime;

/**
 * Created by 212566304 on 6/13/2016.
 */
public class Token {

    private String tokenID;
    private User user;
    private LocalDateTime expiresOn;

    public String getTokenID() {
        return tokenID;
    }

    public void setTokenID(String tokenID) {
        this.tokenID = tokenID;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public LocalDateTime getExpiresOn() {
        return expiresOn;
    }

    public void setExpiresOn(LocalDateTime expiresOn) {
        this.expiresOn = expiresOn;
    }


}
