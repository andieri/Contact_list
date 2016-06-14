package com.ge.academy.contact_list.mock;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * Created by 212565332 on 6/13/2016.
 */
@RestController
@RequestMapping(path = "/groups")
public class GroupController {

    List<ContactGroupResource> list = new ArrayList<>();
    ContactGroupResource contactGroupResource = null;

    @RequestMapping(method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<List<ContactGroupResource>> getAllGroups() {
        return ResponseEntity.ok(list);
    }

    @RequestMapping(method = RequestMethod.POST, produces = "application/json")
    public ResponseEntity<Void> createGroup(@RequestBody ContactGroupResource contactGroupResource) {
        final URI location = ServletUriComponentsBuilder.fromCurrentServletMapping().path("/groups/{gid}")
                .build().expand(contactGroupResource.getName()).toUri();
        list.add(contactGroupResource);
        this.contactGroupResource = contactGroupResource;
        return ResponseEntity.created(location).build();
    }

    @RequestMapping(path = "/{gid}", method = RequestMethod.PUT, produces = "application/json")
    public ResponseEntity<List<ContactGroupResource>> renameGroup(@RequestBody ContactGroupResource contactGroupResource,
                                                                    @PathVariable String gid) {
        final URI location = ServletUriComponentsBuilder.fromCurrentServletMapping().path("/groups/{gid}")
                .build().expand(contactGroupResource.getName()).toUri();

        for (Iterator<ContactGroupResource> iterator = list.iterator(); iterator.hasNext(); ) {
            ContactGroupResource cgr = iterator.next();
            if (cgr.getName().equals(gid)) {
                iterator.remove();
            }
        }

        ContactGroupResource contactToBeAdded = new ContactGroupResource();
        contactToBeAdded.setName(contactGroupResource.getName());
        contactToBeAdded.setDisplayName(contactGroupResource.getDisplayName());

        this.contactGroupResource = contactToBeAdded;
        list.add(contactToBeAdded);

        return ResponseEntity.ok(list);
    }

    @RequestMapping(path = "/{gid}", method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<ContactGroupResource> getGroup() {
        if (contactGroupResource != null)
            return ResponseEntity.ok(contactGroupResource);
        else
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);

    }

    @RequestMapping(method = RequestMethod.DELETE, produces = "application/json")
    public ResponseEntity<Void> clearGroups() {
        this.list = new ArrayList<>();
        return ResponseEntity.ok().build();
    }

    @RequestMapping(path = "/groupId/contacts", method = RequestMethod.POST, produces = "application/json")
    public ResponseEntity<Collection<ContactGroup>> createContact() {
        return ResponseEntity.ok(new ArrayList());
    }

    @RequestMapping(path = "/groupId/contacts/contactId", method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<Collection<ContactGroup>> getContact() {
        return ResponseEntity.ok(new ArrayList());
    }

}
