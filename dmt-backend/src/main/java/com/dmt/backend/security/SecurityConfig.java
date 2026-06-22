package com.dmt.backend.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
@RequiredArgsConstructor
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CorsConfigurationSource corsConfigurationSource;

    /**
     * Endpoints under these paths manage screen/column/filter/procedure/role
     * metadata - i.e. the Admin Console surface from the functional spec. They are
     * restricted to ROLE_ADMIN here at the URL level as the primary defense, with
     * @PreAuthorize on the individual controllers (enabled via @EnableMethodSecurity
     * above) as a second, independent layer in case a new admin controller is added
     * later without updating this matcher list.
     *
     * Note: "/api/dropdowns/**" intentionally does NOT cover
     * "/api/dropdowns/{code}/options" - that runtime lookup endpoint is open to any
     * authenticated user and enforces its own screen-based authorization inside
     * DropdownEngineService, since a dropdown has no single owning screen and can't
     * be gated by a blanket admin-only rule. The more specific matcher for it is
     * registered first, below, so it takes precedence over the wildcard.
     */
    private static final String[] ADMIN_CONFIG_PATHS = {
            "/api/screens/**",
            "/api/columns/**",
            "/api/filters/**",
            "/api/procedures/**",
            "/api/procedure-params/**",
            "/api/dropdowns/**",
            "/api/dropdown-params/**",
            "/api/validations/**",
            "/api/screen-roles/**",
            "/api/users/**"
    };

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http) throws Exception {

        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS))
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint((request, response, authException) ->
                                response.sendError(
                                        HttpStatus.UNAUTHORIZED.value(),
                                        HttpStatus.UNAUTHORIZED.getReasonPhrase()))
                        .accessDeniedHandler((request, response, accessDeniedException) ->
                                response.sendError(
                                        HttpStatus.FORBIDDEN.value(),
                                        HttpStatus.FORBIDDEN.getReasonPhrase()))
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(org.springframework.http.HttpMethod.OPTIONS, "/**")
                        .permitAll()
                        .requestMatchers("/auth/login").permitAll()
                        .requestMatchers("/actuator/health").permitAll()
                        // Must be registered before the ADMIN_CONFIG_PATHS wildcard
                        // below, since Spring Security uses first-match-wins.
                        .requestMatchers("/api/dropdowns/*/options").authenticated()
                        .requestMatchers(ADMIN_CONFIG_PATHS).hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }
}