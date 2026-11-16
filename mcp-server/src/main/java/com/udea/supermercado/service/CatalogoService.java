package com.udea.supermercado.service;

import com.udea.supermercado.dto.CategoriaDto;
import com.udea.supermercado.dto.ProductoDto;
import com.udea.supermercado.repository.CategoriaRepository;
import com.udea.supermercado.repository.ProductoRepository;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class CatalogoService {
    private final CategoriaRepository categorias;
    private final ProductoRepository productos;

    public CatalogoService(CategoriaRepository categorias, ProductoRepository productos) {
        this.categorias = categorias;
        this.productos = productos;
    }

    public List<CategoriaDto> listarCategorias() {
        return categorias.findAll(Sort.by("nombre")).stream().map(CategoriaDto::de).toList();
    }

    public List<ProductoDto> buscarProductos(String texto, Long categoriaId, Boolean soloDisponibles) {
        String patron = "%" + (texto == null ? "" : texto.trim()) + "%";
        return productos.buscar(patron, categoriaId, Boolean.TRUE.equals(soloDisponibles))
                .stream().map(ProductoDto::de).toList();
    }
}
