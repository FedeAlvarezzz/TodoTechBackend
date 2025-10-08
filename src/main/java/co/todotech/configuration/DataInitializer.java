package co.todotech.configuration;

import co.todotech.model.entities.Usuario;
import co.todotech.model.enums.TipoUsuario;
import co.todotech.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${admin.username:admin}")
    private String adminUsername;

    @Value("${admin.password:admin123}")
    private String adminPassword;

    @Value("${admin.email:admin@todotech.com}")
    private String adminEmail;

    @Value("${admin.name:Administrador}")
    private String adminName;

    @Value("${admin.cedula:1234567890}")
    private String adminCedula;

    @Value("${admin.enabled:true}")
    private boolean adminEnabled;

    @Override
    public void run(String... args) throws Exception {
        if (usuarioRepository.findByNombreUsuario(adminUsername).isEmpty()) {
            Usuario admin = new Usuario();
            admin.setNombreUsuario(adminUsername);
            admin.setContrasena(passwordEncoder.encode(adminPassword));
            admin.setNombre(adminName);
            admin.setTipoUsuario(TipoUsuario.ADMIN);
            admin.setEstado(adminEnabled);
            admin.setCedula(adminCedula);
            admin.setCorreo(adminEmail);

            usuarioRepository.save(admin);
            System.out.println("Usuario admin creado: " + adminUsername);

            // Log de seguridad (no mostrar password)
            System.out.println("✅ Admin user created with username: " + adminUsername);

            if (!adminEnabled) {
                System.out.println("⚠️  ADMIN USER IS DISABLED");
            }
        } else {
            System.out.println("✅ Usuario admin ya existe: " + adminUsername);
        }
    }
}