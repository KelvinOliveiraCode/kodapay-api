package com.kodapay.chain;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Monta a infraestrutura da cadeia: coleta os handlers na ordem definida
 * pelas anotações {@code @Order} das classes (valor → lista restrita →
 * velocidade) e expõe o factory que criará uma cadeia por pagamento.
 *
 * <p>Regra nova = classe {@code @Component @Order(n)} nova; nada aqui muda.</p>
 */
@Configuration
public class RiskChainConfig {

    @Bean
    public RiskChainFactory riskChainFactory(List<RiskHandler> handlers) {
        return new RiskChainFactory(handlers);
    }
}
