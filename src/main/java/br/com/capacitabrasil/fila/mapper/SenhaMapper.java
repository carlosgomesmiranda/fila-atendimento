package br.com.capacitabrasil.fila.mapper;

import br.com.capacitabrasil.fila.dto.response.FilaResumoResponse;
import br.com.capacitabrasil.fila.dto.response.SenhaPainelResponse;
import br.com.capacitabrasil.fila.dto.response.SenhaResponse;
import br.com.capacitabrasil.fila.entity.Fila;
import br.com.capacitabrasil.fila.entity.Senha;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SenhaMapper {

    @Mapping(target = "fila", source = "senha.fila")
    @Mapping(target = "posicao", source = "posicao")
    @Mapping(target = "pessoasNaFrente", source = "pessoasNaFrente")
    SenhaResponse toResponse(Senha senha, Integer posicao, Long pessoasNaFrente);

    FilaResumoResponse toResumo(Fila fila);

    SenhaPainelResponse toPainel(Senha senha);
}
