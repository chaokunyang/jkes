package com.timeyang.jkes.services.search.advice;

import com.timeyang.jkes.services.search.api.ApiPackage;
import com.timeyang.jkes.services.search.exception.SearchException;
import com.timeyang.jkes.services.search.exception.SearchResponseException;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;

/**
 * @author chaokunyang
 */
@ControllerAdvice(basePackageClasses = ApiPackage.class)
public class EndpointAdvice {

    @ExceptionHandler(SearchResponseException.class)
    @ResponseBody
    ResponseEntity<?> handleResponseException(SearchResponseException ex) {
        int statusCode = ex.getResponseException().getResponse().getStatusLine().getStatusCode();
        HttpStatus status = HttpStatus.valueOf(statusCode);
        return new ResponseEntity<>(new EndpointError(status.value(), ex.getErrorMessage()), status);
    }

    @ExceptionHandler(value = { SearchException.class, RuntimeException.class })
    @ResponseBody
    ResponseEntity<?> handleException(HttpServletRequest request, Throwable ex) {
        HttpStatus status = getStatus(request);
        return new ResponseEntity<>(new EndpointError(status.value(), ex.getMessage()), status);
    }

    private HttpStatus getStatus(HttpServletRequest request) {
        Integer statusCode = (Integer) request.getAttribute("javax.servlet.error.status_code");
        if (statusCode == null) {
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }
        return HttpStatus.valueOf(statusCode);
    }

    @Data
    @AllArgsConstructor
    public static class EndpointError {
        private int statusCode;
        private Object message;
    }
}
