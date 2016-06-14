package com.ge.academy.contact_list.mock;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by 212565332 on 6/13/2016.
 */
@RestController
@RequestMapping(path = "/groups")
public class GroupController {

    @RequestMapping(method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<Collection<ContactGroup>> getAllGroups() {
        return ResponseEntity.ok(new ArrayList());
    }

    @RequestMapping(method = RequestMethod.POST, produces = "application/json")
    public ResponseEntity<Collection<ContactGroup>> createGroup() {
        return ResponseEntity.ok(new ArrayList());
    }

    @RequestMapping(path = "/groupId", method = RequestMethod.PUT, produces = "application/json")
    public ResponseEntity<Collection<ContactGroup>> renameGroup() {
        return ResponseEntity.ok(new ArrayList());
    }

    @RequestMapping(path = "/groupId", method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<Collection<ContactGroup>> getGroup() {
        return ResponseEntity.ok(new ArrayList());
    }

    @RequestMapping(path = "/groupId", method = RequestMethod.POST, produces = "application/json")
    public ResponseEntity<Collection<ContactGroup>> createContact() {
        return ResponseEntity.ok(new ArrayList());
    }

    @RequestMapping(path = "/groupId/contacts/contactId", method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<Collection<ContactGroup>> getContact() {
        return ResponseEntity.ok(new ArrayList());
    }

}
