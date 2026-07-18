package com.fulfillflow.common;

import com.fulfillflow.catalog.DuplicateSkuException;
import com.fulfillflow.catalog.InsufficientStockException;
import com.fulfillflow.catalog.ProductNotFoundException;
import com.fulfillflow.inventory.ReservationNotFoundException;
import com.fulfillflow.order.OrderNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
class ApiExceptionHandler {

    @ExceptionHandler({ProductNotFoundException.class, ReservationNotFoundException.class, OrderNotFoundException.class})
    ProblemDetail handleNotFound(RuntimeException exception) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, exception.getMessage());
    }

    @ExceptionHandler(DuplicateSkuException.class)
    ProblemDetail handleConflict(DuplicateSkuException exception) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, exception.getMessage());
    }

    @ExceptionHandler(InsufficientStockException.class)
    ProblemDetail handleInsufficientStock(InsufficientStockException exception) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, exception.getMessage());
    }
}
