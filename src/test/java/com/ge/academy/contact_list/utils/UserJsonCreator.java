package com.ge.academy.contact_list.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;


/**
 * Created by 212566304 on 6/17/2016.
 */
public class UserJsonCreator {

    private ObjectMapper mapper;
    private ObjectNode node;

    public UserJsonCreator() {
        this.mapper = new ObjectMapper();
        this.node = mapper.createObjectNode();
    }


    public String getJsonForLogin(String username, String password) throws JsonProcessingException {
        this.node.put("userName", username);
        this.node.put("password", password);
        return this.mapper.writerWithDefaultPrettyPrinter().writeValueAsString(node);
    }

    public String getJSonForCreateUser(String username, String password) throws JsonProcessingException {
        this.node.put("userName", username);
        this.node.put("password", password);
        this.node.put("role", "USER");
        return this.mapper.writerWithDefaultPrettyPrinter().writeValueAsString(node);
    }

    public String getJSonForCreateAdmin(String username, String password) throws JsonProcessingException {
        this.node.put("userName", username);
        this.node.put("password", password);
        this.node.put("role", "ADMIN");
        return this.mapper.writerWithDefaultPrettyPrinter().writeValueAsString(node);
    }

    public String getJSonForChangePassword(String oldPassword, String newPassword) throws JsonProcessingException {
        this.node.put("oldPassword", oldPassword);
        this.node.put("newPassword", newPassword);
        this.node.put("version", 1);
        return this.mapper.writerWithDefaultPrettyPrinter().writeValueAsString(node);
    }
}
