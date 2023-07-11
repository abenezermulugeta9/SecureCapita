/*******************************************************************************
 * @author Abenezer Sefinew
 * @version 1.0
 * @since 06/09/2023
 */

package com.abenezermulugeta.securecapita.resource;

import com.abenezermulugeta.securecapita.domain.HttpResponse;
import com.abenezermulugeta.securecapita.domain.User;
import com.abenezermulugeta.securecapita.domain.UserPrincipal;
import com.abenezermulugeta.securecapita.dto.UserDTO;
import com.abenezermulugeta.securecapita.dtomapper.UserDTOMapper;
import com.abenezermulugeta.securecapita.exception.ApiException;
import com.abenezermulugeta.securecapita.form.LoginForm;
import com.abenezermulugeta.securecapita.form.PasswordResetForm;
import com.abenezermulugeta.securecapita.provider.TokenProvider;
import com.abenezermulugeta.securecapita.service.RoleService;
import com.abenezermulugeta.securecapita.service.UserService;
import com.abenezermulugeta.securecapita.utils.ExceptionUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.Map;

import static java.time.LocalTime.now;
import static java.util.Map.of;
import static org.springframework.security.authentication.UsernamePasswordAuthenticationToken.unauthenticated;

@RestController
@RequestMapping(path = "/users")
@RequiredArgsConstructor
public class UserResource {
    private final UserService userService;
    private final RoleService roleService;
    private final AuthenticationManager authenticationManager;
    private final TokenProvider tokenProvider;
    private final HttpServletRequest request;
    private final HttpServletResponse response;
    public static final String TOKEN_PREFIX = "Bearer ";

    @PostMapping("/login")
    public ResponseEntity<HttpResponse> login(@RequestBody @Valid LoginForm loginForm) {
        Authentication authentication = authenticateUser(loginForm.getEmail(), loginForm.getPassword());
        UserDTO userDTO = getAuthenticatedUser(authentication);
        return userDTO.isUsingMfa() ? sendVerificationCode(userDTO) : sendResponse(userDTO);
    }

    @PostMapping("/register")
    public ResponseEntity<HttpResponse> saveUser(@RequestBody @Valid User user) {
        UserDTO userDTO = userService.createUser(user);
        return ResponseEntity.created(getUri()).body(
                HttpResponse.builder()
                        .timeStamp(now().toString())
                        .data(of("user", userDTO))
                        .message("User registered.")
                        .httpStatus(HttpStatus.CREATED)
                        .statusCode(HttpStatus.CREATED.value())
                        .build());
    }

    @GetMapping("/verify/code/{email}/{code}")
    public ResponseEntity<HttpResponse> verifyCode(@PathVariable("email") String email, @PathVariable("code") String code) {
        UserDTO userDTO = userService.verifyCode(email, code);
        return ResponseEntity.ok().body(
                HttpResponse.builder()
                        .timeStamp(now().toString())
                        .data(of(
                                "user", userDTO,
                                "access_token", tokenProvider.createAccessToken(getUserPrincipal(userDTO)),
                                "refresh_token", tokenProvider.createRefreshToken(getUserPrincipal(userDTO))))
                        .message("Login successful.")
                        .httpStatus(HttpStatus.OK)
                        .statusCode(HttpStatus.OK.value())
                        .build());
    }

    /**
     * @param authentication - set from CustomAuthorizationFilter automatically by Spring and injected to the method
     */
    @GetMapping("/profile")
    public ResponseEntity<HttpResponse> profile(Authentication authentication) {
        // authentication.getName() holds the email of the currently authenticated user
        UserDTO userDTO = userService.getUserByEmail(authentication.getName());
        return ResponseEntity.ok().body(
                HttpResponse.builder()
                        .timeStamp(now().toString())
                        .data(of("user", userDTO))
                        .message("User profile retrieved.")
                        .httpStatus(HttpStatus.OK)
                        .statusCode(HttpStatus.OK.value())
                        .build());
    }

    // START - Reset password for users that can't log in
    @GetMapping("/reset-password/{email}")
    public ResponseEntity<HttpResponse> sendPasswordResetLink(@PathVariable("email") String email) {
        userService.sendPasswordResetLink(email);

        return ResponseEntity.ok().body(
                HttpResponse.builder()
                        .timeStamp(LocalDateTime.now().toString())
                        .message("Password resetting instruction sent to your email. Check you inbox. ")
                        .httpStatus(HttpStatus.OK)
                        .statusCode(HttpStatus.OK.value())
                        .build());
    }

    @GetMapping("/verify/password/{key}")
    public ResponseEntity<HttpResponse> verifyPasswordKey(@PathVariable("key") String key) {
        UserDTO userDTO = userService.verifyPasswordKey(key);

        return ResponseEntity.ok().body(
                HttpResponse.builder()
                        .timeStamp(LocalDateTime.now().toString())
                        .data(Map.of("user", userDTO))
                        .message("Enter a new password.")
                        .httpStatus(HttpStatus.OK)
                        .statusCode(HttpStatus.OK.value())
                        .build());
    }

    @PutMapping("/reset-password/{key}")
    public ResponseEntity<HttpResponse> resetPassword(@PathVariable("key") String key, @RequestBody @Valid PasswordResetForm passwordResetForm) {
        userService.resetPassword(key, passwordResetForm.getPassword(), passwordResetForm.getConfirmPassword());

        return ResponseEntity.ok().body(
                HttpResponse.builder()
                        .timeStamp(LocalDateTime.now().toString())
                        .message("Password changed.")
                        .httpStatus(HttpStatus.OK)
                        .statusCode(HttpStatus.OK.value())
                        .build());
    }

    // END - Reset password for users that can't log in

    @GetMapping("/verify/account/{key}")
    public ResponseEntity<HttpResponse> verifyAccount(@PathVariable("key") String key) {
        return ResponseEntity.ok().body(
                HttpResponse.builder()
                        .timeStamp(LocalDateTime.now().toString())
                        /**
                         * The following code snippet handles the scenario where the service checks the database for an enabled user.
                         * If the user is found enabled, it initiates the first message prompt. However, in case the service returns false,
                         * it may indicate a possibility of receiving a stale User object from the userService.verifyAccountKey() method.
                         * Consequently, the account undergoes a change, but the enabled property remains false.
                         * */
                        .message( userService.verifyAccountKey(key).isEnabled() ? "Account already verified" : "Account verified")
                        .httpStatus(HttpStatus.OK)
                        .statusCode(HttpStatus.OK.value())
                        .build());
    }

    @GetMapping("refresh/token")
    public ResponseEntity<HttpResponse> refreshToken(HttpServletRequest request) {
        if(isHeaderTokenValid(request)) {
            String token = request.getHeader(HttpHeaders.AUTHORIZATION).substring(TOKEN_PREFIX.length());
            UserDTO userDTO = userService.getUserByEmail(tokenProvider.getSubject(token, request));
            return ResponseEntity.ok().body(
                    HttpResponse.builder()
                            .timeStamp(LocalDateTime.now().toString())
                            .data(Map.of("user", userDTO, "access_token", tokenProvider.createAccessToken(getUserPrincipal(userDTO)), "refresh_token", token))
                            .message("Token refreshed")
                            .httpStatus(HttpStatus.OK)
                            .statusCode(HttpStatus.OK.value())
                            .build());
        }
        return ResponseEntity.ok().body(
                HttpResponse.builder()
                        .timeStamp(LocalDateTime.now().toString())
                        .reason("Refresh token is missing or invalid.")
                        .developerMessage("Refresh token is missing or invalid.")
                        .httpStatus(HttpStatus.OK)
                        .statusCode(HttpStatus.OK.value())
                        .build());
    }

    // This method checks if the token in the request header is valid with 3 checks mentioned below
    private boolean isHeaderTokenValid(HttpServletRequest request) {
        return request.getHeader(HttpHeaders.AUTHORIZATION) != null && // #1 Checks if the token exists in the Authorization Header
                request.getHeader(HttpHeaders.AUTHORIZATION).startsWith(TOKEN_PREFIX) && // #2 Checks if the token starts with "Bearer "
                tokenProvider.isTokenValid(tokenProvider.getSubject(request.getHeader(HttpHeaders.AUTHORIZATION).substring(TOKEN_PREFIX.length()), request), //  #3 Checks if the token is valid with tokenProvider.isTokenValid(email, token)
                        request.getHeader(HttpHeaders.AUTHORIZATION).substring(TOKEN_PREFIX.length()));
    }

    private Authentication authenticateUser(String email, String password) {
        try {
            // unauthenticated() is a factory method inside UsernamePasswordAuthenticationToken class that returns the object of UsernamePasswordAuthenticationToken
            Authentication authentication = authenticationManager.authenticate(unauthenticated(email, password));
            return authentication;
        } catch (Exception exception) {
            ExceptionUtils.processError(request, response, exception);
            throw new ApiException(exception.getMessage());
        }
    }

    private UserDTO getAuthenticatedUser(Authentication authentication) {
        return ((UserPrincipal) authentication.getPrincipal()).getUser();
    }

    private ResponseEntity<HttpResponse> sendVerificationCode(UserDTO userDTO) {
        userService.sendVerificationCode(userDTO);
        return ResponseEntity.ok().body(
                HttpResponse.builder()
                        .timeStamp(now().toString())
                        .data(of("user", userDTO))
                        .message("Verification code sent.")
                        .httpStatus(HttpStatus.OK)
                        .statusCode(HttpStatus.OK.value())
                        .build());
    }

    private ResponseEntity<HttpResponse> sendResponse(UserDTO userDTO) {
        return ResponseEntity.ok().body(
                HttpResponse.builder()
                        .timeStamp(now().toString())
                        .data(of(
                                "user", userDTO,
                                "access_token", tokenProvider.createAccessToken(getUserPrincipal(userDTO)),
                                "refresh_token", tokenProvider.createRefreshToken(getUserPrincipal(userDTO))))
                        .message("Login successful.")
                        .httpStatus(HttpStatus.OK)
                        .statusCode(HttpStatus.OK.value())
                        .build());
    }

    private UserPrincipal getUserPrincipal(UserDTO userDTO) {
        return new UserPrincipal(UserDTOMapper.toUser(userService.getUserByEmail(userDTO.getEmail())), roleService.getRoleByUserId(userDTO.getId()));
    }

    private URI getUri() {
        return URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/user/get/<userId>").toUriString());
    }
}

