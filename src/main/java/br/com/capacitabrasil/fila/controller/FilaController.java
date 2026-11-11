package br.com.capacitabrasil.fila.controller;

import br.com.capacitabrasil.fila.dto.request.ChamarProximaRequest;
import br.com.capacitabrasil.fila.dto.response.ChamadaSenhaResponse;
import br.com.capacitabrasil.fila.dto.response.EstatisticasResponse;
import br.com.capacitabrasil.fila.dto.response.FilaResponse;
import br.com.capacitabrasil.fila.service.FilaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/fila")
@RequiredArgsConstructor
@Tag(name = "Fila", description = "Painel, estatisticas e chamada de senhas")
public class FilaController {

    private final FilaService filaService;

    @GetMapping
    @Operation(summary = "Consulta a fila em tempo real (RF003/RF005)")
    public ResponseEntity<FilaResponse> consultar(@RequestParam(required = false) Long filaId) {
        return ResponseEntity.ok(filaService.consultar(filaId));
    }

    @GetMapping("/estatisticas")
    @Operation(summary = "Retorna as estatisticas do dia (RF003)")
    public ResponseEntity<EstatisticasResponse> estatisticas(@RequestParam(required = false) Long filaId) {
        return ResponseEntity.ok(filaService.estatisticas(filaId));
    }

    @PostMapping("/proxima")
    @PreAuthorize("hasAnyRole('ATENDENTE', 'ADMINISTRADOR')")
    @Operation(summary = "Chama a proxima senha disponivel respeitando FIFO (RF006)")
    public ResponseEntity<ChamadaSenhaResponse> chamarProxima(
            @Valid @RequestBody(required = false) ChamarProximaRequest request) {
        ChamarProximaRequest body = request == null ? new ChamarProximaRequest(null, null) : request;
        return ResponseEntity.ok(filaService.chamarProxima(body));
    }
}
