package com.udea.supermercado.model;

import com.udea.supermercado.exception.ReglaNegocioException;

public enum MetodoPago {
    EFECTIVO, TARJETA, TRANSFERENCIA;

    /** Convierte el texto que envia el modelo; no se confia en que llegue bien escrito. */
    public static MetodoPago de(String texto) {
        try {
            return valueOf(texto.trim().toUpperCase());
        } catch (RuntimeException e) {
            throw new ReglaNegocioException("Método de pago no válido: '" + texto
                    + "'. Los valores permitidos son EFECTIVO, TARJETA o TRANSFERENCIA.");
        }
    }
}
