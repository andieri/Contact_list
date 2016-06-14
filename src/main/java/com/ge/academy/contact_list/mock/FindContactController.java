package com.ge.academy.contact_list.mock;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by 212565332 on 6/14/2016.
 */
@Component
@RestController
@RequestMapping("/find")

public class FindContactController {

    @Autowired
    ContactController contacts;

    @RequestMapping(method = RequestMethod.POST, produces = "application/json")
    public ResponseEntity<Collection<Contact>> findContacts(@RequestBody Contact contact) {
        ArrayList<Contact> result = new ArrayList<>();
        for (Contact c : contacts.l) {
            if (c.id.contains(contact.id)|| c.firstName.contains(contact.firstName)){
                result.add(c);
            }
        }
        return ResponseEntity.ok(result);
    }

}
