# Bitácora de desarrollo

Registro paso a paso de lo que se hizo en cada fase, por qué, con qué comandos y con qué resultado. Las decisiones se detallan en `DECISIONES.md` y las pruebas de que algo funciona están en `evidencias/`.

## Fase 0. Verificación del entorno (2026-11-15)

**Qué se hizo:** se comprobó que la máquina tiene lo necesario para el proyecto: sistema operativo, JDK 21, Docker con Compose, Node.js, git, GitHub CLI, puertos libres y Claude Desktop. También se leyeron completos la guía de desarrollo y el PDF del laboratorio.

**Por qué:** un fallo de entorno descubierto a mitad del desarrollo cuesta más que detectarlo antes de escribir código, y varias decisiones posteriores dependen de lo instalado (puerto de MySQL, forma de publicar en GitHub, ruta de `npx` para Claude Desktop).

**Comandos:**

```bash
lsb_release -a
java -version && echo "$JAVA_HOME"
/usr/lib/jvm/java-21-openjdk-amd64/bin/java -version
/usr/lib/jvm/java-21-openjdk-amd64/bin/javac -version
docker --version && docker compose version && docker ps
docker run --rm hello-world
node --version && npm --version
git --version && git config user.name && git config user.email
gh --version
ss -ltnp | grep -E ':(3306|8080|8888)\b' || echo "puertos libres"
```

**Resultado:**

| Requisito | Estado |
|---|---|
| Ubuntu 22.04.5 LTS | Cumple |
| JDK 21 | Instalado (OpenJDK 21.0.13); no es el predeterminado, se usa fijando `JAVA_HOME` |
| Docker 29.9.2 y Compose v5.6.1 | Cumple; `hello-world` corre sin `sudo` |
| Node v22.22.3 y npm 10.9.8 (nvm) | Cumple |
| git 2.34.1 con nombre y correo | Cumple |
| GitHub CLI | No instalado; se decidió trabajar sin él (D0.3) |
| Puertos 3306, 8080 y 8888 | Libres |
| Claude Desktop | No instalado; se aplaza a la Fase 6 (D0.4) |
| API key de Gemini | El estudiante confirma que la tiene |

**Problemas y solución:**

- El `java` predeterminado del sistema es el 17 y `JAVA_HOME` estaba vacío. Solución: exportar `JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64` en la terminal del proyecto, sin tocar el predeterminado del sistema.
- GitHub CLI no está instalado. Solución: el repositorio se creará a mano en github.com y se hará push con git (D0.3).
- El tiempo disponible es de pocas horas porque la entrega es el mismo día. Solución: seguir la ruta crítica de la guía (D0.5).

**Evidencias:** `docs/evidencias/f0-entorno.txt`

**Commit:** `4cd2276` (commit inicial de `main`; el repositorio git se creó al empezar la Fase 1)

## Fase 1. Repositorio y andamiaje (2026-11-15)

**Qué se hizo:**

1. Se inició el repositorio git local con rama `main` y se añadió el remoto de GitHub.
2. Se generaron los dos proyectos Maven con Spring Initializr: `mcp-server` (Web, Data JPA, Validation, MySQL Driver) y `chatbot-client` (Web, Validation), ambos con Java 21 y empaquetado jar.
3. Se ajustó cada `pom.xml` a Spring Boot 3.5.16 y se añadieron el BOM de Spring AI 1.1.8 y los starters: `spring-ai-starter-mcp-server-webmvc` en el servidor; `spring-ai-starter-mcp-client` y `spring-ai-starter-model-google-genai` en el chatbot.
4. Se crearon `.gitignore`, `.env.example`, `LICENSE`, `README.md` inicial y `CLAUDE.md`.
5. Se compilaron ambos módulos.

**Por qué:** tener desde el principio la estructura definitiva del monorepo y comprobar que el conjunto de versiones elegido (D1.4) se resuelve y compila antes de escribir lógica. Dos proyectos Maven independientes permiten arrancar servidor y chatbot por separado, que es como se relacionan un servidor MCP y su host.

**Comandos:**

```bash
git init -b main
git remote add origin git@github.com:Dalev10/supermercado-mcp.git

curl -o mcp-server.zip https://start.spring.io/starter.zip \
  -d type=maven-project -d language=java -d javaVersion=21 -d packaging=jar \
  -d groupId=com.udea -d artifactId=mcp-server -d name=supermercado-mcp \
  -d packageName=com.udea.supermercado -d baseDir=mcp-server \
  -d dependencies=web,data-jpa,validation,mysql
curl -o chatbot-client.zip https://start.spring.io/starter.zip \
  -d type=maven-project -d language=java -d javaVersion=21 -d packaging=jar \
  -d groupId=com.udea -d artifactId=chatbot-client -d name=chatbot \
  -d packageName=com.udea.supermercado.chatbot -d baseDir=chatbot-client \
  -d dependencies=web,validation
unzip mcp-server.zip && unzip chatbot-client.zip

export JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64
cd mcp-server && ./mvnw -q -DskipTests package
cd ../chatbot-client && ./mvnw -q -DskipTests package
```

**Resultado:** ambos módulos compilan y generan su jar ejecutable. Maven resolvió Spring Boot 3.5.16, Spring AI 1.1.8, el SDK de MCP para Java 0.18.3 y el SDK `google-genai` 1.37.0.

**Problemas y solución:**

- start.spring.io rechazó `bootVersion=3.5.16` con el mensaje "Spring Boot compatibility range is >=4.0.0": hoy solo genera proyectos con Spring Boot 4.x. Solución: se generó la estructura con la versión por defecto (para obtener el wrapper de Maven y las clases base) y se reescribió el `pom.xml` tomando como base el del PDF: padre 3.5.16, `spring-boot-starter-web` en lugar de `spring-boot-starter-webmvc`, y un único `spring-boot-starter-test` en lugar de los starters de prueba separados de Boot 4.
- Las pruebas se omiten en esta fase (`-DskipTests`) porque la prueba `contextLoads` generada necesita la base de datos y la clave de Gemini, que llegan en fases posteriores.

**Evidencias:** `docs/evidencias/f1-compilacion.txt`

**Commit:** `70150e2` en la rama `fase-1-andamiaje`

## Fase 2. Base de datos y modelo de datos (2026-11-15)

**Qué se hizo:**

1. Se escribió `docker-compose.yml` con MySQL 8.4, publicado solo en `127.0.0.1:3306`, con volumen persistente, zona horaria `America/Bogota` y verificación de salud.
2. Se escribió el esquema en `db/init/01-schema.sql` (tablas `categoria`, `producto`, `cliente`, `pedido` y `detalle_pedido`) y los datos de prueba en `db/init/02-data.sql`.
3. Se creó el archivo `.env` local a partir de `.env.example`, con contraseñas de MySQL generadas al azar.
4. Se levantó el contenedor y se comprobó el contenido de la base con el usuario de aplicación.

**Por qué:**

- Docker Compose hace el entorno reproducible: quien clone el repositorio obtiene la misma base con un solo comando.
- El esquema vive en scripts SQL explícitos y versionados (D2.3), no lo genera Hibernate.
- Frente al laboratorio base se corrigió lo siguiente: la aplicación no se conecta como `root` sino con un usuario que solo tiene permisos sobre `supermercado_mcp`; las fechas son `DATETIME` y no `VARCHAR`; el dinero es `DECIMAL(12,2)`.
- La base refuerza las reglas de negocio con restricciones: teléfono único, una sola línea por producto en cada pedido, `CHECK` de stock no negativo, cantidad mayor que cero y estados válidos.
- `detalle_pedido.precio_unitario` copia el precio del producto al agregarlo, para que un cambio de precio posterior no altere pedidos existentes.

**Comandos:**

```bash
cp .env.example .env    # y cambiar las contraseñas
docker compose up -d && docker compose ps
docker compose exec mysql sh -c 'mysql -u"$MYSQL_USER" -p"$MYSQL_PASSWORD" supermercado_mcp -e "SHOW TABLES; SELECT COUNT(*) FROM producto;"'
```

**Resultado:** contenedor `supermercado-mysql` en estado `healthy`. La base tiene 5 tablas, 6 categorías, 32 productos y 2 clientes; los acentos se guardan bien (utf8mb4). El usuario `supermercado_app` solo tiene privilegios sobre `supermercado_mcp`, y un `UPDATE` que deja el stock en negativo es rechazado por la restricción `ck_producto_stock`.

**Problemas y solución:**

- El primer arranque falló con "Database is uninitialized and password option is not specified". Causa: en la carpeta existía un directorio vacío llamado `.env` en lugar del archivo, así que Docker Compose no encontró las variables. El directorio se creó durante la sesión de trabajo (probablemente por el entorno aislado en el que Claude Code ejecuta comandos), no contenía nada y se eliminó con `rmdir`. Después se creó el archivo `.env` real y el contenedor inicializó la base correctamente.
- La verificación de salud de la referencia usaba `-h localhost` (socket), que puede reportar `healthy` mientras todavía corren los scripts de inicio. Se cambió a `-h 127.0.0.1` (TCP), que solo responde cuando el servidor definitivo está listo.

**Evidencias:** `docs/evidencias/f2-tablas.txt`

**Commit:** `ce87761` en la rama `fase-2-base-de-datos`

## Fase 3. Servidor MCP (2026-11-15)

**Qué se hizo:**

1. Entidades JPA (`Categoria`, `Producto`, `Cliente`, `Pedido`, `DetallePedido`) y enumeraciones `EstadoPedido` y `MetodoPago`, que corresponden al esquema de `db/init/01-schema.sql`.
2. Repositorios Spring Data JPA, con una consulta de búsqueda de productos por texto, categoría y disponibilidad.
3. Servicios de dominio con las reglas de negocio y las transacciones: `CatalogoService`, `ClienteService` y `PedidoService`.
4. DTO como `record` para las respuestas.
5. Clase `SupermercadoTools` con las 10 herramientas `@Tool`, cada una con una descripción que dice qué hace, cuándo usarla, qué devuelve y cuándo falla.
6. Registro de las herramientas con `MethodToolCallbackProvider` y configuración del transporte Streamable HTTP en `application.properties`.
7. 39 pruebas unitarias con JUnit 5 y Mockito.

**Por qué:**

- La capa de herramientas es un adaptador delgado: recibe la llamada MCP y delega. Las reglas viven en los servicios, donde se prueban sin protocolo ni base de datos.
- El servidor valida todo lo que recibe (cantidades, teléfono, campos obligatorios, método de pago) porque los argumentos los produce un modelo de lenguaje y no se puede confiar en ellos.
- Al confirmar, primero se validan todas las líneas y solo después se descuenta el stock, de modo que si una línea falla no se toca ninguna. Todo ocurre en una transacción.
- `Producto` y `Pedido` usan bloqueo optimista (`@Version`) para que dos confirmaciones simultáneas no vendan la misma unidad.
- El servidor escucha solo en `127.0.0.1` porque el endpoint MCP no tiene autenticación.

**Reglas de negocio y su prueba:**

| Regla | Prueba |
|---|---|
| La cantidad debe ser mayor que cero | `agregarFallaSiLaCantidadNoEsMayorQueCero` |
| El producto debe existir y estar activo | `agregarFallaSiElProductoNoExiste`, `agregarFallaSiElProductoEstaInactivo` |
| Agregar un producto repetido suma a la misma línea y copia el precio | `agregarElMismoProductoSumaLaCantidadEnUnaSolaLinea`, `agregarCopiaElPrecioYCalculaTotalesConEnvio` |
| No se modifica un pedido que no esté en `BORRADOR` | `noSeModificaUnPedidoQueNoEstaEnBorrador` |
| No se confirma un pedido vacío | `confirmarFallaSiElPedidoEstaVacio` |
| Confirmar descuenta stock y calcula subtotal, envío y total | `confirmarDescuentaStockYCalculaElTotal` |
| Si falla una línea no se descuenta ninguna | `confirmarConStockInsuficienteNoDescuentaNingunaLinea` |
| Método de pago válido | `confirmarFallaConMetodoDePagoDesconocido` |
| Cancelar un pedido `CONFIRMADO` devuelve el stock | `cancelarUnPedidoConfirmadoDevuelveElStock` |
| No se cancela un pedido `EN_CAMINO`, `ENTREGADO` o ya `CANCELADO` | `noSeCancelaUnPedidoEnCaminoEntregadoOYaCancelado` |
| Teléfono único y con formato válido; campos obligatorios | `registrarFallaSiElTelefonoYaExiste`, `registrarFallaConTelefonoInvalido`, `registrarFallaSinNombreOSinDireccion` |

**Comandos:**

```bash
export JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64
cd mcp-server && ./mvnw clean test

set -a && source ../.env && set +a
./mvnw spring-boot:run
```

**Resultado:** `./mvnw clean test` termina en `BUILD SUCCESS` con 39 pruebas y ningún fallo. El servidor arranca en `127.0.0.1:8888`, se conecta a MySQL, Hibernate valida el esquema sin errores y el log muestra "Registered tools: 10". Una comprobación rápida con curl confirmó el handshake `initialize`, la lista de 10 herramientas y una búsqueda real de productos.

**Problemas y solución:**

- Riesgo previsto con `ddl-auto=validate`: Hibernate 6 espera por defecto un tipo `ENUM` nativo de MySQL para los `enum` de Java, mientras que el esquema usa `VARCHAR` con una restricción `CHECK`. Solución: anotar `estado` y `metodoPago` con `@JdbcTypeCode(SqlTypes.VARCHAR)`. Por la misma razón el campo booleano `activo` se definió como `BIT(1)`, que es el tipo que Hibernate espera en MySQL.
- La prueba `contextLoads` generada por Initializr exige la base de datos encendida. Solución: se eliminó, porque se decidió hacer solo pruebas unitarias (D3.6).

**Evidencias:** `docs/evidencias/f3-arranque.txt`, `docs/evidencias/f3-pruebas.txt`, `docs/evidencias/f3-error-negocio.txt`

**Commit:** `d2fe34b` en la rama `fase-3-servidor-mcp`

## Fase 5. Chatbot web (2026-11-15 y 2026-11-16)

Se ejecutó antes que la Fase 4 por la ruta crítica (D0.5).

**Qué se hizo:**

1. Se probaron los modelos de Gemini con la clave del estudiante antes de fijar uno (D5.2).
2. `ChatConfig`: un `ChatClient` de Spring AI con el prompt de sistema, las herramientas que el cliente MCP descubre en el servidor y memoria de conversación.
3. `HerramientaRegistrada`: decorador que anota cada herramienta invocada para mostrarla en la página.
4. `ChatController`: `POST /api/chat` recibe `{conversacionId, mensaje}` y devuelve `{respuesta, herramientas}`; valida la entrada y traduce los fallos técnicos a mensajes amables.
5. Página de chat estática: historial, campo de texto, indicador "pensando", botón de nueva conversación y lista de herramientas MCP usadas bajo cada respuesta.
6. Seis pruebas unitarias del chatbot.

**Por qué:**

- El chatbot es el **host** MCP: contiene el modelo (Gemini) y un **cliente MCP** de Spring AI que, al arrancar, hace `initialize` y `tools/list` contra el servidor. No conoce las herramientas en tiempo de compilación: las descubre por el protocolo.
- En cada mensaje, Gemini decide qué herramienta llamar y con qué argumentos; Spring AI ejecuta la llamada (`tools/call`) y le devuelve el resultado al modelo, que puede encadenar varias antes de responder.
- El chatbot no tiene acceso a la base de datos ni reglas de negocio: todo pasa por el servidor MCP.

**Comandos:**

```bash
# Con MySQL y mcp-server ya arriba
export JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64
set -a && source .env && set +a
cd chatbot-client && ./mvnw spring-boot:run
# Abrir http://localhost:8080

# La misma petición que envía la página
curl -s -X POST http://localhost:8080/api/chat -H 'Content-Type: application/json' \
  -d '{"conversacionId":"demo-1","mensaje":"Hola, quiero hacer un pedido. Mi teléfono es 3001234567."}'
```

**Resultado:**

- El guion de aceptación de la guía funciona de extremo a extremo: identifica a la clienta por teléfono, lista los lácteos, crea el pedido 5 con 2 leches y una docena de huevos, quita los huevos, muestra el total ($14.600) y confirma en efectivo.
- En MySQL el pedido 5 queda `CONFIRMADO` con una línea de 2 leches, y el stock de leche pasa de 40 a 34 tras tres pedidos de 2 unidades.
- Teléfono no registrado: el asistente lo informa, pide nombre y dirección y registra al cliente 3.
- Stock insuficiente: se agregan 5 cebollas habiendo 3, `confirmarPedido` falla, el asistente explica el motivo y propone ajustar a 3; tras el ajuste el pedido se confirma.
- Tema ajeno al supermercado: el asistente se niega con cortesía.
- Un mensaje vacío recibe HTTP 400 con "Escribe un mensaje."

**Problemas y solución:**

- **`gemini-3.8-flash` no respondió** con la clave del estudiante (dos intentos, 40 s y 90 s). Solución: usar `gemini-3.5-flash-lite`, que responde en poco más de 1 s, y dejar el modelo cambiable con la variable `GEMINI_MODEL`.
- **Respuestas cortadas a mitad de frase.** Causa: Gemini puede repartir el texto en varias partes y Spring AI 1.1.8 crea una "generación" por parte; `ChatClient...call().content()` solo devuelve la primera. Solución: el controlador usa `chatResponse()` y une el texto de todas las generaciones.
- **HTTP 503 cuando una herramienta devolvía un error de negocio** (por ejemplo, teléfono no registrado). Causa: el cliente MCP convierte el resultado con `isError=true` en una excepción cuyo mensaje es texto plano, y el conector de Gemini exige que el resultado de una herramienta sea JSON ("Failed to parse JSON"). Solución: `HerramientaRegistrada` captura la excepción y entrega al modelo un JSON `{"error": "mensaje del servidor"}`. Así el modelo lee el error y propone alternativas.
- **Límite de peticiones del nivel gratuito (HTTP 429).** Al ejecutar varias conversaciones seguidas, un mensaje recibió "Se agotó por ahora la cuota gratuita de Gemini". El chatbot respondió con ese mensaje amable y la conversación siguió en el mensaje siguiente (`f5-chat-cuota-429.txt`). Para la corrida de evidencia se dejaron 20 s entre mensajes.
- La prueba `contextLoads` generada por Initializr se eliminó porque necesita el servidor MCP encendido y la clave de Gemini.

**Limitaciones conocidas:**

- El servidor MCP debe estar arriba antes de arrancar el chatbot; si no, el chatbot no inicia. Si el servidor MCP se reinicia, hay que reiniciar también el chatbot, porque la sesión MCP deja de ser válida.
- Si Gemini falla en mitad de una secuencia de herramientas, las ya ejecutadas no se deshacen (por ejemplo, un pedido creado en `BORRADOR`). El estado queda en MySQL y el asistente puede retomarlo.

**Evidencias:** `f5-chat-pedido.txt`, `f5-chat-cliente-nuevo.txt`, `f5-chat-error-stock.txt`, `f5-chat-cuota-429.txt`, `f5-select-pedido.txt`, `f5-log-servidor.txt`, `f5-pruebas.txt` (todas en `docs/evidencias/`). Capturas pendientes del estudiante: `f5-chat-catalogo.png`, `f5-chat-pedido.png`, `f5-chat-error-stock.png`.

**Commit:** `dbcfa26` en la rama `fase-5-chatbot`

### Ajustes al chatbot tras tomar las capturas (2026-11-16)

Al automatizar las capturas de pantalla aparecieron tres defectos más, que se corrigieron en la rama `fase-4-verificacion-protocolo`.

**Cómo se tomaron las capturas:** con Google Chrome en modo headless (perfil temporal) manejado por el protocolo DevTools desde un script de Node. El script escribe los mensajes en la página real, espera cada respuesta y guarda la imagen. No se instaló nada.

**Problemas y solución:**

- **El chatbot se quedaba colgado sin límite cuando Gemini no respondía.** Un volcado de hilos (`jstack`) mostró la petición bloqueada esperando la respuesta de la API: el SDK de Google no trae tiempo de espera. Solución: `ChatConfig` define el bean `Client` con un tiempo de espera (`chatbot.gemini.timeout`), que reemplaza al que autoconfigura Spring AI. Se comprobó forzando un límite de 50 ms: el chatbot responde en 0,3 s con "La respuesta tardó demasiado" (`f5-timeout-gemini.txt`).
- **Reintentos.** El `RetryTemplate` de Spring AI no reintenta un tiempo de espera agotado y por defecto hace hasta 10 intentos con esperas crecientes para otros errores. Solución: `ChatConfig` define su propio `RetryTemplate` con un máximo de intentos configurable (`chatbot.gemini.intentos`). Reintentar es seguro porque esa llamada solo genera texto; las herramientas se ejecutan fuera del reintento.
- **El modelo duplicaba el pedido de un turno al siguiente.** En el mensaje "confirma el pedido" volvía a identificar al cliente y a crear otro pedido. Causa: la memoria de conversación guarda solo los textos, no las llamadas a herramientas ni sus resultados, así que el modelo no "recordaba" haber creado el pedido. Añadir reglas al prompt no bastó. Solución: `ChatController` guarda por conversación las últimas herramientas ejecutadas con su resultado y se las entrega al modelo en cada turno, dentro del mensaje de sistema.
- **Texto del usuario con llaves.** `ChatClient` trata el texto como plantilla y las llaves `{}` tienen significado en ella. Solución: el prompt, el historial y el mensaje del usuario se pasan como parámetros de la plantilla, no como texto de plantilla.

**Diagnóstico del nivel gratuito de Gemini.** Varias pruebas automáticas seguidas terminaron con todas las llamadas del chatbot agotando el tiempo de espera. Para aislar la causa se capturó la petición exacta que el chatbot envía a Gemini (apuntando una instancia de diagnóstico a un servidor local) y se reenvió con curl: respondió 4 de 4 veces en cerca de 1 s. El contenido de la petición es correcto. Los cuelgues aparecen en ráfagas cuando se hacen muchas llamadas seguidas (cada mensaje del usuario son entre 2 y 6 llamadas al modelo): es el límite de peticiones por minuto del nivel gratuito, que unas veces devuelve 429 y otras deja la llamada sin respuesta. Con el ritmo de una persona escribiendo el problema es mucho menos frecuente.

**Reglas añadidas al prompt de sistema:** trabajar siempre sobre el mismo pedido de la conversación, y que los resúmenes reflejen exactamente lo que devolvió la herramienta.

**Evidencias:** `f5-chat-catalogo.png`, `f5-chat-pedido.png`, `f5-chat-error-stock.png`, `f5-timeout-gemini.txt`

## Fase 4. Verificación del protocolo MCP (2026-11-16)

Se ejecutó después de la Fase 5 por la ruta crítica (D0.5).

**Qué se hizo:**

1. Con curl se recorrió el ciclo completo del protocolo contra `http://localhost:8888/mcp`, guardando cada petición y respuesta.
2. Se ejecutó por JSON-RPC, sin ningún modelo de lenguaje, el plan de pruebas de seis pasos de la guía, comprobando cada escritura con un `SELECT` en MySQL.
3. Se conectó MCP Inspector 2.13.0 al servidor por Streamable HTTP, se listaron las herramientas, se ejecutó `buscarProductos` y se confirmó un pedido desde su interfaz.

**Por qué:** esta fase separa el protocolo del modelo. Si una herramienta responde bien por JSON-RPC y por el Inspector, cualquier fallo posterior en una conversación es del modelo o del host, no del servidor. También muestra qué viaja realmente por la red, que es lo que hay que explicar del protocolo.

**El ciclo MCP, con lo observado:**

| Paso | Petición | Respuesta observada |
|---|---|---|
| 1. Negociación | `initialize` con `protocolVersion`, `capabilities` y `clientInfo` | HTTP 200 en `application/json`, con la cabecera `Mcp-Session-Id`, la versión `2025-03-26`, las capacidades del servidor (`tools`, `resources`, `prompts`, `logging`, `completions`) y `serverInfo` |
| 2. Fin de inicialización | `notifications/initialized` (notificación, sin `id`) con la cabecera de sesión | HTTP 202 sin cuerpo |
| 3. Descubrimiento | `tools/list` | Evento SSE (`text/event-stream`) con las 10 herramientas: nombre, descripción y esquema JSON de entrada con sus campos obligatorios |
| 4. Ejecución | `tools/call` con `name` y `arguments` | Evento SSE con `result.content[0].text` (el DTO serializado como JSON) e `isError: false` |

Un error de negocio no es un error JSON-RPC: llega como un `result` normal con `isError: true` y el mensaje en español.

**Comandos:**

```bash
# 1. initialize: anotar la cabecera Mcp-Session-Id
curl -i -X POST http://localhost:8888/mcp \
  -H "Content-Type: application/json" -H "Accept: application/json, text/event-stream" \
  -d '{"jsonrpc":"2.0","id":1,"method":"initialize","params":{"protocolVersion":"2025-03-26","capabilities":{},"clientInfo":{"name":"curl","version":"1.0"}}}'

# 2. notificación
curl -i -X POST http://localhost:8888/mcp \
  -H "Content-Type: application/json" -H "Accept: application/json, text/event-stream" \
  -H "Mcp-Session-Id: <ID>" -d '{"jsonrpc":"2.0","method":"notifications/initialized"}'

# 3. descubrimiento y 4. ejecución
curl -s -X POST http://localhost:8888/mcp \
  -H "Content-Type: application/json" -H "Accept: application/json, text/event-stream" \
  -H "Mcp-Session-Id: <ID>" -d '{"jsonrpc":"2.0","id":2,"method":"tools/list"}'
curl -s -X POST http://localhost:8888/mcp \
  -H "Content-Type: application/json" -H "Accept: application/json, text/event-stream" \
  -H "Mcp-Session-Id: <ID>" \
  -d '{"jsonrpc":"2.0","id":3,"method":"tools/call","params":{"name":"buscarProductos","arguments":{"texto":"leche"}}}'

# MCP Inspector
npx @modelcontextprotocol/inspector
```

En el Inspector 2.13.0: abrir la URL que imprime la consola, "Add Servers" → "Add manually", identificador `supermercado-mcp`, transporte `streamable-http`, URL `http://localhost:8888/mcp`, activar el interruptor de conexión y abrir la pestaña "Tools".

**Resultado del plan de pruebas (pedido 15, `f4-plan-pruebas.txt`):**

| Paso | Qué se probó | Resultado |
|---|---|---|
| 1 | `listarCategorias` y `buscarProductos` con "leche" | 6 categorías y 2 productos reales de MySQL |
| 2 | `registrarCliente` | Fila nueva en `cliente`; registrar otra vez el mismo teléfono falla |
| 3 | `crearPedido`, dos `agregarProductoAPedido` y `consultarPedido` | Pedido en `BORRADOR` con 2 líneas, subtotal $31.300, envío $5.000 |
| 4 | Agregar 3 gaseosas habiendo 1 y confirmar | Se agrega (D2.7), pero `confirmarPedido` falla con "Stock insuficiente de Gaseosa cola 1.5 L: se pidieron 3 y hay 1"; el pedido sigue en `BORRADOR` y no se descuenta el stock de ninguna línea |
| 5 | Quitar la gaseosa y confirmar | `CONFIRMADO`, total $36.300; arroz 80 → 78, café 16 → 15; un pedido confirmado ya no se modifica |
| 6 | `cancelarPedido` | `CANCELADO`; arroz y café vuelven a 80 y 16; cancelarlo otra vez falla y no devuelve stock de nuevo |

Desde el Inspector se confirmó además el pedido 16, con el mismo efecto en la base (`f4-select-post-confirmar.txt`).

**Problemas y solución:**

- La respuesta de `initialize` llega como `application/json`, mientras que las de `tools/list` y `tools/call` llegan como eventos SSE (líneas `id:`, `event:` y `data:`). El JSON-RPC está en la línea `data:`. Un cliente de Streamable HTTP debe aceptar ambos formatos, por eso la cabecera `Accept: application/json, text/event-stream`.
- El PDF lanza el Inspector 0.12.0 con el argumento `sse`. No se heredó: se usó la versión reciente y el transporte Streamable HTTP. Su interfaz cambió por completo respecto a las capturas del PDF.
- El Inspector cierra la conexión tras unos minutos de inactividad; basta volver a activar el interruptor.

**Evidencias:** `f4-curl-initialize.txt`, `f4-curl-initialized.txt`, `f4-curl-tools-list.txt`, `f4-curl-tools-call.txt`, `f4-plan-pruebas.txt`, `f4-select-post-confirmar.txt`, `f4-inspector-conexion.png`, `f4-inspector-tools.png`, `f4-inspector-confirmar.png` (todas en `docs/evidencias/`).

**Commit:** `95cf16c` en la rama `fase-4-verificacion-protocolo` (junto con `891404f`, los ajustes al chatbot)
