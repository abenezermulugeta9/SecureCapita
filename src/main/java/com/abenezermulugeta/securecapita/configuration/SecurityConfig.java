/*******************************************************************************
 * @author Abenezer Sefinew
 * @version 1.0
 * @since 06/09/2023
 */

package com.abenezermulugeta.securecapita.configuration;

import com.abenezermulugeta.securecapita.handler.CustomAccessDeniedHandler;
import com.abenezermulugeta.securecapita.handler.CustomAuthenticationEntryPoint;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;

@Configuration
@EnableWebSecurity(debug = true)
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final BCryptPasswordEncoder passwordEncoder;
    private final CustomAccessDeniedHandler accessDeniedHandler;
    private final CustomAuthenticationEntryPoint authenticationEntryPoint;
    private final UserDetailsService userDetailsService;
    private static final String[] PUBLIC_URLS = {
            "/users/login/**",
            "/users/register/**"
    };
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // Disable csrf (since we are not sending requests from a web page) and disable cors filter
        http.csrf().disable().cors().disable();

        // Disable http cookie and session since we are not storing state in a RESTful service
        http.sessionManagement().sessionCreationPolicy(STATELESS);

        // Public URLs to be accessed without getting authenticated
        http.authorizeHttpRequests().requestMatchers(PUBLIC_URLS).permitAll();

        // 'DELETE' requests should be only for users with 'DELETE:USER' or 'DELETE:CUSTOMER' authorities accordingly
        http.authorizeHttpRequests().requestMatchers(DELETE, "/user/delete/**").hasAnyAuthority("DELETE:USER");
        http.authorizeHttpRequests().requestMatchers(DELETE, "/customer/delete/**").hasAnyAuthority("DELETE:CUSTOMER");

        // Custom exception for access denial of access
        http.exceptionHandling().accessDeniedHandler(accessDeniedHandler).authenticationEntryPoint(authenticationEntryPoint);

        // Every other request should be authenticated
        http.authorizeHttpRequests().anyRequest().authenticated();

        return http.build();
    }

    // Handles the user authentication
    @Bean
    public AuthenticationManager authenticationManager() {
        // DaoAuthenticationProvider is an implementation of AuthenticationProvider interface
        // It is responsible for authenticating a user based on information stored in a data access object (DAO)
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder);
        return new ProviderManager(authProvider);
    }
}
