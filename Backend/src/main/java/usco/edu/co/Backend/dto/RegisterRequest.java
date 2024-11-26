package usco.edu.co.Backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {
    private String nombreCompleto;
    private String email;
    private String telefono;
    private String direccion;
    private String municipio;
    private String password;
    private String confirmPassword;
}
