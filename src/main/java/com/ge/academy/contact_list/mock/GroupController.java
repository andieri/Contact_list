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
public class GroupController {
    @RequestMapping(method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<Collection<ContactGroup>> getCustomers() {
        return ResponseEntity.ok(new ArrayList());

    }

}
