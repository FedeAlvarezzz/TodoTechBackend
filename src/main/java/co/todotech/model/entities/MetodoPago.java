package co.todotech.model.entities;

import co.todotech.model.enums.TipoMetodo;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "metodo_pago")
public class MetodoPago {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "metodo", nullable = false, length = 50)
    private TipoMetodo metodo;

    @Column(name = "descripcion", length = 255)
    private String descripcion;

    @Column(name = "aprobacion", nullable = false)
    private Boolean aprobacion;

    @Column(name = "comision", nullable = false)
    private Double comision;
}