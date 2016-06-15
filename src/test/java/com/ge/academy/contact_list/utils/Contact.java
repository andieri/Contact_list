package com.ge.academy.contact_list.utils;

import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

/**
 * Created by 212566301 on 6/13/2016.
 */
public class Contact {

    /*
            fluent api builder pattern for groups

            Example usage (all parameters are necessary):
                Contact contact = Contact.builder()
                        .authHeader(authHeader)
                        .groupId(groupName)
                        .id("id")
                        .firstName("firstName")
                        .lastName("lastName")
                        .homeEmail("home@email.email")
                        .workEmail("work@email.email")
                        .nickName("nickName")
                        .jobTitle("jobTitle")
                        .webApplicationContext(context)
                        .create();
            String id = contact.getId();

            TBD: unit tests
     */

    private Contact() {
    }

    private String authHeader;
    private String groupId;
    private String id;
    private String firstName;
    private String lastName;
    private String homeEmail;
    private String workEmail;
    private String nickName;
    private String jobTitle;
    private WebApplicationContext webApplicationContext;

    public String getId() {
        return id;
    }


    public static Builder builder() {
        return new Contact.Builder();
    }

    public static class Builder {

        private Contact instance = new Contact();
        private MockMvc mvc;

        public Builder() {
        }

        public Builder authHeader(String authHeader){
            instance.authHeader = authHeader;
            return this;
        }

        public Builder groupId(String groupId){
            instance.groupId = groupId;
            return this;
        }

        public Builder id(String id){
            instance.id = id;
            return this;
        }

        public Builder firstName(String firstName){
            instance.firstName = firstName;
            return this;
        }

        public Builder lastName(String lastName){
            instance.lastName = lastName;
            return this;
        }

        public Builder homeEmail(String homeEmail){
            instance.homeEmail = homeEmail;
            return this;
        }

        public Builder workEmail(String workEmail){
            instance.workEmail = workEmail;
            return this;
        }

        public Builder nickName(String nickName){
            instance.nickName = nickName;
            return this;
        }

        public Builder jobTitle(String jobTitle){
            instance.jobTitle = jobTitle;
            return this;
        }

        public Builder webApplicationContext(WebApplicationContext webApplicationContext){
            instance.webApplicationContext = webApplicationContext;
            return this;
        }

        public Contact create() throws Exception {
            this.mvc = MockMvcBuilders.webAppContextSetup(instance.webApplicationContext).build();
            mvc.perform(post("/groups/" + instance.groupId + "/contacts")
                    .header("Authorization", instance.authHeader)
                    .contentType(MediaType.APPLICATION_JSON_UTF8)
                    .content("{\"id\":\""+instance.id+"\", \"firstName\":\""+instance.firstName+"\", " +
                            "\"lastName\":\""+instance.lastName+"\", \"homeEmail\":\""+instance.homeEmail+"\", " +
                            "\"workEmail\":\""+instance.workEmail+"\", \"nickName\":\""+instance.nickName+"\", " +
                            "\"jobTitle\":\""+instance.jobTitle+"\"}"))
                    .andDo(print());
            return instance;
        }
    }
}
