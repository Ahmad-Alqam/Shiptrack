package com.shiptrack.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.shiptrack.model.Role;
import com.shiptrack.model.User;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByIdNumber(String idNumber);
    boolean existsByUsername(String username);
    boolean existsByIdNumber(String idNumber);

    long countByRole(com.shiptrack.model.Role role);

    List<User> findByRole(Role role);

    long countByLocked(boolean locked);

}