package com.udea.supermercado.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "cliente")
public class Cliente {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nombre;
    private String telefono;
    private String direccion;
    private String email;
    private LocalDateTime creadoEn = LocalDateTime.now();

    protected Cliente() {
    }

    public Cliente(String nombre, String telefono, String direccion, String email) {
        this.nombre = nombre;
        this.telefono = telefono;
        this.direccion = direccion;
        this.email = email;
    }

    public Long getId() { return id; }
    public String getNombre() { return nombre; }
    public String getTelefono() { return telefono; }
    public String getDireccion() { return direccion; }
    public String getEmail() { return email; }
    public LocalDateTime getCreadoEn() { return creadoEn; }
}
