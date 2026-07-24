package com.launchstack.user.service;

import com.launchstack.auth.security.AuthenticatedUserPrincipal;
import com.launchstack.common.exception.BadRequestException;
import com.launchstack.common.exception.NotFoundException;
import com.launchstack.common.response.PageResponse;
import com.launchstack.role.Role;
import com.launchstack.role.RoleRepository;
import com.launchstack.user.dto.CreateUserRequest;
import com.launchstack.user.dto.CurrentUserProfileResponse;
import com.launchstack.user.dto.UpdateCurrentUserRequest;
import com.launchstack.user.dto.UpdateUserRequest;
import com.launchstack.user.dto.UpdateUserStatusRequest;
import com.launchstack.user.dto.UserDetailResponse;
import com.launchstack.user.dto.UserResponse;
import com.launchstack.user.entity.User;
import com.launchstack.user.repository.UserRepository;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private static final String ROLE_ADMIN = "ROLE_ADMIN";
    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of("id", "email", "firstName", "lastName", "enabled", "createdAt", "updatedAt");

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public PageResponse<UserResponse> listUsers(
            int page,
            int size,
            String sortBy,
            String sortDirection,
            String email,
            Boolean enabled,
            String role) {
        String normalizedSortBy = normalizeSortBy(sortBy);
        Sort.Direction direction = "asc".equalsIgnoreCase(sortDirection) ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, normalizedSortBy));
        Specification<User> specification = userFilterSpecification(email, enabled, role);
        Page<UserResponse> responsePage = userRepository.findAll(specification, pageable).map(this::toUserResponse);
        return PageResponse.from(responsePage);
    }

    @Transactional(readOnly = true)
    public UserDetailResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User was not found."));
        return toUserDetailResponse(user);
    }

    @Transactional
    public UserDetailResponse createUser(CreateUserRequest request) {
        String normalizedEmail = normalizeEmail(request.email());
        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new BadRequestException("Email is already registered.");
        }

        User user = new User(
                normalizedEmail,
                passwordEncoder.encode(request.password()),
                request.firstName().trim(),
                request.lastName().trim());
        user.setEnabled(request.enabled());

        Set<Role> roles = resolveRoles(request.roles());
        user.getRoles().addAll(roles);

        User savedUser = userRepository.save(user);
        return toUserDetailResponse(savedUser);
    }

    @Transactional
    public UserDetailResponse updateUser(Long id, UpdateUserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User was not found."));

        Set<Role> roles = resolveRoles(request.roles());
        enforceAdminRoleAndStatusGuards(user, roles, request.enabled(), user.isAccountNonLocked());

        user.setFirstName(request.firstName().trim());
        user.setLastName(request.lastName().trim());
        user.setEnabled(request.enabled());
        user.getRoles().clear();
        user.getRoles().addAll(roles);

        User savedUser = userRepository.save(user);
        return toUserDetailResponse(savedUser);
    }

    @Transactional
    public UserDetailResponse updateUserStatus(Long id, UpdateUserStatusRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User was not found."));

        boolean newEnabled = request.enabled() != null ? request.enabled() : user.isEnabled();
        boolean newAccountNonLocked = request.accountNonLocked() != null
                ? request.accountNonLocked()
                : user.isAccountNonLocked();

        enforceAdminRoleAndStatusGuards(user, user.getRoles(), newEnabled, newAccountNonLocked);

        user.setEnabled(newEnabled);
        user.setAccountNonLocked(newAccountNonLocked);

        User savedUser = userRepository.save(user);
        return toUserDetailResponse(savedUser);
    }

    @Transactional(readOnly = true)
    public CurrentUserProfileResponse getCurrentUserProfile(AuthenticatedUserPrincipal principal) {
        User user = userRepository.findById(principal.userId())
                .orElseThrow(() -> new NotFoundException("User was not found."));
        return toCurrentUserProfileResponse(user);
    }

    @Transactional
    public CurrentUserProfileResponse updateCurrentUserProfile(
            AuthenticatedUserPrincipal principal,
            UpdateCurrentUserRequest request) {
        User user = userRepository.findById(principal.userId())
                .orElseThrow(() -> new NotFoundException("User was not found."));

        user.setFirstName(request.firstName().trim());
        user.setLastName(request.lastName().trim());

        User savedUser = userRepository.save(user);
        return toCurrentUserProfileResponse(savedUser);
    }

    private Specification<User> userFilterSpecification(String email, Boolean enabled, String role) {
        return (root, query, builder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (email != null && !email.isBlank()) {
                predicates.add(builder.like(builder.lower(root.get("email")), "%" + email.trim().toLowerCase(Locale.ROOT) + "%"));
            }
            if (enabled != null) {
                predicates.add(builder.equal(root.get("enabled"), enabled));
            }
            if (role != null && !role.isBlank()) {
                Join<User, Role> roleJoin = root.join("roles", JoinType.INNER);
                predicates.add(builder.equal(roleJoin.get("name"), normalizeRoleName(role)));
                query.distinct(true);
            }

            return builder.and(predicates.toArray(Predicate[]::new));
        };
    }

    private Set<Role> resolveRoles(Collection<String> requestedRoles) {
        return requestedRoles.stream()
                .map(this::normalizeRoleName)
                .map(this::findRoleByName)
                .collect(java.util.stream.Collectors.toSet());
    }

    private Role findRoleByName(String roleName) {
        return roleRepository.findByName(roleName)
                .orElseThrow(() -> new BadRequestException("Role '" + roleName + "' is not supported."));
    }

    private String normalizeSortBy(String sortBy) {
        String candidate = (sortBy == null || sortBy.isBlank()) ? "createdAt" : sortBy.trim();
        if (!ALLOWED_SORT_FIELDS.contains(candidate)) {
            throw new BadRequestException("sortBy value is not supported.");
        }
        return candidate;
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private String normalizeRoleName(String roleName) {
        return roleName.trim().toUpperCase(Locale.ROOT);
    }

    private void enforceAdminRoleAndStatusGuards(
            User currentUser,
            Collection<Role> resultingRoles,
            boolean resultingEnabled,
            boolean resultingAccountNonLocked) {
        boolean currentlyAdmin = hasRole(currentUser.getRoles(), ROLE_ADMIN);
        boolean remainsAdmin = hasRole(resultingRoles, ROLE_ADMIN);

        if (currentlyAdmin && !remainsAdmin && userRepository.countByRoles_Name(ROLE_ADMIN) <= 1) {
            throw new BadRequestException("At least one ROLE_ADMIN user is required.");
        }

        boolean resultingActive = resultingEnabled && resultingAccountNonLocked;
        if (currentlyAdmin && !resultingActive && currentUser.isEnabled() && currentUser.isAccountNonLocked()
                && userRepository.countByRoles_NameAndEnabledTrueAndAccountNonLockedTrue(ROLE_ADMIN) <= 1) {
            throw new BadRequestException("At least one active ROLE_ADMIN user is required.");
        }
    }

    private boolean hasRole(Collection<Role> roles, String roleName) {
        return roles.stream().anyMatch(role -> roleName.equals(role.getName()));
    }

    private UserResponse toUserResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.isEnabled(),
                user.isAccountNonLocked(),
                user.getRoles().stream().map(Role::getName).sorted().toList());
    }

    private UserDetailResponse toUserDetailResponse(User user) {
        return new UserDetailResponse(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.isEnabled(),
                user.isAccountNonLocked(),
                user.getRoles().stream().map(Role::getName).sorted().toList(),
                user.getCreatedAt(),
                user.getUpdatedAt());
    }

    private CurrentUserProfileResponse toCurrentUserProfileResponse(User user) {
        return new CurrentUserProfileResponse(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getRoles().stream().map(Role::getName).sorted().toList());
    }
}
