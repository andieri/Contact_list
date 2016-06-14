package com.ge.academy.contact_list.mock;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * Created by 212566301 on 6/14/2016.
 */
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class ContactGroupResource {

    private String name;
    private String displayName;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
}
