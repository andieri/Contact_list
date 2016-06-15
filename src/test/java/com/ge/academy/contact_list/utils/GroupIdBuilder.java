package com.ge.academy.contact_list.utils;

import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.Random;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

/**
 * Created by 212566301 on 6/13/2016.
 */
public class GroupIdBuilder {

    /*
            fluent api builder pattern for groups

            Example usage:
            String groupId = GroupIdBuilder.builder()
                            .authHeader(authHeader)
                            .name("name")  // this is not mandatory
                            .displayName("displayName")
                            .webApplicationContext(context)
                            .build();

            TBD: unit tests
     */

    public GroupIdBuilder() {
    }

    private String authHeader;
    private String name;
    private String displayName;
    private WebApplicationContext webApplicationContext;

    public static Builder builder() {
        return new GroupIdBuilder.Builder();
    }

    public static class Builder {

        private GroupIdBuilder instance = new GroupIdBuilder();
        private MockMvc mvc;

        public Builder() {
        }

        public Builder authHeader(String authHeader){
            instance.authHeader = authHeader;
            return this;
        }

        public Builder name(String name){
            instance.name = name;
            return this;
        }

        public Builder displayName(String displayName){
            instance.displayName = displayName;
            return this;
        }

        public Builder webApplicationContext(WebApplicationContext webApplicationContext){
            instance.webApplicationContext = webApplicationContext;
            return this;
        }

        public String build() throws Exception {
            this.mvc = MockMvcBuilders.webAppContextSetup(instance.webApplicationContext).build();

            if (instance.name == null) {
                int randomID = (int) Math.floor(Math.random() * 1000000.0);
                instance.name = "name"+randomID;
            }
//            if (instance.displayName == null) {
//                int randomID = (int) Math.floor(Math.random() * 1000000.0);
//                instance.displayName = "displayName"+randomID;
//            }
            mvc.perform(post("/groups").header("Authorization", instance.authHeader)
                    .contentType(MediaType.APPLICATION_JSON_UTF8)
                    .content("{\"name\": \"" + instance.name + "\", \"displayName\": \"" + instance.displayName + "\"}"))
                    .andDo(print());
            return instance.name;
        }
    }
}