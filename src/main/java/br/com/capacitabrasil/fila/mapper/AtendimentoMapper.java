package br.com.capacitabrasil.fila.mapper;

import br.com.capacitabrasil.fila.dto.response.AtendimentoResponse;
import br.com.capacitabrasil.fila.entity.Atendimento;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AtendimentoMapper {

    @Mapping(target = "senhaId", source = "senha.id")
    @Mapping(target = "numeroSenha", source = "senha.numero")
    @Mapping(target = "statusSenha", source = "senha.status")
    @Mapping(target = "atendenteId", source = "atendente.id")
    @Mapping(target = "atendenteNome", source = "atendente.nome")
    @Mapping(target = "duracaoSegundos", expression = "java(atendimento.duracaoSegundos())")
    AtendimentoResponse toResponse(Atendimento atendimento);
}
