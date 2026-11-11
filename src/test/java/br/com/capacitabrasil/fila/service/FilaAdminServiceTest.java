package br.com.capacitabrasil.fila.service;

import br.com.capacitabrasil.fila.dto.request.FilaRequest;
import br.com.capacitabrasil.fila.dto.response.FilaDetalheResponse;
import br.com.capacitabrasil.fila.entity.Fila;
import br.com.capacitabrasil.fila.exception.RegraNegocioException;
import br.com.capacitabrasil.fila.exception.ResourceNotFoundException;
import br.com.capacitabrasil.fila.mapper.FilaMapper;
import br.com.capacitabrasil.fila.repository.FilaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FilaAdminServiceTest {

    @Mock
    private FilaRepository filaRepository;
    @Mock
    private FilaMapper filaMapper;

    private FilaAdminService filaAdminService;

    @BeforeEach
    void setUp() {
        filaAdminService = new FilaAdminService(filaRepository, filaMapper);
    }

    private Fila fila() {
        return Fila.builder()
                .id(1L)
                .nome("Geral")
                .prefixo("A")
                .descricao("Geral")
                .ativa(true)
                .build();
    }

    @Test
    void deveCriarFilaNormalizandoPrefixo() {
        FilaDetalheResponse response = new FilaDetalheResponse(1L, "Geral", "A", "Geral", true, null, null);

        when(filaRepository.existsByNomeIgnoreCase("Geral")).thenReturn(false);
        when(filaRepository.existsByPrefixoIgnoreCase("A")).thenReturn(false);
        when(filaRepository.save(any(Fila.class))).thenAnswer(invocation -> {
            Fila f = invocation.getArgument(0);
            f.setId(1L);
            return f;
        });
        when(filaMapper.toDetalhe(any(Fila.class))).thenReturn(response);

        FilaDetalheResponse criada = filaAdminService.criar(
                new FilaRequest(" Geral ", "a", " Geral ", null));

        assertThat(criada).isSameAs(response);
    }

    @Test
    void naoDeveCriarFilaComNomeDuplicado() {
        when(filaRepository.existsByNomeIgnoreCase("Geral")).thenReturn(true);

        assertThatThrownBy(() -> filaAdminService.criar(
                new FilaRequest("Geral", "B", null, true)))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("nome");
    }

    @Test
    void naoDeveCriarFilaComPrefixoDuplicado() {
        when(filaRepository.existsByNomeIgnoreCase("Nova")).thenReturn(false);
        when(filaRepository.existsByPrefixoIgnoreCase("A")).thenReturn(true);

        assertThatThrownBy(() -> filaAdminService.criar(
                new FilaRequest("Nova", "a", null, true)))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("prefixo");
    }

    @Test
    void deveAtualizarFila() {
        Fila fila = fila();
        FilaDetalheResponse response = new FilaDetalheResponse(1L, "Geral 2", "G", "Nova", false, null, null);

        when(filaRepository.findById(1L)).thenReturn(Optional.of(fila));
        when(filaRepository.existsByNomeIgnoreCase("Geral 2")).thenReturn(false);
        when(filaRepository.existsByPrefixoIgnoreCase("G")).thenReturn(false);
        when(filaMapper.toDetalhe(fila)).thenReturn(response);

        FilaDetalheResponse atualizada = filaAdminService.atualizar(1L,
                new FilaRequest("Geral 2", "g", "Nova", false));

        assertThat(atualizada).isSameAs(response);
        assertThat(fila.getNome()).isEqualTo("Geral 2");
        assertThat(fila.getPrefixo()).isEqualTo("G");
        assertThat(fila.isAtiva()).isFalse();
    }

    @Test
    void deveManterStatusQuandoNaoInformadoNaAtualizacao() {
        Fila fila = fila();
        when(filaRepository.findById(1L)).thenReturn(Optional.of(fila));
        when(filaMapper.toDetalhe(fila)).thenReturn(
                new FilaDetalheResponse(1L, "Geral", "A", null, true, null, null));

        filaAdminService.atualizar(1L, new FilaRequest("Geral", "A", null, null));

        assertThat(fila.isAtiva()).isTrue();
    }

    @Test
    void deveDesativarFila() {
        Fila fila = fila();
        when(filaRepository.findById(1L)).thenReturn(Optional.of(fila));

        filaAdminService.desativar(1L);

        assertThat(fila.isAtiva()).isFalse();
    }

    @Test
    void deveListarFilas() {
        Fila fila = fila();
        FilaDetalheResponse response = new FilaDetalheResponse(1L, "Geral", "A", null, true, null, null);

        when(filaRepository.findAll(any(Sort.class))).thenReturn(List.of(fila));
        when(filaMapper.toDetalhe(fila)).thenReturn(response);

        assertThat(filaAdminService.listar()).containsExactly(response);
    }

    @Test
    void deveBuscarFilaPorId() {
        Fila fila = fila();
        FilaDetalheResponse response = new FilaDetalheResponse(1L, "Geral", "A", null, true, null, null);

        when(filaRepository.findById(1L)).thenReturn(Optional.of(fila));
        when(filaMapper.toDetalhe(fila)).thenReturn(response);

        assertThat(filaAdminService.buscarPorId(1L)).isSameAs(response);
    }

    @Test
    void deveFalharAoBuscarFilaInexistente() {
        when(filaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> filaAdminService.buscarPorId(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
