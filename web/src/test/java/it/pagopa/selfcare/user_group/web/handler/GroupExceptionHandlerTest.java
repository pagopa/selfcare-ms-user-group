package it.pagopa.selfcare.user_group.web.handler;

import it.pagopa.selfcare.commons.web.model.ErrorResource;
import it.pagopa.selfcare.user_group.connector.exception.ResourceAlreadyExistsException;
import it.pagopa.selfcare.user_group.connector.exception.ResourceNotFoundException;
import it.pagopa.selfcare.user_group.connector.exception.ResourceUpdateException;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class GroupExceptionHandlerTest {
    private static final String DETAIL_MESSAGE = "detail message";
    private final GroupExceptionHandler handler = new GroupExceptionHandler();


    @Test
    void resourceNotFoundException() {
        //given
        ResourceNotFoundException mockException = Mockito.mock(ResourceNotFoundException.class);
        Mockito.when(mockException.getMessage())
                .thenReturn(DETAIL_MESSAGE);
        //when
        ErrorResource response = handler.handleResourceNotFoundException(mockException);
        //then
        assertNotNull(response);
        assertEquals(DETAIL_MESSAGE, response.getMessage());
    }

    @Test
    void resourceAlreadyExistsException() {
        //given
        ResourceAlreadyExistsException mockException = Mockito.mock(ResourceAlreadyExistsException.class);
        Mockito.when(mockException.getMessage())
                .thenReturn(DETAIL_MESSAGE);
        //when
        ErrorResource response = handler.handleResourceAlreadyExistsException(mockException);
        //then
        assertNotNull(response);
        assertEquals(DETAIL_MESSAGE, response.getMessage());
    }

    @Test
    void resourceUpdateException() {
        //given
        ResourceUpdateException mockException = Mockito.mock(ResourceUpdateException.class);
        Mockito.when(mockException.getMessage())
                .thenReturn(DETAIL_MESSAGE);
        //when
        ErrorResource response = handler.handleResourceUpdateException(mockException);
        //then
        assertNotNull(response);
        assertEquals(DETAIL_MESSAGE, response.getMessage());
    }
}