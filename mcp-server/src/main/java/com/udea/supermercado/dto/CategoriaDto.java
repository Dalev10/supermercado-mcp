package com.udea.supermercado.dto;

import com.udea.supermercado.model.Categoria;

public record CategoriaDto(Long id, String nombre, String descripcion) {
    public static CategoriaDto de(Categoria c) {
        return new CategoriaDto(c.getId(), c.getNombre(), c.getDescripcion());
    }
}
