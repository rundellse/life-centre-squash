package org.rundellse.squashleague.model.user;

import jakarta.persistence.*;

import java.util.Set;

@Entity
public class UserRole {

    @Id
    @GeneratedValue
    private Long id;

    @Enumerated(EnumType.STRING)
    private Role role;

    @ManyToMany
    private Set<User> users;


    public UserRole() {
    }

    public UserRole(Role name) {
        this.role = name;
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public Set<User> getUsers() {
        return users;
    }

    public void setUsers(Set<User> users) {
        this.users = users;
    }
}
