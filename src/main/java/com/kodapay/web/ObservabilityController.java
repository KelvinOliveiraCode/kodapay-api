package com.kodapay.web;

import com.kodapay.observer.MessagingListener;
import com.kodapay.observer.MetricsListener;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * Torna o efeito do padrão <b>Observer</b> visível: os listeners reagem aos
 * pagamentos publicados e este endpoint expõe o que cada um acumulou
 * (mensageria e métricas), provando o desacoplamento evento → reação.
 */
@RestController
@RequestMapping("/api/v1/observability")
public class ObservabilityController {

    private final MessagingListener messaging;
    private final MetricsListener metrics;

    public ObservabilityController(MessagingListener messaging, MetricsListener metrics) {
        this.messaging = messaging;
        this.metrics = metrics;
    }

    /** Efeitos colaterais produzidos pelos observadores até agora. */
    @GetMapping
    public Map<String, List<String>> snapshot() {
        return Map.of(
                "notifications", messaging.sentNotifications(),
                "metrics", metrics.collectedMetrics());
    }
}
