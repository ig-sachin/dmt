package com.dmt.backend.security;

import com.dmt.backend.metadata.screenrole.entity.DmtScreenRole;
import com.dmt.backend.metadata.screenrole.entity.PermissionType;
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

    /**
     * Authorizes the current user for a specific operation on a screen.
     * Screen-level access alone is not sufficient - the matching DmtScreenRole row
     * for one of the user's roles must also permit the requested operation.
     */
    public void authorize(String screenCode, PermissionType permissionType) {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            log.warn(
                    "Screen authorization failed screenCode={} permission={} reason=not_authenticated",
                    screenCode,
                    permissionType
            );

            throw new AccessDeniedException(
                    "Access denied for screen " + screenCode);
        }

        List<String> userRoles =
                authentication.getAuthorities()
                        .stream()
                        .map(Object::toString)
                        .toList();

        List<DmtScreenRole> screenRoles =
                repository.findByScreenScreenCode(screenCode);

        boolean authorized =
                screenRoles.stream()
                        .filter(screenRole -> userRoles.contains(screenRole.getRoleName()))
                        .anyMatch(screenRole -> screenRole.permits(permissionType));

        log.info(
                "Authorizing screen operation username={} screenCode={} permission={} userRoles={} authorized={}",
                authentication.getName(),
                screenCode,
                permissionType,
                userRoles,
                authorized
        );

        if (!authorized) {
            log.warn(
                    "Screen authorization failed username={} screenCode={} permission={} userRoles={}",
                    authentication.getName(),
                    screenCode,
                    permissionType,
                    userRoles
            );

            throw new AccessDeniedException(
                    "Access denied for screen "
                            + screenCode);
        }

        log.info(
                "Screen authorization successful username={} screenCode={} permission={}",
                authentication.getName(),
                screenCode,
                permissionType
        );
    }
}