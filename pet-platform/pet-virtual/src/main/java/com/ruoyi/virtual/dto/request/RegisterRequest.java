package com.ruoyi.virtual.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 邮箱注册请求
 */
public class RegisterRequest
{
    @NotBlank(message = "Username cannot be empty")
    @Size(min = 3, max = 100, message = "Username must be 3-100 characters")
    private String username;

    @NotBlank(message = "Email cannot be empty")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Password cannot be empty")
    @Size(min = 6, max = 50, message = "Password must be 6-50 characters")
    private String password;

    @NotBlank(message = "Verification code cannot be empty")
    @Size(min = 4, max = 8, message = "Invalid verification code length")
    private String verificationCode;

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getVerificationCode() { return verificationCode; }
    public void setVerificationCode(String verificationCode) { this.verificationCode = verificationCode; }
}
