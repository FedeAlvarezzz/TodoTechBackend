package co.todotech.model.entities;

import co.todotech.model.enums.EstadoOrden;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "orden_venta")
public class Orden {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long id;

    @Column(name = "numero_orden", nullable = false, unique = true)
    private String numeroOrden;

    @Column(name = "fecha", nullable = false)
    private LocalDateTime fecha;

    @ManyToOne
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @ManyToOne
    @JoinColumn(name = "vendedor_id", nullable = false)
    private Usuario vendedor;

    @OneToMany(mappedBy = "orden", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<DetalleOrden> productos = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    private EstadoOrden estado;

    @Column(name = "subtotal", nullable = false)
    private Double subtotal;

    @Column(name = "descuento")
    private Double descuento;

    @Column(name = "impuestos", nullable = false)
    private Double impuestos;

    @Column(name = "total", nullable = false)
    private Double total;

    @Column(name = "observaciones", length = 1000)
    private String observaciones;

    // Método para calcular los totales automáticamente
    @PrePersist
    @PreUpdate
    public void calcularTotales() {
        // Calcular subtotal sumando los subtotales de los detalles
        this.subtotal = this.productos.stream()
                .mapToDouble(DetalleOrden::getSubtotal)
                .sum();

        // Calcular impuestos (ejemplo: 19% sobre el subtotal - descuento)
        double baseImponible = this.subtotal - (this.descuento != null ? this.descuento : 0.0);
        this.impuestos = baseImponible * 0.19;

        // Calcular total
        this.total = baseImponible + this.impuestos;
    }

    // Método helper para agregar detalle
    public void agregarDetalle(DetalleOrden detalle) {
        detalle.setOrden(this);
        this.productos.add(detalle);
    }

    // Método helper para remover detalle
    public void removerDetalle(DetalleOrden detalle) {
        detalle.setOrden(null);
        this.productos.remove(detalle);
    }
}