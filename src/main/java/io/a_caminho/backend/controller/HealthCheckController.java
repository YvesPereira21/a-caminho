package io.a_caminho.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/health")
@Tag(name = "Health Check", description = "Verificação de disponibilidade e saúde da aplicação")
public class HealthCheckController {

    @Operation(summary = "Verifica integridade do serviço", description = "Retorna status UP indicando que a aplicação está operacional.")
    @ApiResponse(responseCode = "200", description = "Aplicação saudável e operando normalmente")
    @GetMapping
    public ResponseEntity<Map<String, Object>> checkHealth() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "message", "Aplicação 'a-caminho' está rodando com sucesso no Docker!",
                "timestamp", LocalDateTime.now()
        ));
    }
}
