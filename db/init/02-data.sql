-- Datos de prueba. Todos son sinteticos: productos genericos y clientes ficticios.
-- Algunos productos tienen stock 0 o muy bajo y uno esta inactivo, para probar las validaciones.
SET NAMES utf8mb4;

INSERT INTO categoria (id, nombre, descripcion) VALUES
  (1, 'Lácteos y huevos',   'Leche, quesos, yogures, mantequilla y huevos'),
  (2, 'Panadería',          'Pan, arepas y tostadas'),
  (3, 'Frutas y verduras',  'Productos frescos vendidos por kilo o por unidad'),
  (4, 'Carnes y pollo',     'Res, cerdo, pollo y carnes frías'),
  (5, 'Despensa',           'Granos, aceite, azúcar, café y pastas'),
  (6, 'Bebidas',            'Agua, jugos, gaseosas y bebidas calientes');

INSERT INTO producto (nombre, descripcion, precio, stock, unidad, activo, categoria_id) VALUES
  -- Lácteos y huevos
  ('Leche entera',           'Leche entera pasteurizada en bolsa',       4800.00,  40, '1 L',          b'1', 1),
  ('Leche deslactosada',     'Leche semidescremada sin lactosa',         5600.00,  25, '1 L',          b'1', 1),
  ('Yogur de fresa',         'Yogur bebible con sabor a fresa',          9500.00,  15, '1 L',          b'1', 1),
  ('Queso campesino',        'Queso fresco tipo campesino',             13500.00,  12, '500 g',        b'1', 1),
  ('Mantequilla',            'Mantequilla con sal en barra',             9800.00,   2, '250 g',        b'1', 1),
  ('Huevos AA',              'Docena de huevos rojos tipo AA',           9600.00,  30, '12 unidades',  b'1', 1),
  -- Panadería
  ('Pan tajado blanco',      'Pan de molde blanco tajado',               6900.00,  20, '450 g',        b'1', 2),
  ('Pan tajado integral',    'Pan de molde integral tajado',             7800.00,   0, '450 g',        b'1', 2),
  ('Arepas de maíz',         'Arepas blancas de maíz listas para asar',  4500.00,  18, '5 unidades',   b'1', 2),
  ('Tostadas integrales',    'Tostadas horneadas de trigo integral',     5200.00,  14, '200 g',        b'1', 2),
  -- Frutas y verduras
  ('Banano',                 'Banano criollo maduro',                    3200.00,  50, '1 kg',         b'1', 3),
  ('Manzana roja',           'Manzana roja importada',                   8900.00,  20, '1 kg',         b'1', 3),
  ('Tomate chonto',          'Tomate chonto para ensalada y guiso',      4600.00,  35, '1 kg',         b'1', 3),
  ('Papa pastusa',           'Papa pastusa lavada',                      3800.00,  60, '1 kg',         b'1', 3),
  ('Cebolla cabezona',       'Cebolla cabezona blanca',                  4200.00,   3, '1 kg',         b'1', 3),
  ('Aguacate hass',          'Aguacate hass en punto de maduración',     3500.00,   0, '1 unidad',     b'1', 3),
  -- Carnes y pollo
  ('Pechuga de pollo',       'Pechuga de pollo sin piel',               19800.00,  15, '1 kg',         b'1', 4),
  ('Carne molida de res',    'Carne de res molida magra',               14500.00,  10, '500 g',        b'1', 4),
  ('Lomo de cerdo',          'Lomo de cerdo en porciones',              23900.00,   8, '1 kg',         b'1', 4),
  ('Salchichas',             'Salchichas tipo ranchera',                15900.00,  12, '480 g',        b'1', 4),
  -- Despensa
  ('Arroz blanco',           'Arroz blanco de grano largo',              4900.00,  80, '1 kg',         b'1', 5),
  ('Arroz integral',         'Arroz integral de grano largo',            6800.00,  20, '1 kg',         b'1', 5),
  ('Fríjol cargamanto',      'Fríjol cargamanto rojo seco',              7200.00,  25, '500 g',        b'1', 5),
  ('Lenteja',                'Lenteja seca seleccionada',                4300.00,  30, '500 g',        b'1', 5),
  ('Aceite vegetal',         'Aceite vegetal de soya',                  12900.00,  22, '1 L',          b'1', 5),
  ('Azúcar blanca',          'Azúcar blanca refinada',                   5100.00,  40, '1 kg',         b'1', 5),
  ('Café molido',            'Café colombiano molido de tueste medio',  21500.00,  16, '500 g',        b'1', 5),
  ('Pasta espagueti',        'Pasta larga de trigo tipo espagueti',      3900.00,  45, '500 g',        b'1', 5),
  -- Bebidas
  ('Agua sin gas',           'Agua potable tratada en botella',          2200.00, 100, '600 ml',       b'1', 6),
  ('Jugo de naranja',        'Jugo de naranja pasteurizado',             7900.00,  18, '1 L',          b'1', 6),
  ('Gaseosa cola',           'Bebida gaseosa sabor cola',                6500.00,   1, '1.5 L',        b'1', 6),
  ('Chocolate de mesa',      'Chocolate en pastillas (descontinuado)',   8900.00,  10, '250 g',        b'0', 6);

INSERT INTO cliente (nombre, telefono, direccion, email) VALUES
  ('Laura Gómez Restrepo', '3001234567', 'Calle 50 # 40-20, apto 301, Medellín', 'laura.gomez@example.com'),
  ('Carlos Pérez Mejía',   '3109876543', 'Carrera 70 # 30-15, Medellín',         NULL);
