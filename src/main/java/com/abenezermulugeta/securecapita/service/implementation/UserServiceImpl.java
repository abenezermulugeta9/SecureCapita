/*******************************************************************************
 * @author Abenezer Sefinew
 * @version 1.0
 * @since 06/09/2023
 */

package com.abenezermulugeta.securecapita.service.implementation;

import com.abenezermulugeta.securecapita.domain.User;
import com.abenezermulugeta.securecapita.dto.UserDto;
import com.abenezermulugeta.securecapita.dtomapper.UserDTOMapper;
import com.abenezermulugeta.securecapita.repository.UserRepository;
import com.abenezermulugeta.securecapita.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository<User> userRepository;

    @Override
    public UserDto createUser(User user) {
        return UserDTOMapper.fromUser(userRepository.create(user));
    }

    @Override
    public UserDto getUserByEmail(String email) {
        return UserDTOMapper.fromUser(userRepository.getUserByEmail(email));
    }

    @Override
    public void sendVerificationCode(UserDto userDto) {
        userRepository.sendVerificationCode(userDto);
    }
}
