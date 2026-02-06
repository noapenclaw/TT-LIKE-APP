package com.ttlikeapp.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * User Login Request DTO
 * Credentials can be either username or email
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {

    @NotBlank(message = "Username or email is required")
    private String username;  // Can be username OR email

    @NotBlank(message = "Password is required")
    private String password;

    private String deviceId;  // For tracking and security
    private String deviceType; // iOS, Android, Web
}
