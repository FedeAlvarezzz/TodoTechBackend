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

    @OneToMany(mappedBy = "orden", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
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

    // ✅ CORREGIDO: Método para calcular los totales automáticamente
    @PrePersist
    @PreUpdate
    public void calcularTotales() {
        // ✅ CORREGIDO: Asegurar que el descuento tenga valor por defecto
        if (this.descuento == null) {
            this.descuento = 0.0;
        }

        // Calcular subtotal
        this.subtotal = this.productos.stream()
                .mapToDouble(detalle -> {
                    if (detalle.getSubtotal() == null || detalle.getSubtotal() == 0.0) {
                        detalle.calcularSubtotal();
                    }
                    return detalle.getSubtotal() != null ? detalle.getSubtotal() : 0.0;
                })
                .sum();

        // ✅ CORREGIDO: Solo ajustar descuento si hay productos
        // Si no hay productos (subtotal = 0), mantener el descuento original
        if (this.subtotal > 0 && this.descuento > this.subtotal) {
            this.descuento = this.subtotal;
        }

        double baseImponible = this.subtotal - this.descuento;
        if (baseImponible < 0) {
            baseImponible = 0.0;
        }

        this.impuestos = baseImponible * 0.02;
        this.total = baseImponible + this.impuestos;

        if (this.total < 0) {
            this.total = 0.0;
        }
    }
    // ✅ CORREGIDO: Método para aplicar descuento por porcentaje
    public void aplicarDescuentoPorcentaje(Double porcentajeDescuento) {
        if (porcentajeDescuento < 0 || porcentajeDescuento > 100) {
            throw new IllegalArgumentException("El porcentaje de descuento debe estar entre 0 y 100");
        }

        // Recalcular subtotal primero para asegurar que esté actualizado
        this.calcularTotales();

        // Calcular monto del descuento
        this.descuento = this.subtotal * (porcentajeDescuento / 100);

        // Recalcular totales con el nuevo descuento
        this.calcularTotales();
    }

    // ✅ NUEVO: Método para quitar descuento
    public void quitarDescuento() {
        this.descuento = 0.0;
        this.calcularTotales();
    }

    // ✅ NUEVO: Método para obtener el monto después del descuento
    public Double getMontoDespuesDescuento() {
        return this.subtotal - (this.descuento != null ? this.descuento : 0.0);
    }

    // ✅ NUEVO: Método para obtener el porcentaje de descuento aplicado
    public Double getPorcentajeDescuento() {
        if (this.subtotal == null || this.subtotal == 0.0 || this.descuento == null || this.descuento == 0.0) {
            return 0.0;
        }
        return (this.descuento / this.subtotal) * 100;
    }

    // Método helper para agregar detalle
    public void agregarDetalle(DetalleOrden detalle) {
        detalle.setOrden(this);
        this.productos.add(detalle);
        // ✅ CORREGIDO: Recalcular totales después de agregar detalle
        this.calcularTotales();
    }

    // Método helper para remover detalle
    public void removerDetalle(DetalleOrden detalle) {
        detalle.setOrden(null);
        this.productos.remove(detalle);
        // ✅ CORREGIDO: Recalcular totales después de remover detalle
        this.calcularTotales();
    }

    // ✅ NUEVO: Método para validar si la orden puede ser modificada
    public boolean puedeSerModificada() {
        return this.estado != EstadoOrden.CERRADA &&
                this.estado != EstadoOrden.ENTREGADA &&
                this.estado != EstadoOrden.PAGADA;
    }

    // ✅ NUEVO: Método para validar si se puede aplicar descuento
    public boolean puedeAplicarDescuento() {
        return this.estado == EstadoOrden.PENDIENTE ||
                this.estado == EstadoOrden.AGREGANDOPRODUCTOS ||
                this.estado == EstadoOrden.DISPONIBLEPARAPAGO;
    }

    @Override
    public String toString() {
        return "Orden{" +
                "id=" + id +
                ", numeroOrden='" + numeroOrden + '\'' +
                ", subtotal=" + subtotal +
                ", descuento=" + descuento +
                ", impuestos=" + impuestos +
                ", total=" + total +
                ", estado=" + estado +
                '}';
    }
}