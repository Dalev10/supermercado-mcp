package com.udea.supermercado.exception;

/**
 * Error de negocio o de validacion. Su mensaje llega al modelo como resultado de error de la
 * herramienta, asi que debe estar en español y decir que paso y que se puede hacer.
 */
public class ReglaNegocioException extends RuntimeException {
    public ReglaNegocioException(String mensaje) {
        super(mensaje);
    }
}
