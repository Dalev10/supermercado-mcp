package com.udea.supermercado.dto;

import com.udea.supermercado.model.Producto;
import java.math.BigDecimal;

public record ProductoDto(Long id, String nombre, String unidad, String descripcion, String categoria,
        BigDecimal precio, int stock, boolean disponible) {
    public static ProductoDto de(Producto p) {
        return new ProductoDto(p.getId(), p.getNombre(), p.getUnidad(), p.getDescripcion(),
                p.getCategoria().getNombre(), p.getPrecio(), p.getStock(), p.getStock() > 0);
    }
}
