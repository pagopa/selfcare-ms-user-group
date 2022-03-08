package it.pagopa.selfcare.user_group.web.handler;

import it.pagopa.selfcare.commons.web.model.ErrorResource;
import it.pagopa.selfcare.user_group.connector.exception.ResourceAlreadyExistsException;
import it.pagopa.selfcare.user_group.connector.exception.ResourceNotFoundException;
import it.pagopa.selfcare.user_group.web.controller.GroupController;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import static it.pagopa.selfcare.commons.web.handler.RestExceptionsHandler.UNHANDLED_EXCEPTION;

@ControllerAdvice(assignableTypes = GroupController.class)
@Slf4j
public class GroupExceptionHandler {

    @ExceptionHandler({ResourceAlreadyExistsException.class})
    @ResponseStatus(HttpStatus.CONFLICT)
    @ResponseBody
    ErrorResource handleResourceAlreadyExistsException(ResourceAlreadyExistsException e) {
        log.warn(UNHANDLED_EXCEPTION, e);
        return new ErrorResource(e.getMessage());
    }


    @ExceptionHandler({ResourceNotFoundException.class})
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ResponseBody
    ErrorResource handleResourceNotFoundException(ResourceNotFoundException e) {
        log.warn(UNHANDLED_EXCEPTION, e);
        return new ErrorResource(e.getMessage());
    }
}
