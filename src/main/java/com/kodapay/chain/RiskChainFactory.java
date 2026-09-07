package com.kodapay.chain;

import java.util.List;

/**
 * Cria uma {@link RiskChain} nova para cada pagamento processado.
 *
 * <p>A cadeia mantém estado por execução (posição atual e trilha de auditoria);
 * se fosse um bean singleton, a segunda requisição encontraria o ponteiro no
 * fim e nenhuma checagem rodaria — além de condição de corrida entre
 * requisições concorrentes. O factory isola esse estado por chamada.</p>
 */
public class RiskChainFactory {

    private final List<RiskHandler> handlers;

    public RiskChainFactory(List<RiskHandler> handlers) {
        this.handlers = List.copyOf(handlers);
    }

    /** Cadeia nova, pronta para processar uma transação. */
    public RiskChain newChain() {
        return new RiskChain(handlers);
    }
}
