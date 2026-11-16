package com.udea.supermercado.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "pedido")
public class Pedido {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;

    // VARCHAR explicito: sin esto Hibernate espera un ENUM nativo de MySQL y ddl-auto=validate falla
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    private EstadoPedido estado = EstadoPedido.BORRADOR;

    private String direccionEntrega;
    private BigDecimal subtotal = BigDecimal.ZERO;
    private BigDecimal costoEnvio = BigDecimal.ZERO;
    private BigDecimal total = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    private MetodoPago metodoPago;

    private LocalDateTime creadoEn = LocalDateTime.now();
    private LocalDateTime confirmadoEn;

    @Version
    private Long version;

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DetallePedido> detalles = new ArrayList<>();

    protected Pedido() {
    }

    public Pedido(Cliente cliente, String direccionEntrega) {
        this.cliente = cliente;
        this.direccionEntrega = direccionEntrega;
    }

    public Optional<DetallePedido> lineaDe(Long productoId) {
        return detalles.stream().filter(d -> d.getProducto().getId().equals(productoId)).findFirst();
    }

    /** Agrega el producto o, si ya esta en el pedido, suma la cantidad a su linea. */
    public DetallePedido agregar(Producto producto, int cantidad) {
        Optional<DetallePedido> existente = lineaDe(producto.getId());
        if (existente.isPresent()) {
            existente.get().cambiarCantidad(existente.get().getCantidad() + cantidad);
            return existente.get();
        }
        DetallePedido linea = new DetallePedido(this, producto, cantidad);
        detalles.add(linea);
        return linea;
    }

    public boolean quitar(Long productoId) {
        return detalles.removeIf(d -> d.getProducto().getId().equals(productoId));
    }

    /** El envio solo se cobra si el pedido tiene productos. */
    public void recalcular(BigDecimal tarifaEnvio) {
        subtotal = detalles.stream().map(DetallePedido::getSubtotal).reduce(BigDecimal.ZERO, BigDecimal::add);
        costoEnvio = detalles.isEmpty() ? BigDecimal.ZERO : tarifaEnvio;
        total = subtotal.add(costoEnvio);
    }

    public void confirmar(MetodoPago metodo) {
        estado = EstadoPedido.CONFIRMADO;
        metodoPago = metodo;
        confirmadoEn = LocalDateTime.now();
    }

    public void cancelar() {
        estado = EstadoPedido.CANCELADO;
    }

    public Long getId() { return id; }
    public Cliente getCliente() { return cliente; }
    public EstadoPedido getEstado() { return estado; }
    public String getDireccionEntrega() { return direccionEntrega; }
    public BigDecimal getSubtotal() { return subtotal; }
    public BigDecimal getCostoEnvio() { return costoEnvio; }
    public BigDecimal getTotal() { return total; }
    public MetodoPago getMetodoPago() { return metodoPago; }
    public LocalDateTime getCreadoEn() { return creadoEn; }
    public LocalDateTime getConfirmadoEn() { return confirmadoEn; }
    public List<DetallePedido> getDetalles() { return detalles; }
}
