package com.ge.academy.contact_list.utils;

import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Created by 212566301 on 6/13/2016.
 */
public class Contact {

    /*
            fluent api builder pattern for groups

            Example usage (from the beginning):

            //create user
            UserBuilder userBuilder = new UserBuilder(context).createUser().build();
            String authHeader = userBuilder.getAuthenticationString();
            String userName = userBuilder.getUsername();

            // create contactgroup
            ContactGroup contactGroup = ContactGroup.creator()
                    .authHeader(authHeader)
                    .userName(userName)
                    .name("name")
                    .displayName("displayName")
                    .webApplicationContext(context)
                    .create();

            String groupName = contactGroup.getName();

            // create contact
            Contact contact = Contact.builder()
                    .authHeader(authHeader)
                    .groupName(groupName)
                    .firstName("firstName")
                    .lastName("lastName")
                    .homeEmail("home@email.email")
                    .workEmail("work@email.email")
                    .nickName("nickName")
                    .jobTitle("jobTitle")
                    .webApplicationContext(context)
                    .create();

            // contactId is assigned automatically, starts with 1
            // it is read from the location header of the response:
            String contactId = contact.getId();

            // if everything is fine, this should work:
            mvc.perform(get("/groups/" + groupName + "/contacts/" + contactId)
                .header("Authorization", authHeader))
                .andExpect(status().isOk());

            TBD: unit tests
     */

    private Contact() {
    }

    private String authHeader;
    private String userName;
    private String groupName;
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

        public Builder userName(String userName){
            instance.userName = userName;
            return this;
        }

        public Builder groupName(String groupName){
            instance.groupName = groupName;
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
            MvcResult contactCreated = mvc.perform(post("/groups/" + instance.groupName + "/contacts")
                    .header("Authorization", instance.authHeader)
                    .contentType(MediaType.APPLICATION_JSON_UTF8)
                    .content("{\n" +
//                            "    \"id\" : {\n" +
//                            "        \"userName\":\""+instance.userName+"\",\n" +
//                            "        \"contactGroupName\":\""+instance.groupName+"\",\n" +
//                            "        \"contactId\":2\n" +
//                            "    }, \n" +
                            "    \"firstName\":\"" + instance.firstName + "\",\n" +
                            "    \"lastName\":\"" + instance.lastName + "\",\n" +
                            "    \"homeEmail\":\"" + instance.homeEmail + "\",\n" +
                            "    \"workEmail\":\"" + instance.workEmail + "\",\n" +
                            "    \"nickName\":\"" + instance.nickName + "\",\n" +
                            "    \"jobTitle\":\"" + instance.jobTitle + "\"\n" +
                            "}"))
                    .andDo(print())
                    .andExpect(status().isCreated())
                    .andReturn();

            String[] locationHeaderSplitted = contactCreated.getResponse().getHeader("Location").split("/");
            instance.id = locationHeaderSplitted[locationHeaderSplitted.length-1];

            return instance;
        }
    }
}
