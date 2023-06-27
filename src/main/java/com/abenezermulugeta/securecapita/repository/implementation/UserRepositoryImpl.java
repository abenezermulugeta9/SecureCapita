/*******************************************************************************
 * @author Abenezer Sefinew
 * @version 1.0
 * @since 06/09/2023
 */

package com.abenezermulugeta.securecapita.repository.implementation;

import com.abenezermulugeta.securecapita.domain.Role;
import com.abenezermulugeta.securecapita.domain.User;
import com.abenezermulugeta.securecapita.domain.UserPrincipal;
import com.abenezermulugeta.securecapita.dto.UserDto;
import com.abenezermulugeta.securecapita.exception.ApiException;
import com.abenezermulugeta.securecapita.repository.RoleRepository;
import com.abenezermulugeta.securecapita.repository.UserRepository;
import com.abenezermulugeta.securecapita.rowmapper.UserRowMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Repository;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.Collection;
import java.util.Date;
import java.util.UUID;

import static com.abenezermulugeta.securecapita.enumeration.RoleType.ROLE_USER;
import static com.abenezermulugeta.securecapita.enumeration.VerificationType.ACCOUNT;
import static com.abenezermulugeta.securecapita.query.UserQuery.*;
import static java.util.Map.of;
import static java.util.Objects.requireNonNull;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.apache.commons.lang3.time.DateFormatUtils.format;
import static org.apache.commons.lang3.time.DateUtils.addDays;

@Repository
@RequiredArgsConstructor
@Slf4j
public class UserRepositoryImpl implements UserRepository<User>, UserDetailsService {
    private final NamedParameterJdbcTemplate jdbc;
    private final RoleRepository<Role> roleRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final String USER_NOT_FOUND = "User not found in the database.";
    private final String DATE_FORMAT = "yyyy-MM-dd hh:mm:ss";

    @Override
    public User create(User user) {
        // Check if the email is unique
        if (getEmailCount(user.getEmail().trim().toLowerCase()) > 0)
            throw new ApiException("Email already in use. Please use a different email and try again.");
        try {
            // Generates a new id for the user
            KeyHolder holder = new GeneratedKeyHolder();

            SqlParameterSource parameters = getSqlParametersForCreatingUser(user);

            // Save the new User
            jdbc.update(INSERT_USER_QUERY, parameters, holder);

            // Get the generated id of the user
            user.setId(requireNonNull(holder.getKey().longValue()));

            // Add Role to the User
            roleRepository.addRoleToUser(user.getId(), ROLE_USER.name());

            // Send verification url
            String verificationUrl = getVerificationUrl(UUID.randomUUID().toString(), ACCOUNT.getType());

            // Save URL in verification code
            jdbc.update(INSERT_ACCOUNT_VERIFICATION_URL_QUERY, of("userId", user.getId(), "url", verificationUrl));

            // Send email to user with verification URL
            // emailService.sendVerificationUrl(user.getFirstName(), user.getEmail(), verificationUrl, ACCOUNT);

            user.setEnabled(false);
            user.setNotLocked(true);

            // Return the newly created User
            return user;

            // If any errors, throw exception with proper message
        } catch (Exception exception) {
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

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = getUserByEmail(email);
        if (user == null) {
            log.error(USER_NOT_FOUND);
            throw new UsernameNotFoundException(USER_NOT_FOUND);
        } else {
            log.info("User found in the database: {}", email);
            return new UserPrincipal(user, roleRepository.getRoleByUserId(user.getId()).getPermission());
        }
    }

    @Override
    public User getUserByEmail(String email) {
        try {
            User user = jdbc.queryForObject(SELECT_USER_BY_EMAIL_QUERY, of("email", email), new UserRowMapper());
            return user;
        } catch (EmptyResultDataAccessException exception) {
            throw new ApiException("No User found by email: " + email);
        } catch (Exception exception) {
            log.error(exception.getMessage());
            throw new ApiException("An error occurred. Please try again.");
        }
    }

    @Override
    public void sendVerificationCode(UserDto userDto) {
        String expirationDate = format(addDays(new Date(), 1), DATE_FORMAT);
        String verificationCode = randomAlphabetic(8).toUpperCase();

        try {
            jdbc.update(DELETE_VERIFICATION_CODE_BY_USER_ID_QUERY, of("id", userDto.getId()));
            jdbc.update(INSERT_VERIFICATION_CODE_QUERY, of("userId", userDto.getId(), "verificationCode", verificationCode, "expirationDate", expirationDate));

            /**
             * Uncomment this line to activate two-factor authentication using SMS
             * sendSMS(userDto.getPhone(), "From: SecureCapita \n Use the following code within 24 hours to enable two-factor authentication. \n Verification Code: " + verificationCode);
             * */
            log.info("Verification code: {}", verificationCode);
        } catch (Exception exception) {
            log.error(exception.getMessage());
            throw new ApiException("An error occurred. Please try again");
        }
    }

    @Override
    public User verifyCode(String email, String code) {
        if(isVerificationCodeExpired(code)) throw new ApiException("This code has expired. Please login again.");
        try {
            User userByCode = jdbc.queryForObject(SELECT_USER_BY_CODE_QUERY, of("code", code), new UserRowMapper());
            User userByEmail = jdbc.queryForObject(SELECT_USER_BY_EMAIL_QUERY, of("email", email), new UserRowMapper());

            if(userByCode.getEmail().equalsIgnoreCase(userByEmail.getEmail())) {
                jdbc.update(DELETE_CODE, of("code", code));
                return userByCode;
            } else {
                throw new ApiException("Code is invalid. Can you check please?");
            }
        } catch (EmptyResultDataAccessException exception) {
            throw new ApiException("Could not find record.");
        } catch (Exception exception) {
            throw new ApiException("An error occurred. Please try again.");
        }
    }

    private boolean isVerificationCodeExpired(String code) {
        try {
            return jdbc.queryForObject(SELECT_CODE_EXPIRATION_QUERY, of("code", code), Boolean.class);
        } catch (EmptyResultDataAccessException exception) {
            throw new ApiException("This code is not valid. Please try logging in again.");
        } catch (Exception exception) {
            throw new ApiException(("An error occurred. Please try again."));
        }
    }

    private SqlParameterSource getSqlParametersForCreatingUser(User user) {
        return new MapSqlParameterSource()
                .addValue("firstName", user.getFirstName())
                .addValue("lastName", user.getLastName())
                .addValue("email", user.getEmail())
                .addValue("password", passwordEncoder.encode(user.getPassword()));
    }

    private Integer getEmailCount(String email) {
        return jdbc.queryForObject(COUNT_USER_EMAIL_QUERY, of("email", email), Integer.class);
    }

    private String getVerificationUrl(String key, String accountType) {
        // ServletUriComponentsBuilder.fromCurrentContextPath() method returns the url that this server is running on
        return ServletUriComponentsBuilder.fromCurrentContextPath().path("/user/verify/" + accountType + '/' + key).toUriString();
    }
}
