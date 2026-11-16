// Genera el informe del laboratorio en Word, con el formato del informe modelo del Laboratorio 2.
// Uso: node gen.js <carpeta-del-proyecto> <carpeta-de-figuras-nuevas> <salida.docx>
const fs = require('fs');
const path = require('path');
const {
  Document, Packer, Paragraph, TextRun, ImageRun, Table, TableRow, TableCell, WidthType, AlignmentType,
  BorderStyle, ShadingType, LevelFormat, SectionType, ExternalHyperlink, Footer, PageNumber, Tab,
  TabStopType, VerticalAlign,
} = require('docx');

const [, , PROYECTO, FIGS, SALIDA] = process.argv;
const EVID = path.join(PROYECTO, 'docs', 'evidencias');

// ---------- medidas (carta, márgenes de 0,75") ----------
const ANCHO_TEXTO = 10080;            // DXA (7")
const ESPACIO_COL = 360;
const ANCHO_COL = (ANCHO_TEXTO - ESPACIO_COL) / 2; // 4860 DXA
const PX_COL = 318;                   // px a 96 ppp
const PX_ANCHO = 660;
const FUENTE = 'Times New Roman';
const MONO = 'Courier New';

// ---------- texto con marcas: **negrita**, `código`, ^^cursiva^^ ----------
function runs(texto, base = {}) {
  const partes = texto.split(/(\*\*[^*]+\*\*|`[^`]+`|\^\^[^^]+\^\^)/).filter((p) => p !== '');
  return partes.map((p) => {
    if (p.startsWith('**')) return new TextRun({ ...base, text: p.slice(2, -2), bold: true });
    if (p.startsWith('`')) return new TextRun({ ...base, text: p.slice(1, -1), font: MONO, size: (base.size || 20) - 2 });
    if (p.startsWith('^^')) return new TextRun({ ...base, text: p.slice(2, -2), italics: true });
    return new TextRun({ ...base, text: p });
  });
}

const bloques = []; // { capa: 1|2, elementos: [...] }
function agregar(capa, ...elementos) {
  const ult = bloques[bloques.length - 1];
  if (ult && ult.capa === capa) ult.elementos.push(...elementos);
  else bloques.push({ capa, elementos: [...elementos] });
}

let nFig = 0, nTab = 0, nLst = 0;
const P = (t, op = {}) => agregar(op.capa || 2, new Paragraph({
  children: runs(t), alignment: op.alinear || AlignmentType.JUSTIFIED, spacing: { after: 100, line: 252 },
}));
const H1 = (t) => agregar(2, new Paragraph({ children: [new TextRun({ text: t, bold: true, size: 22 })], spacing: { before: 240, after: 120 }, keepNext: true }));
const H2 = (t) => agregar(2, new Paragraph({ children: [new TextRun({ text: t, bold: true, italics: true, size: 20 })], spacing: { before: 180, after: 100 }, keepNext: true }));
const N = (num, t) => agregar(2, new Paragraph({
  children: [new TextRun({ text: num, bold: true }), new TextRun({ children: [new Tab()] }), ...runs(t)],
  alignment: AlignmentType.JUSTIFIED, tabStops: [{ type: TabStopType.LEFT, position: 680 }],
  indent: { left: 680, hanging: 680 }, spacing: { after: 100, line: 252 },
}));
const B = (items, capa = 2) => items.forEach((t) => agregar(capa, new Paragraph({
  children: runs(t), numbering: { reference: 'vinetas', level: 0 }, alignment: AlignmentType.JUSTIFIED, spacing: { after: 60, line: 252 },
})));
const pie = (tipo, n, t) => new Paragraph({
  children: [new TextRun({ text: `${tipo} ${n}: `, bold: true, italics: true, size: 18 }), ...runs(t, { italics: true, size: 18 })],
  alignment: AlignmentType.CENTER, spacing: { before: 60, after: 200 },
});

function tamPng(f) { const b = fs.readFileSync(f); return { w: b.readUInt32BE(16), h: b.readUInt32BE(20), data: b }; }
function FIG(archivo, t, op = {}) {
  const capa = op.ancho ? 1 : 2;
  const { w, h, data } = tamPng(archivo);
  const px = op.px || (capa === 1 ? PX_ANCHO : PX_COL);
  nFig += 1;
  agregar(capa,
    new Paragraph({ children: [new ImageRun({ type: 'png', data, transformation: { width: px, height: Math.round(px * h / w) } })], alignment: AlignmentType.CENTER, spacing: { before: 120 }, keepNext: true }),
    pie('Figura', nFig, t));
}
const borde = { style: BorderStyle.SINGLE, size: 4, color: '808080' };
const bordes = { top: borde, bottom: borde, left: borde, right: borde };
function TAB(encabezados, filas, proporciones, t, op = {}) {
  const capa = op.ancho ? 1 : 2;
  const total = capa === 1 ? ANCHO_TEXTO : ANCHO_COL;
  const suma = proporciones.reduce((a, b) => a + b, 0);
  const anchos = proporciones.map((p) => Math.floor(total * p / suma));
  anchos[anchos.length - 1] = total - anchos.slice(0, -1).reduce((a, b) => a + b, 0);
  const celda = (texto, i, cab) => new TableCell({
    width: { size: anchos[i], type: WidthType.DXA }, borders: bordes, verticalAlign: VerticalAlign.CENTER,
    shading: cab ? { fill: 'E7E6E6', type: ShadingType.CLEAR, color: 'auto' } : undefined,
    margins: { top: 40, bottom: 40, left: 80, right: 80 },
    children: [new Paragraph({ children: runs(texto, { size: 17, bold: cab }) })],
  });
  nTab += 1;
  agregar(capa,
    new Table({
      width: { size: total, type: WidthType.DXA }, columnWidths: anchos,
      rows: [new TableRow({ tableHeader: true, children: encabezados.map((e, i) => celda(e, i, true)) }),
        ...filas.map((f) => new TableRow({ cantSplit: true, children: f.map((c, i) => celda(c, i, false)) }))],
    }),
    pie('Tabla', nTab, t));
}
function LST(lineas, t, op = {}) {
  const capa = op.ancho ? 1 : 2;
  nLst += 1;
  const sombra = { fill: 'F2F2F2', type: ShadingType.CLEAR, color: 'auto' };
  agregar(capa, ...lineas.map((l, i) => new Paragraph({
    children: [new TextRun({ text: l === '' ? ' ' : l, font: MONO, size: op.tam || 14 })], shading: sombra,
    spacing: { after: 0, line: 220, before: i === 0 ? 100 : 0 }, keepNext: i < lineas.length - 1, keepLines: true,
  })), pie('Listado', nLst, t));
}
const link = (texto, url) => new ExternalHyperlink({ link: url, children: [new TextRun({ text: texto, style: 'Hyperlink', italics: true })] });
const ev = (f) => path.join(EVID, f);
const fg = (f) => path.join(FIGS, f);

// =====================================================================
// PORTADA (una columna)
// =====================================================================
const centro = (hijos, extra = {}) => agregar(1, new Paragraph({ children: hijos, alignment: AlignmentType.CENTER, ...extra }));
centro([new TextRun({ text: 'Informe de Laboratorio – Chatbot de Pedidos de Supermercado a Domicilio sobre un Servidor MCP con Spring Boot, Spring AI, MySQL y Gemini', bold: true, size: 26 })], { spacing: { after: 40 } });
centro([new TextRun({ text: 'Lab Report – Home-Delivery Grocery Ordering Chatbot over an MCP Server with Spring Boot, Spring AI, MySQL and Gemini', bold: true, size: 26 })], { spacing: { after: 360 } });
centro([new TextRun({ text: 'Julián Vanegas López', size: 22 })]);
centro([new TextRun({ text: 'Diego Alejandro Vergara Ruiz', size: 22 })], { spacing: { after: 300 } });
centro([new TextRun({ text: 'Repositorio: ', bold: true, italics: true }), link('https://github.com/Dalev10/supermercado-mcp', 'https://github.com/Dalev10/supermercado-mcp')]);
centro([new TextRun({ text: 'Versión entregada: ', bold: true, italics: true }), link('https://github.com/Dalev10/supermercado-mcp/releases/tag/v1.0.0', 'https://github.com/Dalev10/supermercado-mcp/releases/tag/v1.0.0')], { spacing: { after: 300 } });
centro([new TextRun({ text: 'Facultad de Ingeniería, Universidad de Antioquia', italics: true, size: 22 })], { spacing: { after: 360 } });
centro([new TextRun({ text: 'Resumen', bold: true, size: 22 })], { spacing: { after: 120 } });
agregar(1, new Paragraph({
  indent: { left: 720, right: 720 }, alignment: AlignmentType.JUSTIFIED, spacing: { after: 200, line: 252 },
  children: runs('El presente informe documenta la solución al ejercicio propuesto del laboratorio «Construcción e integración de un MCP Server» de la asignatura Arquitectura de Software: un cliente de tipo chatbot integrado con un servidor MCP (Model Context Protocol) conectado a una base de datos, para realizar pedidos de productos de un supermercado a domicilio. La solución es un monorepo con tres piezas: un servidor MCP en Spring Boot 3.5.16 y Spring AI 1.1.8 que publica diez herramientas de catálogo, clientes y pedidos sobre el transporte Streamable HTTP; una base de datos MySQL 8.4 en Docker Compose con el esquema y los datos de prueba versionados; y un chatbot web, también en Spring AI, en el que el modelo Gemini decide qué herramientas invocar mientras el usuario conversa. Las reglas de negocio viven en el servidor: el pedido nace en estado borrador, el stock se valida y descuenta al confirmar dentro de una transacción con bloqueo optimista, y cada error llega al modelo como un mensaje claro en español. El servidor cuenta con 39 pruebas unitarias y el chatbot con 6. El protocolo se verificó sin ningún modelo, con curl y MCP Inspector, y el mismo servidor, sin cambiar una línea, atendió además a Claude Code y a Claude Desktop: un pedido creado conversando con el chatbot se consultó después desde Claude Code. El trabajo se integró mediante siete Pull Requests, uno por fase, y se publicó como la versión v1.0.0.', { italics: true }),
}));
agregar(1, new Paragraph({
  alignment: AlignmentType.JUSTIFIED, spacing: { after: 240 },
  children: [new TextRun({ text: 'Palabras clave: ', bold: true }), new TextRun('Model Context Protocol (MCP), servidor MCP, Spring AI, Spring Boot, chatbot, Gemini, MySQL, Docker, JSON-RPC, Streamable HTTP, MCP Inspector, Claude Desktop, Claude Code.')],
}));

// =====================================================================
// 1-4
// =====================================================================
H1('1. Introducción');
P('Un modelo de lenguaje, por sí solo, no conoce el catálogo de un supermercado ni puede registrar un pedido: necesita acceder a los datos y a las operaciones del negocio. Hasta hace poco, cada aplicación resolvía esa integración de forma particular para cada modelo. El Model Context Protocol (MCP), publicado por Anthropic en 2024, estandariza esa conexión: un servidor anuncia sus capacidades, en particular herramientas con nombre, descripción y esquema de parámetros, y cualquier aplicación compatible las descubre e invoca mediante mensajes JSON-RPC 2.0. El modelo decide cuándo usar una herramienta y con qué argumentos; el servidor la ejecuta y devuelve el resultado.');
P('El laboratorio base construye `tarea-mcp`, un servidor MCP con Spring Boot y Spring AI que publica siete herramientas de gestión de tareas sobre MySQL, y lo consume desde MCP Inspector, Claude Desktop y un agente de Google ADK. Al final plantea el ejercicio que resuelve este trabajo: «Realice un cliente de tipo ChatBot e intégrelo con un servidor MCP que conecte a una BD con MySQL, PostgreSQL u otro motor de su elección, para realizar pedidos de productos de un supermercado a domicilio. Explique cada paso que emplee para la solución».');
P('Para cumplir la exigencia de explicar cada paso, el desarrollo se organizó en fases con un registro continuo: una bitácora con lo que se hizo, por qué, los comandos usados, el resultado y los problemas encontrados; un registro de decisiones con las opciones consideradas y su justificación; y una carpeta de evidencias con salidas de terminal y capturas. Este informe se apoya en ese registro; las evidencias están disponibles en el repositorio. Además, se corrigieron algunas inconsistencias del material base que no convenía heredar: el puerto del servidor aparece como 8888 y como 8088, el Inspector se lanza con transporte SSE contra un servidor Streamable HTTP, la aplicación se conecta a MySQL como `root`, las fechas se guardan como texto y las herramientas devuelven entidades JPA.');
H1('2. Objetivo General');
P('Construir un chatbot de pedidos de supermercado a domicilio integrado con un servidor MCP que conecta a una base de datos MySQL, y demostrar que el mismo servidor, sin modificaciones, puede ser utilizado por otras aplicaciones compatibles con MCP.');
H1('3. Objetivos Específicos');
B([
  'Diseñar un modelo de datos de catálogo, clientes y pedidos con estados, y desplegarlo en MySQL mediante Docker Compose con scripts versionados.',
  'Implementar un servidor MCP con Spring Boot y Spring AI que publique solo las operaciones necesarias como herramientas, con las reglas de negocio validadas en el servidor.',
  'Construir un chatbot web con Spring AI y Gemini que descubra las herramientas por el protocolo y las use para armar y confirmar pedidos conversando.',
  'Verificar el protocolo sin ningún modelo de lenguaje, con curl y MCP Inspector, comprobando cada escritura en la base de datos.',
  'Conectar el mismo servidor a Claude Code y Claude Desktop para demostrar la interoperabilidad que propone MCP.',
  'Respaldar el trabajo con pruebas automatizadas, un flujo de ramas con Pull Requests y la documentación de cada paso.',
]);
H1('4. Herramientas de Software Empleadas');
TAB(['Herramienta', 'Uso en el laboratorio'], [
  ['Ubuntu 22.04 LTS, VS Code y Claude Code', 'Entorno de desarrollo; Claude Code como asistente en VS Code y, en terminal, como host MCP adicional.'],
  ['Git y GitHub', 'Monorepo, una rama y un Pull Request por fase, tag v1.0.0.'],
  ['OpenJDK 21 y Maven Wrapper', 'Compilación y ejecución de ambos módulos (`./mvnw`).'],
  ['Spring Boot 3.5.16', 'Base del servidor y del chatbot: Spring Web, Spring Data JPA y validación.'],
  ['Spring AI 1.1.8', 'Starters de servidor MCP (WebMVC), cliente MCP y modelo Google GenAI; anotaciones `@Tool`.'],
  ['SDK de MCP para Java 0.18.3', 'Implementación del protocolo, incluida por Spring AI.'],
  ['MySQL 8.4 y Docker Compose', 'Base de datos `supermercado_mcp` en un contenedor reproducible.'],
  ['API de Gemini', 'Modelo `gemini-3.5-flash-lite` del chatbot, con clave de Google AI Studio.'],
  ['HTML, CSS y JavaScript', 'Página de chat servida por el propio chatbot.'],
  ['JUnit 5, Mockito y AssertJ', 'Pruebas unitarias de las reglas de negocio y del chatbot.'],
  ['curl', 'Intercambio JSON-RPC crudo con el servidor.'],
  ['MCP Inspector 2.13.0', 'Cliente MCP de prueba, sin modelo de lenguaje.'],
  ['Node.js 22 y mcp-remote 0.16.6', 'Ejecución del Inspector y puente STDIO a HTTP para Claude Desktop.'],
  ['Claude Desktop 5.11492.5 y Claude Code', 'Hosts MCP adicionales con el modelo Claude.'],
  ['Mermaid y Chrome (sin interfaz)', 'Diagramas y capturas automatizadas del informe.'],
], [32, 68], 'Herramientas de software empleadas durante el desarrollo del laboratorio.');

// =====================================================================
// 5. Arquitectura
// =====================================================================
H1('5. Arquitectura Propuesta');
H2('5.1 Roles del protocolo MCP');
P('MCP distingue cuatro papeles. Usarlos con precisión ayuda a entender qué parte de la solución hace cada cosa (Tabla 2): el modelo nunca toca la base de datos; decide qué herramienta llamar, y el servidor la ejecuta aplicando sus propias reglas.');
TAB(['Rol', 'Qué hace', 'En este proyecto'], [
  ['Host', 'Aplicación conversacional que contiene el modelo', 'El chatbot (con Gemini), Claude Desktop y Claude Code (con Claude)'],
  ['Cliente MCP', 'Negocia el protocolo, descubre herramientas y envía las llamadas', 'Cliente MCP de Spring AI; `mcp-remote` en Claude Desktop; MCP Inspector'],
  ['Servidor MCP', 'Anuncia las herramientas y las ejecuta', '`mcp-server` en `http://localhost:8888/mcp`'],
  ['Herramienta', 'Operación que el modelo puede invocar', 'Cada método `@Tool` de `SupermercadoTools`'],
], [18, 40, 42], 'Roles MCP y su correspondencia en la solución.');
H2('5.2 Componentes');
P('La solución tiene tres piezas en el mismo repositorio (Figura 1). El **servidor MCP** (`mcp-server`, puerto 8888) está organizado en capas: la clase `SupermercadoTools` es un adaptador delgado que recibe cada llamada MCP y delega en servicios de dominio, donde viven las reglas y las transacciones; los servicios usan repositorios de Spring Data JPA. El **chatbot** (`chatbot-client`, puerto 8080) es el host: contiene la página de chat, un controlador REST, el `ChatClient` de Spring AI conectado a Gemini y un cliente MCP que descubre las herramientas del servidor al arrancar. La **base de datos** MySQL 8.4 corre en Docker. Las tres piezas escuchan solo en `127.0.0.1`.');
P('El mismo servidor atiende a otros clientes sin cambios: MCP Inspector y Claude Code se conectan directamente por Streamable HTTP; Claude Desktop lanza sus servidores por STDIO, así que usa el puente `mcp-remote`, que traduce STDIO a Streamable HTTP.');
FIG(fg('componentes.png'), 'Arquitectura de componentes: chatbot (host), servidor MCP en capas, MySQL y los demás clientes del mismo servidor.', { ancho: true, px: 560 });
H2('5.3 Modelo de datos y estados del pedido');
P('El modelo tiene cinco tablas (Figura 2). El estado del pedido vive en la base de datos y no en la memoria del modelo de lenguaje: el pedido nace en `BORRADOR` y se le agregan o quitan productos con herramientas, lo que permite conversar con naturalidad («agrega dos leches», «quita los huevos») y consultar el mismo pedido desde cualquier host. `detalle_pedido.precio_unitario` copia el precio al agregar el producto, para que un cambio posterior no altere pedidos existentes. El dinero se guarda como `DECIMAL(12,2)` y las fechas como `DATETIME`. La base refuerza las reglas con restricciones: teléfono único, una línea por producto en cada pedido, `CHECK` de stock no negativo, cantidad mayor que cero y estados válidos. `producto` y `pedido` tienen una columna `version` para el bloqueo optimista.');
FIG(fg('entidad-relacion.png'), 'Modelo entidad-relación de la base de datos `supermercado_mcp`.');
P('La Figura 3 muestra el ciclo de vida del pedido. Las transiciones a `EN_CAMINO` y `ENTREGADO` existen en el modelo, pero no se publican como herramientas: corresponden a la operación del supermercado, no al cliente.');
FIG(fg('estados.png'), 'Estados del pedido y herramientas que producen cada transición.', { ancho: true, px: 620 });
H2('5.4 Herramientas MCP publicadas');
P('El servidor publica diez herramientas (Tabla 3). Siguiendo la recomendación del laboratorio de publicar solo las operaciones necesarias, no hay herramientas de administración, como cambiar precios o borrar datos.');
TAB(['Herramienta', 'Parámetros', 'Efecto'], [
  ['`listarCategorias`', '—', 'Lectura'],
  ['`buscarProductos`', '`texto?`, `categoriaId?`, `soloDisponibles?`', 'Lectura con precio y stock'],
  ['`buscarCliente`', '`telefono`', 'Lectura; error si no existe'],
  ['`registrarCliente`', '`nombre`, `telefono`, `direccion`, `email?`', 'Crea un cliente; teléfono único'],
  ['`crearPedido`', '`clienteId`, `direccionEntrega?`', 'Pedido vacío en `BORRADOR`'],
  ['`agregarProductoAPedido`', '`pedidoId`, `productoId`, `cantidad`', 'Agrega o suma a la línea; no descuenta stock'],
  ['`quitarProductoDePedido`', '`pedidoId`, `productoId`', 'Quita la línea'],
  ['`consultarPedido`', '`pedidoId`', 'Lectura con detalle y totales'],
  ['`confirmarPedido`', '`pedidoId`, `metodoPago`', 'Valida y descuenta stock, calcula el total'],
  ['`cancelarPedido`', '`pedidoId`', 'Cancela; devuelve stock si estaba confirmado'],
], [36, 38, 26], 'Herramientas MCP publicadas por `mcp-server`.');

// =====================================================================
// 6. Procedimiento
// =====================================================================
H1('6. Procedimiento');
P('El procedimiento se describe en el orden en que fue ejecutado. Como el tiempo disponible era corto, se siguió una ruta crítica: primero el servidor, luego el chatbot y después la verificación del protocolo y los demás hosts. Cada fase empezó con la consulta de sus decisiones abiertas y terminó con un registro en la bitácora y un Pull Request.');
H2('6.1 Verificación del entorno');
N('6.1.1', 'Antes de escribir código se comprobaron las herramientas de la máquina: Ubuntu 22.04.5 LTS; Docker 29.9.2 con Compose v5.6.1, que ejecutó `hello-world` sin `sudo`; Node.js 22.22.3 con npm 10.9.8; git 2.34.1 configurado; y los puertos 3306, 8080 y 8888 libres.');
N('6.1.2', 'El `java` predeterminado del sistema era el 17, aunque el JDK 21 (OpenJDK 21.0.13) estaba instalado. En lugar de cambiar el predeterminado, que afectaría a otros proyectos, se fijó `JAVA_HOME` solo para este proyecto.');
N('6.1.3', 'GitHub CLI no estaba instalado; se decidió no instalarlo y crear el repositorio desde la web de GitHub. Claude Desktop tampoco estaba instalado y se dejó para el final.');
H2('6.2 Repositorio, versiones y flujo de ramas');
N('6.2.1', 'Se creó un monorepo con dos proyectos Maven independientes, `mcp-server` y `chatbot-client`, más `db/init/` con los scripts SQL, `docker-compose.yml` y `docs/` con la documentación y las evidencias.');
N('6.2.2', 'Para la línea de versiones se consultó Maven Central: Spring AI 1.1.8 es el último parche de la línea 1.1, que es la del laboratorio, y está construido contra Spring Boot 3.5.15. Se eligió Spring Boot 3.5.16, el mismo que usa el proyecto del agente del laboratorio, de modo que el código del material base (`@Tool`, `MethodToolCallbackProvider`, propiedades `spring.ai.mcp.server.*`) aplicara sin migraciones. Ambos módulos importan el BOM de Spring AI y no declaran versión en los starters.');
N('6.2.3', 'Spring Initializr ya solo genera proyectos con Spring Boot 4 («compatibility range is >=4.0.0»), así que se generó la estructura por defecto y se reescribió cada `pom.xml` con Spring Boot 3.5.16 y los starters de Spring AI.');
N('6.2.4', 'Cada fase se desarrolló en una rama propia y se integró a `main` con un Pull Request, con mensajes en español según Conventional Commits. La historia resultante tiene 25 commits y siete integraciones (Tabla 4, Figuras 4 y 5).');
TAB(['PR', 'Rama', 'Propósito'], [
  ['#1', '`fase-1-andamiaje`', 'Monorepo y proyectos Maven'],
  ['#2', '`fase-2-base-de-datos`', 'MySQL, esquema y datos'],
  ['#3', '`fase-3-servidor-mcp`', 'Servidor MCP y pruebas'],
  ['#4', '`fase-5-chatbot`', 'Chatbot web con Gemini'],
  ['#5', '`fase-4-verificacion-protocolo`', 'curl, Inspector y ajustes al chatbot'],
  ['#6', '`fase-8-documentacion`', 'Claude Code, README y pruebas'],
  ['#7', '`fase-6-cierre`', 'Consultas desde Claude Code'],
], [10, 45, 45], 'Pull Requests integrados en `main`.');
FIG(fg('grafo-ramas.png'), 'Historia de `main`: ramas por fase integradas mediante los Pull Requests #1 a #7.', { ancho: true, px: 600 });
FIG(fg('pull-requests.png'), 'Pull Requests fusionados en GitHub.');
N('6.2.5', 'Los secretos (clave de Gemini y contraseñas de MySQL) viven en un archivo `.env` ignorado por git; el repositorio solo contiene `.env.example` con valores ficticios. Antes de cada commit se buscaron los valores reales en los cambios, y antes de cada push en todo el historial.');
H2('6.3 Base de datos MySQL en Docker');
N('6.3.1', 'Se definió el servicio MySQL en `docker-compose.yml` (Listado 1): imagen `mysql:8.4` (la serie LTS, con etiqueta fija para que el entorno sea reproducible), puerto publicado solo en `127.0.0.1`, un usuario de aplicación distinto de `root` y los scripts de `db/init/`, que el contenedor ejecuta al crearse.');
LST([
  'services:',
  '  mysql:',
  '    image: mysql:8.4',
  '    environment:',
  '      MYSQL_ROOT_PASSWORD: ${MYSQL_ROOT_PASSWORD}',
  '      MYSQL_DATABASE: supermercado_mcp',
  '      MYSQL_USER: ${DB_USER}        # usuario de la aplicacion',
  '      MYSQL_PASSWORD: ${DB_PASSWORD}',
  '    ports:',
  '      - "127.0.0.1:${DB_PORT:-3306}:3306"',
  '    volumes:',
  '      - mysql_data:/var/lib/mysql',
  '      - ./db/init:/docker-entrypoint-initdb.d:ro',
  '    healthcheck:',
  '      test: ["CMD-SHELL", "mysqladmin ping -h 127.0.0.1',
  '             -u root -p$$MYSQL_ROOT_PASSWORD"]',
], 'Extracto de `docker-compose.yml`.');
N('6.3.2', 'El esquema vive en un único lugar, `db/init/01-schema.sql`, y Spring arranca con `ddl-auto=validate`: Hibernate no lo crea, solo comprueba que las entidades coincidan. Así se evitó la ambigüedad del material base, donde `ddl-auto=update` convivía con un `schema.sql` manual.');
N('6.3.3', 'Los datos de prueba (`02-data.sql`) son sintéticos: 6 categorías, 32 productos genéricos con precios en pesos colombianos (dos agotados, varios con stock bajo y uno inactivo, para probar las validaciones) y 2 clientes ficticios.');
N('6.3.4', 'Se verificó el contenedor en estado `healthy`, las 5 tablas, los conteos y que el usuario de la aplicación solo tiene permisos sobre `supermercado_mcp`; además, un `UPDATE` que dejaba un stock negativo fue rechazado por la restricción `ck_producto_stock`.');
N('6.3.5', 'Problemas resueltos: el primer arranque falló porque en la carpeta existía un directorio vacío llamado `.env` en lugar del archivo, y Compose no encontraba las variables; se eliminó y se creó el archivo real. La verificación de salud usaba la conexión por socket, que puede reportar «healthy» mientras aún corren los scripts de inicio; se cambió a TCP (`-h 127.0.0.1`).');
H2('6.4 Servidor MCP con Spring Boot y Spring AI');
N('6.4.1', 'Se implementaron las entidades JPA (`Categoria`, `Producto`, `Cliente`, `Pedido`, `DetallePedido`) y las enumeraciones `EstadoPedido` y `MetodoPago`. Para que `ddl-auto=validate` aceptara las columnas `VARCHAR` de los estados, los `enum` se anotaron con `@JdbcTypeCode(SqlTypes.VARCHAR)`; sin eso, Hibernate espera un tipo `ENUM` nativo de MySQL.');
N('6.4.2', 'Las reglas de negocio se implementaron en `CatalogoService`, `ClienteService` y `PedidoService`, sin confiar en los argumentos que produce el modelo: cantidad mayor que cero; producto existente y activo; solo se modifican pedidos en `BORRADOR`; no se confirma un pedido vacío; teléfono único y con formato válido. Al confirmar, primero se validan todas las líneas y solo después se descuenta el stock, en una transacción: si a una línea le falta stock no se descuenta ninguna. Cancelar un pedido confirmado devuelve el stock, y no se cancela uno en camino, entregado o ya cancelado.');
N('6.4.3', 'Las herramientas devuelven DTO como `record` y no entidades JPA, lo que evita problemas de carga perezosa al serializar relaciones y controla exactamente qué ve el modelo. Los errores de negocio se lanzan como `ReglaNegocioException`, con mensajes en español que dicen qué pasó y qué hacer.');
N('6.4.4', 'La clase `SupermercadoTools` publica las diez herramientas. Cada descripción le dice al modelo qué hace la herramienta, cuándo usarla, qué devuelve y cuándo falla, porque es lo único que el modelo lee para decidir. El registro se hace con un bean `ToolCallbackProvider`, igual que en el laboratorio (Listado 2).');
LST([
  '@Tool(description = "Confirma un pedido que está en estado BORRADOR:',
  '    valida el stock de cada producto, lo descuenta del inventario y',
  '    calcula el total con el costo de envío. Úsala solo después de que',
  '    el usuario haya confirmado explícitamente el resumen del pedido.',
  '    Si falta stock de algún producto falla sin descontar nada ...")',
  'public PedidoDto confirmarPedido(',
  '    @ToolParam(description = "Id del pedido en borrador") Long pedidoId,',
  '    @ToolParam(description = "Método de pago: EFECTIVO, TARJETA o',
  '        TRANSFERENCIA") String metodoPago) {',
  '  return pedidos.confirmar(pedidoId, metodoPago);   // delega en el servicio',
  '}',
  '',
  '// SupermercadoMcpApplication: registro de las herramientas',
  '@Bean',
  'public ToolCallbackProvider supermercadoToolCallbacks(SupermercadoTools tools) {',
  '  return MethodToolCallbackProvider.builder().toolObjects(tools).build();',
  '}',
], 'Una herramienta `@Tool` y su registro con `MethodToolCallbackProvider`.', { ancho: true });
N('6.4.5', 'La configuración del servidor (Listado 3) activa el transporte Streamable HTTP en `/mcp`, toma las credenciales de variables de entorno y fija el costo de envío como propiedad.');
LST([
  'server.port=8888',
  'server.address=127.0.0.1',
  'spring.ai.mcp.server.name=supermercado-mcp-server',
  'spring.ai.mcp.server.type=SYNC',
  'spring.ai.mcp.server.protocol=STREAMABLE',
  'spring.ai.mcp.server.streamable-http.mcp-endpoint=/mcp',
  'spring.datasource.url=jdbc:mysql://localhost:${DB_PORT:3306}/',
  '    supermercado_mcp?serverTimezone=America/Bogota',
  'spring.datasource.username=${DB_USER}',
  'spring.datasource.password=${DB_PASSWORD}',
  'spring.jpa.hibernate.ddl-auto=validate',
  'spring.jpa.open-in-view=false',
  'supermercado.costo-envio=5000',
], 'Extracto de `application.properties` del servidor MCP.');
N('6.4.6', 'Se escribieron 39 pruebas unitarias con JUnit 5 y Mockito (25 en `PedidoServiceTest` y 14 en `ClienteServiceTest`), al menos una por regla de negocio, y todas pasaron. Al arrancar, el servidor se conectó a MySQL, Hibernate validó el esquema y el registro mostró «Registered tools: 10».');
N('6.4.7', 'Con una llamada real se comprobó cómo llega un error de negocio al cliente: no como un error JSON-RPC, sino como un resultado normal con `"isError": true` y el mensaje en `content[0].text`.');
H2('6.5 Chatbot web con Gemini');
N('6.5.1', 'El chatbot es el host. Al arrancar, su cliente MCP envía `initialize` y `tools/list` al servidor y convierte las herramientas recibidas en herramientas que Gemini puede usar. El chatbot no conoce las herramientas al compilarse, no tiene acceso a la base de datos ni contiene reglas de negocio: todo pasa por el protocolo (Listado 4).');
LST([
  '@Bean',
  'ChatClient chatClient(ChatClient.Builder builder,',
  '                      ToolCallbackProvider herramientasMcp, ChatMemory memoria) {',
  '  ToolCallback[] herramientas = Arrays.stream(herramientasMcp.getToolCallbacks())',
  '      .map(HerramientaRegistrada::new)   // anota cada llamada para la página',
  '      .toArray(ToolCallback[]::new);',
  '  return builder.defaultToolCallbacks(herramientas)',
  '      .defaultAdvisors(MessageChatMemoryAdvisor.builder(memoria).build())',
  '      .build();',
  '}',
  '',
  '# application.properties del chatbot',
  'spring.ai.google.genai.chat.options.model=${GEMINI_MODEL:gemini-3.5-flash-lite}',
  'spring.ai.mcp.client.streamable-http.connections.supermercado.url=http://localhost:8888',
  'spring.ai.mcp.client.streamable-http.connections.supermercado.endpoint=/mcp',
], 'Configuración del `ChatClient` con las herramientas descubiertas por el cliente MCP.', { ancho: true });
N('6.5.2', 'La elección del modelo cambió respecto al laboratorio. La documentación de Google indicaba que `gemini-2.5-flash` estaba deprecado y que desde el 1 de diciembre de 2026 sus peticiones se redirigen a otro modelo. Se probó cada candidato con la clave del equipo y una petición con llamada a herramienta: `gemini-3.8-flash` no respondió en 90 s, `gemini-3.6-flash` tardó 35 s y `gemini-3.5-flash-lite` respondió en cerca de 1 s. Se eligió este último, configurable sin tocar código mediante la variable `GEMINI_MODEL`.');
N('6.5.3', 'El prompt de sistema presenta al asistente como «MercaBot» y fija sus reglas: usar siempre las herramientas y nunca inventar productos, precios ni disponibilidad; identificar al cliente por teléfono y ofrecer registrarlo si no existe; mostrar el resumen y pedir confirmación explícita antes de confirmar o cancelar; explicar los errores y ofrecer alternativas; y no atender temas ajenos al supermercado.');
N('6.5.4', 'La página de chat, en HTML, CSS y JavaScript sin framework, envía cada mensaje a `POST /api/chat` con un identificador de conversación. Un decorador de las herramientas registra cada llamada, y la página muestra debajo de cada respuesta qué herramientas MCP usó el modelo y con qué argumentos, lo que hace visible el protocolo (Figuras 6 a 8).');
FIG(ev('f5-chat-catalogo.png'), 'Identificación por teléfono y consulta del catálogo: el modelo usa `buscarCliente`, `listarCategorias` y `buscarProductos`.');
FIG(ev('f5-chat-pedido.png'), 'Pedido completo conversando: agregar, quitar, ver el total y confirmar.');
FIG(ev('f5-chat-error-stock.png'), 'Stock insuficiente: `confirmarPedido` devuelve error y el asistente propone alternativas.');
N('6.5.5', 'Se ejecutó el guion de aceptación: «Hola, quiero hacer un pedido. Mi teléfono es 3001234567», «¿Qué lácteos tienen?», «Agrega 2 leches enteras y una docena de huevos», «Quita los huevos y muéstrame el total» y «Confirma, pago en efectivo». El pedido quedó confirmado por $14.600 ($9.600 de productos más $5.000 de envío), lo que se comprobó con un `SELECT` en MySQL. También funcionaron el registro de un cliente con un teléfono nuevo, el caso de stock insuficiente y el rechazo de temas ajenos.');
N('6.5.6', 'Durante las pruebas aparecieron y se corrigieron varios problemas de integración entre Spring AI 1.1.8 y Gemini. (a) Respuestas cortadas: Gemini puede dividir el texto en varias partes y `content()` devolvía solo la primera; se unió el texto de todas. (b) Error 503 cuando una herramienta fallaba: el cliente MCP convierte el resultado con `isError` en una excepción de texto plano y Gemini exige JSON; se entrega al modelo como `{"error": "..."}`. (c) Peticiones colgadas: el SDK de Google no trae tiempo de espera; se fijaron 12 s por intento y un máximo de dos intentos. (d) Pedidos duplicados: la memoria de chat solo guarda textos, no las herramientas ejecutadas, y el modelo volvía a crear el pedido; ahora recibe en cada turno las herramientas que ya usó y su resultado. (e) El nivel gratuito de Gemini limita las peticiones por minuto; el chatbot responde con un mensaje amable.');
N('6.5.7', 'Se agregaron 6 pruebas unitarias del chatbot: la traducción de fallos técnicos a mensajes sin trazas ni claves, y la entrega de errores de herramienta como JSON.');
H2('6.6 Verificación del protocolo: curl y MCP Inspector');
N('6.6.1', 'Para separar el protocolo del modelo, se recorrió con curl el ciclo completo de JSON-RPC 2.0 (Tabla 5 y Listado 5). La respuesta a `initialize` llega como JSON, mientras que las de `tools/list` y `tools/call` llegan como eventos SSE: por eso la cabecera `Accept` debe aceptar ambos formatos.');
TAB(['Paso', 'Petición', 'Respuesta observada'], [
  ['1. Negociación', '`initialize`', 'HTTP 200, cabecera `Mcp-Session-Id`, versión `2025-03-26`, capacidades y `serverInfo`'],
  ['2. Fin de inicialización', '`notifications/initialized` (sin `id`)', 'HTTP 202 sin cuerpo'],
  ['3. Descubrimiento', '`tools/list`', 'Las 10 herramientas con descripción y esquema JSON'],
  ['4. Ejecución', '`tools/call`', '`result.content[0].text` con el DTO en JSON e `isError: false`'],
], [24, 30, 46], 'Ciclo MCP observado con curl.');
LST([
  '$ curl -i -X POST http://localhost:8888/mcp \\',
  '    -H "Content-Type: application/json" \\',
  '    -H "Accept: application/json, text/event-stream" \\',
  '    -d \'{"jsonrpc":"2.0","id":1,"method":"initialize","params":{',
  '         "protocolVersion":"2025-03-26","capabilities":{},',
  '         "clientInfo":{"name":"curl","version":"1.0"}}}\'',
  'HTTP/1.1 200',
  'Mcp-Session-Id: ecb83beb-a8cd-446e-81c0-00a3faa1b37a',
  '{"jsonrpc":"2.0","id":1,"result":{"protocolVersion":"2025-03-26",',
  ' "capabilities":{"tools":{"listChanged":true}, ...},',
  ' "serverInfo":{"name":"supermercado-mcp-server","version":"1.0.0"}}}',
  '',
  '# tools/call buscarProductos {"texto":"leche"}  (con Mcp-Session-Id)',
  'data:{"jsonrpc":"2.0","id":3,"result":{"content":[{"type":"text",',
  ' "text":"[{\\"id\\":2,\\"nombre\\":\\"Leche deslactosada\\", ...}]"}],',
  ' "isError":false}}',
], 'Negociación y ejecución de una herramienta con curl.', { ancho: true });
N('6.6.2', 'Se ejecutó por JSON-RPC, sin ningún modelo, el plan de pruebas de seis pasos, con un `SELECT` después de cada escritura (Tabla 6).');
TAB(['Paso', 'Prueba', 'Resultado'], [
  ['1', 'Lectura de categorías y de productos con «leche»', '6 categorías y 2 productos reales'],
  ['2', '`registrarCliente`, y otra vez con el mismo teléfono', 'Fila nueva; el duplicado falla'],
  ['3', '`crearPedido` y dos `agregarProductoAPedido`', 'Pedido 15 en `BORRADOR`, subtotal $31.300'],
  ['4', 'Agregar 3 gaseosas habiendo 1 y confirmar', 'Error «Stock insuficiente…»; no se descuenta ninguna línea'],
  ['5', 'Quitar la gaseosa y confirmar', 'Total $36.300; arroz 80 → 78 y café 16 → 15'],
  ['6', '`cancelarPedido` y cancelar otra vez', 'Stock devuelto; la segunda cancelación falla'],
], [8, 47, 45], 'Plan de pruebas ejecutado sin modelo de lenguaje.');
N('6.6.3', 'Se conectó MCP Inspector, en su versión 2.13.0, por Streamable HTTP (Figuras 9 a 11). Su interfaz difiere de la versión 0.12.0 del laboratorio: el servidor se agrega en «Add Servers» y se conecta con un interruptor. Desde el Inspector se listaron las herramientas, se buscó «leche» y se confirmó el pedido 16, cuyo efecto en el stock se verificó en MySQL.');
FIG(ev('f4-inspector-conexion.png'), 'MCP Inspector conectado al servidor por Streamable HTTP.', { ancho: true, px: 520 });
FIG(ev('f4-inspector-tools.png'), 'Herramientas descubiertas y resultado de `buscarProductos` en el Inspector.', { ancho: true, px: 520 });
FIG(ev('f4-inspector-confirmar.png'), 'Confirmación de un pedido desde el Inspector, sin modelo de lenguaje.', { ancho: true, px: 520 });
H2('6.7 Otros hosts: Claude Code y Claude Desktop');
N('6.7.1', '**Claude Code.** El comando `claude mcp add --transport http --scope project supermercado-mcp http://localhost:8888/mcp` creó el archivo `.mcp.json` en la raíz del repositorio. Claude Code pide aprobar estos servidores la primera vez y los lee solo al iniciar una sesión, por lo que fue necesario abrir una sesión nueva en la carpeta del proyecto. Con el servidor ya conectado se preguntó «¿Qué categorías de productos hay?» y «Muéstrame el pedido número 6». Claude Code usó `listarCategorias` y `consultarPedido` y mostró el pedido creado antes desde el chatbot (Figuras 12 y 13). El registro del servidor confirma ambas llamadas y MySQL confirma los datos del pedido.');
FIG(ev('f6-claude-code-categorias.png'), 'Claude Code consulta las categorías a través del servidor MCP.', { ancho: true, px: 600 });
FIG(ev('f6-claude-code-pedido.png'), 'Claude Code consulta el pedido 6, creado conversando con el chatbot.', { ancho: true, px: 600 });
N('6.7.2', '**Claude Desktop.** Se instaló la versión 5.11492.5, en beta para Linux, desde el repositorio apt oficial de Anthropic, verificando antes la huella de la llave de firma. Como el archivo de configuración de la aplicación lanza servidores por STDIO, se usó el puente `mcp-remote`, que primero se probó con un entorno limpio para simular cómo lo lanza la aplicación. Como Node.js estaba instalado con nvm, la configuración usa rutas absolutas y define el `PATH` (Listado 6).');
LST([
  '{',
  '  "mcpServers": {',
  '    "supermercado-mcp": {',
  '      "command": "<node>/bin/node",',
  '      "args": ["<node>/bin/npx", "-y", "mcp-remote",',
  '               "http://localhost:8888/mcp"],',
  '      "env": { "PATH": "<node>/bin:/usr/bin:/bin" }',
  '    }',
  '  }',
  '}',
], 'Conector de Claude Desktop en `~/.config/Claude/claude_desktop_config.json` (`<node>` es la ruta de Node.js).');
N('6.7.3', 'Al abrir Claude Desktop, su registro mostró que lanzó el puente, negoció con el servidor y recibió la lista de herramientas en milisegundos. Unos minutos después aparecen errores `HeadersTimeoutError` en la conexión GET opcional que el cliente deja abierta para recibir avisos del servidor; este servidor no los envía, el cliente corta esa conexión vacía y el puente la reabre, sin afectar las llamadas. No se capturaron consultas desde Claude Desktop: la aplicación se niega a arrancar con un puerto de depuración, que era la vía para automatizar las capturas, y la demostración se completó con Claude Code.');
N('6.7.4', 'La Tabla 7 compara los hosts. El servidor y sus herramientas se reutilizaron íntegros: para conectar un host nuevo no se modificó `mcp-server`, solo la configuración del host.');
TAB(['', 'Chatbot', 'Claude Desktop', 'Claude Code', 'Inspector'], [
  ['Modelo', 'Gemini', 'Claude', 'Claude', 'Ninguno'],
  ['Transporte', 'Streamable HTTP', 'STDIO → `mcp-remote` → HTTP', 'Streamable HTTP', 'Streamable HTTP'],
  ['Configuración', '`application.properties`', '`claude_desktop_config.json`', '`.mcp.json`', 'Interfaz'],
  ['Código propio', 'Interfaz y orquestación', 'Ninguno', 'Ninguno', 'Ninguno'],
], [16, 21, 25, 19, 19], 'Comparación de los hosts que usaron el mismo servidor MCP.', { ancho: true });

// =====================================================================
// 6.8 Flujo en nueve pasos
// =====================================================================
H2('6.8 Flujo de un pedido en nueve pasos');
P('La sección 11 del laboratorio explica el flujo MCP en nueve pasos. Aplicados al mensaje «Agrega 2 leches enteras» (Figura 14):');
FIG(fg('secuencia.png'), 'Diagrama de secuencia del mensaje «Agrega 2 leches enteras», desde la página de chat hasta MySQL y de vuelta.', { ancho: true, px: 660 });
N('1.', '**Negociación.** Al arrancar, el cliente MCP del chatbot envía `initialize`; acuerdan versión y capacidades y el servidor asigna un `Mcp-Session-Id`.');
N('2.', '**Descubrimiento.** El cliente pide `tools/list` y recibe las diez herramientas con su descripción y esquema JSON.');
N('3.', '**Necesidad.** El usuario escribe en lenguaje natural: «Agrega 2 leches enteras».');
N('4.', '**Decisión.** Gemini lee el mensaje, el prompt de sistema y las descripciones, y elige `buscarProductos` y luego `agregarProductoAPedido` con argumentos estructurados.');
N('5.', '**Invocación.** Spring AI envía cada `tools/call` como JSON-RPC 2.0 sobre Streamable HTTP.');
N('6.', '**Despacho.** En el servidor, Spring AI enruta la llamada al método `@Tool` correspondiente.');
N('7.', '**Ejecución.** `PedidoService` valida las reglas y modifica MySQL mediante Spring Data JPA, en una transacción.');
N('8.', '**Resultado.** El servidor devuelve el `PedidoDto` serializado en `result.content`.');
N('9.', '**Respuesta.** Gemini convierte el resultado en un mensaje comprensible y la página muestra las herramientas usadas.');

// =====================================================================
// 6.9 - 6.11
// =====================================================================
H2('6.9 Resultados de las pruebas');
P('La Tabla 8 resume las pruebas integrales; el detalle de las 26 pruebas, con su evidencia, está en `docs/pruebas.md`. Las pruebas automatizadas, 39 del servidor y 6 del chatbot, terminaron sin fallos.');
TAB(['Prueba', 'Resultado'], [
  ['Arranque del servidor', 'Esquema validado; «Registered tools: 10»'],
  ['Negociación y descubrimiento', 'Respuesta válida con capacidades; 10 herramientas'],
  ['Escrituras por MCP', 'Filas nuevas en `cliente`, `pedido` y `detalle_pedido`'],
  ['Stock insuficiente', 'Error claro; stock intacto'],
  ['Confirmación y cancelación', 'Stock descontado y devuelto; total correcto'],
  ['Chatbot de extremo a extremo', 'Pedido confirmado en la base conversando'],
  ['Inspector', 'Conexión, herramientas y confirmación'],
  ['Claude Code', 'Consultas con datos reales del chatbot'],
  ['Claude Desktop', 'Conexión y descubrimiento según su registro'],
  ['Automatizadas', '45 pruebas sin fallos'],
], [42, 58], 'Resumen de las pruebas integrales.');
H2('6.10 Seguridad');
P('El riesgo principal es que el endpoint MCP no tiene autenticación: cualquier proceso que lo alcance puede crear clientes y confirmar pedidos, y los argumentos los produce un modelo en el que no se puede confiar. Mitigaciones aplicadas:');
B([
  'El servidor, el chatbot y MySQL escuchan solo en `127.0.0.1`.',
  'La aplicación usa un usuario de MySQL con permisos solo sobre su base, no `root`.',
  'El servidor valida todas las entradas, y la base refuerza las reglas con restricciones.',
  'Solo se publican las operaciones que necesita un cliente.',
  'El asistente pide confirmación explícita antes de confirmar o cancelar.',
  'Los secretos están fuera del repositorio, y al navegador no llegan trazas ni claves.',
  'Todos los datos son sintéticos.',
]);
P('Un despliegue real necesitaría además TLS, autenticación y autorización del endpoint (OAuth 2.1, como prevé la especificación de MCP, o una clave detrás de un proxy inverso), que cada cliente solo acceda a sus pedidos, límites de tasa y auditoría de las llamadas que modifican datos.');
H2('6.11 Observaciones y oportunidades de mejora');
N('6.11.1', 'La memoria de conversación y el historial de herramientas del chatbot viven en memoria: se pierden al reiniciarlo, aunque los pedidos no. Además, si el servidor MCP se reinicia hay que reiniciar el chatbot, porque su sesión deja de ser válida.');
N('6.11.2', 'El nivel gratuito de Gemini limita las peticiones por minuto, y cada mensaje del usuario genera entre dos y seis llamadas al modelo. Un plan de pago o un modelo local eliminaría esa restricción.');
N('6.11.3', 'La integración con la base de datos se verificó a mano; una prueba con Testcontainers sobre MySQL real automatizaría esa verificación.');
N('6.11.4', 'Los pedidos abandonados en `BORRADOR` no se limpian solos, aunque no retienen stock. También podrían agregarse herramientas para el personal del supermercado (pedidos en camino y entregados) con autorización propia, y una consulta del historial de pedidos de un cliente.');
N('6.11.5', 'Faltó capturar consultas desde Claude Desktop, cuya conexión sí quedó verificada; y la línea 2.x de Spring AI permitiría usar anotaciones MCP más recientes, a costa de migrar la configuración del laboratorio.');

// =====================================================================
// 7. Conclusiones y referencias
// =====================================================================
H1('7. Conclusiones');
B([
  'MCP desacopla el negocio del modelo: las mismas diez herramientas sirvieron a Gemini en el chatbot, a Claude en Claude Code y en Claude Desktop, y a una persona en el Inspector, sin cambiar una línea del servidor. Lo único que cambia entre hosts es su configuración.',
  'Dejar el estado del pedido en la base de datos, y no en la memoria del modelo, permitió conversar con naturalidad y consultar desde Claude Code un pedido creado en el chatbot.',
  'Las reglas de negocio deben vivir en el servidor: el modelo produce argumentos que pueden ser incorrectos, y la validación, la transacción al confirmar y las restricciones de la base garantizaron la consistencia del inventario en todos los casos probados.',
  'Las descripciones de las herramientas son parte del diseño: son lo que el modelo lee para decidir, y escribir cuándo usar cada una y cuándo falla redujo las llamadas incorrectas.',
  'Verificar el protocolo sin modelo, con curl e Inspector, separó los problemas del servidor de los del host. Así se ubicaron con precisión fallos que eran de la integración entre Spring AI y Gemini, como las respuestas cortadas, los errores no JSON y la falta de tiempo de espera.',
  'Registrar cada decisión y cada problema mientras se trabajaba, con una rama y un Pull Request por fase, convirtió el proceso en evidencia verificable y permitió explicar cada paso, como pide el enunciado.',
]);
H1('Referencias');
const refs = [
  'Botia, D. J. L. (2026). Guía de laboratorio: Construcción e integración de un MCP Server — Spring Boot, Spring AI, MySQL, Claude Desktop y Google ADK. Arquitectura de Software, Universidad de Antioquia.',
  'Anthropic. (2026). Model Context Protocol — Specification. https://modelcontextprotocol.io/',
  'Spring. (2026). Spring AI Reference: Model Context Protocol. https://docs.spring.io/spring-ai/reference/1.1/api/mcp/mcp-overview.html',
  'Spring. (2026). Spring AI Reference: MCP Server Boot Starter (Streamable HTTP). https://docs.spring.io/spring-ai/reference/1.1/api/mcp/mcp-streamable-http-server-boot-starter-docs.html',
  'Spring. (2026). Spring AI Reference: MCP Client Boot Starter. https://docs.spring.io/spring-ai/reference/1.1/api/mcp/mcp-client-boot-starter-docs.html',
  'Spring. (2026). Spring AI Reference: Google GenAI Chat. https://docs.spring.io/spring-ai/reference/api/chat/google-genai-chat.html',
  'Spring. (2026). Spring Boot Reference Documentation. https://docs.spring.io/spring-boot/documentation.html',
  'Google. (2026). Gemini API: Models. https://ai.google.dev/gemini-api/docs/models',
  'Model Context Protocol. (2026). MCP Inspector. https://github.com/modelcontextprotocol/inspector',
  'mcp-remote. (2026). mcp-remote: puente entre transporte STDIO y servidores MCP remotos [Paquete npm]. https://www.npmjs.com/package/mcp-remote',
  'Anthropic. (2026). Connect Claude Code to tools via MCP. https://code.claude.com/docs/en/mcp-quickstart',
  'Anthropic. (2026). Claude Desktop on Linux (beta). https://code.claude.com/docs/en/desktop-linux',
  'Oracle. (2026). MySQL 8.4 Reference Manual. https://dev.mysql.com/doc/refman/8.4/en/',
  'Docker Inc. (2026). Docker Compose Documentation. https://docs.docker.com/compose/',
  'Vanegas López, J. y Vergara Ruiz, D. A. (2026). supermercado-mcp [Repositorio del laboratorio]. GitHub. https://github.com/Dalev10/supermercado-mcp',
];
refs.forEach((r) => agregar(2, new Paragraph({ children: runs(r, { size: 18 }), numbering: { reference: 'vinetas', level: 0 }, spacing: { after: 60 } })));

// =====================================================================
// Ensamblado: una sección por bloque, continuas, con 1 o 2 columnas
// =====================================================================
const pagina = { size: { width: 12240, height: 15840 }, margin: { top: 1080, bottom: 1080, left: 1080, right: 1080 } };
const piePagina = new Footer({ children: [new Paragraph({ alignment: AlignmentType.CENTER, children: [new TextRun({ children: [PageNumber.CURRENT], size: 18 })] })] });
const doc = new Document({
  creator: 'Julián Vanegas López y Diego Alejandro Vergara Ruiz',
  title: 'Informe de Laboratorio – Chatbot de pedidos sobre un servidor MCP',
  styles: { default: { document: { run: { font: FUENTE, size: 20 } } },
    characterStyles: [{ id: 'Hyperlink', name: 'Hyperlink', run: { color: '1155CC', underline: {} } }] },
  numbering: { config: [{ reference: 'vinetas', levels: [{ level: 0, format: LevelFormat.BULLET, text: '•', alignment: AlignmentType.LEFT,
    style: { paragraph: { indent: { left: 360, hanging: 240 } } } }] }] },
  sections: bloques.map((b, i) => ({
    properties: { page: pagina, type: i === 0 ? SectionType.NEXT_PAGE : SectionType.CONTINUOUS,
      column: b.capa === 2 ? { count: 2, space: ESPACIO_COL } : { count: 1 } },
    footers: { default: piePagina },
    children: b.elementos,
  })),
});
Packer.toBuffer(doc).then((buf) => {
  fs.writeFileSync(SALIDA, buf);
  console.log(`informe -> ${SALIDA} | secciones: ${bloques.length} | figuras: ${nFig} | tablas: ${nTab} | listados: ${nLst} | ${(buf.length / 1024).toFixed(0)} KB`);
});
