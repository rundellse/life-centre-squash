package org.rundellse.squashleague.model;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Entity
public class Player {

    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String email;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "availability_notes")
    private String availabilityNotes;

    private Integer division;

    private boolean isRedFlagged;

    private boolean isAnonymised;

    @OneToMany
    private List<SquashMatch> homeMatches;
    @OneToMany
    private List<SquashMatch> awayMatches;

    public static Comparator<Player> PLAYER_POINTS_COMPARATOR = (p1, p2) -> {
        Integer p1Total = 0;
        Integer p2Total = 0;

        //TODO Possible n+1, needs to be looked at.
        for (SquashMatch squashMatch : p1.getHomeMatches()) {
            p1Total += Objects.equals(p1.id, squashMatch.getHomePlayer().getId()) ? squashMatch.getHomePlayerPoints() : squashMatch.getAwayPlayerPoints();
        }
        for (SquashMatch squashMatch : p2.getHomeMatches()) {
            p2Total += Objects.equals(p2.id, squashMatch.getHomePlayer().getId()) ? squashMatch.getHomePlayerPoints() : squashMatch.getAwayPlayerPoints();
        }
        // If p1 is higher it should be placed first. Negative return places p1 first. So if p1 is larger negative should be returned, hence p2-p1.
        return p2Total - p1Total;
    };

    public Player() {
    }

    public Player(String name, String email, String phoneNumber, String availabilityNotes, Integer division, boolean isRedFlagged, boolean isAnonymised) {
        this.name = name;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.availabilityNotes = availabilityNotes;
        this.division = division;
        this.isRedFlagged = isRedFlagged;
        this.isAnonymised = isAnonymised;
    }

    public Player(Long id, String name, String email, String phoneNumber, String availabilityNotes, Integer division) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.availabilityNotes = availabilityNotes;
        this.division = division;
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

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getAvailabilityNotes() {
        return availabilityNotes;
    }

    public void setAvailabilityNotes(String availabilityNotes) {
        this.availabilityNotes = availabilityNotes;
    }

    public Integer getDivision() {
        return division;
    }

    public void setDivision(Integer division) {
        this.division = division;
    }

    public boolean isRedFlagged() {
        return isRedFlagged;
    }

    public void setRedFlagged(boolean redFLagged) {
        isRedFlagged = redFLagged;
    }

    public boolean isAnonymised() {
        return isAnonymised;
    }

    public void setAnonymised(boolean anonymised) {
        isAnonymised = anonymised;
    }

    public List<SquashMatch> getHomeMatches() {
        if (homeMatches == null) {
            homeMatches = new ArrayList<>();
        }
        return homeMatches;
    }

    public void setHomeMatches(List<SquashMatch> homeMatches) {
        this.homeMatches = homeMatches;
    }

    public List<SquashMatch> getAwayMatches() {
        if (awayMatches == null) {
            awayMatches = new ArrayList<>();
        }
        return awayMatches;
    }

    public void setAwayMatches(List<SquashMatch> awayMatches) {
        this.awayMatches = awayMatches;
    }

    @Override
    public String toString() {
        return "Player{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", availabilityNotes='" + availabilityNotes + '\'' +
                ", division=" + division +
                '}';
    }
}
