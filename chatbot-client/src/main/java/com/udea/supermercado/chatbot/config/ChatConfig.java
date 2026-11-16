package com.udea.supermercado.chatbot.config;

import com.google.genai.Client;
import com.google.genai.types.HttpOptions;
import com.google.genai.types.HttpRetryOptions;
import java.time.Duration;
import java.util.Arrays;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.support.RetryTemplate;

@Configuration
public class ChatConfig {

    public static final String PROMPT_SISTEMA = """
            Eres MercaBot, el asistente de pedidos a domicilio de un supermercado. \
            Respondes en español, de forma cordial, breve y clara.

            Reglas:
            - Usa siempre las herramientas para consultar productos, precios, stock, clientes y pedidos. \
            Nunca inventes productos, precios, identificadores ni disponibilidad.
            - Flujo de un pedido: identifica al cliente por su teléfono (si no existe, ofrece registrarlo \
            pidiendo nombre y dirección), crea el pedido, agrega los productos, muestra el resumen con el \
            total y confirma.
            - Durante una conversación trabaja siempre sobre el mismo pedido: si ya creaste uno, reutiliza su \
            número y no crees otro ni vuelvas a identificar al cliente, salvo que el usuario pida un pedido nuevo.
            - Los resúmenes deben reflejar exactamente lo que devolvió la herramienta (productos, cantidades, \
            subtotal, envío y total); no los ajustes ni los corrijas por tu cuenta.
            - Antes de confirmar o cancelar un pedido, muestra el resumen y pide una confirmación explícita. \
            Pregunta el método de pago (efectivo, tarjeta o transferencia) si no lo han dicho.
            - Si una herramienta devuelve un error, explícalo con sencillez y ofrece alternativas \
            (otro producto, menos cantidad).
            - Muestra los precios en pesos colombianos y no menciones identificadores internos, salvo el \
            número del pedido.
            - No atiendas temas ajenos al supermercado.
            - Escribe en texto plano, sin Markdown; para las listas usa guiones.
            """;

    /**
     * Cliente de la API de Gemini con tiempo de espera. El SDK de Google no trae ninguno por defecto:
     * sin esto, si Gemini no responde, la peticion del navegador queda colgada indefinidamente.
     * Reemplaza al bean que autoconfigura Spring AI (que es @ConditionalOnMissingBean).
     */
    @Bean
    Client googleGenAiClient(@Value("${spring.ai.google.genai.api-key}") String apiKey,
            @Value("${chatbot.gemini.timeout}") Duration timeout) {
        return Client.builder()
                .apiKey(apiKey)
                // attempts(1): el SDK no reintenta por su cuenta; los reintentos los hace reintentosDelModelo()
                .httpOptions(HttpOptions.builder()
                        .timeout((int) timeout.toMillis())
                        .retryOptions(HttpRetryOptions.builder().attempts(1).build())
                        .build())
                .build();
    }

    /**
     * Reintentos de cada llamada al modelo. Se observo que la API de Gemini (nivel gratuito), cuando recibe
     * muchas peticiones seguidas, deja algunas sin responder: se cortan por tiempo y se vuelven a intentar.
     * Es seguro porque esta llamada solo genera texto; las herramientas se ejecutan fuera del reintento.
     * Reemplaza al RetryTemplate de Spring AI, que no reintenta un tiempo de espera agotado.
     */
    @Bean
    RetryTemplate reintentosDelModelo(@Value("${chatbot.gemini.intentos}") int intentos) {
        return RetryTemplate.builder().maxAttempts(intentos).fixedBackoff(300).build();
    }

    /**
     * ChatClient con las herramientas que el cliente MCP descubrio en el servidor (tools/list) y con
     * memoria de conversacion. ChatMemory lo autoconfigura Spring AI en memoria (ventana de mensajes).
     */
    @Bean
    ChatClient chatClient(ChatClient.Builder builder, ToolCallbackProvider herramientasMcp, ChatMemory memoria) {
        ToolCallback[] herramientas = Arrays.stream(herramientasMcp.getToolCallbacks())
                .map(HerramientaRegistrada::new)
                .toArray(ToolCallback[]::new);
        return builder
                .defaultToolCallbacks(herramientas)
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(memoria).build())
                .build();
    }
}
