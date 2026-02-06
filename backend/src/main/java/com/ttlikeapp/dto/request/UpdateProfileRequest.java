package com.ttlikeapp.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Update User Profile Request DTO
 * All fields optional - only provided fields will be updated
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProfileRequest {

    @Size(max = 100, message = "Display name cannot exceed 100 characters")
    private String displayName;

    @Email(message = "Please provide a valid email address")
    private String email;

    @Size(max = 500, message = "Bio cannot exceed 500 characters")
    private String bio;

    private String avatarUrl;

    private Boolean privateAccount;
}
