package com.udea.supermercado.model;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "detalle_pedido")
public class DetallePedido {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pedido_id")
    private Pedido pedido;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "producto_id")
    private Producto producto;

    private int cantidad;
    // Copia del precio al agregar: un cambio de precio posterior no altera el pedido
    private BigDecimal precioUnitario;
    private BigDecimal subtotal;

    protected DetallePedido() {
    }

    DetallePedido(Pedido pedido, Producto producto, int cantidad) {
        this.pedido = pedido;
        this.producto = producto;
        this.precioUnitario = producto.getPrecio();
        cambiarCantidad(cantidad);
    }

    void cambiarCantidad(int nuevaCantidad) {
        this.cantidad = nuevaCantidad;
        this.subtotal = precioUnitario.multiply(BigDecimal.valueOf(nuevaCantidad));
    }

    public Producto getProducto() { return producto; }
    public int getCantidad() { return cantidad; }
    public BigDecimal getPrecioUnitario() { return precioUnitario; }
    public BigDecimal getSubtotal() { return subtotal; }
}
