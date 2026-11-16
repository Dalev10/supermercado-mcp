package com.udea.supermercado.model;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.math.BigDecimal;

@Entity
@Table(name = "producto")
public class Producto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nombre;
    private String descripcion;
    private BigDecimal precio;
    private int stock;
    private String unidad;
    private boolean activo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "categoria_id")
    private Categoria categoria;

    // Bloqueo optimista: dos confirmaciones simultaneas no pueden vender la misma unidad
    @Version
    private Long version;

    protected Producto() {
    }

    public Producto(String nombre, String unidad, BigDecimal precio, int stock, boolean activo, Categoria categoria) {
        this.nombre = nombre;
        this.unidad = unidad;
        this.precio = precio;
        this.stock = stock;
        this.activo = activo;
        this.categoria = categoria;
    }

    public void descontarStock(int cantidad) {
        stock -= cantidad;
    }

    public void devolverStock(int cantidad) {
        stock += cantidad;
    }

    /** Nombre con presentacion, como lo ve el cliente: "Leche entera 1 L". */
    public String getNombreCompleto() {
        return nombre + " " + unidad;
    }

    public Long getId() { return id; }
    public String getNombre() { return nombre; }
    public String getDescripcion() { return descripcion; }
    public BigDecimal getPrecio() { return precio; }
    public int getStock() { return stock; }
    public String getUnidad() { return unidad; }
    public boolean isActivo() { return activo; }
    public Categoria getCategoria() { return categoria; }
}
