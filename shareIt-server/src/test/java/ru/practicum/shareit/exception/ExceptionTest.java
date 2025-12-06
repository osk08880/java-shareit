package ru.practicum.shareit.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ExceptionTest {

    @Test
    void runtimeExceptions_constructorsWork() {
        assertThrows(DuplicateEmailException.class, () ->
        {
            throw new DuplicateEmailException("duplicate");
        });
        assertThrows(MappingException.class, () ->
        {
            throw new MappingException("mapping");
        });
        assertThrows(NotFoundException.class, () ->
        {
            throw new NotFoundException("not found");
        });
    }

    @Test
    void globalExceptionHandler_handlesNotFound() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        NotFoundException ex = new NotFoundException("not found");

        ResponseEntity<Map<String, String>> response = handler.handleNotFound(ex);

        assertThat(response.getStatusCodeValue()).isEqualTo(404);
        assertThat(response.getBody()).containsEntry("error", "not found");
    }

    @Test
    void globalExceptionHandler_handlesResponseStatus() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        ResponseStatusException ex = new ResponseStatusException(org.springframework.http.HttpStatus.FORBIDDEN, "forbidden");

        ResponseEntity<Map<String, String>> response = handler.handleResponseStatus(ex);

        assertThat(response.getStatusCodeValue()).isEqualTo(403);
        assertThat(response.getBody()).containsEntry("error", "forbidden");
    }

    @Test
    void globalExceptionHandler_handlesValidation() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);

        FieldError fieldError = new FieldError("object", "field", "must not be blank");
        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getAllErrors()).thenReturn(java.util.List.of(fieldError));

        ResponseEntity<Map<String, String>> response = handler.handleValidation(ex);

        assertThat(response.getStatusCodeValue()).isEqualTo(400);
        assertThat(response.getBody()).containsEntry("error", "field: must not be blank");
    }

    @Test
    void globalExceptionHandler_handlesGenericException() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        Exception ex = new Exception("something went wrong");

        ResponseEntity<Object> response = handler.handleAll(ex);

        assertThat(response.getStatusCodeValue()).isEqualTo(500);
        assertThat(response.getBody()).isInstanceOf(Map.class);
        assertThat(((Map<?, ?>) response.getBody()).get("error")).isEqualTo("something went wrong");
    }
}
