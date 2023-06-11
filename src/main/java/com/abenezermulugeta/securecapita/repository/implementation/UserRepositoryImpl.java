/*******************************************************************************
 * @author Abenezer Sefinew
 * @version 1.0
 * @since 06/09/2023
 */

package com.abenezermulugeta.securecapita.repository.implementation;

import com.abenezermulugeta.securecapita.domain.User;
import com.abenezermulugeta.securecapita.exception.ApiException;
import com.abenezermulugeta.securecapita.repository.RoleRepository;
import com.abenezermulugeta.securecapita.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Repository;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import static com.abenezermulugeta.securecapita.enumeration.RoleType.ROLE_USER;
import static com.abenezermulugeta.securecapita.enumeration.VerificationType.ACCOUNT;
import static com.abenezermulugeta.securecapita.query.UserQuery.*;
import static java.util.Objects.requireNonNull;

@Repository
@RequiredArgsConstructor
@Slf4j
public class UserRepositoryImpl implements UserRepository {
    private final NamedParameterJdbcTemplate jdbc;
    private final BCryptPasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;

    @Override
    public User create(User user) {
        // Check if the email is unique
        if(getEmailCount(user.getEmail().trim().toLowerCase()) > 0) throw new ApiException("Email already in use. Please use a different email and try again.");
        try {
            // Generates a new id for the user
            KeyHolder holder = new GeneratedKeyHolder();

            SqlParameterSource parameters = getSqlParameterSource(user);

            // Save the new User
            jdbc.update(INSERT_USER_QUERY, parameters, holder);

            // Get the generated id of the user
            user.setId(requireNonNull(holder.getKey().longValue()));

            // Add Role to the User
            roleRepository.addRoleToUser(user.getId(), ROLE_USER.name());

            // Send verification url
            String verificationUrl = getVerificationUrl(UUID.randomUUID().toString(), ACCOUNT.getType());

            // Save URL in verification code
            jdbc.update(INSERT_ACCOUNT_VERIFICATION_URL_QUERY, Map.of("userId", user.getId(), "url", verificationUrl));

            // Send email to user with verification URL
            // emailService.sendVerificationUrl(user.getFirstName(), user.getEmail(), verificationUrl, ACCOUNT);

            user.setEnabled(false);
            user.setNotLocked(true);

            // Return the newly created User
            return user;

            // If any errors, throw exception with proper message
        } catch (Exception exception){
            throw new ApiException("An error occurred. Please try again.");
        }
    }

    @Override
    public Collection list(int page, int pageSize) {
        return null;
    }

    @Override
    public User get(Long id) {
        return null;
    }

    @Override
    public User update(User data) {
        return null;
    }

    @Override
    public Boolean delete(Long id) {
        return null;
    }

    private Integer getEmailCount(String email) {
        return jdbc.queryForObject(COUNT_USER_EMAIL_QUERY, Map.of("email", email), Integer.class);
    }

    private SqlParameterSource getSqlParameterSource(User user) {
        return new MapSqlParameterSource()
                .addValue("firstName", user.getFirstName())
                .addValue("lastName", user.getLastName())
                .addValue("email", user.getEmail())
                .addValue("password", passwordEncoder.encode(user.getPassword()));
    }

    private String getVerificationUrl(String key, String accountType) {
        // ServletUriComponentsBuilder.fromCurrentContextPath() method returns the url that this server is running on
        return ServletUriComponentsBuilder.fromCurrentContextPath().path("/user/verify/" + accountType + '/' + key).toUriString();
    }
}
