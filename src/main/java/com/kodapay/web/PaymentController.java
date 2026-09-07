package com.kodapay.web;

import com.kodapay.dto.PaymentRequest;
import com.kodapay.dto.PaymentResponse;
import com.kodapay.dto.PaymentView;
import com.kodapay.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * Camada web: fina, sem regra de negócio — tudo é delegado ao Facade
 * ({@code PaymentService}), que concentra a coordenação dos padrões.
 */
@RestController
@RequestMapping("/api/v1/payments")
@Tag(name = "Payments", description = "Processamento de pagamentos KodaPay")
public class PaymentController {

    private final PaymentService service;

    public PaymentController(PaymentService service) {
        this.service = service;
    }

    /** Processa um pagamento (Strategy + Chain + Observer via Facade). */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Processa um pagamento",
            description = "Calcula a taxa (Strategy), avalia risco (Chain of "
                    + "Responsibility) e notifica observadores (Observer).")
    public PaymentResponse process(@Valid @RequestBody PaymentRequest request) {
        return service.process(request);
    }

    /** Detalhe de uma transação. */
    @GetMapping("/{id}")
    @Operation(summary = "Busca um pagamento por id")
    public PaymentView get(@PathVariable UUID id) {
        return service.findById(id);
    }

    /** Histórico de transações. */
    @GetMapping
    @Operation(summary = "Lista todas as transações")
    public List<PaymentView> history() {
        return service.history();
    }
}
