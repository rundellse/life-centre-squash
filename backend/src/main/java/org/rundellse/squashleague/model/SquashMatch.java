package org.rundellse.squashleague.model;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
public class SquashMatch {

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne
    private Season season;
    private Integer division;

    @ManyToOne
    @JoinColumn(name = "home_player_id")
    private Player homePlayer;
    @ManyToOne
    @JoinColumn(name = "away_player_id")
    private Player awayPlayer;

    @Column(name = "home_player_points")
    private Integer homePlayerPoints;
    @Column(name = "away_player_points")
    private Integer awayPlayerPoints;

    @Column(name = "home_player_match_score")
    private Integer homePlayerMatchScore;
    @Column(name = "away_player_match_score")
    private Integer awayPlayerMatchScore;

    @Transient
    private List<SquashGame> squashGames;


    public SquashMatch() {
    }

    public SquashMatch(Season season, Integer division, Player homePlayer, Player awayPlayer, Integer homePlayerPoints, Integer awayPlayerPoints) {
        this.season = season;
        this.division = division;
        this.homePlayer = homePlayer;
        this.awayPlayer = awayPlayer;
        this.homePlayerPoints = homePlayerPoints;
        this.awayPlayerPoints = awayPlayerPoints;
    }

    public SquashMatch(Long id, Season season, Player homePlayer, Player awayPlayer, Integer homePlayerPoints, Integer awayPlayerPoints, Integer homePlayerMatchScore, Integer awayPlayerMatchScore, List<SquashGame> games) {
        this.id = id;
        this.season = season;
        this.homePlayer = homePlayer;
        this.awayPlayer = awayPlayer;
        this.homePlayerPoints = homePlayerPoints;
        this.awayPlayerPoints = awayPlayerPoints;
        this.homePlayerMatchScore = homePlayerMatchScore;
        this.awayPlayerMatchScore = awayPlayerMatchScore;
        this.squashGames = games;
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Season getSeason() {
        return season;
    }

    public void setSeason(Season season) {
        this.season = season;
    }

    public Integer getDivision() {
        return division;
    }

    public void setDivision(Integer division) {
        this.division = division;
    }

    public Player getHomePlayer() {
        return homePlayer;
    }

    public void setHomePlayer(Player homePlayer) {
        this.homePlayer = homePlayer;
    }

    public Player getAwayPlayer() {
        return awayPlayer;
    }

    public void setAwayPlayer(Player awayPlayer) {
        this.awayPlayer = awayPlayer;
    }

    public Integer getHomePlayerPoints() {
        return homePlayerPoints;
    }

    public void setHomePlayerPoints(Integer homePlayerPoints) {
        this.homePlayerPoints = homePlayerPoints;
    }

    public Integer getAwayPlayerPoints() {
        return awayPlayerPoints;
    }

    public void setAwayPlayerPoints(Integer awayPlayerPoints) {
        this.awayPlayerPoints = awayPlayerPoints;
    }

    public Integer getHomePlayerMatchScore() {
        return homePlayerMatchScore;
    }

    public void setHomePlayerMatchScore(Integer homePlayerMatchScore) {
        this.homePlayerMatchScore = homePlayerMatchScore;
    }

    public Integer getAwayPlayerMatchScore() {
        return awayPlayerMatchScore;
    }

    public void setAwayPlayerMatchScore(Integer awayPlayerMatchScore) {
        this.awayPlayerMatchScore = awayPlayerMatchScore;
    }

    public List<SquashGame> getSquashGames() {
        if (squashGames == null) {
            squashGames = new ArrayList<>();
        }
        return squashGames;
    }

    public void setSquashGames(List<SquashGame> squashGames) {
        this.squashGames = squashGames;
    }

    @Override
    public String toString() {
        return "SquashMatch{" +
                "id=" + id +
                ", season=" + season +
                ", division=" + division +
                ", homePlayer=" + homePlayer.getId() +
                ", awayPlayer=" + awayPlayer.getId() +
                ", homePlayerPoints=" + homePlayerPoints +
                ", awayPlayerPoints=" + awayPlayerPoints +
                ", homePlayerMatchScore=" + homePlayerMatchScore +
                ", awayPlayerMatchScore=" + awayPlayerMatchScore +
                ", squashGames=" + squashGames +
                '}';
    }
}
