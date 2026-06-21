package com.dmt.backend;

import com.dmt.backend.metadata.screenrole.entity.DmtScreenRole;
import com.dmt.backend.metadata.screenrole.entity.PermissionType;
import com.dmt.backend.metadata.screenrole.repository.DmtScreenRoleRepository;
import com.dmt.backend.security.ScreenAuthorizationService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ScreenAuthorizationServiceTest {

    @Mock
    private DmtScreenRoleRepository repository;

    @AfterEach
    void tearDown() {

        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldAllowUserWhenAnyRoleMatchesScreenRoleMetadata() {

        ScreenAuthorizationService service =
                new ScreenAuthorizationService(repository);

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        "sachin",
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
                )
        );

        when(repository.findByScreenScreenCode("CUSTOMER"))
                .thenReturn(List.of(screenRole("ROLE_ADMIN", true, true, true, true)));

        service.authorize("CUSTOMER", PermissionType.VIEW);
    }

    @Test
    void shouldRejectUserWhenNoRoleMatchesScreenRoleMetadata() {

        ScreenAuthorizationService service =
                new ScreenAuthorizationService(repository);

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        "regular-user",
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_USER"))
                )
        );

        when(repository.findByScreenScreenCode("EMPLOYEE"))
                .thenReturn(List.of(screenRole("ROLE_ADMIN", true, true, true, true)));

        assertThatThrownBy(() -> service.authorize("EMPLOYEE", PermissionType.VIEW))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("Access denied for screen EMPLOYEE");
    }

    @Test
    void shouldRejectWhenNoAuthenticationExists() {

        ScreenAuthorizationService service =
                new ScreenAuthorizationService(repository);

        assertThatThrownBy(() -> service.authorize("CUSTOMER", PermissionType.VIEW))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("Access denied for screen CUSTOMER");
    }

    @Test
    void shouldAllowViewButRejectDeleteForReadOnlyRole() {

        ScreenAuthorizationService service =
                new ScreenAuthorizationService(repository);

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        "viewer-user",
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_VIEWER"))
                )
        );

        when(repository.findByScreenScreenCode("CUSTOMER"))
                .thenReturn(List.of(screenRole("ROLE_VIEWER", true, false, false, false)));

        service.authorize("CUSTOMER", PermissionType.VIEW);

        assertThatThrownBy(() -> service.authorize("CUSTOMER", PermissionType.DELETE))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("Access denied for screen CUSTOMER");
    }

    @Test
    void shouldAllowWhenAnyOfTheUsersMultipleRolesPermitsTheOperation() {

        ScreenAuthorizationService service =
                new ScreenAuthorizationService(repository);

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        "dual-role-user",
                        null,
                        List.of(
                                new SimpleGrantedAuthority("ROLE_VIEWER"),
                                new SimpleGrantedAuthority("ROLE_OPERATIONS"))
                )
        );

        when(repository.findByScreenScreenCode("CUSTOMER"))
                .thenReturn(List.of(
                        screenRole("ROLE_VIEWER", true, false, false, false),
                        screenRole("ROLE_OPERATIONS", true, true, true, false)
                ));

        service.authorize("CUSTOMER", PermissionType.INSERT);

        assertThatThrownBy(() -> service.authorize("CUSTOMER", PermissionType.DELETE))
                .isInstanceOf(AccessDeniedException.class);
    }

    private DmtScreenRole screenRole(
            String roleName,
            boolean canView,
            boolean canInsert,
            boolean canUpdate,
            boolean canDelete) {

        DmtScreenRole screenRole = new DmtScreenRole();

        screenRole.setRoleName(roleName);
        screenRole.setCanView(canView);
        screenRole.setCanInsert(canInsert);
        screenRole.setCanUpdate(canUpdate);
        screenRole.setCanDelete(canDelete);

        return screenRole;
    }
}