package br.com.capacitabrasil.fila.mapper;

import br.com.capacitabrasil.fila.dto.response.FilaDetalheResponse;
import br.com.capacitabrasil.fila.dto.response.FilaResumoResponse;
import br.com.capacitabrasil.fila.entity.Fila;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface FilaMapper {

    FilaResumoResponse toResumo(Fila fila);

    FilaDetalheResponse toDetalhe(Fila fila);
}
