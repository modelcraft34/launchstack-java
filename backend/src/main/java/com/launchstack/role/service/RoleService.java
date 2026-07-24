package com.launchstack.role.service;

import com.launchstack.role.RoleRepository;
import com.launchstack.role.dto.RoleResponse;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RoleService {

    private final RoleRepository roleRepository;

    public RoleService(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Transactional(readOnly = true)
    public List<RoleResponse> getRoles() {
        return roleRepository.findAll().stream()
                .map(role -> new RoleResponse(role.getName()))
                .sorted(java.util.Comparator.comparing(RoleResponse::name))
                .toList();
    }
}
