package org.rundellse.squashleague.model.user;

import java.util.List;

public class Role {

    private Long id;

    private String name;

    private List<User> users;

    public Role(Long id, String name) {
        this.id = id;
        this.name = name;
    }
}
