package org.rundellse.squashleague.model;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class Player {

    private Long id;

    private String name;

    private String email;

    private String phoneNumber;

    private String availabilityNotes;

    private Integer division;

    private List<SquashMatch> results;


    public static Comparator<Player> PLAYER_POINTS_COMPARATOR = (p1, p2) -> {
        Integer p1Total = 0;
        Integer p2Total = 0;
        for (SquashMatch squashMatch : p1.getResults()) {
            p1Total += Objects.equals(p1.id, squashMatch.homePlayer().getId()) ? squashMatch.homePlayerPoints() : squashMatch.awayPlayerPoints();
        }
        for (SquashMatch squashMatch : p2.getResults()) {
            p2Total += Objects.equals(p2.id, squashMatch.homePlayer().getId()) ? squashMatch.homePlayerPoints() : squashMatch.awayPlayerPoints();
        }
        // If p1 is higher it should be placed first. Negative return places p1 first. So if p1 is larger negative should be returned, hence p2-p1.
        return p2Total - p1Total;
    };


    public Player(Long id, String name, String email, String phoneNumber, String availabilityNotes, Integer division, List<SquashMatch> results) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.availabilityNotes = availabilityNotes;
        this.division = division;
        this.results = results;
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

    public List<SquashMatch> getResults() {
        if (results == null) {
            results = new ArrayList<>();
        }
        return results;
    }

    public void setResults(List<SquashMatch> results) {
        this.results = results;
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
