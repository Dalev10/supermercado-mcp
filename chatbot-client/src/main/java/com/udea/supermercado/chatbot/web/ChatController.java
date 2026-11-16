package com.udea.supermercado.chatbot.web;

import com.udea.supermercado.chatbot.config.HerramientaRegistrada;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/chat")
public class ChatController {
    private static final Logger log = LoggerFactory.getLogger(ChatController.class);

    public record Peticion(
            @NotBlank(message = "Falta el identificador de la conversación.") @Size(max = 64) String conversacionId,
            @NotBlank(message = "Escribe un mensaje.")
            @Size(max = 1000, message = "El mensaje no puede superar 1000 caracteres.") String mensaje) {
    }

    public record Respuesta(String respuesta, List<HerramientaRegistrada.Llamada> herramientas) {
    }

    private final ChatClient chatClient;

    public ChatController(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    @PostMapping
    public Respuesta chat(@Valid @RequestBody Peticion peticion) {
        HerramientaRegistrada.tomarLlamadas(); // el hilo viene de un pool: se descarta lo que hubiera quedado
        try {
            ChatResponse respuesta = chatClient.prompt()
                    .user(peticion.mensaje())
                    .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, peticion.conversacionId()))
                    .call()
                    .chatResponse();
            // Gemini puede repartir el texto en varias partes y Spring AI crea una generacion por parte:
            // content() solo devolveria la primera y la respuesta saldria cortada
            String texto = respuesta == null ? "" : respuesta.getResults().stream()
                    .map(g -> g.getOutput().getText())
                    .filter(Objects::nonNull)
                    .collect(Collectors.joining());
            if (texto.isBlank()) {
                texto = "No pude generar una respuesta. ¿Puedes reformular tu mensaje?";
            }
            return new Respuesta(texto, HerramientaRegistrada.tomarLlamadas());
        } finally {
            HerramientaRegistrada.tomarLlamadas();
        }
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> entradaInvalida(MethodArgumentNotValidException e) {
        return Map.of("error", e.getBindingResult().getFieldErrors().stream()
                .map(f -> f.getDefaultMessage()).findFirst().orElse("La petición no es válida."));
    }

    /** El detalle tecnico queda en el log; al navegador solo va un mensaje amable, sin trazas ni claves. */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public Map<String, String> fallo(Exception e) {
        log.error("Fallo al atender el chat", e);
        return Map.of("error", mensajeAmable(e));
    }

    static String mensajeAmable(Throwable e) {
        StringBuilder cadena = new StringBuilder();
        for (Throwable t = e; t != null && t != t.getCause(); t = t.getCause()) {
            cadena.append(t.getClass().getSimpleName()).append(' ').append(t.getMessage()).append(' ');
        }
        String detalle = cadena.toString().toLowerCase();
        if (detalle.contains("429") || detalle.contains("resource_exhausted") || detalle.contains("quota")) {
            return "Se agotó por ahora la cuota gratuita de Gemini. Espera un minuto e inténtalo de nuevo.";
        }
        if (detalle.contains("401") || detalle.contains("403") || detalle.contains("api key")
                || detalle.contains("permission_denied")) {
            return "La clave de Gemini no es válida o no está configurada. Revisa GEMINI_API_KEY en el archivo .env.";
        }
        if (detalle.contains("timeout") || detalle.contains("timed out")) {
            return "La respuesta tardó demasiado. Inténtalo de nuevo en un momento.";
        }
        if (detalle.contains("mcp") || detalle.contains("connect") || detalle.contains("8888")) {
            return "No pude comunicarme con el servidor del supermercado. "
                    + "Comprueba que el servidor MCP esté encendido y reinicia el chatbot.";
        }
        return "Ocurrió un problema al procesar tu mensaje. Inténtalo de nuevo.";
    }
}
