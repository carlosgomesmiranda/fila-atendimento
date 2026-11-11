package br.com.capacitabrasil.fila.mapper;

import br.com.capacitabrasil.fila.dto.response.UsuarioResponse;
import br.com.capacitabrasil.fila.entity.Usuario;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UsuarioMapper {

    UsuarioResponse toResponse(Usuario usuario);
}
