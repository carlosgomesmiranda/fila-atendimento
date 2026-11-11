package br.com.capacitabrasil.fila.service;

import br.com.capacitabrasil.fila.dto.request.CriarSenhaRequest;
import br.com.capacitabrasil.fila.dto.response.PageResponse;
import br.com.capacitabrasil.fila.dto.response.SenhaResponse;
import br.com.capacitabrasil.fila.entity.Fila;
import br.com.capacitabrasil.fila.entity.Senha;
import br.com.capacitabrasil.fila.entity.Usuario;
import br.com.capacitabrasil.fila.entity.enums.PerfilUsuario;
import br.com.capacitabrasil.fila.entity.enums.StatusSenha;
import br.com.capacitabrasil.fila.exception.RegraNegocioException;
import br.com.capacitabrasil.fila.exception.ResourceNotFoundException;
import br.com.capacitabrasil.fila.mapper.SenhaMapper;
import br.com.capacitabrasil.fila.repository.FilaRepository;
import br.com.capacitabrasil.fila.repository.SenhaRepository;
import br.com.capacitabrasil.fila.security.SecurityContextService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SenhaServiceTest {

    private static final Clock CLOCK = Clock.fixed(
            Instant.parse("2026-11-10T12:00:00Z"), ZoneId.of("America/Sao_Paulo"));

    @Mock
    private SenhaRepository senhaRepository;
    @Mock
    private FilaRepository filaRepository;
    @Mock
    private SenhaMapper senhaMapper;
    @Mock
    private SenhaEventoService senhaEventoService;
    @Mock
    private SecurityContextService securityContextService;
    @Mock
    private FilaResolver filaResolver;

    private SenhaService senhaService;

    @BeforeEach
    void setUp() {
        senhaService = new SenhaService(
                senhaRepository,
                filaRepository,
                senhaMapper,
                senhaEventoService,
                securityContextService,
                filaResolver,
                CLOCK);
    }

    private Fila fila(boolean ativa) {
        return Fila.builder()
                .id(1L)
                .nome("Geral")
                .prefixo("A")
                .ativa(ativa)
                .ultimaSequencia(0)
                .build();
    }

    private Senha senha(StatusSenha status) {
        return Senha.builder()
                .id(10L)
                .fila(fila(true))
                .numero("A001")
                .sequencia(1)
                .dataReferencia(LocalDate.of(2026, 11, 10))
                .dataGeracao(LocalDateTime.of(2026, 11, 10, 8, 0))
                .status(status)
                .build();
    }

    @Test
    void deveGerarSenhaComNumeracaoDiaria() {
        Fila fila = fila(true);
        Usuario usuario = Usuario.builder().id(1L).email("admin@fila.local")
                .perfil(PerfilUsuario.ADMINISTRADOR).build();
        SenhaResponse esperada = new SenhaResponse(10L, "A001", StatusSenha.AGUARDANDO, null,
                LocalDateTime.of(2026, 11, 10, 9, 0), null, null, null, 3, 2L);

        when(filaResolver.resolver(null)).thenReturn(fila);
        when(filaRepository.findByIdParaAtualizacao(1L)).thenReturn(Optional.of(fila));
        when(senhaRepository.save(any(Senha.class))).thenAnswer(invocation -> {
            Senha s = invocation.getArgument(0);
            s.setId(10L);
            return s;
        });
        when(securityContextService.usuarioAtual()).thenReturn(Optional.of(usuario));
        when(senhaRepository.contarAntes(eq(1L), eq(StatusSenha.AGUARDANDO), eq(10L))).thenReturn(2L);
        when(senhaMapper.toResponse(any(Senha.class), eq(3), eq(2L))).thenReturn(esperada);

        SenhaResponse response = senhaService.gerar(new CriarSenhaRequest(null));

        assertThat(response).isSameAs(esperada);
        assertThat(fila.getUltimaSequencia()).isEqualTo(1);
        assertThat(fila.getDataUltimaSequencia()).isEqualTo(LocalDate.of(2026, 11, 10));

        ArgumentCaptor<Senha> captor = ArgumentCaptor.forClass(Senha.class);
        verify(senhaRepository).save(captor.capture());
        assertThat(captor.getValue().getNumero()).isEqualTo("A001");
        assertThat(captor.getValue().getStatus()).isEqualTo(StatusSenha.AGUARDANDO);
        assertThat(captor.getValue().getDataGeracao()).isEqualTo(LocalDateTime.of(2026, 11, 10, 9, 0));
        verify(senhaEventoService).registrar(any(Senha.class), isNull(),
                eq(StatusSenha.AGUARDANDO), eq(usuario), eq("Senha gerada"));
    }

    @Test
    void deveGerarSenhaEmFilaEspecifica() {
        Fila fila = fila(true);
        when(filaResolver.resolver(1L)).thenReturn(fila);
        when(filaRepository.findByIdParaAtualizacao(1L)).thenReturn(Optional.of(fila));
        when(senhaRepository.save(any(Senha.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(securityContextService.usuarioAtual()).thenReturn(Optional.empty());
        when(senhaRepository.contarAntes(any(), any(), any())).thenReturn(0L);
        when(senhaMapper.toResponse(any(Senha.class), eq(1), eq(0L)))
                .thenReturn(new SenhaResponse(1L, "A001", StatusSenha.AGUARDANDO, null,
                        null, null, null, null, 1, 0L));

        senhaService.gerar(new CriarSenhaRequest(1L));

        verify(senhaRepository).save(any(Senha.class));
    }

    @Test
    void naoDeveGerarSenhaEmFilaInativa() {
        Fila fila = fila(false);
        when(filaResolver.resolver(null)).thenReturn(fila);
        when(filaRepository.findByIdParaAtualizacao(1L)).thenReturn(Optional.of(fila));

        assertThatThrownBy(() -> senhaService.gerar(new CriarSenhaRequest(null)))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("inativa");
    }

    @Test
    void deveFalharQuandoFilaNaoExisteAoBloquear() {
        when(filaResolver.resolver(null)).thenReturn(fila(true));
        when(filaRepository.findByIdParaAtualizacao(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> senhaService.gerar(new CriarSenhaRequest(null)))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Fila");
    }

    @Test
    void deveConsultarUltimaSenhaDoDia() {
        Fila fila = fila(true);
        Senha senha = senha(StatusSenha.AGUARDANDO);
        SenhaResponse esperada = new SenhaResponse(10L, "A001", StatusSenha.AGUARDANDO, null,
                null, null, null, null, null, null);

        when(filaResolver.resolver(null)).thenReturn(fila);
        when(senhaRepository.findTopByFilaIdAndDataReferenciaOrderBySequenciaDesc(
                1L, LocalDate.of(2026, 11, 10))).thenReturn(Optional.of(senha));
        when(senhaMapper.toResponse(senha, null, null)).thenReturn(esperada);

        assertThat(senhaService.ultima(null)).isSameAs(esperada);
    }

    @Test
    void deveFalharQuandoNaoHaSenhaNoDia() {
        when(filaResolver.resolver(null)).thenReturn(fila(true));
        when(senhaRepository.findTopByFilaIdAndDataReferenciaOrderBySequenciaDesc(
                1L, LocalDate.of(2026, 11, 10))).thenReturn(Optional.empty());

        assertThatThrownBy(() -> senhaService.ultima(null))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Nenhuma senha");
    }

    @Test
    void deveConsultarSenhaAguardandoComPosicao() {
        Senha senha = senha(StatusSenha.AGUARDANDO);
        SenhaResponse esperada = new SenhaResponse(10L, "A001", StatusSenha.AGUARDANDO, null,
                null, null, null, null, 5, 4L);

        when(senhaRepository.findById(10L)).thenReturn(Optional.of(senha));
        when(senhaRepository.contarAntes(eq(1L), eq(StatusSenha.AGUARDANDO), eq(10L))).thenReturn(4L);
        when(senhaMapper.toResponse(senha, 5, 4L)).thenReturn(esperada);

        assertThat(senhaService.buscarPorId(10L)).isSameAs(esperada);
    }

    @Test
    void deveConsultarSenhaFinalizadaSemPosicao() {
        Senha senha = senha(StatusSenha.ATENDIDA);
        SenhaResponse esperada = new SenhaResponse(10L, "A001", StatusSenha.ATENDIDA, null,
                null, null, null, null, null, null);

        when(senhaRepository.findById(10L)).thenReturn(Optional.of(senha));
        when(senhaMapper.toResponse(senha, null, null)).thenReturn(esperada);

        assertThat(senhaService.buscarPorId(10L)).isSameAs(esperada);
    }

    @Test
    void deveFalharAoConsultarSenhaInexistente() {
        when(senhaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> senhaService.buscarPorId(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void deveCancelarSenha() {
        Senha senha = senha(StatusSenha.AGUARDANDO);
        SenhaResponse esperada = new SenhaResponse(10L, "A001", StatusSenha.CANCELADA, null,
                null, null, null, null, null, null);

        when(senhaRepository.findById(10L)).thenReturn(Optional.of(senha));
        when(securityContextService.usuarioAtual()).thenReturn(Optional.empty());
        when(senhaMapper.toResponse(senha, null, null)).thenReturn(esperada);

        assertThat(senhaService.cancelar(10L)).isSameAs(esperada);
        assertThat(senha.getStatus()).isEqualTo(StatusSenha.CANCELADA);
        verify(senhaEventoService).registrar(senha, StatusSenha.AGUARDANDO,
                StatusSenha.CANCELADA, null, "Senha cancelada");
    }

    @Test
    void deveListarSenhasPaginadas() {
        Pageable pageable = PageRequest.of(0, 20);
        Senha senha = senha(StatusSenha.AGUARDANDO);
        SenhaResponse response = new SenhaResponse(10L, "A001", StatusSenha.AGUARDANDO, null,
                null, null, null, null, null, null);

        when(senhaRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(senha), pageable, 1));
        when(senhaMapper.toResponse(senha, null, null)).thenReturn(response);

        PageResponse<SenhaResponse> pagina = senhaService.listar(
                StatusSenha.AGUARDANDO, 1L, LocalDate.of(2026, 11, 10), pageable);

        assertThat(pagina.conteudo()).containsExactly(response);
        assertThat(pagina.totalElementos()).isEqualTo(1);
    }
}
