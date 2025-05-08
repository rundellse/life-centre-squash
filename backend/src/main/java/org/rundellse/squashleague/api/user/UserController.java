package org.rundellse.squashleague.api.user;

import org.rundellse.squashleague.model.user.User;
import org.rundellse.squashleague.persistence.UserH2Dao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController("/user")
public class UserController {

    @Autowired
    private UserH2Dao userH2Dao;


    @GetMapping("/{id}")
    public User getUser(@PathVariable Long id) {
        return userH2Dao.getSiteUserById(id);
    }


}
