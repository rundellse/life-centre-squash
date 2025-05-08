package org.rundellse.squashleague.model.user;

import org.rundellse.squashleague.model.Player;

import java.util.ArrayList;
import java.util.List;

public class User {

    private Long id;

    private String name;

    private String email;

    private String password;

    private List<Role> roles = new ArrayList<>();

    private Player player;

    public User(Long id, String name, String email, String password, List<Role> roles, Player player) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
        this.roles = roles;
        this.player = player;
    }

    public User(Long id, String name, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public List<Role> getUserRoles() {
        return roles;
    }

    public void setUserRoles(List<Role> roles) {
        this.roles = roles;
    }

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }
}
