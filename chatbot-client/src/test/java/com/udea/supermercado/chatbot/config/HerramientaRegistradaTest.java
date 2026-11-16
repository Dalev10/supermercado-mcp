package com.udea.supermercado.chatbot.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class HerramientaRegistradaTest {

    @Test
    void elErrorDelServidorMcpSeEntregaAlModeloComoJsonConSoloElMensaje() {
        String excepcion = "Error calling tool: [TextContent[annotations=null, "
                + "text=Stock insuficiente de Mantequilla 250 g: se pidieron 5 y hay 2., meta=null]]";

        assertThat(HerramientaRegistrada.errorComoJson(excepcion))
                .isEqualTo("{\"error\":\"Stock insuficiente de Mantequilla 250 g: se pidieron 5 y hay 2.\"}");
    }

    @Test
    void unErrorConOtroFormatoSeEntregaCompletoYConComillasEscapadas() {
        assertThat(HerramientaRegistrada.errorComoJson("Connection refused \"8888\""))
                .isEqualTo("{\"error\":\"Connection refused \\\"8888\\\"\"}");
        assertThat(HerramientaRegistrada.errorComoJson(null)).isEqualTo("{\"error\":\"null\"}");
    }
}
