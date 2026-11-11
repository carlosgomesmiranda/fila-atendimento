package br.com.capacitabrasil.fila.service;

import br.com.capacitabrasil.fila.dto.request.FinalizarAtendimentoRequest;
import br.com.capacitabrasil.fila.dto.request.IniciarAtendimentoRequest;
import br.com.capacitabrasil.fila.dto.response.AtendimentoResponse;
import br.com.capacitabrasil.fila.dto.response.PageResponse;
import br.com.capacitabrasil.fila.entity.Atendimento;
import br.com.capacitabrasil.fila.entity.Senha;
import br.com.capacitabrasil.fila.entity.Usuario;
import br.com.capacitabrasil.fila.entity.enums.StatusSenha;
import br.com.capacitabrasil.fila.exception.RegraNegocioException;
import br.com.capacitabrasil.fila.exception.ResourceNotFoundException;
import br.com.capacitabrasil.fila.mapper.AtendimentoMapper;
import br.com.capacitabrasil.fila.repository.AtendimentoRepository;
import br.com.capacitabrasil.fila.repository.SenhaRepository;
import br.com.capacitabrasil.fila.repository.specification.AtendimentoSpecification;
import br.com.capacitabrasil.fila.security.SecurityContextService;
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
public class AtendimentoService {

    private final AtendimentoRepository atendimentoRepository;
    private final SenhaRepository senhaRepository;
    private final SenhaEventoService senhaEventoService;
    private final SecurityContextService securityContextService;
    private final AtendimentoMapper atendimentoMapper;
    private final Clock clock;

    /**
     * Inicia o atendimento de uma senha previamente chamada (RF007).
     */
    @Transactional
    public AtendimentoResponse iniciar(IniciarAtendimentoRequest request) {
        Senha senha = senhaRepository.findById(request.senhaId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Senha nao encontrada: " + request.senhaId(), "SENHA_NAO_ENCONTRADA"));

        Atendimento atendimento = atendimentoRepository.findBySenhaId(senha.getId())
                .orElseThrow(() -> new RegraNegocioException(
                        "A senha %s nao possui atendimento aberto. Chame a senha primeiro."
                                .formatted(senha.getNumero()),
                        "ATENDIMENTO_NAO_ENCONTRADO"));

        StatusSenha anterior = senha.getStatus();
        LocalDateTime agora = LocalDateTime.now(clock);
        senha.iniciarAtendimento(agora);
        atendimento.iniciar(agora, request.guiche());

        Usuario atendente = securityContextService.usuarioAtual().orElse(null);
        senhaEventoService.registrar(senha, anterior, StatusSenha.EM_ATENDIMENTO, atendente,
                "Atendimento iniciado");

        return atendimentoMapper.toResponse(atendimento);
    }

    /**
     * Finaliza o atendimento, alterando a senha para ATENDIDA (RF008/RN005).
     */
    @Transactional
    public AtendimentoResponse finalizar(FinalizarAtendimentoRequest request) {
        Atendimento atendimento = atendimentoRepository.findById(request.atendimentoId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Atendimento nao encontrado: " + request.atendimentoId(), "ATENDIMENTO_NAO_ENCONTRADO"));

        Senha senha = atendimento.getSenha();
        StatusSenha anterior = senha.getStatus();
        LocalDateTime agora = LocalDateTime.now(clock);
        senha.finalizarAtendimento(agora);
        atendimento.finalizar(agora, request.observacao());

        Usuario atendente = securityContextService.usuarioAtual().orElse(null);
        senhaEventoService.registrar(senha, anterior, StatusSenha.ATENDIDA, atendente,
                "Atendimento finalizado");

        return atendimentoMapper.toResponse(atendimento);
    }

    @Transactional(readOnly = true)
    public PageResponse<AtendimentoResponse> listar(LocalDate dataInicio,
                                                    LocalDate dataFim,
                                                    String guiche,
                                                    Long atendenteId,
                                                    StatusSenha statusSenha,
                                                    Pageable pageable) {
        Page<Atendimento> pagina = atendimentoRepository.findAll(
                AtendimentoSpecification.comFiltros(dataInicio, dataFim, guiche, atendenteId, statusSenha),
                pageable);
        return PageResponse.de(pagina.map(atendimentoMapper::toResponse));
    }

    @Transactional(readOnly = true)
    public AtendimentoResponse buscarPorId(Long id) {
        Atendimento atendimento = atendimentoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Atendimento nao encontrado: " + id, "ATENDIMENTO_NAO_ENCONTRADO"));
        return atendimentoMapper.toResponse(atendimento);
    }
}
