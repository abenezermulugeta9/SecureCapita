/*******************************************************************************
 * @author Abenezer Sefinew
 * @version 1.0
 * @since 06/09/2023
 */

package com.abenezermulugeta.securecapita.form;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class LoginForm {
    @NotEmpty(message = "Email cannot be empty.")
    @Email(message = "Invalid email. Please enter a valid email address.")
    private String email;

    @NotEmpty(message = "Password cannot be empty.")
    private String password;
}
