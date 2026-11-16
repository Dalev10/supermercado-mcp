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
