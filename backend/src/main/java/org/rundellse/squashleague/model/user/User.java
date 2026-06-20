package org.rundellse.squashleague.model.user;

import jakarta.persistence.*;
import org.rundellse.squashleague.model.Player;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

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
    private Set<UserRole> userRoles = new HashSet<>();

    @OneToOne
    private Player player;


    public User() {
    }

    public User(String name, String email, String password, Set<UserRole> userRoles, Player player) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.userRoles = userRoles;
        this.player = player;
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

    public Set<UserRole> getUserRoles() {
        return userRoles;
    }

    public void setUserRoles(Set<UserRole> userRoles) {
        this.userRoles = userRoles;
    }

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }


    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", userRoles=" + userRoles +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return id.equals(user.id) && name.equals(user.name) && email.equals(user.email) && password.equals(user.password);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, email, password);
    }
}
