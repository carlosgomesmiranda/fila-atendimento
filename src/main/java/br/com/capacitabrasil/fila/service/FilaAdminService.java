package br.com.capacitabrasil.fila.service;

import br.com.capacitabrasil.fila.dto.request.FilaRequest;
import br.com.capacitabrasil.fila.dto.response.FilaDetalheResponse;
import br.com.capacitabrasil.fila.entity.Fila;
import br.com.capacitabrasil.fila.exception.RegraNegocioException;
import br.com.capacitabrasil.fila.exception.ResourceNotFoundException;
import br.com.capacitabrasil.fila.mapper.FilaMapper;
import br.com.capacitabrasil.fila.repository.FilaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class FilaAdminService {

    private final FilaRepository filaRepository;
    private final FilaMapper filaMapper;

    @Transactional(readOnly = true)
    public List<FilaDetalheResponse> listar() {
        return filaRepository.findAll(Sort.by(Sort.Direction.ASC, "nome"))
                .stream()
                .map(filaMapper::toDetalhe)
                .toList();
    }

    @Transactional(readOnly = true)
    public FilaDetalheResponse buscarPorId(Long id) {
        return filaMapper.toDetalhe(buscarEntidade(id));
    }

    @Transactional
    public FilaDetalheResponse criar(FilaRequest request) {
        String nome = request.nome().trim();
        String prefixo = normalizarPrefixo(request.prefixo());

        if (filaRepository.existsByNomeIgnoreCase(nome)) {
            throw new RegraNegocioException("Ja existe uma fila com o nome " + nome, "FILA_NOME_DUPLICADO");
        }
        if (filaRepository.existsByPrefixoIgnoreCase(prefixo)) {
            throw new RegraNegocioException("Ja existe uma fila com o prefixo " + prefixo, "FILA_PREFIXO_DUPLICADO");
        }

        Fila fila = Fila.builder()
                .nome(nome)
                .prefixo(prefixo)
                .descricao(limpar(request.descricao()))
                .ativa(request.ativa() == null || request.ativa())
                .build();

        return filaMapper.toDetalhe(filaRepository.save(fila));
    }

    @Transactional
    public FilaDetalheResponse atualizar(Long id, FilaRequest request) {
        Fila fila = buscarEntidade(id);
        String nome = request.nome().trim();
        String prefixo = normalizarPrefixo(request.prefixo());

        if (!fila.getNome().equalsIgnoreCase(nome) && filaRepository.existsByNomeIgnoreCase(nome)) {
            throw new RegraNegocioException("Ja existe uma fila com o nome " + nome, "FILA_NOME_DUPLICADO");
        }
        if (!fila.getPrefixo().equalsIgnoreCase(prefixo) && filaRepository.existsByPrefixoIgnoreCase(prefixo)) {
            throw new RegraNegocioException("Ja existe uma fila com o prefixo " + prefixo, "FILA_PREFIXO_DUPLICADO");
        }

        fila.setNome(nome);
        fila.setPrefixo(prefixo);
        fila.setDescricao(limpar(request.descricao()));
        if (request.ativa() != null) {
            fila.setAtiva(request.ativa());
        }
        return filaMapper.toDetalhe(fila);
    }

    @Transactional
    public void desativar(Long id) {
        Fila fila = buscarEntidade(id);
        fila.setAtiva(false);
    }

    private Fila buscarEntidade(Long id) {
        return filaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Fila nao encontrada: " + id, "FILA_NAO_ENCONTRADA"));
    }

    private String normalizarPrefixo(String prefixo) {
        return prefixo.trim().toUpperCase(Locale.ROOT);
    }

    private String limpar(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }
}
