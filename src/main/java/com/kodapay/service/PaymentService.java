package com.kodapay.service;

import com.kodapay.chain.PaymentRegistry;
import com.kodapay.chain.RiskChainFactory;
import com.kodapay.domain.AuditEvent;
import com.kodapay.domain.Payment;
import com.kodapay.domain.PaymentMethod;
import com.kodapay.domain.PaymentStatus;
import com.kodapay.dto.PaymentRequest;
import com.kodapay.dto.PaymentResponse;
import com.kodapay.dto.PaymentView;
import com.kodapay.exception.InvalidPaymentException;
import com.kodapay.exception.PaymentNotFoundException;
import com.kodapay.observer.PaymentEvent;
import com.kodapay.observer.PaymentEventPublisher;
import com.kodapay.strategy.FeeCalculator;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Padrão <b>Facade</b> (GoF estrutural): um método único —
 * {@code process(...)} — esconde do controller toda a coordenação entre
 * subsistemas: taxa (Strategy), risco (Chain of Responsibility),
 * persistência (Registry/Singleton) e notificações (Observer).
 */
@Service
public class PaymentService {

    private final Map<PaymentMethod, FeeCalculator> calculators;
    private final RiskChainFactory chainFactory;
    private final PaymentRegistry registry;
    private final PaymentEventPublisher events;

    public PaymentService(List<FeeCalculator> calculators, RiskChainFactory chainFactory,
                          PaymentRegistry registry, PaymentEventPublisher events) {
        this.calculators = calculators.stream()
                .collect(Collectors.toMap(FeeCalculator::supports, Function.identity()));
        this.chainFactory = chainFactory;
        this.registry = registry;
        this.events = events;
    }

    /**
     * Pipeline de processamento: valida payload → calcula taxa (Strategy) →
     * monta a transação → roda a cadeia de risco (CoR) → persiste →
     * notifica observadores. Qualquer veto interrompe tudo.
     */
    public PaymentResponse process(PaymentRequest request) {
        validar(request);

        FeeCalculator calculator = calculators.get(request.method());
        if (calculator == null) {
            throw new InvalidPaymentException(
                    "Sem estratégia de taxa para " + request.method());
        }

        BigDecimal fee = calculator.calculateFee(request.amount());
        BigDecimal total = request.amount().add(fee).setScale(2, RoundingMode.HALF_EVEN);

        Payment payment = new Payment(
                UUID.randomUUID(),
                request.payerName().trim(),
                request.description().trim(),
                request.amount(),
                request.method(),
                fee,
                total,
                PaymentStatus.APPROVED,
                OffsetDateTime.now());

        List<AuditEvent> audit = chainFactory.newChain().run(payment);
        registry.save(payment);
        events.publish(new PaymentEvent(payment,
                "aprovado: R$ " + total + " via " + payment.method()));

        return PaymentResponse.from(payment, audit);
    }

    /** Busca uma transação por id (404 quando inexistente). */
    public PaymentView findById(UUID id) {
        return registry.findById(id)
                .map(PaymentView::from)
                .orElseThrow(() -> new PaymentNotFoundException(id));
    }

    /** Histórico completo, do mais recente para o mais antigo. */
    public List<PaymentView> history() {
        return registry.findAll().stream()
                .map(PaymentView::from)
                .collect(Collectors.toList());
    }

    private void validar(PaymentRequest request) {
        if (request.amount() == null || request.amount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidPaymentException("Valor deve ser maior que zero.");
        }
    }
}
