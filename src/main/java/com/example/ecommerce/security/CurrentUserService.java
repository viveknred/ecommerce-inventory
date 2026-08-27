package com.example.ecommerce.security;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.ecommerce.entity.Role;
import com.example.ecommerce.entity.User;
import com.example.ecommerce.repository.UserRepository;

/**
 * Resolves the caller behind the current JWT into a persistent {@link User}.
 *
 * <p>Phase 4 needs this in three places: to stamp a review with its author,
 * to work out which vendor a ROLE_VENDOR account may manage, and to attribute
 * uploaded media. Centralising it keeps that logic out of the controllers.
 */
@Service
public class CurrentUserService {

    private final UserRepository userRepository;

    public CurrentUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * @return the authenticated user
     * @throws AccessDeniedException when there is no authenticated principal,
     *                               or the principal no longer exists
     */
    @Transactional(readOnly = true)
    public User requireCurrentUser() {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()) {

            throw new AccessDeniedException(
                    "Authentication is required for this operation"
            );
        }

        String email = authentication.getName();

        return userRepository
                .findByEmail(email)
                .orElseThrow(() ->
                        new AccessDeniedException(
                                "Authenticated user '"
                                        + email
                                        + "' no longer exists"
                        ));
    }

    /**
     * Vendor id the caller is allowed to act on.
     *
     * @throws AccessDeniedException when a ROLE_VENDOR account has not been
     *                               linked to a vendor by an administrator
     */
    @Transactional(readOnly = true)
    public Long requireCurrentVendorId() {

        User user = requireCurrentUser();

        if (user.getVendor() == null) {

            throw new AccessDeniedException(
                    "Your account is not linked to a vendor. "
                            + "Ask an administrator to link it before "
                            + "managing marketplace products."
            );
        }

        return user.getVendor().getId();
    }

    @Transactional(readOnly = true)
    public boolean isCurrentUserAdmin() {
        return requireCurrentUser().getRole() == Role.ADMIN;
    }
}
