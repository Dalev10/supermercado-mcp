package com.udea.supermercado.repository;

import com.udea.supermercado.model.Producto;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ProductoRepository extends JpaRepository<Producto, Long> {

    // Solo productos activos. La colacion utf8mb4_unicode_ci ya ignora mayusculas y tildes en el LIKE.
    @Query("""
            select p from Producto p join fetch p.categoria c
            where p.activo = true
              and (p.nombre like :patron or p.descripcion like :patron or c.nombre like :patron)
              and (:categoriaId is null or c.id = :categoriaId)
              and (:soloDisponibles = false or p.stock > 0)
            order by c.nombre, p.nombre
            """)
    List<Producto> buscar(String patron, Long categoriaId, boolean soloDisponibles);
}
