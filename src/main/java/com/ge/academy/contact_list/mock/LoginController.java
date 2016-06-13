package com.ge.academy.contact_list.mock;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by 212566304 on 6/13/2016.
 */

@RestController
@RequestMapping("/login")
public class LoginController {

    @RequestMapping(method = RequestMethod.POST, produces = "application/json")
    public Token login(String username, String password) {
        Token t = new Token();
        t.setTokenID("bksjl kskdjfl skldf");
        return t;

    }

}
