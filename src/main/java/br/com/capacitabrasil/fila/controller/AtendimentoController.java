package br.com.capacitabrasil.fila.controller;

import br.com.capacitabrasil.fila.dto.request.FinalizarAtendimentoRequest;
import br.com.capacitabrasil.fila.dto.request.IniciarAtendimentoRequest;
import br.com.capacitabrasil.fila.dto.response.AtendimentoResponse;
import br.com.capacitabrasil.fila.dto.response.PageResponse;
import br.com.capacitabrasil.fila.entity.enums.StatusSenha;
import br.com.capacitabrasil.fila.service.AtendimentoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/atendimentos")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ATENDENTE', 'ADMINISTRADOR')")
@Tag(name = "Atendimentos", description = "Inicio, fim e historico de atendimentos")
public class AtendimentoController {

    private final AtendimentoService atendimentoService;

    @PostMapping("/iniciar")
    @Operation(summary = "Inicia o atendimento de uma senha chamada (RF007)")
    public ResponseEntity<AtendimentoResponse> iniciar(
            @Valid @RequestBody IniciarAtendimentoRequest request) {
        return ResponseEntity.ok(atendimentoService.iniciar(request));
    }

    @PostMapping("/finalizar")
    @Operation(summary = "Finaliza um atendimento em andamento (RF008/RN005)")
    public ResponseEntity<AtendimentoResponse> finalizar(
            @Valid @RequestBody FinalizarAtendimentoRequest request) {
        return ResponseEntity.ok(atendimentoService.finalizar(request));
    }

    @GetMapping
    @Operation(summary = "Consulta o historico de atendimentos (RF010)")
    public ResponseEntity<PageResponse<AtendimentoResponse>> listar(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim,
            @RequestParam(required = false) String guiche,
            @RequestParam(required = false) Long atendenteId,
            @RequestParam(required = false) StatusSenha status,
            @ParameterObject
            @PageableDefault(size = 20, sort = "inicio", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(atendimentoService.listar(
                dataInicio, dataFim, guiche, atendenteId, status, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consulta um atendimento pelo identificador")
    public ResponseEntity<AtendimentoResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(atendimentoService.buscarPorId(id));
    }
}
