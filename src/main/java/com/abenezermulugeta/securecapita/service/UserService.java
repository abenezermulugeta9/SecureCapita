/*******************************************************************************
 * @author Abenezer Sefinew
 * @version 1.0
 * @since 06/09/2023
 */

package com.abenezermulugeta.securecapita.service;

import com.abenezermulugeta.securecapita.domain.User;
import com.abenezermulugeta.securecapita.dto.UserDTO;

public interface UserService {
    UserDTO createUser(User user);
    UserDTO getUserByEmail(String email);
    void sendVerificationCode(UserDTO userDto);
    UserDTO verifyCode(String email, String code);
    void sendPasswordResetLink(String email);
    UserDTO verifyPasswordKey(String key);

    void resetPassword(String key, String password, String confirmPassword);

    UserDTO verifyAccountKey(String key);
}
