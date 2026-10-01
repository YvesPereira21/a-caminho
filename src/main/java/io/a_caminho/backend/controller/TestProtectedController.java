package io.a_caminho.backend.controller;

import io.a_caminho.backend.config.OpenApiConfig;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/test")
@Tag(name = "Testes de Segurança", description = "Endpoints de validação de controle de acesso baseado em papéis (RBAC)")
@SecurityRequirement(name = OpenApiConfig.BEARER_SECURITY_SCHEME)
public class TestProtectedController {

    @Operation(summary = "Endpoint restrito a Estudantes", description = "Requer token JWT com a role STUDENT.")
    @ApiResponse(responseCode = "200", description = "Acesso autorizado com perfil STUDENT")
    @ApiResponse(responseCode = "401", description = "Não autenticado")
    @ApiResponse(responseCode = "403", description = "Acesso negado para o perfil atual")
    @GetMapping("/student")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<Map<String, String>> studentOnly(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(Map.of(
                "message", "Área restrita de estudante acessada com sucesso",
                "user", userDetails != null ? userDetails.getUsername() : "anonymous"
        ));
    }

    @Operation(summary = "Endpoint restrito a Administradores", description = "Requer token JWT com a role ADMIN.")
    @ApiResponse(responseCode = "200", description = "Acesso autorizado com perfil ADMIN")
    @ApiResponse(responseCode = "401", description = "Não autenticado")
    @ApiResponse(responseCode = "403", description = "Acesso negado para o perfil atual")
    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> adminOnly(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(Map.of(
                "message", "Área restrita de administrador acessada com sucesso",
                "user", userDetails != null ? userDetails.getUsername() : "anonymous"
        ));
    }
}
