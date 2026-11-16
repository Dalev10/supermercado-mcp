package com.udea.supermercado.service;

import com.udea.supermercado.dto.PedidoDto;
import com.udea.supermercado.exception.ReglaNegocioException;
import com.udea.supermercado.model.Cliente;
import com.udea.supermercado.model.DetallePedido;
import com.udea.supermercado.model.EstadoPedido;
import com.udea.supermercado.model.MetodoPago;
import com.udea.supermercado.model.Pedido;
import com.udea.supermercado.model.Producto;
import com.udea.supermercado.repository.ClienteRepository;
import com.udea.supermercado.repository.PedidoRepository;
import com.udea.supermercado.repository.ProductoRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Reglas de negocio de los pedidos. Cada metodo publico es una transaccion. */
@Service
public class PedidoService {
    static final int CANTIDAD_MAXIMA = 999;

    private final PedidoRepository pedidos;
    private final ClienteRepository clientes;
    private final ProductoRepository productos;
    private final BigDecimal costoEnvio;

    public PedidoService(PedidoRepository pedidos, ClienteRepository clientes, ProductoRepository productos,
            @Value("${supermercado.costo-envio}") BigDecimal costoEnvio) {
        this.pedidos = pedidos;
        this.clientes = clientes;
        this.productos = productos;
        this.costoEnvio = costoEnvio;
    }

    @Transactional
    public PedidoDto crear(Long clienteId, String direccionEntrega) {
        Cliente cliente = clientes.findById(id(clienteId, "clienteId"))
                .orElseThrow(() -> new ReglaNegocioException("No existe el cliente con id " + clienteId
                        + ". Búscalo con buscarCliente o regístralo con registrarCliente."));
        boolean sinDireccion = direccionEntrega == null || direccionEntrega.isBlank();
        String direccion = sinDireccion ? cliente.getDireccion() : direccionEntrega.trim();
        if (direccion.length() > 255) {
            throw new ReglaNegocioException("La dirección de entrega no puede superar 255 caracteres.");
        }
        return PedidoDto.de(pedidos.save(new Pedido(cliente, direccion)));
    }

    @Transactional
    public PedidoDto agregarProducto(Long pedidoId, Long productoId, Integer cantidad) {
        if (cantidad == null || cantidad <= 0) {
            throw new ReglaNegocioException("La cantidad debe ser mayor que cero.");
        }
        Pedido pedido = borrador(pedidoId);
        Producto producto = productos.findById(id(productoId, "productoId")).filter(Producto::isActivo)
                .orElseThrow(() -> new ReglaNegocioException("No existe un producto disponible con id " + productoId
                        + ". Usa buscarProductos para obtener los ids válidos."));
        int yaEnPedido = pedido.lineaDe(productoId).map(DetallePedido::getCantidad).orElse(0);
        if (cantidad > CANTIDAD_MAXIMA - yaEnPedido) {
            throw new ReglaNegocioException("No se pueden pedir más de " + CANTIDAD_MAXIMA
                    + " unidades de un mismo producto por pedido.");
        }
        pedido.agregar(producto, cantidad);
        pedido.recalcular(costoEnvio);
        return PedidoDto.de(pedido);
    }

    @Transactional
    public PedidoDto quitarProducto(Long pedidoId, Long productoId) {
        Pedido pedido = borrador(pedidoId);
        if (!pedido.quitar(id(productoId, "productoId"))) {
            throw new ReglaNegocioException("El pedido " + pedidoId + " no contiene el producto con id " + productoId
                    + ". Revisa su contenido con consultarPedido.");
        }
        pedido.recalcular(costoEnvio);
        return PedidoDto.de(pedido);
    }

    @Transactional(readOnly = true)
    public PedidoDto consultar(Long pedidoId) {
        return PedidoDto.de(buscar(pedidoId));
    }

    @Transactional
    public PedidoDto confirmar(Long pedidoId, String metodoPago) {
        Pedido pedido = borrador(pedidoId);
        if (pedido.getDetalles().isEmpty()) {
            throw new ReglaNegocioException("El pedido " + pedidoId
                    + " está vacío: agrega al menos un producto antes de confirmarlo.");
        }
        MetodoPago metodo = MetodoPago.de(metodoPago);
        // Primero se validan todas las lineas y solo despues se descuenta: si una falla, no se toca ningun stock
        List<String> problemas = pedido.getDetalles().stream()
                .map(PedidoService::problemaDeStock).filter(Objects::nonNull).toList();
        if (!problemas.isEmpty()) {
            throw new ReglaNegocioException("No se puede confirmar el pedido " + pedidoId + ". "
                    + String.join(" ", problemas) + " Reduce la cantidad o quita el producto y vuelve a confirmar.");
        }
        pedido.getDetalles().forEach(d -> d.getProducto().descontarStock(d.getCantidad()));
        pedido.recalcular(costoEnvio);
        pedido.confirmar(metodo);
        return PedidoDto.de(pedido);
    }

    @Transactional
    public PedidoDto cancelar(Long pedidoId) {
        Pedido pedido = buscar(pedidoId);
        switch (pedido.getEstado()) {
            case BORRADOR -> { } // nunca desconto stock
            case CONFIRMADO -> pedido.getDetalles().forEach(d -> d.getProducto().devolverStock(d.getCantidad()));
            default -> throw new ReglaNegocioException("El pedido " + pedidoId + " está en estado "
                    + pedido.getEstado() + " y ya no se puede cancelar.");
        }
        pedido.cancelar();
        return PedidoDto.de(pedido);
    }

    private static String problemaDeStock(DetallePedido d) {
        Producto p = d.getProducto();
        if (!p.isActivo()) {
            return p.getNombreCompleto() + " ya no está disponible.";
        }
        if (p.getStock() < d.getCantidad()) {
            return "Stock insuficiente de " + p.getNombreCompleto() + ": se pidieron " + d.getCantidad()
                    + " y hay " + p.getStock() + ".";
        }
        return null;
    }

    private Pedido buscar(Long pedidoId) {
        return pedidos.findById(id(pedidoId, "pedidoId"))
                .orElseThrow(() -> new ReglaNegocioException("No existe el pedido con id " + pedidoId + "."));
    }

    private Pedido borrador(Long pedidoId) {
        Pedido pedido = buscar(pedidoId);
        if (pedido.getEstado() != EstadoPedido.BORRADOR) {
            throw new ReglaNegocioException("El pedido " + pedidoId + " está en estado " + pedido.getEstado()
                    + " y ya no se puede modificar. Solo se modifican pedidos en BORRADOR.");
        }
        return pedido;
    }

    private static Long id(Long valor, String nombre) {
        if (valor == null) {
            throw new ReglaNegocioException("Falta el parámetro obligatorio " + nombre + ".");
        }
        return valor;
    }
}
