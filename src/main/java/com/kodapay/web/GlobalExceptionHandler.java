package com.kodapay.web;

import com.kodapay.exception.InvalidPaymentException;
import com.kodapay.exception.PaymentNotFoundException;
import com.kodapay.exception.RiskViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** Traduz exceções de domínio em respostas HTTP sem poluir o controller. */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** Payload inválido → 400. */
    @ExceptionHandler(InvalidPaymentException.class)
    public ProblemDetail invalid(InvalidPaymentException ex) {
        return problem(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    /** Bean Validation → 400 com o primeiro erro de campo. */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail validation(MethodArgumentNotValidException ex) {
        String msg = ex.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .orElse("Payload inválido.");
        return problem(HttpStatus.BAD_REQUEST, msg);
    }

    /** Veto de risco → 422. */
    @ExceptionHandler(RiskViolationException.class)
    public ProblemDetail risk(RiskViolationException ex) {
        return problem(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage());
    }

    /** Inexistente → 404. */
    @ExceptionHandler(PaymentNotFoundException.class)
    public ProblemDetail notFound(PaymentNotFoundException ex) {
        return problem(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    private ProblemDetail problem(HttpStatus status, String message) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(status, message);
        pd.setTitle(status.getReasonPhrase());
        return pd;
    }
}
