package com.api.e_commerce.role;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RoleService {

    private final IRoleRepository roleRepository;

    public List<Role> addRole(RoleType roleEnum){
       return roleRepository.findByRoleName(roleEnum)
                .map(List::of)
                .orElseGet(ArrayList::new);
    }

}
