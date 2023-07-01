/*******************************************************************************
 * @author Abenezer Sefinew
 * @version 1.0
 * @since 06/09/2023
 */

package com.abenezermulugeta.securecapita.filter;

import com.abenezermulugeta.securecapita.provider.TokenProvider;
import com.abenezermulugeta.securecapita.utils.ExceptionUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@Component
@RequiredArgsConstructor
@Slf4j
public class CustomAuthorizationFilter extends OncePerRequestFilter {
    private final TokenProvider tokenProvider;
    private static final String[] PUBLIC_ROUTES = {
            "/users/login",
            "/users/register",
            "/users/verify/code"
    };
    private static final String HTTP_OPTIONS_METHOD = "OPTIONS";
    private static final String TOKEN_PREFIX = "Bearer ";
    protected static final String TOKEN_KEY = "token";
    protected static final String EMAIL_KEY = "email";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {
            Map<String, String> values = getRequestValues(request);
            String token = getTokenFromRequest(request);
            if(tokenProvider.isTokenValid(values.get(EMAIL_KEY), token)) {
                List<GrantedAuthority> authorities = tokenProvider.getAuthorities(values.get(TOKEN_KEY));

                // Setting the Authentication object for the servlet/ request, this can be accessed from anywhere in the code.
                Authentication authentication = tokenProvider.getAuthentication(values.get(EMAIL_KEY), authorities, request);

                // Set the authenticated user to the context
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } else {
                SecurityContextHolder.clearContext();
            }
            // Allowing the filter chain to continue to the next filters in the pipeline
            filterChain.doFilter(request, response);
        } catch (Exception exception) {
            log.error(exception.getMessage());
            ExceptionUtils.processError(request, response, exception);
        }
    }

    // This method specifies the conditions where the above filter does not apply.
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        return
                        request.getHeader(AUTHORIZATION) == null ||
                        !request.getHeader(AUTHORIZATION).startsWith(TOKEN_PREFIX) ||
                        request.getMethod().equalsIgnoreCase(HTTP_OPTIONS_METHOD) ||
                        Arrays.asList(PUBLIC_ROUTES).contains(request.getRequestURI());
    }

    private Map<String, String> getRequestValues(HttpServletRequest request) {
        return Map.of(EMAIL_KEY, tokenProvider.getSubject(getTokenFromRequest(request), request), TOKEN_KEY, getTokenFromRequest(request));
    }
    private String getTokenFromRequest(HttpServletRequest request) {
        return Optional.ofNullable(request.getHeader(AUTHORIZATION))
                .filter(header -> header.startsWith(TOKEN_PREFIX))
                .map(token -> token.replace(TOKEN_PREFIX, StringUtils.EMPTY)).get();
    }
}
