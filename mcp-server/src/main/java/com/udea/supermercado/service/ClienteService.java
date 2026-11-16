package com.udea.supermercado.service;

import com.udea.supermercado.dto.ClienteDto;
import com.udea.supermercado.exception.ReglaNegocioException;
import com.udea.supermercado.model.Cliente;
import com.udea.supermercado.repository.ClienteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ClienteService {
    private final ClienteRepository clientes;

    public ClienteService(ClienteRepository clientes) {
        this.clientes = clientes;
    }

    @Transactional(readOnly = true)
    public ClienteDto buscarPorTelefono(String telefono) {
        String normalizado = normalizarTelefono(telefono);
        return clientes.findByTelefono(normalizado).map(ClienteDto::de)
                .orElseThrow(() -> new ReglaNegocioException("No hay ningún cliente registrado con el teléfono "
                        + normalizado + ". Pide nombre y dirección y regístralo con registrarCliente."));
    }

    @Transactional
    public ClienteDto registrar(String nombre, String telefono, String direccion, String email) {
        String nombreLimpio = obligatorio(nombre, "nombre", 120);
        String direccionLimpia = obligatorio(direccion, "dirección", 255);
        String telefonoLimpio = normalizarTelefono(telefono);
        String emailLimpio = (email == null || email.isBlank()) ? null : email.trim();
        if (emailLimpio != null && (emailLimpio.length() > 120 || !emailLimpio.matches("[^@\\s]+@[^@\\s]+\\.[^@\\s]+"))) {
            throw new ReglaNegocioException("El correo '" + emailLimpio + "' no tiene un formato válido.");
        }
        if (clientes.existsByTelefono(telefonoLimpio)) {
            throw new ReglaNegocioException("Ya existe un cliente registrado con el teléfono " + telefonoLimpio
                    + ". Consúltalo con buscarCliente en lugar de registrarlo de nuevo.");
        }
        return ClienteDto.de(clientes.save(new Cliente(nombreLimpio, telefonoLimpio, direccionLimpia, emailLimpio)));
    }

    /** Quita espacios, guiones y parentesis; exige de 7 a 10 digitos (fijo o celular colombiano). */
    static String normalizarTelefono(String telefono) {
        String limpio = telefono == null ? "" : telefono.replaceAll("[\\s\\-()]", "");
        if (!limpio.matches("\\d{7,10}")) {
            throw new ReglaNegocioException("El teléfono '" + telefono
                    + "' no es válido: debe tener entre 7 y 10 dígitos, sin letras ni indicativo de país.");
        }
        return limpio;
    }

    private static String obligatorio(String valor, String campo, int maximo) {
        if (valor == null || valor.isBlank()) {
            throw new ReglaNegocioException("El campo " + campo + " es obligatorio.");
        }
        if (valor.trim().length() > maximo) {
            throw new ReglaNegocioException("El campo " + campo + " no puede superar " + maximo + " caracteres.");
        }
        return valor.trim();
    }
}
