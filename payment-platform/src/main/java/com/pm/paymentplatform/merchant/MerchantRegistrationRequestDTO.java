package com.pm.paymentplatform.merchant;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class MerchantRegistrationRequestDTO {

    @Email
    @NotNull
    private String email;


    @NotNull
    @Size(min = 8, max = 72)
    private String password;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

}
