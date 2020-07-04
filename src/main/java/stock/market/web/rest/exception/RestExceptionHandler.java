package stock.market.web.rest.exception;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import stock.market.core.gateway.exception.TradingGatewayNotOpenException;
import stock.market.core.gateway.exception.TradingGatewayTimeoutException;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@ControllerAdvice
public class RestExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler({TradingGatewayTimeoutException.class})
    protected ResponseEntity<Object> handleTradingGatewayTimeoutException(
            Exception ex, WebRequest request) {
        return handleExceptionInternal(ex, "The Trading Gateway did not process the request",
                new HttpHeaders(), HttpStatus.REQUEST_TIMEOUT, request);
    }

    @ExceptionHandler({TradingGatewayNotOpenException.class})
    protected ResponseEntity<Object> handleTradingGatewayNotOpenException(
            Exception ex, WebRequest request) {
        return handleExceptionInternal(ex, "The Trading Gateway is not open",
                new HttpHeaders(), HttpStatus.REQUEST_TIMEOUT, request);
    }

    // error handle for @Valid
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                  HttpHeaders headers,
                                                                  HttpStatus status, WebRequest request) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", new Date());
        body.put("status", status.value());

        //Get all errors
        List<String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(x -> x.getField() + " " + x.getDefaultMessage())
                .collect(Collectors.toList());

        body.put("errors", errors);

        return new ResponseEntity<>(body, headers, status);

    }
}