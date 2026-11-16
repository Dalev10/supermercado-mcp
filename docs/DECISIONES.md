# Registro de decisiones

Cada decisión del proyecto queda aquí con sus opciones, la recomendación de Claude Code, lo que eligió el estudiante y el porqué. El formato sigue el Anexo B de `GUIA_DESARROLLO.md`.

## Decisiones tomadas antes de empezar (sección 2 de la guía)

| # | Decisión | Elección | Motivo |
|---|---|---|---|
| T1 | Tipo de cliente | Chatbot web propio con Spring AI | Cumple literalmente "realice un cliente" y reutiliza el stack del laboratorio |
| T2 | Modelo de lenguaje | Gemini (Gemini Developer API con API key) | Clave gratuita de Google AI Studio; es el proveedor que ya usa el laboratorio |
| T3 | Base de datos | MySQL 8 en Docker Compose | Mismo motor del laboratorio y entorno reproducible para quien clone el repositorio |
| T4 | Alcance | Intermedio: categorías, productos con stock, clientes, pedidos con detalle y estados; 8-10 herramientas, validaciones y pruebas | Equilibrio entre calidad y tiempo |
| T5 | Repositorio | Monorepo público en GitHub | Un único enlace para el profesor, sin gestionar accesos |
| T6 | Clientes adicionales | MCP Inspector y Claude Desktop | Verificación del protocolo sin LLM y demostración de interoperabilidad |
| T7 | Entorno | Ubuntu 22.04 LTS, VS Code, Claude Code | Los comandos van en bash y con `./mvnw` |
| T8 | Ritmo de consulta | Pausa por fase | Protocolo de la sección 3 de la guía |

## Fase 0. Verificación del entorno

### D0.1 Instalación de JDK 21
- **Fecha:** 2026-11-15
- **Fase:** 0
- **Opciones consideradas:** a) SDKMAN con Temurin 21; b) `sudo apt install openjdk-21-jdk`
- **Recomendación de Claude Code:** ninguna de las dos; no aplica
- **Decisión del estudiante:** no se consultó: el JDK 21 ya estaba instalado
- **Justificación:** la máquina tiene OpenJDK 21.0.13 en `/usr/lib/jvm/java-21-openjdk-amd64`, aunque el `java` predeterminado es el 17 y `JAVA_HOME` estaba vacío. Se fija `JAVA_HOME` solo para este proyecto en lugar de cambiar el predeterminado del sistema, que afectaría a otros proyectos y exigiría `sudo`.
- **Consecuencias:** antes de usar `./mvnw` hay que exportar `JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64` en esa terminal; se documenta en `CLAUDE.md` y en el README.

### D0.2 Instalación de Node.js
- **Fecha:** 2026-11-15
- **Fase:** 0
- **Opciones consideradas:** a) nvm con la LTS vigente; b) repositorio NodeSource
- **Recomendación de Claude Code:** no aplica
- **Decisión del estudiante:** no se consultó: ya había Node v22.22.3 con npm 10.9.8 instalado por nvm
- **Justificación:** la versión instalada es suficiente para `npx @modelcontextprotocol/inspector` y `mcp-remote`.
- **Consecuencias:** como Node viene de nvm, Claude Desktop no lo tendrá en su `PATH`; en la Fase 6 se usará la ruta absoluta a `npx`.

### D0.3 Autenticación con GitHub
- **Fecha:** 2026-11-15
- **Fase:** 0
- **Opciones consideradas:** a) `sudo apt install gh` y `gh auth login` por navegador; b) GitHub CLI desde su repositorio apt oficial; c) sin GitHub CLI: el estudiante crea el repositorio vacío en github.com y se hace push con las credenciales git existentes
- **Recomendación de Claude Code:** a
- **Decisión del estudiante:** c
- **Justificación:** GitHub CLI no estaba instalado y la opción c evita instalar software en el sistema. git ya está configurado con nombre y correo.
- **Consecuencias:** el repositorio remoto se crea a mano en la web en la Fase 1; el tag y el release de la Fase 8 se hacen con `git tag` y la interfaz web de GitHub en lugar de `gh release`.

### D0.4 Claude Desktop en Ubuntu
- **Fecha:** 2026-11-15
- **Fase:** 0
- **Opciones consideradas:** a) dejar la instalación para la Fase 6; b) instalarlo ahora; c) recortarlo del alcance
- **Recomendación de Claude Code:** a
- **Decisión del estudiante:** a
- **Justificación:** Claude Desktop no está instalado y no bloquea el servidor, el chatbot ni el Inspector. Con el tiempo disponible (D0.5) conviene no gastar tiempo ahora en algo que va al final de la ruta crítica.
- **Consecuencias:** la Fase 6 empieza con la instalación, que hace el estudiante porque requiere `sudo`.

### D0.5 Tiempo disponible
- **Fecha:** 2026-11-15
- **Fase:** 0
- **Opciones consideradas:** a) solo hoy, pocas horas; b) de 1 a 3 días; c) una semana o más
- **Recomendación de Claude Code:** no aplica (es un dato del estudiante)
- **Decisión del estudiante:** a
- **Justificación:** la fecha de entrega del PDF es el 15 de noviembre de 2026, el mismo día en que se inicia el desarrollo.
- **Consecuencias:** se sigue la ruta crítica de la guía: Fases 0 a 3 (servidor), luego Fase 5 (chatbot), Fase 4 con Inspector, Fase 8 (README), Fase 6 (Claude Desktop) y por último las pruebas automatizadas adicionales. En cada fase se preferirá la opción marcada "si el tiempo es corto". Lo que se recorte se anotará aquí como alcance pendiente.

### API key de Gemini
- **Fecha:** 2026-11-15
- **Fase:** 0
- **Decisión del estudiante:** confirma que ya tiene la clave de Google AI Studio.
- **Consecuencias:** la escribirá él mismo en `.env`; la clave no aparece en el chat, el código, los commits ni las evidencias.

## Fase 1. Repositorio y andamiaje

### D1.1 Nombre del repositorio y cuenta
- **Fecha:** 2026-11-15
- **Fase:** 1
- **Opciones consideradas:** a) `supermercado-mcp`; b) `laboratorio-mcp-supermercado`; c) `Laboratory_MCP`
- **Recomendación de Claude Code:** a
- **Decisión del estudiante:** a, en la cuenta `Dalev10` (`git@github.com:Dalev10/supermercado-mcp.git`)
- **Justificación:** es descriptivo y coincide con el nombre del servidor MCP y del conector, así hay un solo nombre en todo el proyecto.
- **Consecuencias:** descripción del repositorio: "Chatbot de pedidos de supermercado a domicilio sobre un servidor MCP (Spring Boot, Spring AI, MySQL, Gemini)". La carpeta local conserva su nombre.

### D1.2 Autores
- **Fecha:** 2026-11-15
- **Fase:** 1
- **Opciones consideradas:** a) individual; b) en grupo
- **Recomendación de Claude Code:** no aplica (es un dato del estudiante)
- **Decisión del estudiante:** b, en grupo: Julian Vanegas Lopez y Diego Alejandro Vergara Ruiz
- **Justificación:** el trabajo se entrega en pareja.
- **Consecuencias:** ambos nombres figuran en el README, la licencia y el entregable.

### D1.3 Paquete base e idioma del código
- **Fecha:** 2026-11-15
- **Fase:** 1
- **Opciones consideradas:** a) paquete `com.udea.supermercado` y dominio en español; b) todo en inglés
- **Recomendación de Claude Code:** a
- **Decisión del estudiante:** a
- **Justificación:** sigue la convención del laboratorio (`com.udea.tarea_mcp`) y las descripciones de las herramientas quedan en el idioma en que conversa el usuario.
- **Consecuencias:** el servidor usa `com.udea.supermercado` y el chatbot `com.udea.supermercado.chatbot`; clases, métodos y herramientas en español.

### D1.4 Línea de versiones
- **Fecha:** 2026-11-15
- **Fase:** 1
- **Opciones consideradas:** a) Spring Boot 3.5.16 con Spring AI 1.1.8; b) Spring Boot 3.4.7 con Spring AI 1.1.4, las del PDF; c) Spring Boot 4.1.2 con Spring AI 2.0.2
- **Recomendación de Claude Code:** a
- **Decisión del estudiante:** a
- **Justificación:** es la misma línea del laboratorio con los últimos parches, así que el código del PDF (`@Tool`, `MethodToolCallbackProvider`, propiedades `spring.ai.mcp.server.*`) aplica sin migrar. Se verificó en Maven Central que 1.1.8 es el último parche de Spring AI 1.1, que está construido contra Spring Boot 3.5.15 y que los tres starters necesarios existen en esa versión. Spring Boot 3.5.16 es el último parche de 3.5 y el que usa el PDF en el proyecto del agente. La línea 2.x exigiría migrar propiedades y starters, algo desaconsejable con pocas horas.
- **Consecuencias:** ambos módulos fijan `spring-ai.version` 1.1.8 mediante el BOM y no declaran versión en los starters. start.spring.io ya no genera proyectos con Spring Boot 3.x, por lo que el `pom.xml` se ajustó a mano (ver bitácora).

### D1.5 Licencia
- **Fecha:** 2026-11-15
- **Fase:** 1
- **Opciones consideradas:** a) MIT; b) sin licencia
- **Recomendación de Claude Code:** a
- **Decisión del estudiante:** a
- **Justificación:** repositorio público académico; es permisiva y breve.
- **Consecuencias:** archivo `LICENSE` con el año 2026 y los dos autores.

### D1.6 Convención de commits y ramas
- **Fecha:** 2026-11-15
- **Fase:** 1
- **Opciones consideradas:** a) Conventional Commits en español directamente sobre `main`; b) una rama y un pull request por fase
- **Recomendación de Claude Code:** a
- **Decisión del estudiante:** b
- **Justificación:** el estudiante prefiere la trazabilidad de un pull request por fase.
- **Consecuencias:** cada fase se desarrolla en una rama `fase-N-descripcion` con mensajes de Conventional Commits en español. Como no hay GitHub CLI (D0.3), Claude Code sube la rama y el estudiante abre y fusiona el pull request en la web; después se actualiza `main` en local antes de abrir la rama siguiente. La Fase 0 queda como commit inicial de `main`, porque un pull request necesita una rama base con historial.

### D1.7 Momento de crear el repositorio remoto
- **Fecha:** 2026-11-15
- **Fase:** 1
- **Opciones consideradas:** a) ahora, con push al cerrar cada fase; b) solo al final
- **Recomendación de Claude Code:** a
- **Decisión del estudiante:** a
- **Justificación:** respaldo continuo, historial con fechas reales y el enlace queda listo aunque se acabe el tiempo.
- **Consecuencias:** el estudiante creó el repositorio vacío en GitHub y entregó la URL SSH. Cada push se hace con su confirmación explícita.

### D1.8 PDF del laboratorio en el repositorio público (no prevista en la guía)
- **Fecha:** 2026-11-15
- **Fase:** 1
- **Opciones consideradas:** a) no subirlo y dejarlo en `.gitignore`; b) subirlo en `docs/referencia/` como indica la estructura de la guía
- **Recomendación de Claude Code:** a
- **Decisión del estudiante:** a
- **Justificación:** es material del profesor con capturas de su equipo, y un archivo subido a un repositorio público no se puede retirar del todo. No hace falta redistribuirlo para entender el proyecto.
- **Consecuencias:** `docs/referencia/` queda ignorado por git; el README cita el laboratorio y enlaza los repositorios públicos del profesor.
