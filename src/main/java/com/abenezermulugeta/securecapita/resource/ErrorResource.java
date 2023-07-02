/*******************************************************************************
 * @author Abenezer Sefinew
 * @version 1.0
 * @since 06/09/2023
 */

package com.abenezermulugeta.securecapita.resource;

import com.abenezermulugeta.securecapita.domain.HttpResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
public class ErrorResource {
    @RequestMapping("/error")
    public ResponseEntity<HttpResponse> handleWildCardError() {
        return ResponseEntity.badRequest().body(
                HttpResponse.builder()
                        .timeStamp(LocalDateTime.now().toString())
                        .reason("This url was not found on the server.")
                        .httpStatus(HttpStatus.NOT_FOUND)
                        .statusCode(HttpStatus.NOT_FOUND.value())
                        .build());
    }

}
