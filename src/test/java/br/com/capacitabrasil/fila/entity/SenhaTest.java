package br.com.capacitabrasil.fila.entity;

import br.com.capacitabrasil.fila.entity.enums.StatusSenha;
import br.com.capacitabrasil.fila.exception.TransicaoInvalidaException;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SenhaTest {

    private static final LocalDateTime AGORA = LocalDateTime.of(2026, 11, 10, 9, 0);

    private Senha senha(StatusSenha status) {
        return Senha.builder()
                .id(1L)
                .numero("A001")
                .status(status)
                .build();
    }

    @Test
    void deveChamarSenhaAguardando() {
        Senha senha = senha(StatusSenha.AGUARDANDO);

        senha.chamar(AGORA);

        assertThat(senha.getStatus()).isEqualTo(StatusSenha.CHAMADA);
        assertThat(senha.getHoraChamada()).isEqualTo(AGORA);
    }

    @Test
    void naoDeveChamarSenhaQueNaoEstaAguardando() {
        Senha senha = senha(StatusSenha.CHAMADA);

        assertThatThrownBy(() -> senha.chamar(AGORA))
                .isInstanceOf(TransicaoInvalidaException.class)
                .hasMessageContaining("A001")
                .hasMessageContaining("CHAMADA");
    }

    @Test
    void deveIniciarAtendimentoDeSenhaChamada() {
        Senha senha = senha(StatusSenha.CHAMADA);

        senha.iniciarAtendimento(AGORA);

        assertThat(senha.getStatus()).isEqualTo(StatusSenha.EM_ATENDIMENTO);
        assertThat(senha.getHoraInicioAtendimento()).isEqualTo(AGORA);
    }

    @Test
    void naoDeveIniciarAtendimentoSemChamada() {
        Senha senha = senha(StatusSenha.AGUARDANDO);

        assertThatThrownBy(() -> senha.iniciarAtendimento(AGORA))
                .isInstanceOf(TransicaoInvalidaException.class);
    }

    @Test
    void deveFinalizarAtendimento() {
        Senha senha = senha(StatusSenha.EM_ATENDIMENTO);

        senha.finalizarAtendimento(AGORA);

        assertThat(senha.getStatus()).isEqualTo(StatusSenha.ATENDIDA);
        assertThat(senha.getHoraFimAtendimento()).isEqualTo(AGORA);
    }

    @Test
    void naoDeveFinalizarAtendimentoNaoIniciado() {
        Senha senha = senha(StatusSenha.CHAMADA);

        assertThatThrownBy(() -> senha.finalizarAtendimento(AGORA))
                .isInstanceOf(TransicaoInvalidaException.class);
    }

    @Test
    void deveCancelarSenhaAguardando() {
        Senha senha = senha(StatusSenha.AGUARDANDO);

        senha.cancelar();

        assertThat(senha.getStatus()).isEqualTo(StatusSenha.CANCELADA);
    }

    @Test
    void deveCancelarSenhaChamada() {
        Senha senha = senha(StatusSenha.CHAMADA);

        senha.cancelar();

        assertThat(senha.getStatus()).isEqualTo(StatusSenha.CANCELADA);
    }

    @Test
    void naoDeveCancelarSenhaAtendida() {
        Senha senha = senha(StatusSenha.ATENDIDA);

        assertThatThrownBy(senha::cancelar)
                .isInstanceOf(TransicaoInvalidaException.class)
                .hasMessageContaining("ATENDIDA");
    }

    @Test
    void naoDeveCancelarSenhaJaCancelada() {
        Senha senha = senha(StatusSenha.CANCELADA);

        assertThatThrownBy(senha::cancelar)
                .isInstanceOf(TransicaoInvalidaException.class)
                .hasMessageContaining("CANCELADA");
    }

    @Test
    void deveInformarSeEstaAguardando() {
        assertThat(senha(StatusSenha.AGUARDANDO).isAguardando()).isTrue();
        assertThat(senha(StatusSenha.CHAMADA).isAguardando()).isFalse();
    }
}
