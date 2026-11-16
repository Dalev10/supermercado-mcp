# Registro de decisiones

Cada decisión del proyecto queda aquí con sus opciones, la recomendación de Claude Code, lo que eligió el estudiante y el porqué. El formato sigue el Anexo B de la guía de desarrollo del proyecto (`GUIA_DESARROLLO.md`, archivo local que no se versiona).

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

## Fase 2. Base de datos y modelo de datos

### D2.1 Puerto de MySQL en el host
- **Fecha:** 2026-11-15
- **Fase:** 2
- **Opciones consideradas:** a) 3306; b) 3307 en el host hacia 3306 del contenedor
- **Recomendación de Claude Code:** a
- **Decisión del estudiante:** a
- **Justificación:** el puerto 3306 está libre en la máquina (los otros contenedores usan 1521 y 5434) y es el mismo del laboratorio.
- **Consecuencias:** se publica solo en `127.0.0.1`. El puerto sigue siendo configurable con `DB_PORT` en `.env`.

### D2.2 Versión de la imagen
- **Fecha:** 2026-11-15
- **Fase:** 2
- **Opciones consideradas:** a) `mysql:8.4`; b) `mysql:8.0`; c) `mysql:latest`
- **Recomendación de Claude Code:** a
- **Decisión del estudiante:** a
- **Justificación:** 8.4 es la serie LTS de MySQL 8 y una etiqueta concreta hace el entorno reproducible; `latest` apunta a otra serie mayor.
- **Consecuencias:** quien clone el repositorio obtiene el mismo motor.

### D2.3 Gestión del esquema
- **Fecha:** 2026-11-15
- **Fase:** 2
- **Opciones consideradas:** a) scripts SQL en `db/init/` con `ddl-auto=validate`; b) Flyway; c) `ddl-auto=update` como el laboratorio
- **Recomendación de Claude Code:** a
- **Decisión del estudiante:** a
- **Justificación:** el esquema queda explícito y versionado en una sola fuente de verdad, sirve directamente para el informe y Hibernate avisa al arrancar si las entidades no coinciden. Evita la inconsistencia del PDF, donde `ddl-auto=update` convive con un `schema.sql` manual.
- **Consecuencias:** los scripts solo se ejecutan cuando el volumen está vacío; para recargarlos hay que borrar el volumen (`docker compose down -v`), acción destructiva que requiere confirmación.

### D2.4 Dónde vive el carrito
- **Fecha:** 2026-11-15
- **Fase:** 2
- **Opciones consideradas:** a) el pedido nace en `BORRADOR` y se le agregan o quitan productos con herramientas; b) una sola herramienta `crearPedido` con la lista completa
- **Recomendación de Claude Code:** a
- **Decisión del estudiante:** a
- **Justificación:** el estado queda en la base de datos y no en la memoria del modelo, así que no se pierde ni se inventa; permite una conversación natural y el mismo pedido es visible desde otro host.
- **Consecuencias:** se necesita el estado `BORRADOR` y tres herramientas más (agregar, quitar y consultar).

### D2.5 Identificación del cliente
- **Fecha:** 2026-11-15
- **Fase:** 2
- **Opciones consideradas:** a) número de teléfono, único; b) correo electrónico; c) documento de identidad
- **Recomendación de Claude Code:** a
- **Decisión del estudiante:** a
- **Justificación:** es el dato natural en un domicilio y fácil de dictar en un chat; el documento es más sensible sin beneficio para la práctica.
- **Consecuencias:** `cliente.telefono` tiene restricción `UNIQUE`; el formato se valida en el servidor.

### D2.6 Dinero y costo de envío
- **Fecha:** 2026-11-15
- **Fase:** 2
- **Opciones consideradas:** precios en `DECIMAL(12,2)` / `BigDecimal` en pesos colombianos. Envío: a) tarifa fija configurable por propiedad; b) sin costo de envío; c) gratis a partir de un monto mínimo
- **Recomendación de Claude Code:** a
- **Decisión del estudiante:** a
- **Justificación:** `double` no representa dinero con exactitud. La tarifa fija es simple, visible en el total y se cambia sin tocar código.
- **Consecuencias:** propiedad `supermercado.costo-envio=5000` en el servidor; `pedido` guarda `subtotal`, `costo_envio` y `total`.

### D2.7 Momento de descontar el stock
- **Fecha:** 2026-11-15
- **Fase:** 2
- **Opciones consideradas:** a) al confirmar el pedido, en una transacción y con bloqueo optimista (`@Version`) en `Producto`; b) al agregar al borrador
- **Recomendación de Claude Code:** a
- **Decisión del estudiante:** a
- **Justificación:** los borradores abandonados no retienen inventario y se evita vender dos veces la misma unidad.
- **Consecuencias:** se puede agregar a un borrador más cantidad de la disponible; el error de stock aparece al confirmar. `producto` y `pedido` tienen columna `version`, y la base refuerza la regla con `CHECK (stock >= 0)`.

### D2.8 Datos de prueba
- **Fecha:** 2026-11-15
- **Fase:** 2
- **Opciones consideradas:** a) 6 categorías, unos 30 productos y 2 clientes; b) conjunto mínimo (3 categorías, 10 productos, 1 cliente)
- **Recomendación de Claude Code:** a
- **Decisión del estudiante:** a
- **Justificación:** da variedad suficiente para las demostraciones y capturas e incluye casos para probar validaciones.
- **Consecuencias:** 32 productos genéricos (sin marcas) con precios verosímiles en COP: 2 agotados, 3 con stock de 3 unidades o menos y 1 inactivo. Dos clientes ficticios, uno con el teléfono 3001234567 del guion de prueba. Todos los datos son sintéticos.

### D2.9 Guía de desarrollo fuera del repositorio (no prevista en la guía)
- **Fecha:** 2026-11-15
- **Fase:** 2
- **Opciones consideradas:** a) mantener `GUIA_DESARROLLO.md` versionado, como indica la estructura de la guía; b) dejar de versionarlo e incluirlo en `.gitignore`
- **Recomendación de Claude Code:** no se consultó; fue una instrucción directa del estudiante al confirmar el push de la Fase 2
- **Decisión del estudiante:** b
- **Justificación:** es un documento de trabajo interno, no parte del producto que se entrega.
- **Consecuencias:** el archivo se conserva en la máquina local y deja de aparecer en el repositorio desde la rama `fase-2-base-de-datos`. Sigue existiendo en el historial (commit inicial de `main`); borrarlo también de ahí exige reescribir el historial y un `git push --force`, que requieren confirmación explícita.

## Fase 3. Servidor MCP

### D3.1 Catálogo de herramientas
- **Fecha:** 2026-11-15
- **Fase:** 3
- **Opciones consideradas:** a) las 10 herramientas propuestas; b) las 10 más `listarPedidosDeCliente`; c) otro cambio
- **Recomendación de Claude Code:** a
- **Decisión del estudiante:** a
- **Justificación:** cubren el flujo completo de un pedido y entran en el alcance de 8 a 10 herramientas (T4); con pocas horas, cada herramienta extra es código, descripción y prueba adicionales.
- **Consecuencias:** se publican `listarCategorias`, `buscarProductos`, `buscarCliente`, `registrarCliente`, `crearPedido`, `agregarProductoAPedido`, `quitarProductoDePedido`, `consultarPedido`, `confirmarPedido` y `cancelarPedido`. No hay herramientas de administración (crear productos, cambiar precios), siguiendo la recomendación del PDF de publicar solo las operaciones necesarias. `listarPedidosDeCliente` queda como alcance pendiente.

### D3.2 Organización del código
- **Fecha:** 2026-11-15
- **Fase:** 3
- **Opciones consideradas:** a) clase `SupermercadoTools` delgada que delega en servicios de dominio; b) todo en una clase, como `TareaToolsService` del laboratorio
- **Recomendación de Claude Code:** a
- **Decisión del estudiante:** a
- **Justificación:** las reglas de negocio se prueban sin MCP y la capa de herramientas queda como un adaptador del protocolo.
- **Consecuencias:** paquetes `model`, `repository`, `dto`, `exception`, `service` (`CatalogoService`, `ClienteService`, `PedidoService`) y `tools`. Las transacciones viven en los servicios.

### D3.3 Qué devuelven las herramientas
- **Fecha:** 2026-11-15
- **Fase:** 3
- **Opciones consideradas:** a) DTO como `record` de Java; b) entidades JPA directamente, como el laboratorio
- **Recomendación de Claude Code:** a
- **Decisión del estudiante:** a
- **Justificación:** evita errores de carga perezosa y ciclos al serializar entidades con relaciones, y controla qué ve el modelo (no se exponen campos internos como `version`).
- **Consecuencias:** `CategoriaDto`, `ProductoDto`, `ClienteDto`, `PedidoDto` y `DetalleDto`. La conversión se hace dentro de la transacción del servicio. Las fechas viajan como texto ISO.

### D3.4 Registro de herramientas
- **Fecha:** 2026-11-15
- **Fase:** 3
- **Opciones consideradas:** a) bean `ToolCallbackProvider` construido con `MethodToolCallbackProvider`; b) anotaciones `@McpTool` con autoescaneo
- **Recomendación de Claude Code:** a
- **Decisión del estudiante:** a
- **Justificación:** es el mecanismo que enseña el PDF del laboratorio.
- **Consecuencias:** el bean se declara en `SupermercadoMcpApplication`; al arrancar, el log muestra "Registered tools: 10".

### D3.5 Errores de negocio
- **Fecha:** 2026-11-15
- **Fase:** 3
- **Opciones consideradas:** a) excepciones de dominio con mensajes claros en español; b) objetos resultado con un campo `exito`
- **Recomendación de Claude Code:** a
- **Decisión del estudiante:** a
- **Justificación:** el mensaje llega al modelo como resultado de error y este puede explicarlo y proponer alternativas; además, una excepción revierte la transacción sin código adicional.
- **Consecuencias:** una única excepción `ReglaNegocioException`. Se comprobó con llamadas reales (`docs/evidencias/f3-error-negocio.txt`) que en Spring AI 1.1.8 el error llega al cliente como un resultado normal de `tools/call` con `"isError": true` y el mensaje en `content[0].text`, no como un error JSON-RPC.

### D3.6 Pruebas automatizadas
- **Fecha:** 2026-11-15
- **Fase:** 3
- **Opciones consideradas:** a) unitarias con Mockito más una de integración con Testcontainers; b) solo unitarias con JUnit 5 y Mockito; c) `@DataJpaTest` con H2
- **Recomendación de Claude Code:** b, por el tiempo disponible (D0.5)
- **Decisión del estudiante:** b
- **Justificación:** son rápidas, no dependen de la base de datos y cubren cada regla de negocio; es la opción que la guía aconseja con tiempo corto.
- **Consecuencias:** 39 pruebas en `PedidoServiceTest` y `ClienteServiceTest`. Se eliminó la prueba `contextLoads` generada por Initializr porque exige la base de datos encendida. La integración real con MySQL se verifica a mano en la Fase 4. La prueba con Testcontainers queda como alcance pendiente.

## Fase 5. Chatbot web

Esta fase se ejecutó antes que la Fase 4 por la ruta crítica acordada en D0.5.

### D5.1 Interfaz
- **Fecha:** 2026-11-15
- **Fase:** 5
- **Opciones consideradas:** a) HTML, CSS y JavaScript sin framework, servidos como recursos estáticos por Spring Boot; b) Thymeleaf; c) React con Vite
- **Recomendación de Claude Code:** a
- **Decisión del estudiante:** a
- **Justificación:** un solo proceso, sin toolchain de frontend, suficiente para un chat y fácil de explicar.
- **Consecuencias:** `index.html`, `app.js` y `styles.css` en `chatbot-client/src/main/resources/static/`. Todo el texto se inserta con `textContent`, por lo que nunca se interpreta como HTML.

### D5.2 Modelo concreto de Gemini
- **Fecha:** 2026-11-15 (ajustada el 2026-11-16 tras la prueba real)
- **Fase:** 5
- **Opciones consideradas:** a) `gemini-3.8-flash`; b) `gemini-3.5-flash-lite`; c) `gemini-2.5-flash`, el del laboratorio
- **Recomendación de Claude Code:** a, con b como alternativa si a fallaba. La recomendación de la guía (`gemini-2.5-flash`) cambió porque la documentación de Google, consultada el 2026-11-15, lo marca como deprecado: el acceso por API queda limitado a usuarios existentes y desde el 2026-12-01 las peticiones se redirigen a `gemini-3.5-flash-lite`.
- **Decisión del estudiante:** a
- **Modelo finalmente usado:** `gemini-3.5-flash-lite`
- **Justificación:** se probó cada modelo con la clave del estudiante y una petición con llamada a herramienta. `gemini-3.8-flash` no respondió en dos intentos (40 s y 90 s de espera) y `gemini-3.6-flash` tardó 35 s; `gemini-3.5-flash-lite` respondió en algo más de 1 s y generó la llamada a la herramienta. Un chatbot que encadena varias llamadas por mensaje no es usable con el primero.
- **Consecuencias:** el modelo va en configuración (`spring.ai.google.genai.chat.options.model`) y se puede cambiar sin tocar código con la variable `GEMINI_MODEL`. No se fijó temperatura: se usa la del modelo por defecto, con la que eligió bien las herramientas en todas las pruebas. El nivel gratuito tiene un límite de peticiones por minuto que se alcanza con pruebas muy seguidas (ver bitácora).

### D5.3 Entrega de la respuesta
- **Fecha:** 2026-11-15
- **Fase:** 5
- **Opciones consideradas:** a) respuesta completa por `POST /api/chat`; b) streaming con SSE
- **Recomendación de Claude Code:** a
- **Decisión del estudiante:** a
- **Justificación:** simple y robusta; con herramientas de por medio el streaming complica el cliente.
- **Consecuencias:** la página muestra un indicador "pensando" mientras llega la respuesta. El streaming queda como mejora pendiente.

### D5.4 Memoria de conversación
- **Fecha:** 2026-11-15
- **Fase:** 5
- **Opciones consideradas:** a) en memoria, por identificador de conversación generado en el navegador; b) persistente en base de datos
- **Recomendación de Claude Code:** a
- **Decisión del estudiante:** a
- **Justificación:** el estado importante (el pedido) ya está en MySQL; basta recordar el hilo mientras la aplicación está encendida.
- **Consecuencias:** se usa el `ChatMemory` en memoria que autoconfigura Spring AI, con `MessageChatMemoryAdvisor`. Al reiniciar el chatbot se pierde el hilo, pero no los pedidos. El botón "Nueva conversación" genera otro identificador.

### D5.5 Visibilidad de las herramientas invocadas
- **Fecha:** 2026-11-15
- **Fase:** 5
- **Opciones consideradas:** a) mostrar bajo cada respuesta qué herramientas se llamaron y con qué argumentos; b) solo en los logs
- **Recomendación de Claude Code:** a
- **Decisión del estudiante:** a
- **Justificación:** hace visible el flujo MCP en la demostración y en las capturas del informe.
- **Consecuencias:** clase `HerramientaRegistrada`, un decorador de `ToolCallback` que anota cada llamada (nombre, argumentos y si devolvió error). La respuesta de `/api/chat` incluye la lista `herramientas`.

### D5.6 Identificación del cliente
- **Fecha:** 2026-11-15
- **Fase:** 5
- **Opciones consideradas:** a) el asistente pide el teléfono, usa `buscarCliente` y, si no existe, ofrece registrarlo; b) formulario de teléfono antes de chatear
- **Recomendación de Claude Code:** a
- **Decisión del estudiante:** a
- **Justificación:** todo ocurre conversando, que es el objetivo del ejercicio.
- **Consecuencias:** el flujo está descrito en el prompt de sistema; se verificó con un teléfono no registrado (`docs/evidencias/f5-chat-cliente-nuevo.txt`).

### D5.7 Nombre y tono del asistente
- **Fecha:** 2026-11-15
- **Fase:** 5
- **Opciones consideradas:** a) "MercaBot", cordial y breve, con el prompt de sistema propuesto; b) otro nombre o cambios al prompt
- **Recomendación de Claude Code:** a
- **Decisión del estudiante:** a
- **Justificación:** nombre sencillo y tono adecuado para atención al cliente.
- **Consecuencias:** el prompt de sistema está en `ChatConfig.PROMPT_SISTEMA`. Al texto aprobado se le añadió una regla ("Escribe en texto plano, sin Markdown; para las listas usa guiones") porque la página muestra el texto tal cual.

## Fase 4. Verificación del protocolo MCP

Se ejecutó después de la Fase 5 por la ruta crítica acordada en D0.5.

### D4.1 Profundidad de la prueba con curl
- **Fecha:** 2026-11-16
- **Fase:** 4
- **Opciones consideradas:** a) solo `initialize`, como en el PDF; b) secuencia completa `initialize` → `notifications/initialized` → `tools/list` → una `tools/call` de lectura
- **Recomendación de Claude Code:** b
- **Decisión del estudiante:** b
- **Justificación:** muestra el JSON-RPC crudo de todo el ciclo (negociación, descubrimiento y ejecución), que es lo que el informe debe explicar.
- **Consecuencias:** cuatro archivos `f4-curl-*.txt` con cada petición y su respuesta. Además se ejecutó por JSON-RPC el plan de pruebas completo de la guía (`f4-plan-pruebas.txt`), con un `SELECT` después de cada escritura.

### D4.2 Versión del Inspector
- **Fecha:** 2026-11-16
- **Fase:** 4
- **Opciones consideradas:** a) la más reciente (`npx @modelcontextprotocol/inspector`); b) la 0.12.0 que fija el PDF
- **Recomendación de Claude Code:** a
- **Decisión del estudiante:** a
- **Justificación:** tiene las correcciones actuales y soporta Streamable HTTP desde la interfaz.
- **Consecuencias:** la versión instalada por `npx` fue la 2.13.0, cuya interfaz es distinta de la 0.12.0 de las capturas del PDF: los servidores se registran en una lista ("Add Servers") y se conectan con un interruptor, en lugar del formulario lateral con "Transport Type" y "URL". El Inspector guarda esa lista en `~/.mcp-inspector/mcp.json`, fuera del proyecto.

### D4.3 Quién toma las capturas (no prevista en la guía)
- **Fecha:** 2026-11-16
- **Fase:** 4 y 5
- **Opciones consideradas:** a) las toma el estudiante a mano, como indica la guía; b) las toma Claude Code de forma automatizada
- **Recomendación de Claude Code:** no se consultó; fue una instrucción directa del estudiante ("realiza las capturas y guárdalas")
- **Decisión del estudiante:** b
- **Justificación:** ahorrar tiempo al estudiante.
- **Consecuencias:** las capturas del chatbot y del Inspector se tomaron con Google Chrome en modo headless (perfil temporal) manejado por el protocolo DevTools desde un script de Node, sin instalar nada. Son capturas de las aplicaciones reales en ejecución. Las de Claude Desktop (Fase 6) no se pueden automatizar así y siguen a cargo del estudiante.

## Fase 6. Claude Desktop y Claude Code como hosts adicionales

### Alcance de la Fase 6
- **Fecha:** 2026-11-16
- **Fase:** 6
- **Opciones consideradas:** a) dejar Claude Desktop como alcance pendiente; b) instalarlo y hacer la demostración
- **Recomendación de Claude Code:** a, porque la fecha de entrega ya había pasado y era lo último de la ruta crítica
- **Decisión del estudiante:** b. Después pidió que la instalación la hiciera Claude Code desde la terminal, escribiendo él mismo la contraseña de sudo.
- **Justificación:** completar la demostración de interoperabilidad que pide el laboratorio.
- **Consecuencias:** se instaló Claude Desktop 5.11492.5 (beta para Linux) desde el repositorio apt oficial de Anthropic, siguiendo https://code.claude.com/docs/en/desktop-linux. Antes de instalar la llave del repositorio se verificó su huella (`31DDDE24DDFAB679F42D7BD2BAA929FF1A7ECACE`). La contraseña de sudo la escribió el estudiante en una ventana de `zenity` (`SUDO_ASKPASS`); Claude Code nunca la vio. El paquete instaló también dependencias de QEMU que usa la función Cowork de la app.

### D6.1 Forma de conexión de Claude Desktop
- **Fecha:** 2026-11-16
- **Fase:** 6
- **Opciones consideradas:** a) puente `mcp-remote` en el archivo de configuración, como la Parte D del PDF; b) mecanismo HTTP nativo, si la versión lo admitiera para servidores locales
- **Recomendación de Claude Code:** a
- **Decisión del estudiante:** a (aceptada al elegir "Instalarlo y hacerlo", cuya descripción indicaba `mcp-remote` y el nombre `supermercado-mcp`)
- **Justificación:** el archivo de configuración de Claude Desktop lanza servidores por STDIO; `mcp-remote` traduce STDIO a Streamable HTTP hacia el servidor local.
- **Consecuencias:** como Node viene de nvm y la aplicación no carga ese `PATH`, la configuración usa rutas absolutas a `node` y `npx` y define `PATH` en `env`. Antes de tocar la aplicación se probó el puente con un entorno limpio (`f6-mcp-remote-stdio.txt`). Con la confirmación del estudiante se creó `~/.config/Claude/claude_desktop_config.json`, que no existía.

### D6.2 Nombre del conector
- **Fecha:** 2026-11-16
- **Fase:** 6
- **Decisión:** `supermercado-mcp`, igual en Claude Desktop, en `.mcp.json` y en la documentación (aceptada junto con D6.1).
- **Justificación:** evitar la inconsistencia del PDF, que usa un nombre en el JSON y otro en la captura.

### D6.3 Claude Code como tercer host
- **Fecha:** 2026-11-16
- **Fase:** 6
- **Opciones consideradas:** a) sí, con `claude mcp add --transport http --scope project`; b) no
- **Recomendación de Claude Code:** a
- **Decisión del estudiante:** a
- **Justificación:** es un solo comando, aporta evidencia de un tercer host y quien clone el repositorio obtiene la conexión.
- **Consecuencias:** `.mcp.json` en la raíz del repositorio. Claude Code pide aprobar el servidor la primera vez; esa aprobación la hace el usuario.

## Fase 7. Pruebas integrales

### D7.1 Pruebas del chatbot
- **Fecha:** 2026-11-16
- **Fase:** 7
- **Opciones consideradas:** a) manuales con guion y capturas; b) añadir una prueba automatizada del controlador con el `ChatClient` simulado
- **Recomendación de Claude Code:** a
- **Decisión del estudiante:** a
- **Justificación:** las respuestas de un modelo no son deterministas y automatizarlas bien excede el alcance. La lógica propia del chatbot (traducción de errores y entrega de errores de herramienta) ya tiene 6 pruebas unitarias.
- **Consecuencias:** guion manual en `docs/pruebas.md`, con las transcripciones y capturas de la Fase 5 como evidencia.

## Fase 8. Documentación del repositorio

### D8.1 Diagramas
- **Fecha:** 2026-11-16
- **Fase:** 8
- **Opciones consideradas:** a) Mermaid dentro del Markdown; b) imágenes hechas en una herramienta externa
- **Recomendación de Claude Code:** a
- **Decisión del estudiante:** a
- **Justificación:** GitHub los renderiza, quedan versionados como texto y se pueden exportar a imagen para el entregable.
- **Consecuencias:** diagramas de componentes, entidad-relación, estados del pedido y secuencia de un pedido en `docs/arquitectura.md`, y versiones resumidas en el README.

### D8.2 Publicación
- **Fecha:** 2026-11-16
- **Fase:** 8
- **Opciones consideradas:** a) tag `v1.0.0` y release en GitHub; b) sin tag
- **Recomendación de Claude Code:** a
- **Decisión del estudiante:** a
- **Justificación:** fija la versión exacta que se entrega.
- **Consecuencias:** sin GitHub CLI, Claude Code crea y sube el tag con git (con confirmación) cuando todos los pull requests estén fusionados en `main`, y el estudiante crea el release en la web.

### D8.3 Capturas en el repositorio
- **Fecha:** 2026-11-16
- **Fase:** 8
- **Opciones consideradas:** a) incluirlas en `docs/evidencias/` y enlazarlas desde el README; b) solo en el entregable
- **Recomendación de Claude Code:** a
- **Decisión del estudiante:** a
- **Justificación:** quien abra el repositorio ve el proyecto funcionando sin ejecutarlo. Se revisó que ninguna muestre claves ni datos personales reales.
- **Consecuencias:** el README muestra capturas del chatbot y del Inspector.

### D6.4 Pasos de la Fase 6 que no se pudieron automatizar (no prevista en la guía)
- **Fecha:** 2026-11-16
- **Fase:** 6
- **Contexto:** el estudiante pidió que Claude Code completara todo el proceso pendiente.
- **Hallazgos:** el clasificador de seguridad de Claude Code bloqueó que el agente aprobara para sí mismo el servidor MCP del proyecto, y Claude Desktop se niega a arrancar con un puerto de depuración, que era la vía para escribir en la app y tomar capturas.
- **Decisión:** respetar ambas protecciones y no buscar formas de rodearlas.
- **Consecuencias:** la aprobación en Claude Code y las pruebas y capturas en Claude Desktop las hace el estudiante. Como evidencia automática de que Claude Desktop se conecta al servidor queda su propio registro (`f6-claude-desktop-registro.txt`).
