package br.com.capacitabrasil.fila.controller;

import br.com.capacitabrasil.fila.dto.request.CriarSenhaRequest;
import br.com.capacitabrasil.fila.dto.response.PageResponse;
import br.com.capacitabrasil.fila.dto.response.SenhaResponse;
import br.com.capacitabrasil.fila.entity.enums.StatusSenha;
import br.com.capacitabrasil.fila.service.SenhaService;
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
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/senhas")
@RequiredArgsConstructor
@Tag(name = "Senhas", description = "Emissao, consulta e cancelamento de senhas")
public class SenhaController {

    private final SenhaService senhaService;

    @PostMapping
    @Operation(summary = "Gera uma nova senha (RF001)")
    public ResponseEntity<SenhaResponse> gerar(@Valid @RequestBody(required = false) CriarSenhaRequest request) {
        CriarSenhaRequest body = request == null ? new CriarSenhaRequest(null) : request;
        SenhaResponse response = senhaService.gerar(body);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.id())
                .toUri();

        return ResponseEntity.created(location).body(response);
    }

    @GetMapping
    @Operation(summary = "Lista senhas com filtros e paginacao (RF009)")
    public ResponseEntity<PageResponse<SenhaResponse>> listar(
            @RequestParam(required = false) StatusSenha status,
            @RequestParam(required = false) Long filaId,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data,
            @ParameterObject
            @PageableDefault(size = 20, sort = "dataGeracao", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(senhaService.listar(status, filaId, data, pageable));
    }

    @GetMapping("/ultima")
    @Operation(summary = "Retorna a ultima senha emitida no dia (RF002)")
    public ResponseEntity<SenhaResponse> ultima(@RequestParam(required = false) Long filaId) {
        return ResponseEntity.ok(senhaService.ultima(filaId));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consulta uma senha e sua posicao atual na fila (RF004)")
    public ResponseEntity<SenhaResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(senhaService.buscarPorId(id));
    }

    @PostMapping("/{id}/cancelar")
    @PreAuthorize("hasAnyRole('ATENDENTE', 'ADMINISTRADOR')")
    @Operation(summary = "Cancela uma senha. Senhas canceladas nao retornam a fila (RN006)")
    public ResponseEntity<SenhaResponse> cancelar(@PathVariable Long id) {
        return ResponseEntity.ok(senhaService.cancelar(id));
    }
}
