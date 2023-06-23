/*******************************************************************************
 * @author Abenezer Sefinew
 * @version 1.0
 * @since 06/09/2023
 */

package com.abenezermulugeta.securecapita;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * The exclude property ignores the specified classes in the array
 * I ignored the SecurityAutoConfiguration class because, Spring Security will make all my requests to the server 'Unauthorized'
 */
@SpringBootApplication(exclude = { SecurityAutoConfiguration.class })
public class SecurecapitaApplication {
	private static final int PASSWORD_STRENGTH = 12;

	public static void main(String[] args) {
		SpringApplication.run(SecurecapitaApplication.class, args);
	}

	@Bean
	public BCryptPasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder(PASSWORD_STRENGTH);
	}

}
