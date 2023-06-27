/*******************************************************************************
 * @author Abenezer Sefinew
 * @version 1.0
 * @since 06/09/2023
 */

package com.abenezermulugeta.securecapita.service.implementation;

import com.abenezermulugeta.securecapita.domain.Role;
import com.abenezermulugeta.securecapita.repository.RoleRepository;
import com.abenezermulugeta.securecapita.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {
    private final RoleRepository<Role> roleRepository;

    @Override
    public Role getRoleByUserId(long userId) {
        return roleRepository.getRoleByUserId(userId);
    }
}
