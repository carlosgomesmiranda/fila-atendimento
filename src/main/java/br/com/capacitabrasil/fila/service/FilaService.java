package br.com.capacitabrasil.fila.service;

import br.com.capacitabrasil.fila.dto.request.ChamarProximaRequest;
import br.com.capacitabrasil.fila.dto.response.ChamadaSenhaResponse;
import br.com.capacitabrasil.fila.dto.response.EstatisticasResponse;
import br.com.capacitabrasil.fila.dto.response.FilaResponse;
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
import br.com.capacitabrasil.fila.util.DuracaoUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FilaService {

    private final SenhaRepository senhaRepository;
    private final AtendimentoRepository atendimentoRepository;
    private final SenhaEventoService senhaEventoService;
    private final SecurityContextService securityContextService;
    private final FilaResolver filaResolver;
    private final SenhaMapper senhaMapper;
    private final FilaMapper filaMapper;
    private final Clock clock;

    /**
     * Retorna o painel da fila: aguardando, chamadas e em atendimento (RF003).
     */
    @Transactional(readOnly = true)
    public FilaResponse consultar(Long filaId) {
        Fila fila = filaResolver.resolver(filaId);

        List<Senha> aguardando = senhaRepository
                .findByFilaIdAndStatusOrderByDataGeracaoAscSequenciaAsc(fila.getId(), StatusSenha.AGUARDANDO);
        List<Senha> chamadas = senhaRepository
                .findByFilaIdAndStatusOrderByHoraChamadaAsc(fila.getId(), StatusSenha.CHAMADA);
        List<Senha> emAtendimento = senhaRepository
                .findByFilaIdAndStatusOrderByHoraInicioAtendimentoAsc(fila.getId(), StatusSenha.EM_ATENDIMENTO);

        return new FilaResponse(
                filaMapper.toResumo(fila),
                aguardando.stream().map(senhaMapper::toPainel).toList(),
                chamadas.stream().map(senhaMapper::toPainel).toList(),
                emAtendimento.stream().map(senhaMapper::toPainel).toList(),
                aguardando.size(),
                LocalDateTime.now(clock));
    }

    /**
     * Chama a proxima senha disponivel (RF006), respeitando FIFO e criando o atendimento.
     */
    @Transactional
    public ChamadaSenhaResponse chamarProxima(ChamarProximaRequest request) {
        Long filaId = request == null ? null : request.filaId();
        Fila fila = filaResolver.resolver(filaId);

        if (!fila.isAtiva()) {
            throw new RegraNegocioException(
                    "A fila %s esta inativa.".formatted(fila.getNome()), "FILA_INATIVA");
        }

        List<Senha> proximas = senhaRepository.buscarPrimeiras(
                fila.getId(), StatusSenha.AGUARDANDO, PageRequest.of(0, 1));

        if (proximas.isEmpty()) {
            throw new FilaVaziaException(fila.getNome());
        }

        Senha senha = proximas.get(0);
        LocalDateTime agora = LocalDateTime.now(clock);
        senha.chamar(agora);

        Usuario atendente = securityContextService.usuarioAtual().orElse(null);
        String guiche = normalizar(request == null ? null : request.guiche());

        Atendimento atendimento = Atendimento.builder()
                .senha(senha)
                .atendente(atendente)
                .guiche(guiche)
                .build();
        atendimentoRepository.save(atendimento);

        String detalhe = guiche == null ? "Senha chamada" : "Senha chamada - guiche " + guiche;
        senhaEventoService.registrar(senha, StatusSenha.AGUARDANDO, StatusSenha.CHAMADA, atendente, detalhe);

        long restantes = senhaRepository.countByFilaIdAndStatus(fila.getId(), StatusSenha.AGUARDANDO);

        return new ChamadaSenhaResponse(
                senha.getId(),
                senha.getNumero(),
                senha.getStatus(),
                senha.getHoraChamada(),
                atendimento.getId(),
                atendimento.getGuiche(),
                restantes);
    }

    /**
     * Estatisticas do dia para a fila informada (RF003/RF009/RF010).
     */
    @Transactional(readOnly = true)
    public EstatisticasResponse estatisticas(Long filaId) {
        Fila fila = filaResolver.resolver(filaId);
        LocalDate hoje = LocalDate.now(clock);
        List<Senha> senhas = senhaRepository
                .findByFilaIdAndDataReferenciaOrderByDataGeracaoAscSequenciaAsc(fila.getId(), hoje);

        long aguardando = contar(senhas, StatusSenha.AGUARDANDO);
        long chamadas = contar(senhas, StatusSenha.CHAMADA);
        long emAtendimento = contar(senhas, StatusSenha.EM_ATENDIMENTO);
        long atendidas = contar(senhas, StatusSenha.ATENDIDA);
        long canceladas = contar(senhas, StatusSenha.CANCELADA);

        double tempoMedioEspera = senhas.stream()
                .filter(s -> s.getHoraChamada() != null)
                .mapToLong(s -> DuracaoUtil.segundosEntre(s.getDataGeracao(), s.getHoraChamada()))
                .average()
                .orElse(0.0);

        double tempoMedioAtendimento = senhas.stream()
                .filter(s -> s.getHoraInicioAtendimento() != null && s.getHoraFimAtendimento() != null)
                .mapToLong(s -> DuracaoUtil.segundosEntre(
                        s.getHoraInicioAtendimento(), s.getHoraFimAtendimento()))
                .average()
                .orElse(0.0);

        String ultimaEmitida = senhas.stream()
                .max(Comparator.comparingInt(Senha::getSequencia))
                .map(Senha::getNumero)
                .orElse(null);

        String ultimaChamada = senhas.stream()
                .filter(s -> s.getHoraChamada() != null)
                .max(Comparator.comparing(Senha::getHoraChamada))
                .map(Senha::getNumero)
                .orElse(null);

        return new EstatisticasResponse(
                hoje,
                senhas.size(),
                aguardando,
                chamadas,
                emAtendimento,
                atendidas,
                canceladas,
                DuracaoUtil.arredondar(tempoMedioEspera, 1),
                DuracaoUtil.arredondar(tempoMedioAtendimento, 1),
                ultimaEmitida,
                ultimaChamada);
    }

    private long contar(List<Senha> senhas, StatusSenha status) {
        return senhas.stream().filter(s -> s.getStatus() == status).count();
    }

    private String normalizar(String guiche) {
        if (guiche == null || guiche.isBlank()) {
            return null;
        }
        return guiche.trim();
    }
}
