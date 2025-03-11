package org.rundellse.squashleague.api.player;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class PlayerNotFoundAdvice {

    private static Logger LOG = LoggerFactory.getLogger(PlayerNotFoundAdvice.class.getName());


//    /**
//     * Default, backup exception handling. Throws the most generic possible information to the client.
//     * Generally exceptions should be dealt with with ResponseStatusExceptions.
//     */
//    @ExceptionHandler(RuntimeException.class)
//    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
//    public String defaultExceptionHandler(RuntimeException runtimeException) {
//        LOG.error("Error thrown up to default exception handler: \n" + runtimeException);
//        return "Unexpected exception encountered during server processing. Exception type: " + runtimeException.getClass().getName();
//    }

}
