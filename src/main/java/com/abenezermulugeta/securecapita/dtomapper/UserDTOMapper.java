/*******************************************************************************
 * @author Abenezer Sefinew
 * @version 1.0
 * @since 06/09/2023
 */

package com.abenezermulugeta.securecapita.dtomapper;

import com.abenezermulugeta.securecapita.domain.Role;
import com.abenezermulugeta.securecapita.domain.User;
import com.abenezermulugeta.securecapita.dto.UserDTO;
import org.springframework.beans.BeanUtils;

public class UserDTOMapper {
    public static UserDTO fromUser(User user) {
        UserDTO userDto = new UserDTO();
        BeanUtils.copyProperties(user, userDto);
        return userDto;
    }

    public static UserDTO fromUser(User user, Role role) {
        UserDTO userDto = new UserDTO();
        BeanUtils.copyProperties(user, userDto);
        userDto.setRoleName(role.getName());
        userDto.setPermissions(role.getPermission());
        return userDto;
    }

    public static User toUser(UserDTO userDto) {
        User user = new User();
        BeanUtils.copyProperties(userDto, user);
        return user;
    }
}
