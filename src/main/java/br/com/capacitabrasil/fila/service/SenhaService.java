package br.com.capacitabrasil.fila.service;

import br.com.capacitabrasil.fila.dto.request.CriarSenhaRequest;
import br.com.capacitabrasil.fila.dto.response.PageResponse;
import br.com.capacitabrasil.fila.dto.response.SenhaResponse;
import br.com.capacitabrasil.fila.entity.Fila;
import br.com.capacitabrasil.fila.entity.Senha;
import br.com.capacitabrasil.fila.entity.Usuario;
import br.com.capacitabrasil.fila.entity.enums.StatusSenha;
import br.com.capacitabrasil.fila.exception.RegraNegocioException;
import br.com.capacitabrasil.fila.exception.ResourceNotFoundException;
import br.com.capacitabrasil.fila.mapper.SenhaMapper;
import br.com.capacitabrasil.fila.repository.FilaRepository;
import br.com.capacitabrasil.fila.repository.SenhaRepository;
import br.com.capacitabrasil.fila.repository.specification.SenhaSpecification;
import br.com.capacitabrasil.fila.security.SecurityContextService;
import br.com.capacitabrasil.fila.util.NumeroSenhaUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class SenhaService {

    private final SenhaRepository senhaRepository;
    private final FilaRepository filaRepository;
    private final SenhaMapper senhaMapper;
    private final SenhaEventoService senhaEventoService;
    private final SecurityContextService securityContextService;
    private final FilaResolver filaResolver;
    private final Clock clock;

    /**
     * Gera uma nova senha respeitando a numeracao diaria e por fila (RF001/RN001).
     */
    @Transactional
    public SenhaResponse gerar(CriarSenhaRequest request) {
        Long filaId = request == null ? null : request.filaId();
        Fila fila = filaResolver.resolver(filaId);
        Fila filaBloqueada = filaRepository.findByIdParaAtualizacao(fila.getId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Fila nao encontrada: " + fila.getId(), "FILA_NAO_ENCONTRADA"));

        if (!filaBloqueada.isAtiva()) {
            throw new RegraNegocioException(
                    "A fila %s esta inativa e nao pode emitir senhas.".formatted(filaBloqueada.getNome()),
                    "FILA_INATIVA");
        }

        LocalDate hoje = LocalDate.now(clock);
        int sequencia = filaBloqueada.proximaSequencia(hoje);
        LocalDateTime agora = LocalDateTime.now(clock);

        Senha senha = Senha.builder()
                .fila(filaBloqueada)
                .numero(NumeroSenhaUtil.formatar(filaBloqueada.getPrefixo(), sequencia))
                .sequencia(sequencia)
                .dataReferencia(hoje)
                .status(StatusSenha.AGUARDANDO)
                .dataGeracao(agora)
                .build();

        senhaRepository.save(senha);

        Usuario usuario = securityContextService.usuarioAtual().orElse(null);
        senhaEventoService.registrar(senha, null, StatusSenha.AGUARDANDO, usuario, "Senha gerada");

        long pessoasNaFrente = contarPessoasNaFrente(senha);
        return senhaMapper.toResponse(senha, (int) pessoasNaFrente + 1, pessoasNaFrente);
    }

    @Transactional(readOnly = true)
    public PageResponse<SenhaResponse> listar(StatusSenha status,
                                              Long filaId,
                                              LocalDate dataReferencia,
                                              Pageable pageable) {
        Page<Senha> pagina = senhaRepository.findAll(
                SenhaSpecification.comFiltros(status, filaId, dataReferencia), pageable);
        return PageResponse.de(pagina.map(senha -> senhaMapper.toResponse(senha, null, null)));
    }

    @Transactional(readOnly = true)
    public SenhaResponse buscarPorId(Long id) {
        Senha senha = buscarEntidade(id);
        long pessoasNaFrente = contarPessoasNaFrente(senha);
        Integer posicao = senha.isAguardando() ? (int) pessoasNaFrente + 1 : null;
        Long frente = senha.isAguardando() ? pessoasNaFrente : null;
        return senhaMapper.toResponse(senha, posicao, frente);
    }

    @Transactional(readOnly = true)
    public SenhaResponse ultima(Long filaId) {
        Fila fila = filaResolver.resolver(filaId);
        LocalDate hoje = LocalDate.now(clock);
        Senha senha = senhaRepository.findTopByFilaIdAndDataReferenciaOrderBySequenciaDesc(fila.getId(), hoje)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Nenhuma senha emitida hoje para a fila " + fila.getNome(), "SENHA_NAO_ENCONTRADA"));
        return senhaMapper.toResponse(senha, null, null);
    }

    @Transactional
    public SenhaResponse cancelar(Long id) {
        Senha senha = buscarEntidade(id);
        StatusSenha anterior = senha.getStatus();
        senha.cancelar();

        Usuario usuario = securityContextService.usuarioAtual().orElse(null);
        senhaEventoService.registrar(senha, anterior, StatusSenha.CANCELADA, usuario, "Senha cancelada");
        return senhaMapper.toResponse(senha, null, null);
    }

    private Senha buscarEntidade(Long id) {
        return senhaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Senha nao encontrada: " + id, "SENHA_NAO_ENCONTRADA"));
    }

    private long contarPessoasNaFrente(Senha senha) {
        if (!senha.isAguardando()) {
            return 0L;
        }
        return senhaRepository.contarAntes(
                senha.getFila().getId(),
                StatusSenha.AGUARDANDO,
                senha.getId());
    }
}
