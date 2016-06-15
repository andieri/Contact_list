package com.ge.academy.contact_list.utils;

import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.concurrent.atomic.AtomicInteger;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Created by 212566301 on 6/13/2016.
 */
public class ContactGroup {

    /*
            fluent api creator pattern for groups

            Example usage:
            UserBuilder userBuilder = new UserBuilder(context).getUser();
            String authHeader = userBuilder.getUserAuthenticationString();
            String userName = userBuilder.getUsername();


            ContactGroup contactGroup = ContactGroup.creator()
                    .authHeader(authHeader)
                    .userName(userName)
                    .name("name")
                    .displayName("displayName")
                    .webApplicationContext(context)
                    .create();

            String name = contactGroup.getName();

            TBD: unit tests
     */

    private ContactGroup() {
    }

    private String authHeader;
    private String name;
    private String displayName;
    private String userName;
    static AtomicInteger nameID;
    private WebApplicationContext webApplicationContext;

    public static ContactGroupCreator creator() {
        return new ContactGroupCreator();
    }

    public String getName() {
        return name;
    }

    public String getUserName() {
        return userName;
    }

    public static class ContactGroupCreator {

        private ContactGroup instance = new ContactGroup();
        private MockMvc mvc;

        public ContactGroupCreator() {
        }

        public ContactGroupCreator authHeader(String authHeader){
            instance.authHeader = authHeader;
            return this;
        }

        public ContactGroupCreator name(String name){
            instance.name = name;
            return this;
        }

        public ContactGroupCreator displayName(String displayName){
            instance.displayName = displayName;
            return this;
        }

        public ContactGroupCreator userName(String userName){
            instance.userName = userName;
            return this;
        }

        public ContactGroupCreator webApplicationContext(WebApplicationContext webApplicationContext){
            instance.webApplicationContext = webApplicationContext;
            return this;
        }

        public ContactGroup create() throws Exception {
            this.mvc = MockMvcBuilders.webAppContextSetup(instance.webApplicationContext).build();

            if (instance.name == null) {
                instance.name = "name"+nameID.incrementAndGet();
            }
//            if (instance.displayName == null) {
//                int randomID = (int) Math.floor(Math.random() * 1000000.0);
//                instance.displayName = "displayName"+randomID;
//            }
            mvc.perform(post("/groups").header("Authorization", instance.authHeader)
                    .contentType(MediaType.APPLICATION_JSON_UTF8)
                    .content("{\"id\" : {\"userName\":\""+instance.userName+"\"," +
                            "\"contactGroupName\":\""+instance.name+"\"}," +
                            "\"displayName\":\""+instance.displayName+"\"}"))
                    .andDo(print())
                    .andExpect(status().isCreated());

            return instance;
        }
    }
}
