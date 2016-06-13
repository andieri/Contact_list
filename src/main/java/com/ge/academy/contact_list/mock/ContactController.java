package com.ge.academy.contact_list.mock;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by 212565332 on 6/13/2016.
 */
@Component
@RestController
@RequestMapping("/groups/{gid}/contacts")
public class ContactController {
    List<Contact> l = new ArrayList<>();
    Contact c = null;

    @RequestMapping(method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<Collection<Contact>> getContacts() {

        return ResponseEntity.ok(l);
    }


    @RequestMapping(method = RequestMethod.POST, consumes = "application/json")
    public ResponseEntity<Void> postContact(@RequestBody Contact contact) {
        final URI location = ServletUriComponentsBuilder.fromCurrentServletMapping().path("/groups/{gid}/contacts/{id}").build().expand(1, 1).toUri();
        l.add(contact);
        c = contact;
        return ResponseEntity.created(location).build();

    }


    @RequestMapping(value = "/{uid}", method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<Contact> getContact() {
        if (c != null)
            return ResponseEntity.ok(c);
        else
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);

    }

    @RequestMapping(value = "/{uid}", method = RequestMethod.PUT, produces = "application/json")
    public ResponseEntity<Collection<Contact>> setContact() {
        return ResponseEntity.ok(new ArrayList());

    }

    @RequestMapping(value = "/{uid}", method = RequestMethod.DELETE, produces = "application/json")
    public ResponseEntity<Collection<Contact>> removeContact() {
        l.remove(c);
        c = null;
        return ResponseEntity.ok(new ArrayList());

    }


}
