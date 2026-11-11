package br.com.capacitabrasil.fila.service;

import br.com.capacitabrasil.fila.dto.request.FinalizarAtendimentoRequest;
import br.com.capacitabrasil.fila.dto.request.IniciarAtendimentoRequest;
import br.com.capacitabrasil.fila.dto.response.AtendimentoResponse;
import br.com.capacitabrasil.fila.dto.response.PageResponse;
import br.com.capacitabrasil.fila.entity.Atendimento;
import br.com.capacitabrasil.fila.entity.Fila;
import br.com.capacitabrasil.fila.entity.Senha;
import br.com.capacitabrasil.fila.entity.Usuario;
import br.com.capacitabrasil.fila.entity.enums.StatusSenha;
import br.com.capacitabrasil.fila.exception.RegraNegocioException;
import br.com.capacitabrasil.fila.exception.ResourceNotFoundException;
import br.com.capacitabrasil.fila.mapper.AtendimentoMapper;
import br.com.capacitabrasil.fila.repository.AtendimentoRepository;
import br.com.capacitabrasil.fila.repository.SenhaRepository;
import br.com.capacitabrasil.fila.security.SecurityContextService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AtendimentoServiceTest {

    private static final Clock CLOCK = Clock.fixed(
            Instant.parse("2026-11-10T12:00:00Z"), ZoneId.of("America/Sao_Paulo"));
    private static final LocalDateTime AGORA = LocalDateTime.of(2026, 11, 10, 9, 0);

    @Mock
    private AtendimentoRepository atendimentoRepository;
    @Mock
    private SenhaRepository senhaRepository;
    @Mock
    private SenhaEventoService senhaEventoService;
    @Mock
    private SecurityContextService securityContextService;
    @Mock
    private AtendimentoMapper atendimentoMapper;

    private AtendimentoService atendimentoService;

    @BeforeEach
    void setUp() {
        atendimentoService = new AtendimentoService(
                atendimentoRepository,
                senhaRepository,
                senhaEventoService,
                securityContextService,
                atendimentoMapper,
                CLOCK);
    }

    private Senha senha(StatusSenha status) {
        Fila fila = Fila.builder().id(1L).nome("Geral").prefixo("A").ativa(true).build();
        return Senha.builder()
                .id(10L)
                .fila(fila)
                .numero("A001")
                .sequencia(1)
                .dataReferencia(LocalDate.of(2026, 11, 10))
                .dataGeracao(LocalDateTime.of(2026, 11, 10, 8, 0))
                .status(status)
                .build();
    }

    private Atendimento atendimento(Senha senha) {
        return Atendimento.builder()
                .id(55L)
                .senha(senha)
                .atendente(Usuario.builder().id(2L).nome("Atendente").build())
                .guiche("3")
                .build();
    }

    @Test
    void deveIniciarAtendimentoDeSenhaChamada() {
        Senha senha = senha(StatusSenha.CHAMADA);
        Atendimento atendimento = atendimento(senha);
        AtendimentoResponse esperada = new AtendimentoResponse(55L, 10L, "A001",
                StatusSenha.EM_ATENDIMENTO, AGORA, null, "4", null, 2L, "Atendente", null);

        when(senhaRepository.findById(10L)).thenReturn(Optional.of(senha));
        when(atendimentoRepository.findBySenhaId(10L)).thenReturn(Optional.of(atendimento));
        when(securityContextService.usuarioAtual()).thenReturn(Optional.empty());
        when(atendimentoMapper.toResponse(atendimento)).thenReturn(esperada);

        AtendimentoResponse response = atendimentoService.iniciar(
                new IniciarAtendimentoRequest(10L, "4"));

        assertThat(response).isSameAs(esperada);
        assertThat(senha.getStatus()).isEqualTo(StatusSenha.EM_ATENDIMENTO);
        assertThat(senha.getHoraInicioAtendimento()).isEqualTo(AGORA);
        assertThat(atendimento.getInicio()).isEqualTo(AGORA);
        assertThat(atendimento.getGuiche()).isEqualTo("4");
        verify(senhaEventoService).registrar(senha, StatusSenha.CHAMADA,
                StatusSenha.EM_ATENDIMENTO, null, "Atendimento iniciado");
    }

    @Test
    void naoDeveIniciarAtendimentoSemSenhaChamada() {
        Senha senha = senha(StatusSenha.AGUARDANDO);
        when(senhaRepository.findById(10L)).thenReturn(Optional.of(senha));
        when(atendimentoRepository.findBySenhaId(10L)).thenReturn(Optional.of(atendimento(senha)));

        assertThatThrownBy(() -> atendimentoService.iniciar(new IniciarAtendimentoRequest(10L, null)))
                .isInstanceOf(RegraNegocioException.class);
    }

    @Test
    void naoDeveIniciarAtendimentoSemRegistroDeAtendimento() {
        Senha senha = senha(StatusSenha.CHAMADA);
        when(senhaRepository.findById(10L)).thenReturn(Optional.of(senha));
        when(atendimentoRepository.findBySenhaId(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> atendimentoService.iniciar(new IniciarAtendimentoRequest(10L, null)))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("Chame a senha primeiro");
    }

    @Test
    void deveFalharAoIniciarComSenhaInexistente() {
        when(senhaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> atendimentoService.iniciar(new IniciarAtendimentoRequest(99L, null)))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void deveFinalizarAtendimentoEmAndamento() {
        Senha senha = senha(StatusSenha.EM_ATENDIMENTO);
        Atendimento atendimento = atendimento(senha);
        atendimento.setInicio(AGORA.minusMinutes(10));
        AtendimentoResponse esperada = new AtendimentoResponse(55L, 10L, "A001",
                StatusSenha.ATENDIDA, atendimento.getInicio(), AGORA, "3", "Concluido",
                2L, "Atendente", 600L);

        when(atendimentoRepository.findById(55L)).thenReturn(Optional.of(atendimento));
        when(securityContextService.usuarioAtual()).thenReturn(Optional.empty());
        when(atendimentoMapper.toResponse(atendimento)).thenReturn(esperada);

        AtendimentoResponse response = atendimentoService.finalizar(
                new FinalizarAtendimentoRequest(55L, "Concluido"));

        assertThat(response).isSameAs(esperada);
        assertThat(senha.getStatus()).isEqualTo(StatusSenha.ATENDIDA);
        assertThat(senha.getHoraFimAtendimento()).isEqualTo(AGORA);
        assertThat(atendimento.getFim()).isEqualTo(AGORA);
        assertThat(atendimento.getObservacao()).isEqualTo("Concluido");
        verify(senhaEventoService).registrar(senha, StatusSenha.EM_ATENDIMENTO,
                StatusSenha.ATENDIDA, null, "Atendimento finalizado");
    }

    @Test
    void naoDeveFinalizarAtendimentoNaoIniciado() {
        Senha senha = senha(StatusSenha.CHAMADA);
        Atendimento atendimento = atendimento(senha);
        when(atendimentoRepository.findById(55L)).thenReturn(Optional.of(atendimento));

        assertThatThrownBy(() -> atendimentoService.finalizar(
                new FinalizarAtendimentoRequest(55L, null)))
                .isInstanceOf(RegraNegocioException.class);
    }

    @Test
    void deveFalharAoFinalizarAtendimentoInexistente() {
        when(atendimentoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> atendimentoService.finalizar(
                new FinalizarAtendimentoRequest(99L, null)))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void deveListarAtendimentosPaginados() {
        Pageable pageable = PageRequest.of(0, 20);
        Senha senha = senha(StatusSenha.ATENDIDA);
        Atendimento atendimento = atendimento(senha);
        AtendimentoResponse response = new AtendimentoResponse(55L, 10L, "A001",
                StatusSenha.ATENDIDA, null, null, "3", null, 2L, "Atendente", null);

        when(atendimentoRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(atendimento), pageable, 1));
        when(atendimentoMapper.toResponse(atendimento)).thenReturn(response);

        PageResponse<AtendimentoResponse> pagina = atendimentoService.listar(
                LocalDate.of(2026, 11, 10), LocalDate.of(2026, 11, 10),
                "3", 2L, StatusSenha.ATENDIDA, pageable);

        assertThat(pagina.conteudo()).containsExactly(response);
        assertThat(pagina.totalElementos()).isEqualTo(1);
    }

    @Test
    void deveBuscarAtendimentoPorId() {
        Atendimento atendimento = atendimento(senha(StatusSenha.ATENDIDA));
        AtendimentoResponse response = new AtendimentoResponse(55L, 10L, "A001",
                StatusSenha.ATENDIDA, null, null, "3", null, 2L, "Atendente", null);

        when(atendimentoRepository.findById(55L)).thenReturn(Optional.of(atendimento));
        when(atendimentoMapper.toResponse(atendimento)).thenReturn(response);

        assertThat(atendimentoService.buscarPorId(55L)).isSameAs(response);
    }

    @Test
    void deveFalharAoBuscarAtendimentoInexistente() {
        when(atendimentoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> atendimentoService.buscarPorId(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
