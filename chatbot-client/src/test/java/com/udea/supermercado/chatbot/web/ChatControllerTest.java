package com.udea.supermercado.chatbot.web;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.ConnectException;
import org.junit.jupiter.api.Test;

/** El navegador nunca recibe trazas ni claves: cada fallo tecnico se traduce a un mensaje amable. */
class ChatControllerTest {

    @Test
    void cuotaAgotadaDeGemini() {
        assertThat(ChatController.mensajeAmable(new RuntimeException("429 RESOURCE_EXHAUSTED: quota exceeded")))
                .contains("cuota");
    }

    @Test
    void claveInvalida() {
        assertThat(ChatController.mensajeAmable(new RuntimeException("403 PERMISSION_DENIED")))
                .contains("GEMINI_API_KEY");
    }

    @Test
    void servidorMcpCaidoAunqueElErrorVengaAnidado() {
        Exception anidada = new RuntimeException("Tool execution failed",
                new ConnectException("Connection refused: localhost/127.0.0.1:8888"));
        assertThat(ChatController.mensajeAmable(anidada)).contains("servidor del supermercado");
    }

    @Test
    void cualquierOtroFalloNoRevelaElDetalle() {
        assertThat(ChatController.mensajeAmable(new IllegalStateException("detalle interno AIzaSecreta")))
                .doesNotContain("AIzaSecreta").contains("Inténtalo de nuevo");
    }
}
