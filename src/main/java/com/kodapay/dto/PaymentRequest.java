package com.kodapay.dto;

import com.kodapay.domain.PaymentMethod;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/** Payload de criação de pagamento. */
public record PaymentRequest(
        @NotBlank @Size(min = 2, max = 80) String payerName,
        @NotBlank @Size(min = 2, max = 120) String description,
        @NotNull @DecimalMin(value = "0.01") BigDecimal amount,
        @NotNull PaymentMethod method
) {
}
