package com.udea.supermercado.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.udea.supermercado.dto.PedidoDto;
import com.udea.supermercado.exception.ReglaNegocioException;
import com.udea.supermercado.model.Categoria;
import com.udea.supermercado.model.Cliente;
import com.udea.supermercado.model.EstadoPedido;
import com.udea.supermercado.model.Pedido;
import com.udea.supermercado.model.Producto;
import com.udea.supermercado.repository.ClienteRepository;
import com.udea.supermercado.repository.PedidoRepository;
import com.udea.supermercado.repository.ProductoRepository;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

/** Una prueba por cada regla de negocio de los pedidos, sin base de datos ni MCP. */
@ExtendWith(MockitoExtension.class)
class PedidoServiceTest {
    private static final BigDecimal ENVIO = new BigDecimal("5000");
    private static final long PEDIDO_ID = 10L;

    @Mock PedidoRepository pedidos;
    @Mock ClienteRepository clientes;
    @Mock ProductoRepository productos;

    PedidoService servicio;
    Cliente cliente;
    Pedido pedido;
    Producto leche;   // id 1, $4.800, stock 40
    Producto huevos;  // id 2, $9.600, stock 30

    @BeforeEach
    void preparar() {
        servicio = new PedidoService(pedidos, clientes, productos, ENVIO);
        cliente = conId(new Cliente("Laura Gómez", "3001234567", "Calle 50 # 40-20", null), 7L);
        pedido = conId(new Pedido(cliente, cliente.getDireccion()), PEDIDO_ID);
        leche = producto(1L, "Leche entera", "1 L", "4800.00", 40, true);
        huevos = producto(2L, "Huevos AA", "12 unidades", "9600.00", 30, true);
    }

    // --- crear ---

    @Test
    void crearUsaLaDireccionDelClienteSiNoSeIndicaOtra() {
        when(clientes.findById(7L)).thenReturn(Optional.of(cliente));
        when(pedidos.save(org.mockito.ArgumentMatchers.any(Pedido.class))).thenAnswer(i -> conId(i.getArgument(0), 11L));

        PedidoDto dto = servicio.crear(7L, "  ");

        assertThat(dto.estado()).isEqualTo("BORRADOR");
        assertThat(dto.direccionEntrega()).isEqualTo("Calle 50 # 40-20");
        assertThat(dto.total()).isEqualByComparingTo("0");
    }

    @Test
    void crearFallaSiElClienteNoExiste() {
        when(clientes.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> servicio.crear(99L, null))
                .isInstanceOf(ReglaNegocioException.class).hasMessageContaining("No existe el cliente");
    }

    // --- agregar y quitar ---

    @Test
    void agregarCopiaElPrecioYCalculaTotalesConEnvio() {
        pedidoExiste();
        productoExiste(leche);

        PedidoDto dto = servicio.agregarProducto(PEDIDO_ID, 1L, 2);

        assertThat(dto.productos()).hasSize(1);
        assertThat(dto.productos().get(0).precioUnitario()).isEqualByComparingTo("4800");
        assertThat(dto.subtotal()).isEqualByComparingTo("9600");
        assertThat(dto.costoEnvio()).isEqualByComparingTo("5000");
        assertThat(dto.total()).isEqualByComparingTo("14600");
        assertThat(leche.getStock()).as("agregar no descuenta stock").isEqualTo(40);
    }

    @Test
    void agregarElMismoProductoSumaLaCantidadEnUnaSolaLinea() {
        pedidoExiste();
        productoExiste(leche);

        servicio.agregarProducto(PEDIDO_ID, 1L, 2);
        PedidoDto dto = servicio.agregarProducto(PEDIDO_ID, 1L, 3);

        assertThat(dto.productos()).hasSize(1);
        assertThat(dto.productos().get(0).cantidad()).isEqualTo(5);
        assertThat(dto.subtotal()).isEqualByComparingTo("24000");
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1})
    void agregarFallaSiLaCantidadNoEsMayorQueCero(int cantidad) {
        assertThatThrownBy(() -> servicio.agregarProducto(PEDIDO_ID, 1L, cantidad))
                .isInstanceOf(ReglaNegocioException.class).hasMessageContaining("mayor que cero");
    }

    @Test
    void agregarFallaSiElProductoNoExiste() {
        pedidoExiste();
        when(productos.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> servicio.agregarProducto(PEDIDO_ID, 99L, 1))
                .isInstanceOf(ReglaNegocioException.class).hasMessageContaining("No existe un producto disponible");
    }

    @Test
    void agregarFallaSiElProductoEstaInactivo() {
        pedidoExiste();
        productoExiste(producto(3L, "Chocolate de mesa", "250 g", "8900.00", 10, false));

        assertThatThrownBy(() -> servicio.agregarProducto(PEDIDO_ID, 3L, 1))
                .isInstanceOf(ReglaNegocioException.class).hasMessageContaining("No existe un producto disponible");
        assertThat(pedido.getDetalles()).isEmpty();
    }

    @ParameterizedTest
    @EnumSource(value = EstadoPedido.class, names = "BORRADOR", mode = EnumSource.Mode.EXCLUDE)
    void noSeModificaUnPedidoQueNoEstaEnBorrador(EstadoPedido estado) {
        pedido.agregar(leche, 1);
        ReflectionTestUtils.setField(pedido, "estado", estado);
        pedidoExiste();

        assertThatThrownBy(() -> servicio.agregarProducto(PEDIDO_ID, 2L, 1))
                .isInstanceOf(ReglaNegocioException.class).hasMessageContaining("ya no se puede modificar");
        assertThatThrownBy(() -> servicio.quitarProducto(PEDIDO_ID, 1L))
                .isInstanceOf(ReglaNegocioException.class).hasMessageContaining("ya no se puede modificar");
        assertThatThrownBy(() -> servicio.confirmar(PEDIDO_ID, "EFECTIVO"))
                .isInstanceOf(ReglaNegocioException.class).hasMessageContaining("ya no se puede modificar");
        assertThat(pedido.getDetalles()).hasSize(1);
    }

    @Test
    void quitarEliminaLaLineaYRecalcula() {
        pedido.agregar(leche, 2);
        pedido.agregar(huevos, 1);
        pedido.recalcular(ENVIO);
        pedidoExiste();

        PedidoDto dto = servicio.quitarProducto(PEDIDO_ID, 2L);

        assertThat(dto.productos()).extracting("productoId").containsExactly(1L);
        assertThat(dto.total()).isEqualByComparingTo("14600");
    }

    @Test
    void quitarFallaSiElProductoNoEstaEnElPedido() {
        pedidoExiste();

        assertThatThrownBy(() -> servicio.quitarProducto(PEDIDO_ID, 2L))
                .isInstanceOf(ReglaNegocioException.class).hasMessageContaining("no contiene el producto");
    }

    // --- confirmar ---

    @Test
    void confirmarDescuentaStockYCalculaElTotal() {
        pedido.agregar(leche, 2);
        pedido.agregar(huevos, 1);
        pedidoExiste();

        PedidoDto dto = servicio.confirmar(PEDIDO_ID, " efectivo ");

        assertThat(dto.estado()).isEqualTo("CONFIRMADO");
        assertThat(dto.metodoPago()).isEqualTo("EFECTIVO");
        assertThat(dto.confirmadoEn()).isNotNull();
        assertThat(dto.subtotal()).isEqualByComparingTo("19200");
        assertThat(dto.costoEnvio()).isEqualByComparingTo("5000");
        assertThat(dto.total()).isEqualByComparingTo("24200");
        assertThat(leche.getStock()).isEqualTo(38);
        assertThat(huevos.getStock()).isEqualTo(29);
    }

    @Test
    void confirmarFallaSiElPedidoEstaVacio() {
        pedidoExiste();

        assertThatThrownBy(() -> servicio.confirmar(PEDIDO_ID, "EFECTIVO"))
                .isInstanceOf(ReglaNegocioException.class).hasMessageContaining("está vacío");
        assertThat(pedido.getEstado()).isEqualTo(EstadoPedido.BORRADOR);
    }

    @Test
    void confirmarConStockInsuficienteNoDescuentaNingunaLinea() {
        Producto mantequilla = producto(5L, "Mantequilla", "250 g", "9800.00", 2, true);
        pedido.agregar(leche, 2);          // hay stock
        pedido.agregar(mantequilla, 5);    // no hay stock
        pedidoExiste();

        assertThatThrownBy(() -> servicio.confirmar(PEDIDO_ID, "TARJETA"))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("Stock insuficiente de Mantequilla 250 g: se pidieron 5 y hay 2.");
        assertThat(leche.getStock()).as("la linea valida tampoco se descuenta").isEqualTo(40);
        assertThat(mantequilla.getStock()).isEqualTo(2);
        assertThat(pedido.getEstado()).isEqualTo(EstadoPedido.BORRADOR);
    }

    @Test
    void confirmarFallaSiUnProductoDelPedidoFueDesactivado() {
        pedido.agregar(leche, 1);
        ReflectionTestUtils.setField(leche, "activo", false);
        pedidoExiste();

        assertThatThrownBy(() -> servicio.confirmar(PEDIDO_ID, "EFECTIVO"))
                .isInstanceOf(ReglaNegocioException.class).hasMessageContaining("ya no está disponible");
        assertThat(leche.getStock()).isEqualTo(40);
    }

    @Test
    void confirmarFallaConMetodoDePagoDesconocido() {
        pedido.agregar(leche, 1);
        pedidoExiste();

        assertThatThrownBy(() -> servicio.confirmar(PEDIDO_ID, "bitcoin"))
                .isInstanceOf(ReglaNegocioException.class).hasMessageContaining("EFECTIVO, TARJETA o TRANSFERENCIA");
        assertThat(leche.getStock()).isEqualTo(40);
        assertThat(pedido.getEstado()).isEqualTo(EstadoPedido.BORRADOR);
    }

    // --- cancelar ---

    @Test
    void cancelarUnPedidoConfirmadoDevuelveElStock() {
        pedido.agregar(leche, 2);
        pedidoExiste();
        servicio.confirmar(PEDIDO_ID, "EFECTIVO");
        assertThat(leche.getStock()).isEqualTo(38);

        PedidoDto dto = servicio.cancelar(PEDIDO_ID);

        assertThat(dto.estado()).isEqualTo("CANCELADO");
        assertThat(leche.getStock()).isEqualTo(40);
    }

    @Test
    void cancelarUnBorradorNoTocaElStock() {
        pedido.agregar(leche, 2);
        pedidoExiste();

        PedidoDto dto = servicio.cancelar(PEDIDO_ID);

        assertThat(dto.estado()).isEqualTo("CANCELADO");
        assertThat(leche.getStock()).isEqualTo(40);
    }

    @ParameterizedTest
    @EnumSource(value = EstadoPedido.class, names = {"EN_CAMINO", "ENTREGADO", "CANCELADO"})
    void noSeCancelaUnPedidoEnCaminoEntregadoOYaCancelado(EstadoPedido estado) {
        pedido.agregar(leche, 2);
        ReflectionTestUtils.setField(pedido, "estado", estado);
        pedidoExiste();

        assertThatThrownBy(() -> servicio.cancelar(PEDIDO_ID))
                .isInstanceOf(ReglaNegocioException.class).hasMessageContaining("ya no se puede cancelar");
        assertThat(leche.getStock()).as("no se devuelve stock dos veces").isEqualTo(40);
        assertThat(pedido.getEstado()).isEqualTo(estado);
    }

    @Test
    void consultarFallaSiElPedidoNoExiste() {
        when(pedidos.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> servicio.consultar(99L))
                .isInstanceOf(ReglaNegocioException.class).hasMessageContaining("No existe el pedido");
    }

    // --- apoyo ---

    private void pedidoExiste() {
        when(pedidos.findById(PEDIDO_ID)).thenReturn(Optional.of(pedido));
    }

    private void productoExiste(Producto p) {
        when(productos.findById(p.getId())).thenReturn(Optional.of(p));
    }

    private static Producto producto(long id, String nombre, String unidad, String precio, int stock, boolean activo) {
        return conId(new Producto(nombre, unidad, new BigDecimal(precio), stock, activo,
                new Categoria("Prueba", null)), id);
    }

    /** Las entidades reciben su id de la base de datos; en pruebas unitarias se asigna por reflexion. */
    private static <T> T conId(T entidad, long id) {
        ReflectionTestUtils.setField(entidad, "id", id);
        return entidad;
    }
}
