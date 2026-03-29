package com.ibisscore.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {

    @NotBlank(message = "Kullanıcı adı veya e-posta boş olamaz")
    private String usernameOrEmail;

    @NotBlank(message = "Şifre boş olamaz")
    private String password;
}
