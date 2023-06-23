/*******************************************************************************
 * @author Abenezer Sefinew
 * @version 1.0
 * @since 06/09/2023
 */

package com.abenezermulugeta.securecapita.dtomapper;

import com.abenezermulugeta.securecapita.domain.User;
import com.abenezermulugeta.securecapita.dto.UserDto;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

@Component
public class UserDTOMapper {
    public static UserDto fromUser(User user) {
        UserDto userDto = new UserDto();
        BeanUtils.copyProperties(user, userDto);
        return userDto;
    }

    public static User toUser(UserDto userDto) {
        User user = new User();
        BeanUtils.copyProperties(userDto, user);
        return user;
    }
}
