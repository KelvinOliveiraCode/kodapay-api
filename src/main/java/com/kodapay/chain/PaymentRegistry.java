package com.kodapay.chain;

import com.kodapay.domain.Payment;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Padrão <b>Singleton</b> (GoF criacional) com um objetivo real: dentro de uma
 * mesma JVM, existe um único registro de transações por aplicação. O Spring
 * garante a instância única por padrão de escopo singleton; aqui também
 * assumimos explicitamente essa responsabilidade documentando o contrato.
 *
 * <p>Serve de repositório em memória (padrão <b>Repository</b> da JPA
 * reimaginado sem banco) e mantém o histórico usado pela regra de
 * velocidade (anti-fraude) do Chain of Responsibility.</p>
 */
@Component
public class PaymentRegistry {

    private final List<Payment> payments = new CopyOnWriteArrayList<>();
    private final ConcurrentHashMap<UUID, Payment> byId = new ConcurrentHashMap<>();

    /** Salva (ou atualiza) uma transação. */
    public Payment save(Payment payment) {
        byId.put(payment.id(), payment);
        payments.removeIf(p -> p.id().equals(payment.id()));
        payments.add(payment);
        return payment;
    }

    /** Busca por id. */
    public Optional<Payment> findById(UUID id) {
        return Optional.ofNullable(byId.get(id));
    }

    /** Todas as transações, na ordem de criação. */
    public List<Payment> findAll() {
        return List.copyOf(payments);
    }

    /** Transações do pagador desde {@code since} (para a regra de velocidade). */
    public long countByPayerSince(String payerName, OffsetDateTime since) {
        return payments.stream()
                .filter(p -> p.payerName().equalsIgnoreCase(payerName))
                .filter(p -> p.createdAt().isAfter(since))
                .count();
    }
}
