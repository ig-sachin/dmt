package com.dmt.backend;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

class SecurityIntegrationTest extends AbstractDmtIntegrationTest {

    @Test
    void shouldRejectUnauthorizedAccess() {

        ResponseEntity<String> response = restTemplate.exchange(
                url("/api/screens"),
                HttpMethod.GET,
                HttpEntity.EMPTY,
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void shouldRejectInvalidJwt() {

        HttpHeaders headers = new HttpHeaders();

        headers.setBearerAuth("abc.xyz.fake");

        ResponseEntity<String> response = restTemplate.exchange(
                url("/api/screens"),
                HttpMethod.GET,
                new HttpEntity<>(null, headers),
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }
}
