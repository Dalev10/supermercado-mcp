package com.udea.supermercado;

import com.udea.supermercado.tools.SupermercadoTools;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class SupermercadoMcpApplication {

	public static void main(String[] args) {
		SpringApplication.run(SupermercadoMcpApplication.class, args);
	}

	/** Registra como herramientas MCP todos los metodos @Tool de SupermercadoTools. */
	@Bean
	public ToolCallbackProvider supermercadoToolCallbacks(SupermercadoTools tools) {
		return MethodToolCallbackProvider.builder()
				.toolObjects(tools)
				.build();
	}

}
