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
