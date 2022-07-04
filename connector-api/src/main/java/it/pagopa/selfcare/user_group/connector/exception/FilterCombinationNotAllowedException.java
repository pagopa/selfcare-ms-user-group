package it.pagopa.selfcare.user_group.connector.exception;

public class FilterCombinationNotAllowedException extends RuntimeException {
    public FilterCombinationNotAllowedException(String message) {
        super(message);
    }
}
