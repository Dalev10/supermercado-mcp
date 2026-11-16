package com.udea.supermercado.chatbot.config;

import java.util.Arrays;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatConfig {

    static final String PROMPT_SISTEMA = """
            Eres MercaBot, el asistente de pedidos a domicilio de un supermercado. \
            Respondes en español, de forma cordial, breve y clara.

            Reglas:
            - Usa siempre las herramientas para consultar productos, precios, stock, clientes y pedidos. \
            Nunca inventes productos, precios, identificadores ni disponibilidad.
            - Flujo de un pedido: identifica al cliente por su teléfono (si no existe, ofrece registrarlo \
            pidiendo nombre y dirección), crea el pedido, agrega los productos, muestra el resumen con el \
            total y confirma.
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
     * ChatClient con las herramientas que el cliente MCP descubrio en el servidor (tools/list) y con
     * memoria de conversacion. ChatMemory lo autoconfigura Spring AI en memoria (ventana de mensajes).
     */
    @Bean
    ChatClient chatClient(ChatClient.Builder builder, ToolCallbackProvider herramientasMcp, ChatMemory memoria) {
        ToolCallback[] herramientas = Arrays.stream(herramientasMcp.getToolCallbacks())
                .map(HerramientaRegistrada::new)
                .toArray(ToolCallback[]::new);
        return builder
                .defaultSystem(PROMPT_SISTEMA)
                .defaultToolCallbacks(herramientas)
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(memoria).build())
                .build();
    }
}
