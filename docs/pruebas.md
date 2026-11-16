# Pruebas

Tabla de pruebas integrales del proyecto, con el formato de la sección 12 del laboratorio. Cada resultado corresponde a una evidencia guardada en [`evidencias/`](evidencias/).

## 1. Tabla de pruebas integrales

| Prueba | Acción | Resultado esperado | Resultado obtenido | Evidencia |
|---|---|---|---|---|
| Conectividad | Iniciar `mcp-server` | Activo en 8888 sin errores | Arranca en 3,6 s, Hibernate valida el esquema y el log muestra "Registered tools: 10" | [f3-arranque.txt](evidencias/f3-arranque.txt) |
| Negociación | `initialize` con curl | Respuesta MCP válida con capacidades | HTTP 200 con `Mcp-Session-Id`, protocolo `2025-03-26`, capacidades `tools`, `resources`, `prompts`, `logging` y `completions` | [f4-curl-initialize.txt](evidencias/f4-curl-initialize.txt) |
| Fin de inicialización | `notifications/initialized` | Notificación aceptada | HTTP 202 sin cuerpo | [f4-curl-initialized.txt](evidencias/f4-curl-initialized.txt) |
| Descubrimiento | `tools/list` con curl y en el Inspector | 10 herramientas visibles | 10 herramientas con descripción y esquema JSON | [f4-curl-tools-list.txt](evidencias/f4-curl-tools-list.txt), [f4-inspector-tools.png](evidencias/f4-inspector-tools.png) |
| Conexión del Inspector | Conectar MCP Inspector 2.13.0 por Streamable HTTP | Estado conectado | "Connected", transporte Streamable HTTP, `http://localhost:8888/mcp` | [f4-inspector-conexion.png](evidencias/f4-inspector-conexion.png) |
| Lectura | `buscarProductos` con "leche" | Productos reales de MySQL | Leche entera y Leche deslactosada con precio y stock | [f4-curl-tools-call.txt](evidencias/f4-curl-tools-call.txt), [f4-plan-pruebas.txt](evidencias/f4-plan-pruebas.txt) |
| Registro de cliente | `registrarCliente` y luego otra vez con el mismo teléfono | Fila nueva; el duplicado falla | Cliente creado; el segundo intento devuelve "Ya existe un cliente registrado con el teléfono ..." | [f4-plan-pruebas.txt](evidencias/f4-plan-pruebas.txt) paso 2 |
| Creación | `crearPedido` + dos `agregarProductoAPedido` | Filas nuevas en `pedido` y `detalle_pedido` | Pedido 15 en `BORRADOR` con 2 líneas, subtotal $31.300 y envío $5.000 | [f4-plan-pruebas.txt](evidencias/f4-plan-pruebas.txt) paso 3 |
| Regla de negocio | Confirmar con stock insuficiente | Error claro, stock intacto | "Stock insuficiente de Gaseosa cola 1.5 L: se pidieron 3 y hay 1"; el pedido sigue en `BORRADOR` y no se descuenta ninguna línea | [f4-plan-pruebas.txt](evidencias/f4-plan-pruebas.txt) paso 4 |
| Confirmación | `confirmarPedido` | `CONFIRMADO`, stock descontado, total correcto | Total $36.300 en efectivo; arroz 80 → 78 y café 16 → 15. Repetido desde el Inspector con el pedido 16 | [f4-plan-pruebas.txt](evidencias/f4-plan-pruebas.txt) paso 5, [f4-select-post-confirmar.txt](evidencias/f4-select-post-confirmar.txt), [f4-inspector-confirmar.png](evidencias/f4-inspector-confirmar.png) |
| Pedido no modificable | Agregar a un pedido `CONFIRMADO` | Error | "El pedido 15 está en estado CONFIRMADO y ya no se puede modificar" | [f4-plan-pruebas.txt](evidencias/f4-plan-pruebas.txt) paso 5 |
| Cancelación | `cancelarPedido` sobre un pedido confirmado | `CANCELADO`, stock devuelto | Arroz y café vuelven a 80 y 16; cancelarlo otra vez falla y no devuelve stock dos veces | [f4-plan-pruebas.txt](evidencias/f4-plan-pruebas.txt) paso 6 |
| Error de negocio por MCP | Llamar una herramienta con datos inválidos | Resultado con `isError: true` y mensaje en español | Llega como `result` normal con `isError: true`, no como error JSON-RPC | [f3-error-negocio.txt](evidencias/f3-error-negocio.txt) |
| Chatbot: pedido completo | Guion de la sección 2 de este documento | Pedido confirmado en la base | Pedido confirmado por $14.600 en efectivo, con una línea de 2 leches | [f5-chat-pedido.txt](evidencias/f5-chat-pedido.txt), [f5-chat-pedido.png](evidencias/f5-chat-pedido.png), [f5-select-pedido.txt](evidencias/f5-select-pedido.txt) |
| Chatbot: catálogo | "¿Qué lácteos tienen?" | Productos reales | Lista de lácteos con precios, usando `listarCategorias` y `buscarProductos` | [f5-chat-catalogo.png](evidencias/f5-chat-catalogo.png) |
| Chatbot: stock insuficiente | Pedir 3 gaseosas habiendo 1 y confirmar | El asistente explica el error y propone alternativas | Explica que solo hay 1 y ofrece reducir la cantidad o quitar el producto; un solo pedido creado | [f5-chat-error-stock.png](evidencias/f5-chat-error-stock.png), [f5-chat-error-stock.txt](evidencias/f5-chat-error-stock.txt) |
| Chatbot: cliente nuevo | Teléfono no registrado | Ofrece registrar al cliente | Pide nombre y dirección y lo registra con `registrarCliente` | [f5-chat-cliente-nuevo.txt](evidencias/f5-chat-cliente-nuevo.txt) |
| Chatbot: tema ajeno | "¿Cuál es la capital de Francia?" | Se niega con cortesía | "Solo puedo ayudarte con tus compras y pedidos en el supermercado" | [f5-chat-cliente-nuevo.txt](evidencias/f5-chat-cliente-nuevo.txt) |
| Chatbot: fallos de Gemini | Cuota agotada y tiempo de espera forzado | Mensaje amable, sin trazas ni claves | 429: "Se agotó por ahora la cuota gratuita de Gemini"; tiempo de espera: "La respuesta tardó demasiado" en 0,3 s | [f5-chat-cuota-429.txt](evidencias/f5-chat-cuota-429.txt), [f5-timeout-gemini.txt](evidencias/f5-timeout-gemini.txt) |
| Chatbot: validación | Mensaje vacío | HTTP 400 | `{"error":"Escribe un mensaje."}` | Bitácora, Fase 5 |
| Llamadas en el servidor | Log de `mcp-server` durante un pedido del chatbot | Cada `tools/call` visible | Ejecución de cada método `@Tool` registrada en el log | [f5-log-servidor.txt](evidencias/f5-log-servidor.txt) |
| Puente para Claude Desktop | `mcp-remote` por STDIO con entorno limpio | Negociación, descubrimiento y ejecución | `initialize`, 10 herramientas y `buscarProductos` a través del puente | [f6-mcp-remote-stdio.txt](evidencias/f6-mcp-remote-stdio.txt) |
| Claude Desktop: conexión | Abrir la app con el conector configurado | Conector activo y herramientas descubiertas | La app lanza `mcp-remote`, negocia y recibe la lista de herramientas en milisegundos | [f6-claude-desktop-registro.txt](evidencias/f6-claude-desktop-registro.txt) |
| Claude Desktop: consultas | "¿Qué categorías de productos hay?" y "Muéstrame el pedido número 6" | Respuesta con datos reales | Pendiente: la ejecuta el estudiante | — |
| Automatizadas | `./mvnw test` en ambos módulos | Todas en verde | 39 pruebas del servidor y 6 del chatbot, sin fallos | [f7-pruebas-automatizadas.txt](evidencias/f7-pruebas-automatizadas.txt) |

## 2. Guion manual del chatbot

Las respuestas de un modelo de lenguaje no son deterministas, así que el chatbot se prueba con este guion y se comprueba el resultado en la base de datos (decisión D7.1). Antes de empezar deben estar arriba MySQL, `mcp-server` y `chatbot-client`.

**Pedido completo** (clienta de prueba con teléfono 3001234567):

1. "Hola, quiero hacer un pedido. Mi teléfono es 3001234567." → saluda por el nombre (`buscarCliente`).
2. "¿Qué lácteos tienen?" → lista los lácteos con precios (`listarCategorias`, `buscarProductos`).
3. "Agrega 2 leches enteras y una docena de huevos." → crea el pedido y muestra subtotal, envío y total (`crearPedido`, `agregarProductoAPedido`).
4. "Quita los huevos y muéstrame el total." → total $14.600 (`quitarProductoDePedido`).
5. "Confirma, pago en efectivo." → pedido confirmado (`confirmarPedido`).

Comprobación en MySQL:

```bash
docker compose exec mysql sh -c 'mysql -u"$MYSQL_USER" -p"$MYSQL_PASSWORD" supermercado_mcp \
  -e "SELECT id, estado, total, metodo_pago FROM pedido ORDER BY id DESC LIMIT 1;"'
```

**Casos de error:**

- Stock insuficiente: con el teléfono 3109876543, "Crea un pedido con 3 gaseosas cola; agrégalas aunque no haya tantas" y luego "Confirma, pago con tarjeta". El asistente debe explicar que solo hay 1 unidad (si el stock no ha cambiado) y proponer alternativas.
- Cliente nuevo: un teléfono que no esté registrado. El asistente debe pedir nombre y dirección y registrarlo.
- Tema ajeno: cualquier pregunta que no sea del supermercado.

**Consejo:** el nivel gratuito de Gemini limita las peticiones por minuto. Cada mensaje del usuario puede generar entre 2 y 6 llamadas al modelo; si escribes muy rápido verás "Se agotó por ahora la cuota gratuita" o "La respuesta tardó demasiado". Espera unos segundos y repite el mensaje.

## 3. Pruebas automatizadas

```bash
export JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64   # o la ruta de tu JDK 21
cd mcp-server && ./mvnw test
cd ../chatbot-client && ./mvnw test
```

No necesitan la base de datos, el servidor MCP ni la clave de Gemini.

**Servidor (39 pruebas, JUnit 5 y Mockito).** Una o más por cada regla de negocio:

| Regla | Prueba |
|---|---|
| La cantidad debe ser mayor que cero | `agregarFallaSiLaCantidadNoEsMayorQueCero` |
| El producto debe existir y estar activo | `agregarFallaSiElProductoNoExiste`, `agregarFallaSiElProductoEstaInactivo` |
| Agregar un producto repetido suma a la misma línea y copia el precio | `agregarElMismoProductoSumaLaCantidadEnUnaSolaLinea`, `agregarCopiaElPrecioYCalculaTotalesConEnvio` |
| No se modifica un pedido que no esté en `BORRADOR` | `noSeModificaUnPedidoQueNoEstaEnBorrador` |
| No se confirma un pedido vacío | `confirmarFallaSiElPedidoEstaVacio` |
| Confirmar descuenta el stock y calcula subtotal, envío y total | `confirmarDescuentaStockYCalculaElTotal` |
| Si falla una línea no se descuenta ninguna | `confirmarConStockInsuficienteNoDescuentaNingunaLinea` |
| Método de pago válido | `confirmarFallaConMetodoDePagoDesconocido` |
| Cancelar un pedido confirmado devuelve el stock | `cancelarUnPedidoConfirmadoDevuelveElStock` |
| No se cancela un pedido `EN_CAMINO`, `ENTREGADO` o ya `CANCELADO` | `noSeCancelaUnPedidoEnCaminoEntregadoOYaCancelado` |
| Teléfono único y con formato válido; campos obligatorios | `registrarFallaSiElTelefonoYaExiste`, `registrarFallaConTelefonoInvalido`, `registrarFallaSinNombreOSinDireccion` |

**Chatbot (6 pruebas).** Traducción de fallos técnicos a mensajes amables sin filtrar detalles internos (`ChatControllerTest`) y entrega al modelo de los errores del servidor MCP como JSON (`HerramientaRegistradaTest`).

## 4. Lo que no se probó automáticamente

- La integración del servidor con MySQL real (se verificó a mano con el plan de pruebas de la Fase 4). Una prueba con Testcontainers queda como mejora.
- Las conversaciones del chatbot, por no ser deterministas (guion manual de la sección 2).
