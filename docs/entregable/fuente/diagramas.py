# Genera una página HTML por diagrama Mermaid, con estilo sobrio para un informe impreso.
import pathlib, sys

dest = pathlib.Path(sys.argv[1])
dest.mkdir(parents=True, exist_ok=True)

DIAGRAMAS = {
"componentes": """flowchart TB
    U([Usuario]) -->|navegador| UI
    subgraph CB["chatbot-client · Spring Boot · 127.0.0.1:8080 (host)"]
        UI["Página de chat<br/>HTML, CSS y JavaScript"]
        CC["ChatController<br/>POST /api/chat"]
        CL["ChatClient de Spring AI<br/>prompt de sistema + memoria"]
        MC["Cliente MCP de Spring AI<br/>Streamable HTTP"]
        UI -->|JSON| CC --> CL
        CL <-->|herramientas descubiertas| MC
    end
    CL <-->|HTTPS| G[("API de Gemini<br/>gemini-3.5-flash-lite")]
    subgraph SV["mcp-server · Spring Boot + Spring AI · 127.0.0.1:8888/mcp"]
        T["SupermercadoTools<br/>10 métodos @Tool"]
        S["Servicios de dominio<br/>Catálogo, Cliente, Pedido"]
        R["Repositorios Spring Data JPA"]
        T --> S --> R
    end
    MC <-->|"JSON-RPC 2.0 sobre Streamable HTTP"| T
    INS["MCP Inspector<br/>(sin modelo)"] <-->|Streamable HTTP| T
    CD["Claude Desktop"] <-->|STDIO| BR["mcp-remote"] <-->|Streamable HTTP| T
    CCO["Claude Code<br/>.mcp.json"] <-->|Streamable HTTP| T
    R <-->|JDBC| DB[("MySQL 8.4 en Docker<br/>supermercado_mcp<br/>127.0.0.1:3306")]
""",
"entidad-relacion": """erDiagram
    CATEGORIA ||--o{ PRODUCTO : agrupa
    CLIENTE ||--o{ PEDIDO : hace
    PEDIDO ||--|{ DETALLE_PEDIDO : contiene
    PRODUCTO ||--o{ DETALLE_PEDIDO : aparece_en
    CATEGORIA {
        bigint id PK
        varchar nombre UK
        varchar descripcion
    }
    PRODUCTO {
        bigint id PK
        varchar nombre
        decimal precio "DECIMAL(12,2)"
        int stock "CHECK >= 0"
        varchar unidad
        bit activo
        bigint categoria_id FK
        bigint version
    }
    CLIENTE {
        bigint id PK
        varchar nombre
        varchar telefono UK
        varchar direccion
        varchar email
        datetime creado_en
    }
    PEDIDO {
        bigint id PK
        bigint cliente_id FK
        varchar estado "CHECK"
        varchar direccion_entrega
        decimal subtotal
        decimal costo_envio
        decimal total
        varchar metodo_pago "CHECK"
        datetime creado_en
        datetime confirmado_en
        bigint version
    }
    DETALLE_PEDIDO {
        bigint id PK
        bigint pedido_id FK
        bigint producto_id FK
        int cantidad "CHECK > 0"
        decimal precio_unitario
        decimal subtotal
    }
""",
"estados": """stateDiagram-v2
    direction LR
    [*] --> BORRADOR: crearPedido
    BORRADOR --> BORRADOR: agregar o quitar producto
    BORRADOR --> CONFIRMADO: confirmarPedido (valida y descuenta stock)
    BORRADOR --> CANCELADO: cancelarPedido
    CONFIRMADO --> CANCELADO: cancelarPedido (devuelve stock)
    CONFIRMADO --> EN_CAMINO
    EN_CAMINO --> ENTREGADO
""",
"secuencia": """sequenceDiagram
    autonumber
    actor U as Usuario
    participant W as Página de chat
    participant CL as ChatClient (Spring AI)
    participant G as Gemini
    participant MC as Cliente MCP
    participant S as mcp-server
    participant DB as MySQL
    Note over MC,S: Al arrancar el chatbot: initialize, notifications/initialized y tools/list
    U->>W: "Agrega 2 leches enteras"
    W->>CL: POST /api/chat {conversacionId, mensaje}
    CL->>G: mensaje + historial + 10 declaraciones de herramientas
    G-->>CL: functionCall buscarProductos {texto: "leche entera"}
    CL->>MC: ejecutar herramienta
    MC->>S: tools/call buscarProductos
    S->>DB: SELECT producto
    S-->>MC: result.content = [ProductoDto]
    MC-->>CL: resultado
    CL->>G: resultado de la herramienta
    G-->>CL: functionCall agregarProductoAPedido {pedidoId, productoId: 1, cantidad: 2}
    CL->>MC: ejecutar herramienta
    MC->>S: tools/call agregarProductoAPedido
    S->>DB: INSERT detalle_pedido, UPDATE pedido (una transacción)
    S-->>MC: result.content = PedidoDto con subtotal, envío y total
    MC-->>CL: resultado
    CL->>G: resultado de la herramienta
    G-->>CL: texto final con el resumen
    CL-->>W: {respuesta, herramientas}
    W-->>U: mensaje y herramientas MCP usadas
""",
}

PLANTILLA = """<!doctype html><html lang="es"><head><meta charset="utf-8"><title>{nombre}</title>
<style>body{{margin:0;padding:24px;background:#fff;font-family:Arial,Helvetica,sans-serif}} #d{{display:inline-block}}</style>
<script src="https://cdn.jsdelivr.net/npm/mermaid@10.9.1/dist/mermaid.min.js"></script></head>
<body><div id="d"><pre class="mermaid">{fuente}</pre></div>
<script>mermaid.initialize({{startOnLoad:true, theme:'neutral', fontFamily:'Arial, Helvetica, sans-serif',
 flowchart:{{curve:'basis', htmlLabels:true, useMaxWidth:false}}, er:{{fontSize:14, useMaxWidth:false}}, state:{{useMaxWidth:false}}, sequence:{{mirrorActors:false, useMaxWidth:false}} }});</script>
</body></html>"""

for nombre, fuente in DIAGRAMAS.items():
    (dest / f"{nombre}.html").write_text(PLANTILLA.format(nombre=nombre, fuente=fuente.replace("<", "&lt;").replace(">", "&gt;") if False else fuente), encoding="utf-8")
    print("html ->", nombre)
