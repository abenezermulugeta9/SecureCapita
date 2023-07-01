/*******************************************************************************
 * @author Abenezer Sefinew
 * @version 1.0
 * @since 06/09/2023
 */

package com.abenezermulugeta.securecapita.resource;

import com.abenezermulugeta.securecapita.domain.HttpResponse;
import com.abenezermulugeta.securecapita.domain.User;
import com.abenezermulugeta.securecapita.domain.UserPrincipal;
import com.abenezermulugeta.securecapita.dto.UserDto;
import com.abenezermulugeta.securecapita.dtomapper.UserDTOMapper;
import com.abenezermulugeta.securecapita.form.LoginForm;
import com.abenezermulugeta.securecapita.provider.TokenProvider;
import com.abenezermulugeta.securecapita.service.RoleService;
import com.abenezermulugeta.securecapita.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

import static java.time.LocalTime.now;
import static java.util.Map.of;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.security.authentication.UsernamePasswordAuthenticationToken.unauthenticated;

@RestController
@RequestMapping(path = "/users")
@RequiredArgsConstructor
public class UserResource {
    private final UserService userService;
    private final RoleService roleService;
    private final AuthenticationManager authenticationManager;
    private final TokenProvider tokenProvider;

    @PostMapping("/login")
    public ResponseEntity<HttpResponse> login(@RequestBody @Valid LoginForm loginForm) {
        // unauthenticated() is a factory method inside UsernamePasswordAuthenticationToken class that returns the object of UsernamePasswordAuthenticationToken
        authenticationManager.authenticate(unauthenticated(loginForm.getEmail(), loginForm.getPassword()));
        UserDto userDto = userService.getUserByEmail(loginForm.getEmail());
        return userDto.isUsingMfa() ? sendVerificationCode(userDto) : sendResponse(userDto);
    }

    @PostMapping("/register")
    public ResponseEntity<HttpResponse> saveUser(@RequestBody @Valid User user) {
        UserDto userDto = userService.createUser(user);
        return ResponseEntity.created(getUri()).body(
                HttpResponse.builder()
                        .timeStamp(now().toString())
                        .data(of("user", userDto))
                        .message("User registered.")
                        .httpStatus(CREATED)
                        .statusCode(CREATED.value())
                        .build());
    }

    @GetMapping("/verify/code/{email}/{code}")
    public ResponseEntity<HttpResponse> verifyCode(@PathVariable("email") String email, @PathVariable("code") String code) {
        UserDto userDto = userService.verifyCode(email, code);
        return ResponseEntity.ok().body(
                HttpResponse.builder()
                        .timeStamp(now().toString())
                        .data(of(
                                "user", userDto,
                                "access_token", tokenProvider.createAccessToken(getUserPrincipal(userDto)),
                                "refresh_token", tokenProvider.createRefreshToken(getUserPrincipal(userDto))))
                        .message("Login successful.")
                        .httpStatus(OK)
                        .statusCode(OK.value())
                        .build());
    }

    // The argument for the method "Authentication" is set from CustomAuthorizationFilter automatically, Spring took care of it
    @GetMapping("/profile")
    public ResponseEntity<HttpResponse> profile(Authentication authentication) {
        // authentication.getName() holds the email of the currently authenticated user
        UserDto userDto = userService.getUserByEmail(authentication.getName());
        return ResponseEntity.ok().body(
                HttpResponse.builder()
                        .timeStamp(now().toString())
                        .data(of("user", userDto))
                        .message("User profile retrieved.")
                        .httpStatus(OK)
                        .statusCode(OK.value())
                        .build());
    }

    private ResponseEntity<HttpResponse> sendVerificationCode(UserDto userDto) {
        userService.sendVerificationCode(userDto);
        return ResponseEntity.ok().body(
                HttpResponse.builder()
                        .timeStamp(now().toString())
                        .data(of("user", userDto))
                        .message("Verification code sent.")
                        .httpStatus(OK)
                        .statusCode(OK.value())
                        .build());
    }

    private ResponseEntity<HttpResponse> sendResponse(UserDto userDto) {
        return ResponseEntity.ok().body(
                HttpResponse.builder()
                        .timeStamp(now().toString())
                        .data(of(
                                "user", userDto,
                                "access_token", tokenProvider.createAccessToken(getUserPrincipal(userDto)),
                                "refresh_token", tokenProvider.createRefreshToken(getUserPrincipal(userDto))))
                        .message("Login successful.")
                        .httpStatus(OK)
                        .statusCode(OK.value())
                        .build());
    }

    private UserPrincipal getUserPrincipal(UserDto userDto) {
        return new UserPrincipal(UserDTOMapper.toUser(userService.getUserByEmail(userDto.getEmail())), roleService.getRoleByUserId(userDto.getId()).getPermission());
    }

    private URI getUri() {
        return URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/user/get/<userId>").toUriString());
    }
}

