package com.udea.supermercado.dto;

import com.udea.supermercado.model.Pedido;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

// Las fechas van como texto ISO para no depender de como serialice LocalDateTime cada cliente MCP
public record PedidoDto(Long id, String estado, Long clienteId, String cliente, String direccionEntrega,
        List<DetalleDto> productos, BigDecimal subtotal, BigDecimal costoEnvio, BigDecimal total,
        String metodoPago, String creadoEn, String confirmadoEn) {
    public static PedidoDto de(Pedido p) {
        return new PedidoDto(p.getId(), p.getEstado().name(), p.getCliente().getId(), p.getCliente().getNombre(),
                p.getDireccionEntrega(), p.getDetalles().stream().map(DetalleDto::de).toList(),
                p.getSubtotal(), p.getCostoEnvio(), p.getTotal(),
                p.getMetodoPago() == null ? null : p.getMetodoPago().name(),
                texto(p.getCreadoEn()), texto(p.getConfirmadoEn()));
    }

    private static String texto(LocalDateTime fecha) {
        return fecha == null ? null : fecha.truncatedTo(ChronoUnit.SECONDS).toString();
    }
}
