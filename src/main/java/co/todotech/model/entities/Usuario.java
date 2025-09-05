package co.todotech.model.entities;

import co.todotech.model.enums.TipoUsuario;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "usuario")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "nombre", nullable = false, length = 120)
    private String nombre;

    @Column(name = "cedula", nullable = false, length = 15, unique = true)
    private String cedula;

    @Column(name = "correo", nullable = false, length = 150, unique = true)
    private String correo;

    @Column(name = "telefono", length = 10)
    private String telefono;

    @Column(name = "usuario", nullable = false, length = 60)
    private String nombreUsuario;

    @Column(name = "contrasena", nullable = false, length = 255)
    private String contrasena;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_usuario", nullable = false, length = 30)
    private TipoUsuario tipoUsuario;

    @CreationTimestamp
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "estado", nullable = false)
    @Builder.Default  // ← ESTA ES LA SOLUCIÓN
    private boolean estado = false;
}