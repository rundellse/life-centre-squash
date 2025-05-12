package org.rundellse.squashleague.model.user;

import jakarta.persistence.*;
import org.rundellse.squashleague.model.Player;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "site_user")
public class User {

    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @ManyToMany
    private List<Role> roles = new ArrayList<>();

    @OneToOne
    private Player player;


    public User() {
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
