package io.a_caminho.backend.exception;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Estrutura padrão de erro retornada pela API")
public class ApiError {

    @Schema(description = "Data e hora do erro", example = "2026-09-30T22:30:00")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;

    @Schema(description = "Código de status HTTP", example = "400")
    private Integer code;

    @Schema(description = "Descrição textual do status HTTP", example = "BAD_REQUEST")
    private String status;

    @Schema(description = "Lista detalhada de mensagens de erro", example = "[\"E-mail já cadastrado\"]")
    private List<String> errors;

    @Schema(description = "Mensagem principal de erro", example = "E-mail já cadastrado")
    public String getMessage() {
        if (errors != null && !errors.isEmpty()) {
            return errors.get(0);
        }
        return null;
    }
}
