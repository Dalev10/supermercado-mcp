-- Esquema de supermercado_mcp. Es la unica fuente de verdad del modelo de datos:
-- Spring arranca con ddl-auto=validate y solo comprueba que las entidades coincidan.
-- La base y el usuario de aplicacion los crea el contenedor (MYSQL_DATABASE, MYSQL_USER).
SET NAMES utf8mb4;

CREATE TABLE categoria (
  id          BIGINT       NOT NULL AUTO_INCREMENT,
  nombre      VARCHAR(80)  NOT NULL,
  descripcion VARCHAR(255) NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_categoria_nombre (nombre)
) ENGINE=InnoDB;

CREATE TABLE producto (
  id           BIGINT        NOT NULL AUTO_INCREMENT,
  nombre       VARCHAR(120)  NOT NULL,
  descripcion  VARCHAR(255)  NULL,
  precio       DECIMAL(12,2) NOT NULL,              -- pesos colombianos
  stock        INT           NOT NULL DEFAULT 0,
  unidad       VARCHAR(30)   NOT NULL,              -- presentacion: "1 L", "500 g"
  activo       BIT(1)        NOT NULL DEFAULT b'1', -- los inactivos no se ofrecen ni se venden
  categoria_id BIGINT        NOT NULL,
  version      BIGINT        NOT NULL DEFAULT 0,    -- bloqueo optimista (@Version)
  PRIMARY KEY (id),
  KEY idx_producto_nombre (nombre),
  CONSTRAINT fk_producto_categoria FOREIGN KEY (categoria_id) REFERENCES categoria (id),
  CONSTRAINT ck_producto_precio CHECK (precio >= 0),
  CONSTRAINT ck_producto_stock  CHECK (stock >= 0)  -- la base tampoco permite vender lo que no hay
) ENGINE=InnoDB;

CREATE TABLE cliente (
  id        BIGINT       NOT NULL AUTO_INCREMENT,
  nombre    VARCHAR(120) NOT NULL,
  telefono  VARCHAR(20)  NOT NULL,                  -- identifica al cliente
  direccion VARCHAR(255) NOT NULL,
  email     VARCHAR(120) NULL,
  creado_en DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_cliente_telefono (telefono)
) ENGINE=InnoDB;

CREATE TABLE pedido (
  id                BIGINT        NOT NULL AUTO_INCREMENT,
  cliente_id        BIGINT        NOT NULL,
  estado            VARCHAR(20)   NOT NULL DEFAULT 'BORRADOR',
  direccion_entrega VARCHAR(255)  NOT NULL,
  subtotal          DECIMAL(12,2) NOT NULL DEFAULT 0,
  costo_envio       DECIMAL(12,2) NOT NULL DEFAULT 0,
  total             DECIMAL(12,2) NOT NULL DEFAULT 0,
  metodo_pago       VARCHAR(20)   NULL,             -- se define al confirmar
  creado_en         DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  confirmado_en     DATETIME      NULL,
  version           BIGINT        NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  KEY idx_pedido_cliente (cliente_id),
  CONSTRAINT fk_pedido_cliente FOREIGN KEY (cliente_id) REFERENCES cliente (id),
  CONSTRAINT ck_pedido_estado CHECK (estado IN ('BORRADOR', 'CONFIRMADO', 'EN_CAMINO', 'ENTREGADO', 'CANCELADO')),
  CONSTRAINT ck_pedido_metodo_pago CHECK (metodo_pago IS NULL OR metodo_pago IN ('EFECTIVO', 'TARJETA', 'TRANSFERENCIA'))
) ENGINE=InnoDB;

CREATE TABLE detalle_pedido (
  id              BIGINT        NOT NULL AUTO_INCREMENT,
  pedido_id       BIGINT        NOT NULL,
  producto_id     BIGINT        NOT NULL,
  cantidad        INT           NOT NULL,
  precio_unitario DECIMAL(12,2) NOT NULL,           -- copia del precio al agregar: un cambio posterior no altera el pedido
  subtotal        DECIMAL(12,2) NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_detalle_pedido_producto (pedido_id, producto_id),
  CONSTRAINT fk_detalle_pedido   FOREIGN KEY (pedido_id)   REFERENCES pedido (id),
  CONSTRAINT fk_detalle_producto FOREIGN KEY (producto_id) REFERENCES producto (id),
  CONSTRAINT ck_detalle_cantidad CHECK (cantidad > 0)
) ENGINE=InnoDB;
