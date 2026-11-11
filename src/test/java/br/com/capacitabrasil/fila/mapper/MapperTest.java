package br.com.capacitabrasil.fila.mapper;

import br.com.capacitabrasil.fila.dto.response.AtendimentoResponse;
import br.com.capacitabrasil.fila.dto.response.FilaDetalheResponse;
import br.com.capacitabrasil.fila.dto.response.FilaResumoResponse;
import br.com.capacitabrasil.fila.dto.response.SenhaPainelResponse;
import br.com.capacitabrasil.fila.dto.response.SenhaResponse;
import br.com.capacitabrasil.fila.dto.response.UsuarioResponse;
import br.com.capacitabrasil.fila.entity.Atendimento;
import br.com.capacitabrasil.fila.entity.Fila;
import br.com.capacitabrasil.fila.entity.Senha;
import br.com.capacitabrasil.fila.entity.Usuario;
import br.com.capacitabrasil.fila.entity.enums.PerfilUsuario;
import br.com.capacitabrasil.fila.entity.enums.StatusSenha;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class MapperTest {

    private final SenhaMapper senhaMapper = Mappers.getMapper(SenhaMapper.class);
    private final AtendimentoMapper atendimentoMapper = Mappers.getMapper(AtendimentoMapper.class);
    private final UsuarioMapper usuarioMapper = Mappers.getMapper(UsuarioMapper.class);
    private final FilaMapper filaMapper = Mappers.getMapper(FilaMapper.class);

    private Fila fila() {
        return Fila.builder().id(1L).nome("Geral").prefixo("A").descricao("Geral").ativa(true).build();
    }

    private Senha senha() {
        return Senha.builder()
                .id(10L)
                .fila(fila())
                .numero("A001")
                .sequencia(1)
                .status(StatusSenha.AGUARDANDO)
                .dataGeracao(LocalDateTime.of(2026, 11, 10, 9, 0))
                .build();
    }

    @Test
    void deveMapearSenhaComPosicao() {
        SenhaResponse response = senhaMapper.toResponse(senha(), 2, 1L);

        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.numero()).isEqualTo("A001");
        assertThat(response.status()).isEqualTo(StatusSenha.AGUARDANDO);
        assertThat(response.fila().nome()).isEqualTo("Geral");
        assertThat(response.posicao()).isEqualTo(2);
        assertThat(response.pessoasNaFrente()).isEqualTo(1L);
    }

    @Test
    void deveMapearSenhaParaPainel() {
        SenhaPainelResponse response = senhaMapper.toPainel(senha());

        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.numero()).isEqualTo("A001");
        assertThat(response.status()).isEqualTo(StatusSenha.AGUARDANDO);
    }

    @Test
    void deveMapearFila() {
        FilaResumoResponse resumo = filaMapper.toResumo(fila());
        FilaDetalheResponse detalhe = filaMapper.toDetalhe(fila());

        assertThat(resumo.prefixo()).isEqualTo("A");
        assertThat(detalhe.descricao()).isEqualTo("Geral");
        assertThat(detalhe.ativa()).isTrue();
    }

    @Test
    void deveMapearAtendimentoComDuracao() {
        Senha senha = senha();
        senha.setStatus(StatusSenha.ATENDIDA);
        Usuario atendente = Usuario.builder().id(5L).nome("Atendente").build();
        Atendimento atendimento = Atendimento.builder()
                .id(20L)
                .senha(senha)
                .atendente(atendente)
                .inicio(LocalDateTime.of(2026, 11, 10, 9, 0))
                .fim(LocalDateTime.of(2026, 11, 10, 9, 5))
                .guiche("3")
                .build();

        AtendimentoResponse response = atendimentoMapper.toResponse(atendimento);

        assertThat(response.senhaId()).isEqualTo(10L);
        assertThat(response.numeroSenha()).isEqualTo("A001");
        assertThat(response.statusSenha()).isEqualTo(StatusSenha.ATENDIDA);
        assertThat(response.atendenteNome()).isEqualTo("Atendente");
        assertThat(response.duracaoSegundos()).isEqualTo(300L);
    }

    @Test
    void deveMapearAtendimentoSemDuracao() {
        Atendimento atendimento = Atendimento.builder()
                .id(20L)
                .senha(senha())
                .build();

        AtendimentoResponse response = atendimentoMapper.toResponse(atendimento);

        assertThat(response.duracaoSegundos()).isNull();
        assertThat(response.atendenteId()).isNull();
    }

    @Test
    void deveMapearUsuario() {
        Usuario usuario = Usuario.builder()
                .id(1L)
                .nome("Admin")
                .email("admin@fila.local")
                .perfil(PerfilUsuario.ADMINISTRADOR)
                .ativo(true)
                .build();

        UsuarioResponse response = usuarioMapper.toResponse(usuario);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.email()).isEqualTo("admin@fila.local");
        assertThat(response.perfil()).isEqualTo(PerfilUsuario.ADMINISTRADOR);
    }
}
