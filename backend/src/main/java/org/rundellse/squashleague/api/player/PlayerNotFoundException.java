package org.rundellse.squashleague.api.player;

public class PlayerNotFoundException extends RuntimeException {

    public PlayerNotFoundException(Long id) {
        super("Player can not be found with ID: " + id);
    }

    public PlayerNotFoundException(Long id, Throwable cause) {
        super("Player can not be found with ID: " + id, cause);
    }

    public PlayerNotFoundException(Throwable cause) {
        super(cause);
    }

}
