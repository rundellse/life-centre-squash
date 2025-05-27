package org.rundellse.squashleague.model.user;

import jakarta.persistence.*;
import org.hibernate.annotations.NaturalId;

import java.util.List;

@Entity
public class Role {

    @Id
    @GeneratedValue
    private Long id;

    @NaturalId
    @Column(nullable = false)
    private String name;

    @ManyToMany
    private List<User> users;


    public Role() {
    }

    public Role(String name) {
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<User> getUsers() {
        return users;
    }

    public void setUsers(List<User> users) {
        this.users = users;
    }
}
