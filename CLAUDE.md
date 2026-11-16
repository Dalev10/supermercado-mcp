# CLAUDE.md

Chatbot de pedidos de supermercado sobre un servidor MCP. La especificación completa es `GUIA_DESARROLLO.md`; este archivo resume lo que aplica en todo momento.

## Al retomar una sesión

Relee `GUIA_DESARROLLO.md`, `docs/DECISIONES.md` y `docs/BITACORA.md`, y ejecuta `git log --oneline --all` para saber en qué fase quedó el trabajo.

## Reglas de trabajo (sección 3 de la guía)

- **Ciclo por fase:** abrir, consultar todas las decisiones con recomendación y explicación, esperar la respuesta, registrar en `docs/DECISIONES.md`, ejecutar, verificar con comandos reales, documentar en `docs/BITACORA.md`, cerrar con commit.
- **Requieren confirmación explícita:** `git push`, tags y releases; instalar software en el sistema; borrar volúmenes de Docker, `DROP`, `git reset --hard`, reescribir historial; modificar archivos fuera de esta carpeta.
- **Secretos:** viven en `.env` (ignorado por git). Ninguna clave en código, commits, bitácora ni capturas. Revisar `git diff --cached` antes de cada commit. Nunca pedir al estudiante que pegue una clave en el chat.
- **Versiones:** Spring Boot 3.5.16 y Spring AI 1.1.8 en ambos módulos, siempre por el BOM de Spring AI. No mezclar ejemplos de otras líneas (2.x cambia propiedades).
- **Evidencias:** en `docs/evidencias/` con prefijo de fase (`f3-arranque.txt`). Las salidas de terminal las guarda Claude Code; las capturas de interfaz las toma el estudiante.
- **Fase 9 bloqueada** hasta que el estudiante entregue la guía del entregable.
- **Tiempo:** la entrega es el 2026-11-15; se sigue la ruta crítica (Fases 0-3, 5, 4, 8, 6, pruebas adicionales).

## Git

- Una rama por fase (`fase-N-descripcion`) y un pull request hacia `main`.
- No hay GitHub CLI: Claude Code sube la rama (con confirmación) y el estudiante abre y fusiona el pull request en la web.
- Mensajes en español con Conventional Commits: `feat(server): ...`, `feat(chatbot): ...`, `docs: ...`, `test(server): ...`.
- Remoto: `git@github.com:Dalev10/supermercado-mcp.git`.

## Entorno

- Ubuntu 22.04, comandos en bash y con `./mvnw`.
- El `java` predeterminado del sistema es el 17. Antes de usar Maven:

```bash
export JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64
```

## Puertos

| Servicio | Puerto |
|---|---|
| `mcp-server` (endpoint `/mcp`) | 8888 |
| `chatbot-client` | 8080 |
| MySQL (Docker, solo `127.0.0.1`) | 3306 |

## Comandos de arranque

```bash
# 1. Base de datos
docker compose up -d && docker compose ps

# 2. Servidor MCP (en una terminal)
export JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64
set -a && source .env && set +a
cd mcp-server && ./mvnw spring-boot:run

# 3. Chatbot (en otra terminal, con el servidor MCP ya arriba)
export JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64
set -a && source .env && set +a
cd chatbot-client && ./mvnw spring-boot:run

# Compilar y probar un módulo
./mvnw -q -DskipTests package
./mvnw clean test
```

## Código

- Paquete base `com.udea.supermercado` (chatbot: `com.udea.supermercado.chatbot`), dominio en español.
- Nombre único del servidor MCP: `supermercado-mcp`.
