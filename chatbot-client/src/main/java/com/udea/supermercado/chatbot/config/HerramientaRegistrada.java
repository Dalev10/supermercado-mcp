package com.udea.supermercado.chatbot.config;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.mcp.SyncMcpToolCallback;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.definition.ToolDefinition;
import org.springframework.ai.tool.metadata.ToolMetadata;

/**
 * Decorador de una herramienta MCP: la ejecuta igual, pero anota cada llamada para que la pagina
 * pueda mostrar que herramientas uso el modelo en una respuesta.
 */
public class HerramientaRegistrada implements ToolCallback {

    /** El resultado no viaja al navegador; se usa para recordarle al modelo lo que ya hizo en la conversacion. */
    public record Llamada(String herramienta, String argumentos, boolean error, @JsonIgnore String resultado) {
    }

    // El modelo y sus herramientas se ejecutan en el hilo de la peticion HTTP (ChatClient.call() es bloqueante)
    private static final ThreadLocal<List<Llamada>> LLAMADAS = ThreadLocal.withInitial(ArrayList::new);
    private static final ObjectMapper JSON = new ObjectMapper();

    private final ToolCallback delegada;
    private final String nombre;

    public HerramientaRegistrada(ToolCallback delegada) {
        this.delegada = delegada;
        // Nombre tal como lo publica el servidor MCP, sin el prefijo que pueda añadir el cliente
        this.nombre = delegada instanceof SyncMcpToolCallback mcp
                ? mcp.getOriginalToolName() : delegada.getToolDefinition().name();
    }

    /** Devuelve las llamadas del hilo actual y limpia el registro. */
    public static List<Llamada> tomarLlamadas() {
        List<Llamada> llamadas = List.copyOf(LLAMADAS.get());
        LLAMADAS.remove();
        return llamadas;
    }

    @Override
    public ToolDefinition getToolDefinition() {
        return delegada.getToolDefinition();
    }

    @Override
    public ToolMetadata getToolMetadata() {
        return delegada.getToolMetadata();
    }

    @Override
    public String call(String argumentos) {
        return call(argumentos, null);
    }

    @Override
    public String call(String argumentos, ToolContext contexto) {
        try {
            String resultado = contexto == null ? delegada.call(argumentos) : delegada.call(argumentos, contexto);
            LLAMADAS.get().add(new Llamada(nombre, argumentos, false, resultado));
            return resultado;
        } catch (RuntimeException e) {
            // Un error de negocio del servidor MCP (isError=true) llega aqui como excepcion. Se devuelve al
            // modelo como JSON porque Gemini rechaza un resultado de herramienta que no sea JSON valido.
            String error = errorComoJson(e.getMessage());
            LLAMADAS.get().add(new Llamada(nombre, argumentos, true, error));
            return error;
        }
    }

    private static final Pattern TEXTO_MCP = Pattern.compile("text=(.*?), meta=", Pattern.DOTALL);

    /** Extrae el mensaje del servidor de "Error calling tool: [TextContent[annotations=null, text=..., meta=null]]". */
    static String errorComoJson(String mensajeExcepcion) {
        String mensaje = String.valueOf(mensajeExcepcion);
        Matcher texto = TEXTO_MCP.matcher(mensaje);
        try {
            return JSON.writeValueAsString(Map.of("error", texto.find() ? texto.group(1) : mensaje));
        } catch (JsonProcessingException imposible) {
            return "{\"error\":\"La herramienta falló.\"}";
        }
    }
}
