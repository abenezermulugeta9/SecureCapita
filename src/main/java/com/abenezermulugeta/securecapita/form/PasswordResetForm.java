/*******************************************************************************
 * @author Abenezer Sefinew
 * @version 1.0
 * @since 06/09/2023
 */

package com.abenezermulugeta.securecapita.form;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class PasswordResetForm {
    @NotEmpty(message = "Password cannot be empty.")
    public String password;
    @NotEmpty(message = "Confirm password cannot be empty.")
    public String confirmPassword;
}
