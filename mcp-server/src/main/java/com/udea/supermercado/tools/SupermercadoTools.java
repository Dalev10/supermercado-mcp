package com.udea.supermercado.tools;

import com.udea.supermercado.dto.CategoriaDto;
import com.udea.supermercado.dto.ClienteDto;
import com.udea.supermercado.dto.PedidoDto;
import com.udea.supermercado.dto.ProductoDto;
import com.udea.supermercado.service.CatalogoService;
import com.udea.supermercado.service.ClienteService;
import com.udea.supermercado.service.PedidoService;
import java.util.List;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

/**
 * Adaptador MCP: cada metodo @Tool se publica como herramienta y delega en un servicio de dominio.
 * Aqui no hay reglas de negocio; las descripciones son lo que el modelo lee para decidir que llamar.
 */
@Component
public class SupermercadoTools {
    private final CatalogoService catalogo;
    private final ClienteService clientes;
    private final PedidoService pedidos;

    public SupermercadoTools(CatalogoService catalogo, ClienteService clientes, PedidoService pedidos) {
        this.catalogo = catalogo;
        this.clientes = clientes;
        this.pedidos = pedidos;
    }

    @Tool(description = "Lista las categorías de productos del supermercado con su id, nombre y descripción. "
            + "Úsala cuando el usuario pregunte qué tipos de productos hay, o para obtener el id de una categoría "
            + "antes de filtrar con buscarProductos.")
    public List<CategoriaDto> listarCategorias() {
        return catalogo.listarCategorias();
    }

    @Tool(description = "Busca productos del catálogo y devuelve id, nombre, unidad de presentación, categoría, "
            + "precio en pesos colombianos y stock disponible. Úsala siempre antes de ofrecer un producto, decir un "
            + "precio o agregar algo a un pedido: es la única fuente válida de ids, precios y disponibilidad. "
            + "Sin filtros devuelve todo el catálogo. Devuelve una lista vacía si nada coincide.")
    public List<ProductoDto> buscarProductos(
            @ToolParam(required = false, description = "Palabra a buscar en el nombre, la descripción o la categoría "
                    + "del producto, en singular y sin cantidades. Ejemplos: 'leche', 'arroz', 'huevo'") String texto,
            @ToolParam(required = false, description = "Id de una categoría obtenido con listarCategorias, para "
                    + "limitar la búsqueda a esa categoría") Long categoriaId,
            @ToolParam(required = false, description = "true para devolver solo productos con stock mayor que cero; "
                    + "por defecto se incluyen también los agotados") Boolean soloDisponibles) {
        return catalogo.buscarProductos(texto, categoriaId, soloDisponibles);
    }

    @Tool(description = "Busca un cliente registrado por su número de teléfono y devuelve su id, nombre, dirección "
            + "y correo. Úsala al iniciar un pedido para identificar al usuario. Si no existe ningún cliente con ese "
            + "teléfono devuelve un error: en ese caso ofrece registrarlo con registrarCliente.")
    public ClienteDto buscarCliente(
            @ToolParam(description = "Teléfono del cliente, de 7 a 10 dígitos, por ejemplo 3001234567") String telefono) {
        return clientes.buscarPorTelefono(telefono);
    }

    @Tool(description = "Registra un cliente nuevo y devuelve sus datos con el id asignado. Úsala solo cuando "
            + "buscarCliente no encontró el teléfono y el usuario ya dio su nombre y su dirección. "
            + "Falla si el teléfono ya está registrado o si algún dato no es válido.")
    public ClienteDto registrarCliente(
            @ToolParam(description = "Nombre completo del cliente") String nombre,
            @ToolParam(description = "Teléfono de 7 a 10 dígitos, sin indicativo de país. Debe ser único") String telefono,
            @ToolParam(description = "Dirección de entrega habitual, con ciudad") String direccion,
            @ToolParam(required = false, description = "Correo electrónico; es opcional") String email) {
        return clientes.registrar(nombre, telefono, direccion, email);
    }

    @Tool(description = "Crea un pedido vacío en estado BORRADOR para un cliente y devuelve el pedido con su id. "
            + "Es el primer paso para armar un pedido: después se agregan productos con agregarProductoAPedido. "
            + "Crear el pedido no reserva ni descuenta inventario.")
    public PedidoDto crearPedido(
            @ToolParam(description = "Id del cliente, obtenido con buscarCliente o registrarCliente") Long clienteId,
            @ToolParam(required = false, description = "Dirección de entrega de este pedido; si se omite se usa la "
                    + "dirección registrada del cliente") String direccionEntrega) {
        return pedidos.crear(clienteId, direccionEntrega);
    }

    @Tool(description = "Agrega un producto a un pedido en BORRADOR y devuelve el pedido actualizado con subtotal, "
            + "costo de envío y total. Si el producto ya estaba en el pedido, suma la cantidad a la existente. "
            + "No descuenta inventario: el stock se valida al confirmar. Falla si el pedido ya no está en BORRADOR.")
    public PedidoDto agregarProductoAPedido(
            @ToolParam(description = "Id del pedido en BORRADOR") Long pedidoId,
            @ToolParam(description = "Id del producto, obtenido con buscarProductos") Long productoId,
            @ToolParam(description = "Cantidad de unidades a agregar; entero mayor que cero") Integer cantidad) {
        return pedidos.agregarProducto(pedidoId, productoId, cantidad);
    }

    @Tool(description = "Quita por completo un producto de un pedido en BORRADOR y devuelve el pedido actualizado. "
            + "Para reducir la cantidad en lugar de eliminarlo, quítalo y agrégalo de nuevo con la cantidad correcta. "
            + "Falla si el producto no está en el pedido o si el pedido ya no está en BORRADOR.")
    public PedidoDto quitarProductoDePedido(
            @ToolParam(description = "Id del pedido en BORRADOR") Long pedidoId,
            @ToolParam(description = "Id del producto que se quiere quitar") Long productoId) {
        return pedidos.quitarProducto(pedidoId, productoId);
    }

    @Tool(description = "Consulta un pedido por su id y devuelve su estado (BORRADOR, CONFIRMADO, EN_CAMINO, "
            + "ENTREGADO o CANCELADO), el cliente, la dirección de entrega, los productos con cantidades y precios, "
            + "el subtotal, el costo de envío y el total. Úsala para mostrar el resumen antes de confirmar "
            + "o cuando el usuario pregunte por un pedido.")
    public PedidoDto consultarPedido(
            @ToolParam(description = "Id del pedido") Long pedidoId) {
        return pedidos.consultar(pedidoId);
    }

    @Tool(description = "Confirma un pedido que está en estado BORRADOR: valida el stock de cada producto, "
            + "lo descuenta del inventario y calcula el total con el costo de envío. "
            + "Úsala solo después de que el usuario haya confirmado explícitamente el resumen del pedido. "
            + "Si falta stock de algún producto falla sin descontar nada e indica qué producto y cuánto hay.")
    public PedidoDto confirmarPedido(
            @ToolParam(description = "Id del pedido en borrador") Long pedidoId,
            @ToolParam(description = "Método de pago: EFECTIVO, TARJETA o TRANSFERENCIA") String metodoPago) {
        return pedidos.confirmar(pedidoId, metodoPago);
    }

    @Tool(description = "Cancela un pedido en estado BORRADOR o CONFIRMADO; si estaba CONFIRMADO devuelve los "
            + "productos al inventario. Úsala solo después de que el usuario haya confirmado explícitamente que "
            + "quiere cancelar. Falla si el pedido ya está EN_CAMINO, ENTREGADO o CANCELADO.")
    public PedidoDto cancelarPedido(
            @ToolParam(description = "Id del pedido que se quiere cancelar") Long pedidoId) {
        return pedidos.cancelar(pedidoId);
    }
}
