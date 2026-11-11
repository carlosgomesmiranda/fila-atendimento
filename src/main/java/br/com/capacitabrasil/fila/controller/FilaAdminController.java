package br.com.capacitabrasil.fila.controller;

import br.com.capacitabrasil.fila.dto.request.FilaRequest;
import br.com.capacitabrasil.fila.dto.response.FilaDetalheResponse;
import br.com.capacitabrasil.fila.service.FilaAdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/filas")
@RequiredArgsConstructor
@Tag(name = "Filas (administracao)", description = "Gerenciamento de filas")
public class FilaAdminController {

    private final FilaAdminService filaAdminService;

    @GetMapping
    @Operation(summary = "Lista todas as filas")
    public ResponseEntity<List<FilaDetalheResponse>> listar() {
        return ResponseEntity.ok(filaAdminService.listar());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consulta uma fila pelo identificador")
    public ResponseEntity<FilaDetalheResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(filaAdminService.buscarPorId(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Cria uma nova fila")
    public ResponseEntity<FilaDetalheResponse> criar(@Valid @RequestBody FilaRequest request) {
        FilaDetalheResponse response = filaAdminService.criar(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.id())
                .toUri();
        return ResponseEntity.created(location).body(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Atualiza uma fila")
    public ResponseEntity<FilaDetalheResponse> atualizar(@PathVariable Long id,
                                                         @Valid @RequestBody FilaRequest request) {
        return ResponseEntity.ok(filaAdminService.atualizar(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Desativa uma fila")
    public ResponseEntity<Void> desativar(@PathVariable Long id) {
        filaAdminService.desativar(id);
        return ResponseEntity.noContent().build();
    }
}
