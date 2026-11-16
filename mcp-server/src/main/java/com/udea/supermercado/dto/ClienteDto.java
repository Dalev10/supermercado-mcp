package com.udea.supermercado.dto;

import com.udea.supermercado.model.Cliente;

public record ClienteDto(Long id, String nombre, String telefono, String direccion, String email) {
    public static ClienteDto de(Cliente c) {
        return new ClienteDto(c.getId(), c.getNombre(), c.getTelefono(), c.getDireccion(), c.getEmail());
    }
}
