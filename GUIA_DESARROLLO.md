# Guía de desarrollo para Claude Code

**Proyecto:** Chatbot de pedidos de supermercado a domicilio sobre un servidor MCP
**Curso:** Laboratorio Arquitectura de Software (Prof. Diego José Luis Botia V.), 2026
**Documento base:** `Guia_Laboratorio_MCP_SpringBoot_SpringAI_Claude_GoogleADK.pdf`
**Destinatario de esta guía:** Claude Code (extensión de VS Code) trabajando junto al estudiante en Ubuntu 22.04 LTS

---

## 0. Cómo se usa esta guía

### 0.1 Para el estudiante

1. Crea una carpeta vacía (por ejemplo `~/proyectos/supermercado-mcp`) y ábrela en VS Code.
2. Copia dentro este archivo como `GUIA_DESARROLLO.md` y el PDF del laboratorio en `docs/referencia/`.
3. Abre Claude Code y pega el **prompt inicial del Anexo A**.
4. Claude Code avanzará fase por fase. Antes de cada fase te presentará las decisiones abiertas con su recomendación y la explicación; no escribirá código de esa fase hasta que respondas.
5. Las capturas de pantalla (Inspector, chatbot, Claude Desktop) las tomas tú; Claude Code te dirá cuándo, qué debe verse y con qué nombre guardarlas.
6. La Fase 9 (entregable) no empieza hasta que entregues la guía del documento.

### 0.2 Para Claude Code

Esta guía es tu especificación. Léela completa antes de actuar. Las secciones 2 y 3 son obligatorias en todo momento; las fases de la sección 6 se ejecutan en orden. Donde la guía muestre código o propiedades, tómalos como referencia y **verifícalos contra la documentación oficial de la versión que se haya fijado** (sección 7): las API de Spring AI cambian entre líneas 1.1 y 2.x.

---

## 1. Contexto

### 1.1 Qué enseña el laboratorio base

El PDF construye `tarea-mcp`, un servidor MCP con Spring Boot 3.4.7, Java 21 y Spring AI 1.1.4 que publica siete herramientas de gestión de tareas sobre MySQL mediante `@Tool` y `MethodToolCallbackProvider`, con transporte Streamable HTTP en `http://localhost:8888/mcp`. Luego lo consume desde tres clientes: MCP Inspector, Claude Desktop (con el puente `mcp-remote`) y un agente Google ADK con Gemini.

### 1.2 Enunciado a resolver (ejercicio propuesto)

> Realice un cliente de tipo ChatBot e intégrelo con un servidor MCP que conecte a una BD con MySQL, PostgreSQL u otro motor de su elección, para realizar pedidos de productos de un supermercado a domicilio. Explique cada paso que emplee para la solución.

Fecha de entrega indicada en el PDF: **15 de noviembre de 2026**.

### 1.3 Qué se va a construir

Tres piezas en un mismo repositorio:

| Pieza | Qué es | Puerto |
|---|---|---|
| `mcp-server` | Servidor MCP (Spring Boot + Spring AI) con las herramientas de catálogo, clientes y pedidos | 8888 |
| `chatbot-client` | Chatbot web propio: Spring Boot + Spring AI MCP Client + Gemini, con página de chat | 8080 |
| MySQL 8 | Base de datos `supermercado_mcp` en Docker Compose | 3306 (o 3307, ver D2.1) |

Además se demostrará que el mismo servidor sirve a otros hosts: **MCP Inspector** y **Claude Desktop**.

### 1.4 "Explique cada paso"

El enunciado exige explicar cada paso. Por eso la bitácora (`docs/BITACORA.md`) y el registro de decisiones (`docs/DECISIONES.md`) no son opcionales: son la materia prima del entregable final.

---

## 2. Decisiones ya tomadas por el estudiante

No las vuelvas a preguntar. Si durante el desarrollo aparece un motivo técnico para cambiarlas, detente y plantéalo.

| # | Decisión | Elección | Motivo |
|---|---|---|---|
| T1 | Tipo de cliente | Chatbot web propio con Spring AI | Cumple literalmente "realice un cliente" y reutiliza el stack del laboratorio |
| T2 | Modelo de lenguaje | Gemini (Gemini Developer API con API key) | Clave gratuita de Google AI Studio; es el proveedor que ya usa el laboratorio |
| T3 | Base de datos | MySQL 8 en Docker Compose | Mismo motor del laboratorio y entorno reproducible para quien clone el repositorio |
| T4 | Alcance | Intermedio: categorías, productos con stock, clientes, pedidos con detalle y estados; 8-10 herramientas, validaciones y pruebas | Equilibrio entre calidad y tiempo |
| T5 | Repositorio | Monorepo público en GitHub | Un único enlace para el profesor, sin gestionar accesos |
| T6 | Clientes adicionales | MCP Inspector y Claude Desktop | Verificación del protocolo sin LLM y demostración de interoperabilidad |
| T7 | Entorno | Ubuntu 22.04 LTS, VS Code, Claude Code | Los comandos van en bash y con `./mvnw` (no PowerShell ni `mvnw.cmd`) |
| T8 | Ritmo de consulta | Pausa por fase | Ver protocolo en la sección 3 |

---

## 3. Reglas de trabajo (obligatorias)

### 3.1 Protocolo de consulta por fase

En cada fase sigue siempre este ciclo:

1. **Abrir la fase.** Resume en dos o tres líneas qué se va a hacer y qué quedará funcionando al terminar.
2. **Consultar las decisiones de la fase.** Preséntalas todas juntas usando la herramienta de preguntas de Claude Code (o texto, si no está disponible). Cada decisión lleva: la pregunta, las opciones con sus consecuencias, **tu recomendación marcada** y **la explicación de por qué**. Usa como base las decisiones `Dx.y` de esta guía, pero si al inspeccionar el entorno o la documentación tu recomendación cambia, dilo y explica el motivo.
3. **Esperar la respuesta.** No escribas código de la fase antes de recibirla.
4. **Registrar.** Anota cada decisión en `docs/DECISIONES.md` (formato del Anexo B).
5. **Ejecutar** las tareas de la fase.
6. **Verificar** los criterios de aceptación ejecutando de verdad los comandos. No declares nada como "funciona" sin haber visto la salida.
7. **Documentar** el paso en `docs/BITACORA.md`: qué se hizo, por qué, comandos usados, resultado, problemas y cómo se resolvieron.
8. **Cerrar.** Muestra un resumen, las evidencias generadas, pide al estudiante las capturas que correspondan, haz commit y pregunta si se continúa.

Si surge una decisión no prevista en la guía y afecta arquitectura, modelo de datos, dependencias o cualquier acción sobre GitHub, detente y consúltala con el mismo formato. Las decisiones menores de implementación (nombres de variables locales, orden de métodos) tómalas tú.

### 3.2 Acciones que siempre requieren confirmación explícita

- Crear el repositorio en GitHub, hacer `git push`, crear tags o releases.
- Instalar software en el sistema (`apt`, SDKMAN, nvm, Docker).
- Borrar volúmenes de Docker, hacer `DROP`, `git reset --hard`, `git push --force` o reescribir historial.
- Modificar archivos fuera de la carpeta del proyecto (por ejemplo la configuración de Claude Desktop).

### 3.3 Secretos

- Ninguna clave en código, commits, bitácora ni capturas. Aplica a `GEMINI_API_KEY` y a las contraseñas de MySQL.
- Los secretos viven en `.env` (ignorado por git). El repositorio solo contiene `.env.example` con valores ficticios.
- Antes de cada commit revisa `git diff --cached` buscando claves. Antes del primer push revisa todo el historial.
- Nunca pidas al estudiante que pegue la clave en el chat: indícale que la escriba él mismo en `.env`.

### 3.4 Versiones y dependencias

- No inventes versiones ni mezcles ejemplos de versiones distintas (advertencia explícita del PDF). Fija el conjunto en la Fase 1 y respétalo.
- Si Maven no resuelve algo, consulta la documentación oficial antes de cambiar números.
- Usa siempre el BOM de Spring AI; no declares versión en los starters.

### 3.5 Git

- Un commit (o varios pequeños) al cerrar cada fase, con mensaje descriptivo según la convención de D1.6.
- No incluyas `target/`, `logs/`, `.env`, `.idea/` ni archivos de sistema.

### 3.6 Evidencias

Todo lo que demuestre que algo funciona se guarda en `docs/evidencias/` con prefijo de fase: `f4-curl-initialize.txt`, `f4-inspector-tools.png`, `f5-chat-pedido.png`, etc. Las salidas de terminal las guardas tú; las capturas de interfaz las toma el estudiante.

### 3.7 Continuidad entre sesiones

Si se reinicia la conversación, antes de continuar relee esta guía, `docs/DECISIONES.md` y `docs/BITACORA.md`, y ejecuta `git log --oneline` para saber en qué fase se quedó el trabajo.

---

## 4. Arquitectura objetivo

```
                         Navegador
                             | HTTP (POST /api/chat)
                             v
+---------------------------------------------------+
| chatbot-client  (Spring Boot, :8080)              |
|  UI de chat estática                              |
|  ChatClient (Spring AI) -----> Gemini API         |
|  Spring AI MCP Client (Streamable HTTP)           |
+---------------------------+-----------------------+
                            | JSON-RPC 2.0 sobre Streamable HTTP
   MCP Inspector -----------+
   Claude Desktop ----------+   (mcp-remote como puente STDIO->HTTP si hace falta)
                            v
+---------------------------------------------------+
| mcp-server  (Spring Boot + Spring AI, :8888/mcp)  |
|  SupermercadoTools (@Tool)                        |
|  Servicios de dominio (reglas de negocio)         |
|  Spring Data JPA                                  |
+---------------------------+-----------------------+
                            | JDBC
                            v
                 MySQL 8 (Docker) supermercado_mcp
```

Roles MCP, para usarlos con precisión en la documentación: el **host** es la aplicación conversacional (nuestro chatbot, Claude Desktop); el **cliente MCP** es el componente dentro del host que negocia el protocolo; el **servidor MCP** anuncia y ejecuta herramientas; la **herramienta** es cada método `@Tool`.

### 4.1 Estructura del repositorio

```
supermercado-mcp/
├── README.md
├── CLAUDE.md                  # reglas resumidas para Claude Code
├── GUIA_DESARROLLO.md         # este archivo
├── docker-compose.yml
├── .env.example
├── .gitignore
├── .mcp.json                  # opcional (D6.3)
├── db/
│   └── init/
│       ├── 01-schema.sql
│       └── 02-data.sql
├── mcp-server/                # proyecto Maven independiente
│   ├── pom.xml, mvnw
│   └── src/main/java/<paquete>/
│       ├── model/  repository/  service/  tools/  dto/  exception/
│       └── SupermercadoMcpApplication.java
├── chatbot-client/            # proyecto Maven independiente
│   ├── pom.xml, mvnw
│   └── src/main/
│       ├── java/<paquete>/ (config/, web/, ChatbotApplication.java)
│       └── resources/static/ (index.html, app.js, styles.css)
└── docs/
    ├── BITACORA.md
    ├── DECISIONES.md
    ├── arquitectura.md
    ├── pruebas.md
    ├── evidencias/
    └── referencia/            # PDF del laboratorio
```

---

## 5. Inconsistencias del PDF base que no deben heredarse

| Dónde | Problema | Qué hacer |
|---|---|---|
| Secciones 4 y 7.3 vs 8.1, 8.2, 9.2 y 10.4 | El servidor se configura en el puerto **8888**, pero varios comandos y el código del agente usan **8088** | Usar 8888 en todas partes y definirlo en un único lugar |
| Sección 8.2 | El Inspector se lanza con el argumento `sse` contra un servidor STREAMABLE | En la interfaz seleccionar transporte **Streamable HTTP**; no forzar SSE |
| Sección 7.3 | `ddl-auto=update` convive con un `schema.sql` manual | Una sola fuente de verdad para el esquema (D2.3) |
| Sección 6 | La fecha se guarda como `VARCHAR(255)` | Usar tipos de fecha reales (`DATETIME` / `LocalDateTime`) |
| Sección 7.3 | La aplicación se conecta como `root` | Usuario de aplicación con permisos solo sobre su base |
| Sección 7.6 | Las herramientas devuelven entidades JPA | Devolver DTO (D3.3) |
| Secciones 9.2 y 9.3 | El servidor se llama `tareas-mcp` en el JSON y `tareas-conector` en la captura | Un único nombre: `supermercado-mcp` |
| Sección 10.3 | `google-adk` 0.6.0 con `google-adk-dev` 1.0.0 | No aplica: no se usa ADK en este proyecto |
| Todo el documento | Comandos de Windows/PowerShell | Traducir a bash para Ubuntu |

---

## 6. Fases

### Ruta crítica si el tiempo aprieta

La entrega figura para el 15 de noviembre de 2026. Si no alcanza el tiempo, el orden de prioridad es: Fases 0-3 (servidor funcionando) → Fase 5 (chatbot) → Fase 4 con Inspector → Fase 8 (README) → Fase 6 (Claude Desktop) → pruebas automatizadas adicionales. Lo que se recorte se anota en `docs/DECISIONES.md` como alcance pendiente. Pregunta al estudiante al inicio cuánto tiempo real hay disponible.

---

### Fase 0. Verificación del entorno

**Objetivo:** confirmar que la máquina tiene todo lo necesario antes de escribir código.

**Comprobaciones**

```bash
lsb_release -a
java -version && echo "$JAVA_HOME"
docker --version && docker compose version && docker ps
node --version && npm --version
git --version && git config user.name && git config user.email
gh --version && gh auth status
ss -ltnp | grep -E ':(3306|8080|8888)\b' || echo "puertos libres"
```

Se necesita: JDK 21, Docker Engine con el plugin Compose (y el usuario en el grupo `docker`), Node.js LTS con npm, git configurado, GitHub CLI autenticado, y los puertos 3306, 8080 y 8888 libres. Además, fuera de la terminal: una API key de Gemini (Google AI Studio) y Claude Desktop instalado.

**Decisiones a consultar (solo las que apliquen según lo que falte)**

- **D0.1 Instalación de JDK 21, si falta.**
  - a) SDKMAN con Temurin 21 **(recomendada)**: no requiere `sudo`, permite convivir con otras versiones y cambiar por proyecto.
  - b) `sudo apt install openjdk-21-jdk`: más simple, pero una sola versión global.
- **D0.2 Instalación de Node.js, si falta o es antiguo.**
  - a) nvm con la LTS vigente **(recomendada)**: el Node de los repositorios de Ubuntu 22.04 es demasiado viejo para `npx @modelcontextprotocol/inspector`.
  - b) Repositorio NodeSource.
- **D0.3 Autenticación con GitHub.**
  - a) `gh auth login` por navegador con HTTPS **(recomendada)**: lo más rápido y `gh` configura las credenciales de git.
  - b) Clave SSH: preferible si ya existe una configurada.
- **D0.4 Claude Desktop en Ubuntu.** Existe una versión oficial en beta para Ubuntu 22.04+ distribuida por el repositorio apt de Anthropic. Si no está instalada, pregunta si el estudiante la instala ahora (instrucciones en https://code.claude.com/docs/en/desktop-linux y en el centro de ayuda de Claude) o si se deja para la Fase 6.
- **D0.5 Tiempo disponible.** Pregunta cuántas horas reales hay antes de la entrega para ajustar la ruta crítica.

**Criterios de aceptación:** todas las comprobaciones pasan; `docker run --rm hello-world` funciona sin `sudo`; el estudiante confirma que tiene la API key de Gemini (sin mostrarla).

---

### Fase 1. Repositorio y andamiaje

**Objetivo:** repositorio creado en GitHub con la estructura de la sección 4.1, los dos proyectos Maven generados y compilando vacíos.

**Decisiones a consultar**

- **D1.1 Nombre del repositorio y cuenta.** Recomendación: `supermercado-mcp` en la cuenta personal del estudiante. Es descriptivo y coincide con el nombre del servidor MCP. Pregunta también por una descripción corta.
- **D1.2 Autores.** Pregunta si el trabajo es individual o en grupo y los nombres completos para el README y el entregable.
- **D1.3 Paquete base e idioma del código.**
  - a) Paquete `com.udea.supermercado` y dominio en español (`Producto`, `Pedido`, `confirmarPedido`) **(recomendada)**: sigue la convención del laboratorio y las descripciones de las herramientas quedan en el idioma en que conversará el usuario.
  - b) Todo en inglés: más estándar en la industria, pero se aleja del material del curso.
- **D1.4 Línea de versiones.**
  - a) La misma línea del laboratorio: Spring Boot 3.4.x/3.5.x con Spring AI 1.1.x, usando el último parche de 1.1 que sea compatible **(recomendada)**: el código del PDF (`@Tool`, `MethodToolCallbackProvider`, propiedades `spring.ai.mcp.server.*`) aplica tal cual, y el profesor reconocerá la estructura. Verifica en la documentación qué versión de Spring Boot soporta el parche elegido.
  - b) Exactamente las del PDF (Boot 3.4.7 + Spring AI 1.1.4): riesgo mínimo de sorpresas; se renuncia a correcciones posteriores.
  - c) Spring AI 2.x: es la línea estable más reciente, pero cambia propiedades y requisitos de Spring Boot; exige migrar los ejemplos del laboratorio. Solo si el estudiante quiere mostrar actualización tecnológica y hay tiempo.
  - Ambos módulos deben usar **la misma** versión de Spring AI.
- **D1.5 Licencia.**
  - a) MIT **(recomendada)**: repositorio público académico, permisiva y breve.
  - b) Sin licencia.
- **D1.6 Convención de commits y ramas.**
  - a) Conventional Commits en español (`feat(server): ...`, `docs: ...`) directamente sobre `main`, un commit o más por fase **(recomendada)**: historial legible que además sirve de evidencia del proceso, sin la sobrecarga de pull requests para un trabajo corto.
  - b) Una rama y un pull request por fase: mejor trazabilidad, más lento.
- **D1.7 Momento de crear el repositorio remoto.**
  - a) Ahora, y hacer push al cerrar cada fase **(recomendada)**: respaldo continuo y un historial con fechas reales.
  - b) Solo al final.

**Tareas**

1. `git init -b main`; crear `.gitignore` (Java/Maven, `.env`, `logs/`, `target/`, IDE, sistema).
2. Generar los dos proyectos con Spring Initializr (https://start.spring.io, por web o con `curl https://start.spring.io/starter.zip ...`), Maven, Java 21, empaquetado jar:
   - `mcp-server`: Spring Web, Spring Data JPA, Validation, MySQL Driver; añadir a mano el BOM de Spring AI y `spring-ai-starter-mcp-server-webmvc`.
   - `chatbot-client`: Spring Web, Validation; añadir el BOM, `spring-ai-starter-mcp-client` y `spring-ai-starter-model-google-genai`.
3. Crear `.env.example`, `README.md` inicial, `docs/BITACORA.md`, `docs/DECISIONES.md` (con las decisiones T1-T8 ya registradas) y `CLAUDE.md` con un resumen de la sección 3, los comandos de arranque y los puertos.
4. Compilar ambos módulos: `./mvnw -q -DskipTests package` en cada carpeta.
5. Con confirmación: `gh repo create <nombre> --public --source=. --remote=origin --description "..."` y primer push.

**Criterios de aceptación:** ambos módulos compilan; `git status` limpio; el repositorio es visible en GitHub y no contiene secretos.

---

### Fase 2. Base de datos y modelo de datos

**Objetivo:** MySQL corriendo en Docker con el esquema y datos de prueba cargados.

**Decisiones a consultar**

- **D2.1 Puerto de MySQL en el host.**
  - a) 3306 **(recomendada si está libre)**: igual que el laboratorio.
  - b) 3307 → 3306 del contenedor **(recomendada si ya hay un MySQL local)**: evita el conflicto sin tocar la instalación existente.
  - En ambos casos publicar solo en `127.0.0.1`.
- **D2.2 Versión de la imagen.** Recomendación: fijar una etiqueta concreta de la serie 8 (por ejemplo `mysql:8.4`, la LTS) en lugar de `latest`, para que el entorno sea reproducible.
- **D2.3 Gestión del esquema.**
  - a) Scripts SQL en `db/init/` que el contenedor ejecuta al crearse, con `spring.jpa.hibernate.ddl-auto=validate` **(recomendada)**: el esquema queda explícito y versionado, sirve directamente para el informe y Hibernate avisa si las entidades no coinciden.
  - b) Flyway: lo correcto en un proyecto real; añade una dependencia y un concepto más que explicar.
  - c) `ddl-auto=update` como el laboratorio: lo más rápido, pero el esquema queda implícito y no hay control de cambios.
- **D2.4 Dónde vive el carrito.**
  - a) El pedido nace en estado `BORRADOR` y se le agregan o quitan productos con herramientas; al confirmar se valida y descuenta el stock **(recomendada)**: el estado está en la base de datos y no en la memoria del modelo, así que no se pierde ni se inventa; permite una conversación natural ("agrega dos leches", "quita el pan", "confirma") y el mismo pedido es visible desde Claude Desktop.
  - b) Una sola herramienta `crearPedido` que recibe la lista completa de productos: menos herramientas, pero el carrito depende de que el modelo recuerde bien la conversación.
- **D2.5 Identificación del cliente.**
  - a) Por número de teléfono, único **(recomendada)**: es el dato natural en un domicilio y fácil de dictar en un chat.
  - b) Por correo electrónico.
  - c) Por documento de identidad: dato más sensible sin beneficio para la práctica.
- **D2.6 Dinero y costo de envío.**
  - Precios en `DECIMAL(12,2)` / `BigDecimal`, en pesos colombianos **(recomendada)**: nunca `double` para dinero.
  - Costo de envío: a) tarifa fija configurable por propiedad **(recomendada)**, simple y visible en el total; b) sin costo de envío; c) gratis a partir de un monto mínimo.
- **D2.7 Momento de descontar el stock.**
  - a) Al confirmar el pedido, dentro de una transacción y con bloqueo optimista (`@Version`) en `Producto` **(recomendada)**: los borradores abandonados no retienen inventario y se evita vender dos veces la misma unidad.
  - b) Al agregar al borrador: reserva inmediata, pero obliga a liberar stock de borradores abandonados.
- **D2.8 Datos de prueba.** Recomendación: unas 6 categorías, unos 30 productos con nombres y precios verosímiles en COP (algunos con stock 0 o muy bajo para probar validaciones) y 2 clientes ficticios. Todos los datos son sintéticos.

**Modelo propuesto** (ajústalo según D2.4-D2.7 y muéstralo al estudiante antes de crear los scripts)

| Tabla | Columnas principales |
|---|---|
| `categoria` | `id`, `nombre` (único), `descripcion` |
| `producto` | `id`, `nombre`, `descripcion`, `precio`, `stock`, `unidad` (ej. "1 L", "500 g"), `activo`, `categoria_id` FK, `version` |
| `cliente` | `id`, `nombre`, `telefono` (único), `direccion`, `email` (opcional), `creado_en` |
| `pedido` | `id`, `cliente_id` FK, `estado`, `direccion_entrega`, `subtotal`, `costo_envio`, `total`, `metodo_pago`, `creado_en`, `confirmado_en`, `version` |
| `detalle_pedido` | `id`, `pedido_id` FK, `producto_id` FK, `cantidad`, `precio_unitario`, `subtotal`; único (`pedido_id`, `producto_id`) |

Estados del pedido: `BORRADOR → CONFIRMADO → EN_CAMINO → ENTREGADO`, y `CANCELADO` desde `BORRADOR` o `CONFIRMADO`. `precio_unitario` se copia del producto al agregarlo, para que un cambio de precio posterior no altere pedidos existentes.

**Referencia de `docker-compose.yml`**

```yaml
services:
  mysql:
    image: mysql:8.4
    container_name: supermercado-mysql
    environment:
      MYSQL_ROOT_PASSWORD: ${MYSQL_ROOT_PASSWORD}
      MYSQL_DATABASE: supermercado_mcp
      MYSQL_USER: ${DB_USER}
      MYSQL_PASSWORD: ${DB_PASSWORD}
    command: ["--character-set-server=utf8mb4", "--collation-server=utf8mb4_unicode_ci"]
    ports:
      - "127.0.0.1:${DB_PORT:-3306}:3306"
    volumes:
      - mysql_data:/var/lib/mysql
      - ./db/init:/docker-entrypoint-initdb.d:ro
    healthcheck:
      test: ["CMD-SHELL", "mysqladmin ping -h localhost -u root -p$$MYSQL_ROOT_PASSWORD"]
      interval: 10s
      retries: 10
volumes:
  mysql_data:
```

`.env.example`:

```
MYSQL_ROOT_PASSWORD=cambia_esto
DB_USER=supermercado_app
DB_PASSWORD=cambia_esto
DB_PORT=3306
GEMINI_API_KEY=pega_aqui_tu_clave
```

Importante: los scripts de `db/init` solo se ejecutan cuando el volumen está vacío. Para recargarlos hay que borrar el volumen (`docker compose down -v`), que es una acción destructiva y requiere confirmación.

**Criterios de aceptación**

```bash
docker compose up -d && docker compose ps          # estado healthy
docker compose exec mysql sh -c 'mysql -u"$MYSQL_USER" -p"$MYSQL_PASSWORD" supermercado_mcp -e "SHOW TABLES; SELECT COUNT(*) FROM producto;"'
```

Guardar la salida en `docs/evidencias/f2-tablas.txt`.

---

### Fase 3. Servidor MCP

**Objetivo:** `mcp-server` arranca en 8888, se conecta a MySQL y registra las herramientas.

**Decisiones a consultar**

- **D3.1 Catálogo de herramientas.** Propuesta de 10 (con D2.4 opción a). Muéstrala y pregunta si se añade, quita o renombra alguna:

  | # | Herramienta | Parámetros | Efecto |
  |---|---|---|---|
  | 1 | `listarCategorias` | — | Lectura |
  | 2 | `buscarProductos` | `texto?`, `categoriaId?`, `soloDisponibles?` | Lectura; incluye precio y stock |
  | 3 | `buscarCliente` | `telefono` | Lectura |
  | 4 | `registrarCliente` | `nombre`, `telefono`, `direccion`, `email?` | Escritura |
  | 5 | `crearPedido` | `clienteId`, `direccionEntrega?` | Crea un pedido en `BORRADOR` |
  | 6 | `agregarProductoAPedido` | `pedidoId`, `productoId`, `cantidad` | Escritura; suma si ya existe la línea |
  | 7 | `quitarProductoDePedido` | `pedidoId`, `productoId` | Escritura |
  | 8 | `consultarPedido` | `pedidoId` | Lectura; detalle y totales |
  | 9 | `confirmarPedido` | `pedidoId`, `metodoPago` | Valida stock, descuenta, calcula total |
  | 10 | `cancelarPedido` | `pedidoId` | Cancela; devuelve stock si estaba confirmado |

  Opcional: `listarPedidosDeCliente(clienteId)`. No se exponen herramientas de administración (crear o borrar productos, cambiar precios): el PDF pide publicar solo las operaciones necesarias.

- **D3.2 Organización del código.**
  - a) Clase `SupermercadoTools` delgada con los `@Tool`, que delega en servicios de dominio (`CatalogoService`, `ClienteService`, `PedidoService`) donde viven las reglas y las transacciones **(recomendada)**: las reglas se prueban sin MCP y la capa de herramientas queda como un adaptador, lo que da un buen argumento arquitectónico para el informe.
  - b) Todo en una clase como `TareaToolsService` del laboratorio: menos archivos, pero mezcla protocolo y negocio.
- **D3.3 Qué devuelven las herramientas.**
  - a) DTO como `record` de Java **(recomendada)**: evita problemas de carga perezosa y ciclos al serializar entidades con relaciones, y controla exactamente qué ve el modelo.
  - b) Entidades JPA directamente, como el laboratorio: funciona con una sola tabla, pero aquí hay relaciones.
- **D3.4 Registro de herramientas.**
  - a) Bean `ToolCallbackProvider` construido con `MethodToolCallbackProvider`, igual que el PDF **(recomendada)**: es lo que se enseñó en clase.
  - b) Anotaciones MCP con autoescaneo (`@McpTool`), disponibles en versiones recientes de Spring AI: más idiomático hoy, pero distinto al material del curso.
- **D3.5 Errores de negocio.**
  - a) Excepciones de dominio con mensajes claros en español ("Stock insuficiente de Leche entera 1 L: se pidieron 5 y hay 2") **(recomendada)**: el mensaje llega al modelo como resultado de error y este puede explicárselo al usuario y proponer alternativas.
  - b) Devolver objetos resultado con un campo `exito`.
  - Comprueba con una prueba real cómo llega el error al cliente en la versión elegida.
- **D3.6 Pruebas automatizadas.**
  - a) Pruebas unitarias de los servicios con JUnit 5 y Mockito para las reglas críticas, más una prueba de integración con Testcontainers (MySQL real) para confirmar un pedido de punta a punta **(recomendada si hay tiempo)**: Docker ya está disponible y se prueba contra el motor real.
  - b) Solo unitarias con Mockito **(recomendada si el tiempo es corto)**.
  - c) `@DataJpaTest` con H2: rápido, pero H2 no se comporta igual que MySQL.

**Reglas de negocio mínimas** (cada una con su prueba)

- Cantidad mayor que cero; producto existente y activo.
- No se modifica un pedido que no esté en `BORRADOR`.
- No se confirma un pedido vacío.
- Al confirmar: se verifica el stock de todas las líneas, se descuenta, se calculan subtotal, envío y total, todo en una transacción; si falla una línea no se descuenta ninguna.
- Al cancelar un pedido `CONFIRMADO` se devuelve el stock; no se cancela uno `EN_CAMINO` ni `ENTREGADO`.
- Teléfono único por cliente; validar formato y campos obligatorios en el servidor (no confiar en el modelo).

**Descripciones de herramientas.** Son lo que el modelo lee para decidir. Cada `@Tool` y cada `@ToolParam` debe decir qué hace, cuándo usarla, qué devuelve y sus restricciones. Ejemplo:

```java
@Tool(description = "Confirma un pedido que está en estado BORRADOR: valida el stock de cada producto, "
        + "lo descuenta del inventario y calcula el total con el costo de envío. "
        + "Úsala solo después de que el usuario haya confirmado explícitamente el resumen del pedido.")
public PedidoDto confirmarPedido(
        @ToolParam(description = "Id del pedido en borrador") Long pedidoId,
        @ToolParam(description = "Método de pago: EFECTIVO, TARJETA o TRANSFERENCIA") String metodoPago) { ... }
```

**Referencia de `application.properties`** (línea Spring AI 1.1.x)

```properties
spring.application.name=supermercado-mcp
server.port=8888
server.address=127.0.0.1

spring.ai.mcp.server.name=supermercado-mcp-server
spring.ai.mcp.server.version=1.0.0
spring.ai.mcp.server.type=SYNC
spring.ai.mcp.server.protocol=STREAMABLE
spring.ai.mcp.server.stdio=false
spring.ai.mcp.server.streamable-http.mcp-endpoint=/mcp

spring.datasource.url=jdbc:mysql://localhost:${DB_PORT:3306}/supermercado_mcp?serverTimezone=America/Bogota
spring.datasource.username=${DB_USER}
spring.datasource.password=${DB_PASSWORD}

spring.jpa.hibernate.ddl-auto=validate
spring.jpa.open-in-view=false

logging.file.name=./logs/supermercado-mcp.log
logging.level.org.springframework.ai=DEBUG

supermercado.costo-envio=5000
```

No actives `spring.jpa.show-sql` de forma permanente ni registres contraseñas. Las variables se cargan en la terminal antes de arrancar:

```bash
set -a && source .env && set +a
cd mcp-server && ./mvnw spring-boot:run
```

**Criterios de aceptación:** `./mvnw clean test` en verde; el servidor arranca sin errores; el log muestra el número esperado de herramientas registradas ("Registered tools: N"). Guardar el fragmento de log en `docs/evidencias/f3-arranque.txt`.

---

### Fase 4. Verificación del protocolo MCP

**Objetivo:** comprobar handshake, descubrimiento y ejecución de herramientas sin ningún LLM de por medio.

**Decisiones a consultar**

- **D4.1 Profundidad de la prueba con curl.**
  - a) Solo `initialize` como en el PDF.
  - b) Secuencia completa `initialize` → `notifications/initialized` → `tools/list` → una `tools/call` de lectura **(recomendada)**: muestra el JSON-RPC crudo de todo el ciclo, que es justo lo que el informe debe explicar.
- **D4.2 Versión del Inspector.**
  - a) La más reciente (`npx @modelcontextprotocol/inspector`) **(recomendada)**.
  - b) La 0.12.0 que fija el PDF, si la reciente da problemas.

**Referencia**

```bash
# 1. initialize: anotar la cabecera Mcp-Session-Id de la respuesta
curl -i -X POST http://localhost:8888/mcp \
  -H "Content-Type: application/json" \
  -H "Accept: application/json, text/event-stream" \
  -d '{"jsonrpc":"2.0","id":1,"method":"initialize","params":{"protocolVersion":"2025-03-26","capabilities":{},"clientInfo":{"name":"curl","version":"1.0"}}}'

# 2. notificación de inicialización completada
curl -i -X POST http://localhost:8888/mcp \
  -H "Content-Type: application/json" -H "Accept: application/json, text/event-stream" \
  -H "Mcp-Session-Id: <ID>" \
  -d '{"jsonrpc":"2.0","method":"notifications/initialized"}'

# 3. listar herramientas
curl -s -X POST http://localhost:8888/mcp \
  -H "Content-Type: application/json" -H "Accept: application/json, text/event-stream" \
  -H "Mcp-Session-Id: <ID>" \
  -d '{"jsonrpc":"2.0","id":2,"method":"tools/list"}'
```

Si el servidor rechaza la versión de protocolo, usa la que devuelva en la negociación. La respuesta puede llegar como `text/event-stream`.

Inspector: `npx @modelcontextprotocol/inspector`, abrir la URL que imprime la consola, elegir transporte **Streamable HTTP**, URL `http://localhost:8888/mcp`, conectar y abrir la pestaña Tools.

**Plan de pruebas en el Inspector** (el estudiante ejecuta y captura; tú verificas en MySQL después de cada escritura)

1. `listarCategorias` y `buscarProductos` con texto "leche".
2. `registrarCliente` con datos ficticios → `SELECT` en `cliente`.
3. `crearPedido` → `agregarProductoAPedido` (dos productos) → `consultarPedido`.
4. `agregarProductoAPedido` con cantidad mayor al stock → debe poder agregarse o fallar según D2.7, y en todo caso `confirmarPedido` debe fallar con mensaje claro.
5. `confirmarPedido` válido → `SELECT` de `pedido`, `detalle_pedido` y stock de `producto`.
6. `cancelarPedido` → verificar devolución de stock.

**Evidencias:** `f4-curl-*.txt`, `f4-inspector-conexion.png`, `f4-inspector-tools.png`, `f4-inspector-confirmar.png`, `f4-select-post-confirmar.txt`.

---

### Fase 5. Chatbot web

**Objetivo:** aplicación en `http://localhost:8080` donde el usuario conversa en lenguaje natural y el modelo usa las herramientas del servidor MCP para armar y confirmar un pedido.

**Decisiones a consultar**

- **D5.1 Interfaz.**
  - a) HTML, CSS y JavaScript sin framework, servidos como recursos estáticos por el propio Spring Boot **(recomendada)**: un solo proceso, sin toolchain de frontend, suficiente para un chat y fácil de explicar.
  - b) Thymeleaf: plantillas en servidor; aporta poco a una interfaz que es sobre todo asíncrona.
  - c) React con Vite: más vistoso, pero añade un tercer proyecto, CORS y build.
- **D5.2 Modelo concreto de Gemini.**
  - a) `gemini-2.5-flash` **(recomendada como punto de partida)**: es el que usa el laboratorio y soporta llamada a herramientas. Antes de fijarlo, comprueba en la documentación de Google que sigue disponible en el nivel gratuito; si no, propone el modelo Flash vigente.
  - b) Un modelo Pro: mejor razonamiento, cuotas gratuitas más bajas.
  - El nombre del modelo va en configuración, no en código.
- **D5.3 Entrega de la respuesta.**
  - a) Respuesta completa por `POST /api/chat` **(recomendada)**: simple y robusta; con herramientas de por medio el streaming complica el cliente.
  - b) Streaming con SSE: mejor sensación de fluidez; dejarlo como mejora si sobra tiempo.
- **D5.4 Memoria de conversación.**
  - a) En memoria, por identificador de conversación generado en el navegador **(recomendada)**: el estado importante (el pedido) ya está en MySQL, así que basta con recordar el hilo mientras la app está encendida.
  - b) Persistente en base de datos: sobrevive a reinicios; innecesario para la práctica.
- **D5.5 Visibilidad de las herramientas invocadas.**
  - a) Mostrar bajo cada respuesta qué herramientas se llamaron y con qué argumentos **(recomendada)**: hace visible el flujo MCP en la demostración y en las capturas del informe. Impleméntalo con un decorador de `ToolCallback` que registre cada llamada, o con el mecanismo de observabilidad que ofrezca la versión elegida.
  - b) Solo en los logs del servidor.
- **D5.6 Identificación del cliente.**
  - a) El asistente pide el teléfono al iniciar un pedido, usa `buscarCliente` y, si no existe, ofrece registrarlo **(recomendada)**: todo ocurre conversando, que es el objetivo del ejercicio.
  - b) Formulario de teléfono en la interfaz antes de chatear.
- **D5.7 Nombre y tono del asistente.** Recomendación: un nombre sencillo (por ejemplo "MercaBot"), trato cordial y respuestas breves en español. Pregunta si el estudiante quiere otro.

**Dependencias:** `spring-boot-starter-web`, `spring-ai-starter-mcp-client`, `spring-ai-starter-model-google-genai`, validación y test; BOM de Spring AI con la misma versión que el servidor.

**Referencia de `application.properties`** (línea 1.1.x; en 2.x la propiedad del modelo es `spring.ai.google.genai.chat.model`)

```properties
spring.application.name=chatbot-client
server.port=8080
server.address=127.0.0.1

spring.ai.google.genai.api-key=${GEMINI_API_KEY}
spring.ai.google.genai.chat.options.model=gemini-2.5-flash
spring.ai.google.genai.chat.options.temperature=0.2

spring.ai.mcp.client.name=supermercado-chatbot
spring.ai.mcp.client.type=SYNC
spring.ai.mcp.client.request-timeout=30s
spring.ai.mcp.client.streamable-http.connections.supermercado.url=http://localhost:8888
spring.ai.mcp.client.streamable-http.connections.supermercado.endpoint=/mcp
```

No definas `project-id` ni `location`: esas propiedades cambian el modo a Vertex AI. Una temperatura baja hace más predecible la elección de herramientas.

**Referencia de configuración del `ChatClient`**

```java
@Bean
ChatClient chatClient(ChatClient.Builder builder, ToolCallbackProvider mcpTools, ChatMemory memoria) {
    return builder
            .defaultSystem(PROMPT_SISTEMA)
            .defaultToolCallbacks(mcpTools)
            .defaultAdvisors(MessageChatMemoryAdvisor.builder(memoria).build())
            .build();
}
```

En el controlador, cada petición pasa su identificador: `.advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversacionId))`.

**Contenido mínimo del prompt de sistema** (redáctalo y muéstralo al estudiante antes de fijarlo)

- Rol: asistente de pedidos a domicilio de un supermercado; responde en español, breve y claro.
- Usa siempre las herramientas para consultar productos, precios, stock y pedidos; nunca inventes productos, precios, identificadores ni disponibilidad.
- Flujo: identificar al cliente por teléfono → crear el pedido → agregar productos → mostrar resumen con total → confirmar.
- Antes de `confirmarPedido` o `cancelarPedido`, muestra el resumen y pide confirmación explícita.
- Si una herramienta devuelve error, explícalo y ofrece alternativas (otro producto, menos cantidad).
- No atiendas temas ajenos al supermercado.

**API y comportamiento**

- `POST /api/chat` con `{ "conversacionId": "...", "mensaje": "..." }` → `{ "respuesta": "...", "herramientas": [ ... ] }`.
- Validar entrada (mensaje no vacío, longitud máxima).
- Errores controlados con mensaje amable: servidor MCP caído, cuota de Gemini agotada (429), tiempo de espera. Nunca devolver trazas ni claves al navegador.
- El servidor MCP debe estar arriba antes de arrancar el chatbot; documenta el orden y qué ocurre si no lo está.
- Interfaz: historial de mensajes, campo de texto, indicador "pensando", botón de nueva conversación; escapar el HTML de las respuestas.

**Criterios de aceptación.** Conversación completa de extremo a extremo, por ejemplo:

1. "Hola, quiero hacer un pedido. Mi teléfono es 3001234567."
2. "¿Qué lácteos tienen?"
3. "Agrega 2 leches enteras y una docena de huevos."
4. "Quita los huevos y muéstrame el total."
5. "Confirma, pago en efectivo."

Tras confirmar, un `SELECT` muestra el pedido `CONFIRMADO`, su detalle y el stock descontado. Probar también un caso de stock insuficiente y un teléfono no registrado.

**Evidencias:** `f5-chat-catalogo.png`, `f5-chat-pedido.png`, `f5-chat-error-stock.png`, `f5-select-pedido.txt`, fragmento del log del servidor con las llamadas.

---

### Fase 6. Claude Desktop como segundo host

**Objetivo:** demostrar que el mismo servidor, sin cambiar una línea, atiende a otro host MCP.

**Decisiones a consultar**

- **D6.1 Forma de conexión.**
  - a) Puente `mcp-remote` en el archivo de configuración, como la Parte D del PDF **(recomendada)**: funciona con un servidor en `localhost`, que los conectores remotos de la aplicación no pueden alcanzar.
  - b) Mecanismo de servidor HTTP nativo, si la versión instalada lo admite para servidores locales.
- **D6.2 Nombre del conector.** Recomendación: `supermercado-mcp`, igual en el archivo y en la documentación.
- **D6.3 Claude Code como tercer host (opcional).**
  - a) Sí **(recomendada)**: un solo comando, `claude mcp add --transport http --scope project supermercado-mcp http://localhost:8888/mcp`, que genera `.mcp.json` en la raíz; es una evidencia adicional casi gratuita y quien clone el repositorio obtiene la conexión.
  - b) No.

**Referencia**

```bash
npx -y mcp-remote --help      # revisar opciones vigentes antes de fijar la configuración
```

```json
{
  "mcpServers": {
    "supermercado-mcp": {
      "command": "npx",
      "args": ["-y", "mcp-remote", "http://localhost:8888/mcp"]
    }
  }
}
```

- Abre el archivo desde el menú de desarrollador de Claude Desktop ("Abrir archivo de configuración"); la ubicación puede variar entre versiones. Es un archivo fuera del proyecto: muestra el cambio y pide confirmación; conserva los servidores que ya existan.
- Si Node se instaló con nvm, es probable que la aplicación no encuentre `npx`: usa la ruta absoluta (`which npx`).
- Si `mcp-remote` rechaza una URL `http://`, revisa en su ayuda la opción para permitir HTTP en local.
- No pongas contraseñas ni la clave de Gemini en este archivo.
- Reinicia Claude Desktop por completo y verifica que el conector aparece activo.

**Pruebas:** "¿Qué categorías de productos hay?", "Busca productos con arroz", "Muéstrame el pedido número N" (uno creado desde el chatbot). Con esto último se evidencia que ambos hosts comparten servidor y datos.

**Evidencias:** `f6-claude-conector.png`, `f6-claude-consulta.png`, y `f6-claude-code-mcp.txt` si aplica D6.3.

---

### Fase 7. Pruebas integrales

**Objetivo:** tabla de pruebas ejecutada y documentada en `docs/pruebas.md`, siguiendo el formato de la sección 12 del PDF.

| Prueba | Acción | Resultado esperado | Evidencia |
|---|---|---|---|
| Conectividad | Iniciar `mcp-server` | Activo en 8888 sin errores | Log de arranque |
| Negociación | `initialize` con curl | Respuesta MCP válida con capacidades | Salida de curl |
| Descubrimiento | Listar herramientas en Inspector | N herramientas visibles | Captura |
| Lectura | `buscarProductos` | Productos reales de MySQL | JSON |
| Creación | `crearPedido` + `agregarProductoAPedido` | Filas nuevas en `pedido` y `detalle_pedido` | `SELECT` |
| Regla de negocio | Confirmar con stock insuficiente | Error claro, stock intacto | JSON + `SELECT` |
| Confirmación | `confirmarPedido` | Estado `CONFIRMADO`, stock descontado, total correcto | `SELECT` |
| Cancelación | `cancelarPedido` | Estado `CANCELADO`, stock devuelto | `SELECT` |
| Chatbot | Pedido completo conversando | Pedido confirmado en la base | Capturas |
| Claude Desktop | Consultar catálogo y un pedido | Respuesta con datos reales | Captura |
| Automatizadas | `./mvnw test` | Todas en verde | Salida de Maven |

**Decisión a consultar**

- **D7.1 Pruebas del chatbot.**
  - a) Manuales con guion y capturas **(recomendada)**: las respuestas de un LLM no son deterministas y automatizarlas bien excede el alcance.
  - b) Añadir una prueba automatizada del controlador con el `ChatClient` simulado.

---

### Fase 8. Documentación del repositorio

**Objetivo:** que alguien pueda clonar, ejecutar y entender el proyecto solo con el repositorio.

**Decisiones a consultar**

- **D8.1 Diagramas.**
  - a) Mermaid dentro del Markdown **(recomendada)**: GitHub los renderiza, quedan versionados y se pueden exportar a imagen para el entregable.
  - b) Imágenes hechas en una herramienta externa.
- **D8.2 Publicación.**
  - a) Tag `v1.0.0` y release en GitHub al cerrar **(recomendada)**: fija la versión exacta que se entrega.
  - b) Sin tag.
- **D8.3 Capturas en el repositorio.**
  - a) Incluir las de `docs/evidencias/` y enlazarlas desde el README **(recomendada)**, revisando antes que ninguna muestre claves ni datos personales reales.
  - b) Solo en el entregable.

**Contenido del `README.md`**

1. Qué es el proyecto y enunciado que resuelve.
2. Arquitectura (diagrama) y roles MCP.
3. Requisitos (JDK 21, Docker, Node, API key de Gemini).
4. Puesta en marcha paso a paso: `.env`, `docker compose up -d`, servidor, chatbot, URL.
5. Tabla de herramientas MCP con parámetros y descripción.
6. Modelo de datos (diagrama entidad-relación).
7. Cómo probar: curl, Inspector, chatbot, Claude Desktop.
8. Pruebas automatizadas.
9. Seguridad y limitaciones.
10. Autores, curso y licencia.

**`docs/arquitectura.md`:** diagrama de componentes, diagrama de secuencia de un pedido (usuario → chatbot → Gemini → cliente MCP → servidor → MySQL y vuelta), diagrama entidad-relación y la explicación del flujo en nueve pasos de la sección 11 del PDF aplicada a este caso.

**Sección de seguridad** (el PDF la trata en la sección 14 y en la actividad 16): el endpoint MCP no tiene autenticación, por lo que cualquier proceso que lo alcance puede invocar herramientas que modifican datos. Mitigaciones aplicadas: escucha solo en `127.0.0.1`, MySQL publicado solo en local, usuario de base de datos con permisos mínimos, validación en servidor, confirmación explícita antes de operaciones con efecto, datos sintéticos, secretos fuera del repositorio. Mitigaciones para un despliegue real: TLS, autenticación y autorización (OAuth 2.1 según la especificación MCP o API key en un proxy inverso), límites de tasa y auditoría.

**Comparación de hosts:** tabla chatbot propio vs Claude Desktop vs Inspector (quién aporta el modelo, transporte, configuración, qué código se reutiliza). La conclusión a evidenciar: el servidor y sus herramientas se reutilizan íntegros; solo cambia el host.

**Criterio de aceptación:** clonar el repositorio en una carpeta nueva y seguir el README de principio a fin funciona sin pasos ocultos.

---

### Fase 9. Entregable final (bloqueada)

**No inicies esta fase hasta que el estudiante entregue la guía del documento.** Cuando la entregue:

1. Léela completa y resume sus requisitos: estructura, extensión, formato, normas de citación, criterios de evaluación, fecha y medio de entrega.
2. Cruza esos requisitos con lo que ya existe (`BITACORA.md`, `DECISIONES.md`, `arquitectura.md`, `pruebas.md`, evidencias) y lista lo que falta.
3. Consulta las decisiones del entregable con recomendación y explicación. Como mínimo: formato de salida (Word, PDF, Markdown), herramienta de generación, estructura de secciones si la guía deja libertad, nivel de detalle del código incluido, qué capturas van y en qué orden, y si se incluye el enlace al tag de GitHub.
4. Propón el índice y espera aprobación antes de redactar.
5. Redacta explicando **cada paso** (exigencia del enunciado) a partir de la bitácora; no inventes resultados: toda afirmación debe corresponder a una evidencia guardada.
6. Revisa ortografía, numeración de figuras, que no aparezcan secretos y que los enlaces funcionen.

---

## 7. Referencias

Verifica siempre la página de la versión que se fijó en D1.4.

- Spring AI, MCP (visión general): https://docs.spring.io/spring-ai/reference/api/mcp/mcp-overview.html
- Spring AI, servidor MCP Streamable HTTP: https://docs.spring.io/spring-ai/reference/1.1/api/mcp/mcp-streamable-http-server-boot-starter-docs.html
- Spring AI, cliente MCP: https://docs.spring.io/spring-ai/reference/1.1/api/mcp/mcp-client-boot-starter-docs.html
- Spring AI, Google GenAI Chat: https://docs.spring.io/spring-ai/reference/api/chat/google-genai-chat.html
- Model Context Protocol: https://modelcontextprotocol.io/
- mcp-remote: https://www.npmjs.com/package/mcp-remote
- Claude Code, MCP: https://code.claude.com/docs/en/mcp-quickstart
- Claude Desktop en Linux: https://code.claude.com/docs/en/desktop-linux
- Repositorios del profesor: https://github.com/diegobotia/labmcp y https://github.com/diegobotia/agentemcp

---

## Anexo A. Prompt inicial para Claude Code

```
Lee completo el archivo GUIA_DESARROLLO.md de esta carpeta y el PDF que está en docs/referencia/.
Vamos a desarrollar el proyecto que describe, fase por fase.

Reglas:
- Sigue al pie de la letra la sección 3 (protocolo de consulta por fase, confirmaciones, secretos, evidencias).
- Las decisiones de la sección 2 ya están tomadas; no las preguntes de nuevo.
- Antes de cada fase preséntame todas sus decisiones abiertas con opciones, tu recomendación y la explicación, y espera mi respuesta antes de escribir código.
- No hagas push, no instales nada en el sistema ni toques archivos fuera del proyecto sin mi confirmación.
- La Fase 9 queda bloqueada hasta que te entregue la guía del entregable.

Empieza confirmándome en pocas líneas qué entendiste del proyecto y luego ejecuta la Fase 0.
```

## Anexo B. Formato de `docs/DECISIONES.md`

```markdown
### D2.4 Dónde vive el carrito
- **Fecha:** 2026-11-15
- **Fase:** 2
- **Opciones consideradas:** a) pedido en BORRADOR; b) crearPedido con lista completa
- **Recomendación de Claude Code:** a
- **Decisión del estudiante:** a
- **Justificación:** el estado queda en la base de datos y no depende de la memoria del modelo...
- **Consecuencias:** tres herramientas más; se necesita el estado BORRADOR
```

## Anexo C. Formato de entrada en `docs/BITACORA.md`

```markdown
## Fase 3. Servidor MCP (2026-11-15)
**Qué se hizo:** ...
**Por qué:** ...
**Comandos:** ...
**Resultado:** ...
**Problemas y solución:** ...
**Evidencias:** docs/evidencias/f3-arranque.txt
**Commit:** <hash corto>
```

## Anexo D. Problemas frecuentes en Ubuntu

| Síntoma | Causa probable | Solución |
|---|---|---|
| `permission denied` al usar Docker | El usuario no está en el grupo `docker` | `sudo usermod -aG docker $USER` y volver a iniciar sesión |
| Puerto 3306 ocupado | MySQL local activo | Usar 3307 (D2.1) |
| `Access denied` desde Spring | Variables de `.env` no cargadas en esa terminal | `set -a && source .env && set +a` |
| Los scripts de `db/init` no se aplican | El volumen ya existía | `docker compose down -v` (destructivo, confirmar) |
| 404 en `/mcp` | Protocolo o endpoint mal configurados | Revisar `protocol=STREAMABLE` y `mcp-endpoint=/mcp` |
| "Registered tools: 0" | Falta el bean `ToolCallbackProvider` o los `@Tool` | Revisar el registro (D3.4) |
| Error al serializar la respuesta de una herramienta | Se devuelven entidades con relaciones perezosas | Devolver DTO (D3.3) |
| El chatbot no arranca | El servidor MCP no está arriba | Arrancar primero `mcp-server` |
| 401 o 403 de Gemini | Clave inválida o variable no cargada | Revisar `GEMINI_API_KEY` |
| 429 de Gemini | Cuota gratuita agotada | Esperar, o cambiar de modelo en configuración |
| El modelo no llama herramientas | Descripciones pobres o herramientas no registradas en el `ChatClient` | Mejorar descripciones; revisar `defaultToolCallbacks` |
| Claude Desktop no inicia el conector | No encuentra `npx` (nvm) o JSON inválido | Ruta absoluta a `npx`; validar el JSON; reiniciar la aplicación |
| El Inspector no conecta | Transporte SSE seleccionado | Elegir Streamable HTTP |
