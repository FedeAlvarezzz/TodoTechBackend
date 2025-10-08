package co.todotech.mapper;

import co.todotech.model.dto.usuario.UsuarioDto;
import co.todotech.model.entities.Usuario;
import org.mapstruct.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface UsuarioMapper {

    // Método para obtener el logger (sustituye al campo privado)
    default Logger getLog() {
        return LoggerFactory.getLogger(UsuarioMapper.class);
    }

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "fechaCreacion", ignore = true)
    @Mapping(target = "estado", ignore = true)
    Usuario toEntity(UsuarioDto usuarioDto);

    // Mapeo normal SIN contraseña
    @Mapping(target = "contrasena", ignore = true)
    UsuarioDto toDto(Usuario usuario);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "fechaCreacion", ignore = true)
    @Mapping(target = "contrasena", ignore = true)
    void updateUsuarioFromDto(UsuarioDto usuarioDto, @MappingTarget Usuario usuario);

    // Método seguro que NO usa toDto() para evitar problemas
    default UsuarioDto toDtoSafe(Usuario usuario) {
        Logger log = getLog();
        log.info("=== INICIANDO MAPEO SEGURO ===");
        log.info("Mapeando usuario: {}", usuario.getNombre());
        log.info("Contraseña original en Entity: {}", usuario.getContrasena());

        UsuarioDto dto = new UsuarioDto();
        dto.setId(usuario.getId());
        log.info("ID seteado: {}", dto.getId());

        dto.setNombre(usuario.getNombre());
        log.info("Nombre seteado: {}", dto.getNombre());

        dto.setCedula(usuario.getCedula());
        log.info("Cédula seteada: {}", dto.getCedula());

        dto.setCorreo(usuario.getCorreo());
        log.info("Correo seteado: {}", dto.getCorreo());

        dto.setTelefono(usuario.getTelefono());
        log.info("Teléfono seteado: {}", dto.getTelefono());

        dto.setNombreUsuario(usuario.getNombreUsuario());
        log.info("NombreUsuario seteado: {}", dto.getNombreUsuario());

        dto.setContrasena("••••••••"); // Siempre mostrar esto
        log.info("Contraseña en DTO después de setear: {}", dto.getContrasena());



        dto.setTipoUsuario(usuario.getTipoUsuario());
        log.info("TipoUsuario seteado: {}", dto.getTipoUsuario());

        dto.setFechaCreacion(usuario.getFechaCreacion());
        log.info("FechaCreacion seteada: {}", dto.getFechaCreacion());

        dto.setEstado(usuario.isEstado());
        log.info("Estado seteado: {}", dto.getEstado());

        log.info("=== FINALIZANDO MAPEO SEGURO ===");
        log.info("DTO final - Contraseña: {}", dto.getContrasena());

        return dto;
    }

    // Método adicional para debuggear el mapeo normal
    default UsuarioDto toDtoWithLogs(Usuario usuario) {
        Logger log = getLog();
        log.info("=== INICIANDO MAPEO NORMAL ===");
        log.info("Usuario a mapear: {}", usuario.getNombre());

        UsuarioDto dto = toDto(usuario);

        log.info("DTO después de toDto() - Contraseña: {}", dto.getContrasena());
        log.info("=== FINALIZANDO MAPEO NORMAL ===");

        return dto;
    }
}