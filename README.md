# supermercado-mcp

Chatbot de pedidos de supermercado a domicilio sobre un servidor MCP (Model Context Protocol), hecho con Spring Boot, Spring AI, MySQL y Gemini.

Es la solución al ejercicio propuesto del laboratorio "Construcción e integración de un MCP Server" del curso Laboratorio Arquitectura de Software (Prof. Diego José Luis Botia V., 2026):

> Realice un cliente de tipo ChatBot e intégrelo con un servidor MCP que conecte a una BD con MySQL, PostgreSQL u otro motor de su elección, para realizar pedidos de productos de un supermercado a domicilio. Explique cada paso que emplee para la solución.

> **Estado:** en desarrollo. Este README se completa en la fase de documentación; por ahora describe la estructura y cómo compilar.

## Piezas

| Pieza | Qué es | Puerto |
|---|---|---|
| `mcp-server` | Servidor MCP (Spring Boot + Spring AI) con las herramientas de catálogo, clientes y pedidos | 8888 |
| `chatbot-client` | Chatbot web: Spring Boot + Spring AI MCP Client + Gemini, con página de chat | 8080 |
| MySQL 8 | Base de datos `supermercado_mcp` en Docker Compose | 3306 |

## Versiones

| Componente | Versión |
|---|---|
| Java | 21 |
| Spring Boot | 3.5.16 |
| Spring AI | 1.1.8 |
| MySQL | 8 (Docker) |

## Requisitos

- JDK 21 (con `JAVA_HOME` apuntando a él)
- Docker con el plugin Compose
- Node.js LTS con npm (para MCP Inspector y `mcp-remote`)
- Una API key de Gemini de Google AI Studio

## Compilar

```bash
cp .env.example .env        # y edita los valores; .env no se sube a git
cd mcp-server && ./mvnw -q -DskipTests package && cd ..
cd chatbot-client && ./mvnw -q -DskipTests package && cd ..
```

## Documentación del proceso

- [docs/BITACORA.md](docs/BITACORA.md): qué se hizo en cada fase, comandos y resultados.
- [docs/DECISIONES.md](docs/DECISIONES.md): cada decisión con sus opciones y su justificación.
- [docs/evidencias/](docs/evidencias/): salidas de terminal y capturas.

## Autores

- Julian Vanegas Lopez
- Diego Alejandro Vergara Ruiz

## Licencia

[MIT](LICENSE)
