package com.shiptrack.repository;
// Handles database operations for users.
import org.springframework.data.jpa.repository.JpaRepository;

import com.shiptrack.model.PasswordPolicy;

public interface PasswordPolicyRepository extends JpaRepository<PasswordPolicy, Long> {
}