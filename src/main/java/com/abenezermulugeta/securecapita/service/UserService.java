/*******************************************************************************
 * @author Abenezer Sefinew
 * @version 1.0
 * @since 06/09/2023
 */

package com.abenezermulugeta.securecapita.service;

import com.abenezermulugeta.securecapita.domain.User;
import com.abenezermulugeta.securecapita.dto.UserDto;

public interface UserService {
    UserDto createUser(User user);

    UserDto getUserByEmail(String email);

    void sendVerificationCode(UserDto userDto);

    User getUser(String email);

    UserDto verifyCode(String email, String code);
}
