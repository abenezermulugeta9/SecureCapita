/*******************************************************************************
 * @author Abenezer Sefinew
 * @version 1.0
 * @since 06/09/2023
 */

package com.abenezermulugeta.securecapita.service;

import com.abenezermulugeta.securecapita.domain.Role;

public interface RoleService {
    Role getRoleByUserId(long userId);
}
