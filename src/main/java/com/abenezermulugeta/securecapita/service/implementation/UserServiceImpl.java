/*******************************************************************************
 * @author Abenezer Sefinew
 * @version 1.0
 * @since 06/09/2023
 */

package com.abenezermulugeta.securecapita.service.implementation;

import com.abenezermulugeta.securecapita.domain.Role;
import com.abenezermulugeta.securecapita.domain.User;
import com.abenezermulugeta.securecapita.dto.UserDTO;
import com.abenezermulugeta.securecapita.dtomapper.UserDTOMapper;
import com.abenezermulugeta.securecapita.repository.RoleRepository;
import com.abenezermulugeta.securecapita.repository.UserRepository;
import com.abenezermulugeta.securecapita.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository<User> userRepository;
    private final RoleRepository<Role> roleRepository;

    @Override
    public UserDTO createUser(User user) {
        return mapToUserDTO(userRepository.create(user));
    }

    @Override
    public UserDTO getUserByEmail(String email) {
        return mapToUserDTO(userRepository.getUserByEmail(email));
    }

    @Override
    public void sendVerificationCode(UserDTO userDto) {
        userRepository.sendVerificationCode(userDto);
    }

    @Override
    public UserDTO verifyCode(String email, String code) {
        return mapToUserDTO(userRepository.verifyCode(email, code));
    }

    @Override
    public void sendPasswordResetLink(String email) {
        userRepository.sendPasswordResetLink(email);
    }

    @Override
    public UserDTO verifyPasswordKey(String key) {
        return mapToUserDTO(userRepository.verifyPasswordKey(key));
    }

    @Override
    public void resetPassword(String key, String password, String confirmPassword) {
        userRepository.resetPassword(key, password, confirmPassword);
    }

    @Override
    public UserDTO verifyAccountKey(String key) {
        return mapToUserDTO(userRepository.verifyAccountKey(key));
    }

    private UserDTO mapToUserDTO(User user) {
        return UserDTOMapper.fromUser(user, roleRepository.getRoleByUserId(user.getId()));
    }
}
