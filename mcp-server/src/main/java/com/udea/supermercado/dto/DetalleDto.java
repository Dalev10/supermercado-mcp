package com.udea.supermercado.dto;

import com.udea.supermercado.model.DetallePedido;
import java.math.BigDecimal;

public record DetalleDto(Long productoId, String producto, String unidad, int cantidad,
        BigDecimal precioUnitario, BigDecimal subtotal) {
    public static DetalleDto de(DetallePedido d) {
        return new DetalleDto(d.getProducto().getId(), d.getProducto().getNombre(), d.getProducto().getUnidad(),
                d.getCantidad(), d.getPrecioUnitario(), d.getSubtotal());
    }
}
