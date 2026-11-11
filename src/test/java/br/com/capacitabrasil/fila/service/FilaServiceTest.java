package br.com.capacitabrasil.fila.service;

import br.com.capacitabrasil.fila.dto.request.ChamarProximaRequest;
import br.com.capacitabrasil.fila.dto.response.ChamadaSenhaResponse;
import br.com.capacitabrasil.fila.dto.response.EstatisticasResponse;
import br.com.capacitabrasil.fila.dto.response.FilaResumoResponse;
import br.com.capacitabrasil.fila.dto.response.FilaResponse;
import br.com.capacitabrasil.fila.dto.response.SenhaPainelResponse;
import br.com.capacitabrasil.fila.entity.Atendimento;
import br.com.capacitabrasil.fila.entity.Fila;
import br.com.capacitabrasil.fila.entity.Senha;
import br.com.capacitabrasil.fila.entity.Usuario;
import br.com.capacitabrasil.fila.entity.enums.StatusSenha;
import br.com.capacitabrasil.fila.exception.FilaVaziaException;
import br.com.capacitabrasil.fila.exception.RegraNegocioException;
import br.com.capacitabrasil.fila.mapper.FilaMapper;
import br.com.capacitabrasil.fila.mapper.SenhaMapper;
import br.com.capacitabrasil.fila.repository.AtendimentoRepository;
import br.com.capacitabrasil.fila.repository.SenhaRepository;
import br.com.capacitabrasil.fila.security.SecurityContextService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

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
class FilaServiceTest {

    private static final Clock CLOCK = Clock.fixed(
            Instant.parse("2026-11-10T12:00:00Z"), ZoneId.of("America/Sao_Paulo"));
    private static final LocalDate HOJE = LocalDate.of(2026, 11, 10);

    @Mock
    private SenhaRepository senhaRepository;
    @Mock
    private AtendimentoRepository atendimentoRepository;
    @Mock
    private SenhaEventoService senhaEventoService;
    @Mock
    private SecurityContextService securityContextService;
    @Mock
    private FilaResolver filaResolver;
    @Mock
    private SenhaMapper senhaMapper;
    @Mock
    private FilaMapper filaMapper;

    private FilaService filaService;

    @BeforeEach
    void setUp() {
        filaService = new FilaService(
                senhaRepository,
                atendimentoRepository,
                senhaEventoService,
                securityContextService,
                filaResolver,
                senhaMapper,
                filaMapper,
                CLOCK);
    }

    private Fila fila(boolean ativa) {
        return Fila.builder().id(1L).nome("Geral").prefixo("A").ativa(ativa).build();
    }

    private Senha senha(long id, String numero, int sequencia, StatusSenha status) {
        return Senha.builder()
                .id(id)
                .fila(fila(true))
                .numero(numero)
                .sequencia(sequencia)
                .dataReferencia(HOJE)
                .dataGeracao(LocalDateTime.of(2026, 11, 10, 8, 0).plusMinutes(sequencia))
                .status(status)
                .build();
    }

    @Test
    void deveChamarProximaSenhaRespeitandoFifo() {
        Fila fila = fila(true);
        Senha senha = senha(10L, "A001", 1, StatusSenha.AGUARDANDO);
        Usuario atendente = Usuario.builder().id(2L).nome("Atendente").build();

        when(filaResolver.resolver(null)).thenReturn(fila);
        when(senhaRepository.buscarPrimeiras(eq(1L), eq(StatusSenha.AGUARDANDO), any(Pageable.class)))
                .thenReturn(List.of(senha));
        when(securityContextService.usuarioAtual()).thenReturn(Optional.of(atendente));
        when(atendimentoRepository.save(any(Atendimento.class))).thenAnswer(invocation -> {
            Atendimento atendimento = invocation.getArgument(0);
            ReflectionTestUtils.setField(atendimento, "id", 55L);
            return atendimento;
        });
        when(senhaRepository.countByFilaIdAndStatus(1L, StatusSenha.AGUARDANDO)).thenReturn(3L);

        ChamadaSenhaResponse response = filaService.chamarProxima(new ChamarProximaRequest(null, " 3 "));

        assertThat(response.senhaId()).isEqualTo(10L);
        assertThat(response.numero()).isEqualTo("A001");
        assertThat(response.status()).isEqualTo(StatusSenha.CHAMADA);
        assertThat(response.atendimentoId()).isEqualTo(55L);
        assertThat(response.guiche()).isEqualTo("3");
        assertThat(response.aguardandoRestantes()).isEqualTo(3L);
        assertThat(senha.getHoraChamada()).isEqualTo(LocalDateTime.of(2026, 11, 10, 9, 0));

        verify(senhaEventoService).registrar(senha, StatusSenha.AGUARDANDO,
                StatusSenha.CHAMADA, atendente, "Senha chamada - guiche 3");
    }

    @Test
    void deveChamarSemGuicheQuandoNaoInformado() {
        Fila fila = fila(true);
        Senha senha = senha(10L, "A001", 1, StatusSenha.AGUARDANDO);

        when(filaResolver.resolver(1L)).thenReturn(fila);
        when(senhaRepository.buscarPrimeiras(eq(1L), eq(StatusSenha.AGUARDANDO), any(Pageable.class)))
                .thenReturn(List.of(senha));
        when(securityContextService.usuarioAtual()).thenReturn(Optional.empty());
        when(atendimentoRepository.save(any(Atendimento.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(senhaRepository.countByFilaIdAndStatus(1L, StatusSenha.AGUARDANDO)).thenReturn(0L);

        ChamadaSenhaResponse response = filaService.chamarProxima(new ChamarProximaRequest(1L, "  "));

        assertThat(response.guiche()).isNull();
        verify(senhaEventoService).registrar(senha, StatusSenha.AGUARDANDO,
                StatusSenha.CHAMADA, null, "Senha chamada");
    }

    @Test
    void deveFalharQuandoFilaEstiverVazia() {
        when(filaResolver.resolver(null)).thenReturn(fila(true));
        when(senhaRepository.buscarPrimeiras(eq(1L), eq(StatusSenha.AGUARDANDO), any(Pageable.class)))
                .thenReturn(List.of());

        assertThatThrownBy(() -> filaService.chamarProxima(new ChamarProximaRequest(null, null)))
                .isInstanceOf(FilaVaziaException.class)
                .hasMessageContaining("Geral");
    }

    @Test
    void naoDeveChamarSenhaEmFilaInativa() {
        when(filaResolver.resolver(null)).thenReturn(fila(false));

        assertThatThrownBy(() -> filaService.chamarProxima(new ChamarProximaRequest(null, null)))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("inativa");
    }

    @Test
    void deveConsultarPainelDaFila() {
        Fila fila = fila(true);
        Senha aguardando = senha(10L, "A001", 1, StatusSenha.AGUARDANDO);
        Senha chamada = senha(11L, "A002", 2, StatusSenha.CHAMADA);
        Senha emAtendimento = senha(12L, "A003", 3, StatusSenha.EM_ATENDIMENTO);
        FilaResumoResponse resumo = new FilaResumoResponse(1L, "Geral", "A");
        SenhaPainelResponse painel = new SenhaPainelResponse(10L, "A001",
                StatusSenha.AGUARDANDO, null, null, null);

        when(filaResolver.resolver(null)).thenReturn(fila);
        when(senhaRepository.findByFilaIdAndStatusOrderByDataGeracaoAscSequenciaAsc(
                1L, StatusSenha.AGUARDANDO)).thenReturn(List.of(aguardando));
        when(senhaRepository.findByFilaIdAndStatusOrderByHoraChamadaAsc(
                1L, StatusSenha.CHAMADA)).thenReturn(List.of(chamada));
        when(senhaRepository.findByFilaIdAndStatusOrderByHoraInicioAtendimentoAsc(
                1L, StatusSenha.EM_ATENDIMENTO)).thenReturn(List.of(emAtendimento));
        when(filaMapper.toResumo(fila)).thenReturn(resumo);
        when(senhaMapper.toPainel(any(Senha.class))).thenReturn(painel);

        FilaResponse response = filaService.consultar(null);

        assertThat(response.fila()).isEqualTo(resumo);
        assertThat(response.aguardando()).hasSize(1);
        assertThat(response.chamadas()).hasSize(1);
        assertThat(response.emAtendimento()).hasSize(1);
        assertThat(response.totalAguardando()).isEqualTo(1);
        assertThat(response.atualizadoEm()).isEqualTo(LocalDateTime.of(2026, 11, 10, 9, 0));
    }

    @Test
    void deveCalcularEstatisticasDoDia() {
        Fila fila = fila(true);
        Senha aguardando = senha(10L, "A001", 1, StatusSenha.AGUARDANDO);

        Senha chamada = senha(11L, "A002", 2, StatusSenha.CHAMADA);
        chamada.setHoraChamada(LocalDateTime.of(2026, 11, 10, 8, 5));

        Senha emAtendimento = senha(12L, "A003", 3, StatusSenha.EM_ATENDIMENTO);
        emAtendimento.setHoraChamada(LocalDateTime.of(2026, 11, 10, 8, 6));
        emAtendimento.setHoraInicioAtendimento(LocalDateTime.of(2026, 11, 10, 8, 7));

        Senha atendida = senha(13L, "A004", 4, StatusSenha.ATENDIDA);
        atendida.setHoraChamada(LocalDateTime.of(2026, 11, 10, 8, 7));
        atendida.setHoraInicioAtendimento(LocalDateTime.of(2026, 11, 10, 8, 8));
        atendida.setHoraFimAtendimento(LocalDateTime.of(2026, 11, 10, 8, 13));

        Senha cancelada = senha(14L, "A005", 5, StatusSenha.CANCELADA);

        when(filaResolver.resolver(null)).thenReturn(fila);
        when(senhaRepository.findByFilaIdAndDataReferenciaOrderByDataGeracaoAscSequenciaAsc(1L, HOJE))
                .thenReturn(List.of(aguardando, chamada, emAtendimento, atendida, cancelada));

        EstatisticasResponse response = filaService.estatisticas(null);

        assertThat(response.data()).isEqualTo(HOJE);
        assertThat(response.totalEmitidas()).isEqualTo(5);
        assertThat(response.aguardando()).isEqualTo(1);
        assertThat(response.chamadas()).isEqualTo(1);
        assertThat(response.emAtendimento()).isEqualTo(1);
        assertThat(response.atendidas()).isEqualTo(1);
        assertThat(response.canceladas()).isEqualTo(1);
        assertThat(response.tempoMedioEsperaSegundos()).isGreaterThan(0);
        assertThat(response.tempoMedioAtendimentoSegundos()).isEqualTo(300.0);
        assertThat(response.ultimaSenhaEmitida()).isEqualTo("A005");
        assertThat(response.ultimaSenhaChamada()).isEqualTo("A004");
    }

    @Test
    void deveRetornarEstatisticasZeradasQuandoNaoHouverSenhas() {
        when(filaResolver.resolver(null)).thenReturn(fila(true));
        when(senhaRepository.findByFilaIdAndDataReferenciaOrderByDataGeracaoAscSequenciaAsc(1L, HOJE))
                .thenReturn(List.of());

        EstatisticasResponse response = filaService.estatisticas(null);

        assertThat(response.totalEmitidas()).isZero();
        assertThat(response.tempoMedioEsperaSegundos()).isZero();
        assertThat(response.tempoMedioAtendimentoSegundos()).isZero();
        assertThat(response.ultimaSenhaEmitida()).isNull();
        assertThat(response.ultimaSenhaChamada()).isNull();
    }
}
