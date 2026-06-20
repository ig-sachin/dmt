package com.dmt.backend.security;

import com.dmt.backend.metadata.screenrole.repository.DmtScreenRoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScreenAuthorizationService {

    private final DmtScreenRoleRepository repository;

    public void authorize(String screenCode) {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            log.warn(
                    "Screen authorization failed screenCode={} reason=not_authenticated",
                    screenCode
            );

            throw new AccessDeniedException(
                    "Access denied for screen " + screenCode);
        }

        List<String> userRoles =
                authentication.getAuthorities()
                        .stream()
                        .map(Object::toString)
                        .toList();

        List<String> allowedRoles =
                repository.findByScreenScreenCode(screenCode)
                        .stream()
                        .map(screenRole -> screenRole.getRoleName())
                        .toList();

        log.info(
                "Authorizing screen access username={} screenCode={} userRoles={} allowedRoles={}",
                authentication.getName(),
                screenCode,
                userRoles,
                allowedRoles
        );

        boolean authorized =
                userRoles.stream()
                        .anyMatch(allowedRoles::contains);

        if (!authorized) {
            log.warn(
                    "Screen authorization failed username={} screenCode={} userRoles={} allowedRoles={}",
                    authentication.getName(),
                    screenCode,
                    userRoles,
                    allowedRoles
            );

            throw new AccessDeniedException(
                    "Access denied for screen "
                            + screenCode);
        }

        log.info(
                "Screen authorization successful username={} screenCode={}",
                authentication.getName(),
                screenCode
        );
    }
}
