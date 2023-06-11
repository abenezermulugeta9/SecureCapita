/*******************************************************************************
 * @author Abenezer Sefinew
 * @version 1.0
 * @since 06/09/2023
 */

package com.abenezermulugeta.securecapita.repository;

import com.abenezermulugeta.securecapita.domain.Role;
import java.util.Collection;

public interface RoleRepository<T extends Role> {
    /* Basic  CRUD Operations */
    T create(T data);
    Collection<T> list(int page, int pageSize);
    T get(Long id);
    T update(T data);
    Boolean delete(Long id);

    /* Complex Operations */
    void addRoleToUser(Long userId, String roleName);
}
