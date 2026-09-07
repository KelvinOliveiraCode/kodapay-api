package com.kodapay.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Teste de integração: sobe o contexto Spring completo e exercita os
 * endpoints com payloads reais (JSON), cobrindo validação, taxas, cadeia de
 * risco, persistência e tratamento de erros.
 */
@SpringBootTest
@AutoConfigureMockMvc
class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String body(String payer, String amount, String method) {
        return """
                {
                  "payerName": "%s",
                  "description": "compra no site",
                  "amount": %s,
                  "method": "%s"
                }
                """.formatted(payer, amount, method);
    }

    @Test
    @DisplayName("POST cria pagamento Pix com taxa fixa e total corretos (201)")
    void createPixPayment() throws Exception {
        mockMvc.perform(post("/api/v1/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("Kelvin Oliveira", "37.50", "PIX")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.payment.method").value("PIX"))
                .andExpect(jsonPath("$.payment.fee").value(0.49))
                .andExpect(jsonPath("$.payment.total").value(37.99))
                .andExpect(jsonPath("$.auditTrail.length()").value(3));
    }

    @Test
    @DisplayName("POST crédito R$100 aplica 3,99% (201)")
    void createCreditPayment() throws Exception {
        mockMvc.perform(post("/api/v1/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("Maria Souza", "100.00", "CREDIT_CARD")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.payment.fee").value(3.99))
                .andExpect(jsonPath("$.payment.total").value(103.99));
    }

    @Test
    @DisplayName("POST acima de R$15.000 devolve 422 com motivo do veto")
    void highValueRejected() throws Exception {
        mockMvc.perform(post("/api/v1/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("Maria Souza", "15000.01", "PIX")))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.detail").value(
                        org.hamcrest.Matchers.containsString("limite")));
    }

    @Test
    @DisplayName("POST pagador restrito devolve 422")
    void blocklistedPayerRejected() throws Exception {
        mockMvc.perform(post("/api/v1/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("Fraudador da Silva", "100.00", "PIX")))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    @DisplayName("POST sem campos obrigatórios devolve 400")
    void missingFieldsRejected() throws Exception {
        mockMvc.perform(post("/api/v1/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST valor zero/negativo devolve 400")
    void zeroAmountRejected() throws Exception {
        mockMvc.perform(post("/api/v1/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("Maria Souza", "0", "PIX")))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET por id devolve a transação criada; id inexistente dá 404")
    void getPaymentById() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("Kelvin Oliveira", "10.00", "BOLETO")))
                .andExpect(status().isCreated())
                .andReturn();

        String id = objectMapper.readTree(result.getResponse().getContentAsString())
                .get("payment").get("id").asText();

        mockMvc.perform(get("/api/v1/payments/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.method").value("BOLETO"));

        mockMvc.perform(get("/api/v1/payments/"
                        + java.util.UUID.randomUUID()))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET histórico lista transações")
    void historyEndpoint() throws Exception {
        mockMvc.perform(post("/api/v1/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("Listagem Teste", "5.00", "PIX")))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/payments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(
                        org.hamcrest.Matchers.greaterThanOrEqualTo(1)));
    }
}
